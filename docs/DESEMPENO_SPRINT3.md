# Desempeño del Sprint 3 — micro-optimizaciones, memoria y ANR

Documento de apoyo del Sprint 3. Continúa el [análisis del Sprint 2](DESEMPENO_SPRINT2.md): repite el método —medir, cambiar, volver a medir— pero sobre el código de HU05, HU06 y HU08, que es el que entró en esta iteración. Las cifras de perfilamiento están en [PERFILAMIENTO_SPRINT3.md](PERFILAMIENTO_SPRINT3.md).

## 1. Qué se midió antes de cambiar

No se optimizó a ciegas. La línea base fueron tres fuentes sobre el código de la semana 7:

| Fuente | Lectura inicial |
|---|---|
| `lintDebug` sobre el código del Sprint 3 | 13 hallazgos: 0 errores y 13 avisos |
| Revisión del hilo principal en los adaptadores | Formateo de precios y fechas dentro del enlace de cada fila |
| `dumpsys meminfo` y `dumpsys gfxinfo` | PSS de HU05 180 027 KB y de HU06 159 244 KB; detalle de álbum 160 747 KB |

El perfilamiento mostró que el costo visible no estaba en las consultas —todas salen del hilo principal con `flowOn(ioDispatcher)`— sino en el trabajo que se repite por cada elemento: formateadores de moneda y de fecha construidos de nuevo en cada enlace de fila.

## 2. Micro-optimizaciones aplicadas

| # | Dónde | Qué se hacía | Qué se cambió | Efecto |
|---|---|---|---|---|
| M1 | `CollectorDetailActivity.toAlbumCard` (HU06) | `NumberFormat.getCurrencyInstance(...)` por **cada álbum** de la colección | Un único `NumberFormat` como campo de la actividad | De 3,38 µs a 0,13 µs por elemento; deja de asignar un formateador por álbum |
| M2 | `DateTextFormatter.format` (HU02, HU04, HU06) | Dos `SimpleDateFormat` nuevos por cada fecha mostrada | Dos formateadores en `ThreadLocal`, creados una vez por hilo | De 6,59 µs a 1,13 µs por fecha |
| M3 | `AlbumValidator.isDate` (HU07) | `SimpleDateFormat` nuevo en cada validación | Un formateador en `ThreadLocal` | Una asignación menos por validación; la validación corre en el despachador de E/S |
| M4 | `AlbumDetailActivity` (HU02/HU08) | `trackList` y `commentList` con pools de vistas separados aunque usan la misma fila | `commentList.setRecycledViewPool(trackList.recycledViewPool)` | Las filas de tracks y comentarios se reutilizan entre las dos listas |
| M5 | Catálogos HU01, HU03 y HU05 | `RecyclerView` sin `setHasFixedSize` | `setHasFixedSize(true)` en las tres listas | El `RecyclerView` no vuelve a medir cuando cambia el contenido: su tamaño lo fija el contenedor |

### Por qué `ThreadLocal` y no un `val` compartido

`SimpleDateFormat` y `NumberFormat` **no son seguros entre hilos**: guardan el `Calendar` y el `DecimalFormat` de trabajo en campos mutables. Un `val` compartido habría introducido una condición de carrera entre el hilo de interfaz y el despachador de E/S que valida el formulario. `ThreadLocal` conserva la reutilización y mantiene un formateador por hilo, sin sincronización.

La primera versión usó `ThreadLocal.withInitial { ... }`, que es la forma idiomática. El Lint la rechazó: **`withInitial` existe desde API 26 y el mínimo de la app es 21**. En un Android 5.0 habría lanzado `NoSuchMethodError` en la primera pantalla con fecha, es decir, habría roto justo la matriz de compatibilidad que se acababa de certificar. Se reemplazó por una subclase anónima de `ThreadLocal` que sobrescribe `initialValue()`, disponible desde API 1, con la misma reutilización. Es el ejemplo de por qué el Lint se corre también sobre el código nuevo y no solo sobre el heredado.

## 3. Medición de las rutinas de formato

Las dos rutinas se midieron en la JVM del equipo de desarrollo (OpenJDK), con 2 000 iteraciones de calentamiento y 10 000 medidas. El banco compara exactamente el trabajo que hacía el código anterior —crear el formateador dentro de la llamada— contra reutilizarlo.

| Operación | Antes | Después | Mejora |
|---|---:|---:|---:|
| Precio con `NumberFormat` (HU06) | 2,10–3,38 µs/elemento | 0,13–0,15 µs/elemento | **14× a 26×** |
| Fecha con `SimpleDateFormat` (detalles) | 4,95–6,59 µs/fila | 0,82–1,13 µs/fila | **5,8× a 6×** |

Los rangos son dos ejecuciones del mismo banco, no una sola medición presentada como exacta: la variación entre corridas es del orden del 30 % en la JVM. Ambas salidas están en `docs/evidencias/sprint3/desempeno/banco-formatos-salida-1.txt` y `banco-formatos-salida.txt`, con el fuente en `banco-formatos.java`.

Lectura honesta: en valor absoluto son microsegundos. Con una colección de veinte álbumes, M1 ahorra unos 65 µs y veinte asignaciones en el hilo principal por cada refresco; con diez filas de tracks y comentarios, M2 ahorra unas 55 µs y veinte objetos. **No se presenta como una mejora perceptible en pantalla**: es menos trabajo y menos basura para el recolector en el hilo que dibuja, que es lo que importa cuando la pantalla se refresca al agregar un track.

## 4. Memoria y ANR en el código del Sprint 3

| Práctica | Dónde | Por qué importa |
|---|---|---|
| Las tres consultas nuevas salen del hilo principal con `flowOn(ioDispatcher)` | `CollectorRepositoryImpl.getCollectors` y `getCollector` | El parseo con Gson y el mapeo no compiten con el dibujo |
| El `POST` de track y el de álbum usan `withContext(ioDispatcher)` | `AlbumRepositoryImpl.addTrack` y `createAlbum` | La espera de red no bloquea el hilo de interfaz |
| El guardado de track no se puede disparar dos veces | `CreateTrackViewModel.save` retorna si el estado es `Saving` o `Saved` | Evita dos `POST` y dos actualizaciones por un doble toque |
| El detalle del álbum comparte el pool de vistas entre las dos listas | `AlbumDetailActivity` | Menos inflado y menos objetos vivos al abrir una hoja sobre el detalle |
| El estado se observa con el ciclo de vida correcto | `viewLifecycleOwner` en `CollectorsFragment`; `lifecycleScope` en las actividades | No se enlaza una vista destruida |
| La hoja de track libera su enlace | `AddTrackBottomSheet` usa `_binding` y lo anula en `onDestroyView` | Evita retener el `DialogFragment` y su vista |
| Los ítems de lista comparan por identidad con `DiffUtil` | `CollectorRowAdapter`, `DetailRowAdapter` | Solo se redibuja lo que cambió |

## 5. Lint antes y después

| Métrica | Antes | Después |
|---|---:|---:|
| Errores | **0** | **0** |
| Avisos | **13** | **13** |
| `lintDebug` | Termina | Termina |

Los 13 avisos no cambiaron con las micro-optimizaciones y ninguno es un defecto de rendimiento: 4 recursos sin usar, 3 atributos que solo aplican desde API 23, 3 dependencias con versión más nueva, 2 avisos de formato numérico y 1 sugerencia de plural. El detalle está en `docs/evidencias/sprint3/desempeno/`.

La corrida intermedia sí encontró dos hallazgos `NewApi` —los dos `ThreadLocal.withInitial` descritos arriba—. Se corrigieron y la corrida final vuelve a 13 avisos, sin `NewApi` y sin errores. La secuencia completa quedó guardada: `lint-sprint3-antes.txt` (13), `lint-sprint3-intermedio.txt` (15, con los dos `NewApi`) y `lint-sprint3-despues.txt` (13).

## 6. Lo que no se optimizó y por qué

| Candidato | Decisión |
|---|---|
| Índice en `albums(inCatalog, position)` | Descartado: la tabla tiene decenas de filas y añadir el índice obliga a una migración de Room en la víspera de la entrega. El riesgo supera la ganancia |
| Quitar el `requestLayout()` del callback de `submitList` | Descartado: se añadió para corregir el [issue #107](https://github.com/jhonospina2711/MISW4201-202615-Vinilos-Mobile-Grupo13/issues/107) —la lista de tracks se recortaba— y quitarlo reintroduce el defecto |
| `override` en las cargas de Glide | Innecesario: Glide ya ajusta el mapa de bits al tamaño de la vista destino |
| Subir versiones de AndroidX | Fuera de alcance: implica subir el toolchain y revalidar la matriz de cuatro APIs en la última semana |

## 7. Límites

- La medición de las rutinas de formato es de JVM, no del dispositivo. Sirve para comparar las dos implementaciones, no para prometer milisegundos en un teléfono.
- El perfilamiento sigue hecho en **un solo dispositivo físico** (Samsung SM-S948B); faltan dos.
- No hay trazas de CPU por historia ni medición del tiempo hasta contenido.
- No se midió la mejora de M4 y M5 con `gfxinfo` porque el emulador disponible no es representativo y el teléfono físico ya no estaba conectado al aplicar el cambio. Se documentan como decisiones de diseño con su justificación, no como mejoras medidas.
