# Perfilamiento de Vinilos Mobile — Sprint 3

## Entorno y método

El 22 de noviembre de 2026 se midió el APK Debug de `feature/sprint-3-final` en un Samsung SM-S948B físico con Android 17/API 37. BackVynils `7178f83` y PostgreSQL 13 corrían en Docker local; el teléfono llegaba a `127.0.0.1:3000` por `adb reverse`. Se usaron `am start -W`, `dumpsys meminfo` y `dumpsys gfxinfo`; los datos de red y la interfaz no se aislaron de otras cargas del teléfono. Accessibility Scanner estaba activo durante estas mediciones.

`am start -W` se ejecutó después de `am force-stop` tres veces. Para el catálogo se guardaron las salidas completas de [`meminfo`](evidencias/sprint3/perfil-memoria-catalogo-sm-s948b.txt) y [`gfxinfo`](evidencias/sprint3/perfil-gfx-catalogo-sm-s948b.txt). La [bitácora de arranque](evidencias/sprint3/perfil-arranque-sm-s948b.txt) contiene los tres tiempos. Para las demás pantallas se tomaron lecturas puntuales de PSS al abrirlas; son muestras exploratorias y no una mediana repetida. Se guardaron lecturas completas del [listado](evidencias/sprint3/perfil-memoria-artistas-sm-s948b.txt) y [detalle de artista](evidencias/sprint3/perfil-memoria-detalle-artista-sm-s948b.txt), y del [listado](evidencias/sprint3/perfil-memoria-coleccionistas-sm-s948b.txt) y [detalle de coleccionista](evidencias/sprint3/perfil-memoria-detalle-coleccionista-sm-s948b.txt).

## Resultados

| Estado | Medición | Lectura |
|---|---:|---|
| Arranque en frío de `EntryActivity` | 214, 203, 222 ms; **mediana 214 ms** | Tiempo hasta la primera Activity dibujada; no mide cuándo termina el GET del catálogo |
| Catálogo de álbumes HU01 | **156 428 KB PSS** | 76 832 KB corresponden a gráficos; 21 712 KB a heap nativo en la captura guardada |
| Catálogo HU01, frames desde apertura | 14 frames; 3 con jank | Muestra muy pequeña; no extrapolar el 21,4 % a una sesión normal |
| Detalle de álbum HU02 | 160 747 KB PSS | Al abrir un álbum con imágenes, descripción y cuatro tracks; 2/67 frames con jank tras reiniciar el contador |
| Hoja de agregar track HU08 | 220 133 KB PSS | Lectura con hoja visible sobre el detalle; 1/53 frames con jank tras reiniciar el contador |
| Tras cerrar la hoja HU08 | 151 552 KB PSS | La memoria bajó tras cerrar la hoja; una muestra no prueba ausencia de fugas |
| Formulario de nuevo álbum HU07 | 184 776 KB PSS | Lectura con el formulario vacío visible |
| Catálogo de artistas HU03 | 162 783 KB PSS | Dos artistas visibles; una lectura |
| Detalle de Rubén Blades HU04 | 172 518 KB PSS | Álbumes, descripción y premio visibles; una lectura |
| Listado de coleccionistas HU05 | 180 027 KB PSS | Dos coleccionistas visibles; una lectura |
| Detalle de Manolo Bellon HU06 | 159 244 KB PSS | Una lectura tras navegar desde el listado |

La diferencia entre la hoja abierta y cerrada incluye superficies gráficas y recursos que Android puede conservar o liberar según su política. Los valores no demuestran que el formulario por sí solo reserve 68 MB; hacen falta series repetidas y una traza de asignaciones para atribuir causas.

## Lectura por historia y próximo experimento

| Historias | Qué se verificó | Qué falta medir |
|---|---|---|
| HU01 catálogo | Arranque y PSS; catálogo con cinco álbumes contra API local | Tiempo hasta contenido y comparación caché/red; desplazamiento largo y jank en tres teléfonos |
| HU02 detalle álbum | PSS y frames al abrir un álbum con cuatro tracks | Mediana de carga, consumo tras varias entradas/salidas y latencia de imágenes |
| HU03–HU04 artistas | Recorrido funcional, regresión Espresso y PSS puntual de lista y detalle | CPU y tiempo hasta datos; comparación de músicos y bandas |
| HU05–HU06 coleccionistas | Recorrido funcional, regresión Espresso y PSS puntual de lista y detalle | CPU y tiempo de carga de relaciones |
| HU07 crear álbum | PSS del formulario; POST real en la [prueba local](evidencias/sprint3/api-local-sm-s948b.md) | Latencia de POST y refresco con tres repeticiones, incluyendo red lenta |
| HU08 agregar track | PSS y frames de la hoja; POST real y actualización visible | Latencia de POST/refresco y repetición en dispositivos de distinta gama |

Las optimizaciones implementadas y su relación con memoria/ANR se describen en [Sprint 2](DESEMPENO_SPRINT2.md) y la [estrategia de Sprint 3](PRUEBAS_SPRINT3.md). Las solicitudes de red y Room salen del hilo principal mediante los despachadores de E/S; Glide y `RecyclerView` limitan el trabajo de imágenes y filas. El cambio a `NestedScrollView` corrigió la medición de la lista de tracks que crece tras HU08 ([issue #107](https://github.com/jhonospina2711/MISW4201-202615-Vinilos-Mobile-Grupo13/issues/107)).

## Límite de la entrega medido hasta ahora

El perfilamiento de Sprint 2 incluyó tres equipos físicos de Firebase Test Lab, pero fue sobre el código anterior. En Sprint 3 solo se ha medido el Samsung conectado. Para afirmar desempeño de la versión final en **tres celulares físicos** faltan dos dispositivos o nuevas corridas en Test Lab, más tres repeticiones de cada escenario. También faltan trazas de CPU/red y medición del tiempo hasta contenido para HU01–HU08. No se ha medido una mejora cuantitativa frente al Sprint 2 en el mismo equipo.
