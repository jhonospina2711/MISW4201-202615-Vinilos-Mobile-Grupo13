# Desempeño del Sprint 2 — micro-optimizaciones y perfilamiento

Documento de apoyo del Sprint 2. Resume las micro-optimizaciones aplicadas, las buenas prácticas de hilos y memoria que ya están en el código, y las mediciones de perfilamiento obtenidas hasta ahora.

## 1. Micro-optimizaciones aplicadas

La línea base fue el reporte de Lint del proyecto: **6 errores y 47 advertencias**, con el build de `lintDebug` fallando por los errores. Cada hallazgo se corrigió o se justificó.

| Hallazgo de Lint | Qué se cambió | Efecto |
|---|---|---|
| `NewApi` (1 error): `android:windowLightStatusBar` requiere API 23 y el mínimo es 21 | Se quitó del tema base. El tema es `Theme.Material3.Dark.NoActionBar`, donde el valor por defecto ya es `false` | Elimina un riesgo real de incompatibilidad en Android 5.0, que es el mínimo soportado |
| `UseAppTint` (5 errores): `android:tint` en `ImageView` | `app:tint` en `activity_entry`, `activity_main`, `vinilos_item_artist` y `vinilos_view_role_option`, declarando `xmlns:app` donde faltaba | Tinte correcto con `AppCompatImageView`; el atributo de plataforma se ignora en versiones antiguas |
| `Overdraw` (7 advertencias): el layout raíz pintaba `@color/vinilos_background` | Se quitó el fondo de los siete layouts raíz. El tema ya pinta `android:colorBackground` con ese mismo color | Una capa de pintado menos por pantalla, sin cambio visual |
| `DisableBaselineAlignment` (2) | `android:baselineAligned="false"` en dos `LinearLayout` del detalle de álbum y de artista | Evita el cálculo de alineación de línea base en cada medida |
| `ContentDescription` (1) | `android:contentDescription` en la carátula del detalle | Accesibilidad: el lector de pantalla anuncia la imagen |
| `ScrollViewSize` (1) | El hijo del `ScrollView` pasa de `match_parent` a `wrap_content` | Medida correcta del contenido desplazable |
| `UnusedResources` (5) | Se eliminó `vinilotornamesa.png` (2,0 MB), que no se referenciaba en ningún layout ni en código | Menos recursos empaquetados |

### Resultado

| Métrica | Antes | Después |
|---|---:|---:|
| Errores de Lint | **6** | **0** |
| Advertencias de Lint | **47** | **34** |
| `lintDebug` | Fallaba | Termina correctamente |
| APK de Debug | 12 MB | **11 MB** |

Evidencia versionada en `docs/evidencias/sprint2/desempeno/`:

- `lint-antes.txt` y `lint-antes.html` — 6 errores, 47 advertencias.
- `lint-despues.txt` y `lint-despues.html` — 0 errores, 34 advertencias.

Las 34 advertencias restantes son de dependencias con versión más nueva (`GradleDependency`, `NewerVersionAvailable`, `AndroidGradlePluginVersion`) y de recursos o atributos menores; cambiarlas implica subir versiones del toolchain y se dejó fuera del alcance del sprint para no arriesgar el build.

## 2. Buenas prácticas para evitar ANR

La regla es que el hilo principal no haga entrada/salida ni espere. En el código:

| Práctica | Dónde |
|---|---|
| Todas las consultas corren en `viewModelScope`, nunca en el hilo principal | `EntryViewModel`, `HomeViewModel`, `AlbumCatalogViewModel`, `ArtistCatalogViewModel`, `AlbumDetailViewModel`, `ArtistDetailViewModel`, `CreateAlbumViewModel` |
| El despachador de E/S se inyecta, no se instancia | `di/DispatchersModule.kt` expone `@IoDispatcher CoroutineDispatcher = Dispatchers.IO` |
| La red y la base de datos se ejecutan en el despachador de E/S | `AlbumRepositoryImpl.createAlbum` y las consultas usan `withContext(ioDispatcher)` |
| La validación del formulario es pura y no bloquea: no hay E/S dentro de `AlbumValidator` | `domain/validation/AlbumValidator.kt` |
| El estado de guardado evita envíos duplicados mientras hay una petición en curso | `CreateAlbumViewModel.save` retorna si el estado es `Saving` |
| Las imágenes se cargan con Glide, que hace el trabajo fuera del hilo principal y con caché propia | `AlbumCardViewHolder`, `ArtistAvatarView`, `AlbumDetailActivity` |
| Las pantallas observan con el ciclo de vida correcto | `viewLifecycleOwner` en Fragments y `lifecycleScope`; 4 usos verificados |

No hay llamadas de red ni consultas a Room en el hilo principal: el repositorio siempre pasa por `withContext(ioDispatcher)`.

## 3. Buenas prácticas de consumo de memoria

| Práctica | Dónde |
|---|---|
| `ViewBinding` en lugar de `findViewById` repetido; las referencias viven con la vista | 11 archivos |
| Los `ViewHolder` se reutilizan y solo se enlazan los datos que cambian (`ListAdapter` con `DiffUtil`) | `AlbumCardAdapter`, `ArtistRowAdapter`, `DetailRowAdapter` |
| Las imágenes se cargan con Glide, que respeta el tamaño de la vista y usa caché de memoria y de disco | `coreui` y detalle de álbum |
| La caché local de álbumes está acotada por diseño: solo la tabla `albums`, con `replaceAll` transaccional y `upsert` para el detalle | `data/local/AlbumDao.kt` |
| No se guardan DTO ni entidades de Room en la UI: se mapean a modelos de dominio | `data/mappers` |
| Los `LiveData` se exponen como inmutables y el `MutableLiveData` queda privado en el ViewModel | Todos los ViewModels |
| Se eliminó un PNG de 2,0 MB que se empaquetaba sin usarse | `res/drawable/` |

## 4. Perfilamiento

### Método

Las métricas se toman sobre el **APK de Release** o de **Debug** según el escenario, con:

- `adb shell am start -W` para el tiempo de arranque en frío.
- `adb shell dumpsys meminfo <paquete>` para la memoria: PSS total, heap de Dalvik y heap nativo.
- `adb shell dumpsys gfxinfo <paquete>` para los cuadros y el *jank*.
- Android Studio Profiler para CPU, memoria y red cuando hay un dispositivo conectado.

Se recomienda repetir cada medición tres veces y reportar la mediana, porque el arranque en frío varía.

### Mediciones obtenidas

| Métrica | Entorno | Valor |
|---|---|---|
| Arranque en frío (3 corridas) | Emulador Android 5.0.2 (API 21) | 182 / 134 / 153 ms — **mediana 153 ms** |
| PSS total con la app en el catálogo | Emulador Android 5.0.2 (API 21) | **36 399 KB** (≈ 35,5 MB) |
| Heap de Dalvik | Idem | 10 303 KB |
| Heap nativo | Idem | 25 785 KB |
| Vistas instanciadas | Idem | 122 |
| Cuadros y *jank* | Idem | **No disponibles**: el emulador renderiza por software (`swiftshader`), así que `gfxinfo` no reporta estadísticas de cuadros |

### Limitación abierta

La rúbrica pide perfilar en **al menos tres dispositivos físicos diferentes**. Hasta ahora las mediciones se hicieron en un **emulador**, que no cuenta como dispositivo físico y además no reporta estadísticas de cuadros por el render por software.

Falta, por tanto, la tabla con marca, modelo, versión de Android y RAM de tres celulares, y las mismas métricas medidas en cada uno. El método y las métricas ya están definidos; lo que falta es ejecutarlo en hardware real.

## 5. Cómo reproducir

```bash
# 1. Reporte de Lint
./gradlew :app:lintDebug
# informe en app/build/reports/lint-results-debug.html

# 2. Arranque en frío
adb shell am force-stop com.example.vinilosmobile
adb shell am start -W -n com.example.vinilosmobile/.feature.entry.EntryActivity

# 3. Memoria
adb shell dumpsys meminfo com.example.vinilosmobile

# 4. Cuadros (requiere GPU real)
adb shell dumpsys gfxinfo com.example.vinilosmobile
```
