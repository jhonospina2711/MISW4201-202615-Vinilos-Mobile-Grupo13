# Estrategia y resultados de pruebas — Sprint 3

## Alcance y criterio de aceptación

El objetivo es verificar las ocho historias HU01–HU08 del MVP, las tres historias que faltaban (HU05, HU06 y HU08) y la regresión de HU01–HU04/HU07. Se ejecutan pruebas JVM para reglas y mapeos, Espresso con HTTP controlado para el flujo real de la interfaz, y sesiones manuales para accesibilidad, reconocimiento y desempeño. Una historia se acepta cuando sus escenarios de datos, vacío, error, navegación y rol que apliquen pasan y no hay defectos críticos abiertos.

Las fixtures Espresso sustituyen la respuesta HTTP, pero recorren Retrofit, DTO, repositorio, caso de uso, ViewModel, Activity/Fragment y los recursos de interfaz reales. Los tests no escriben en el backend compartido. La verificación contra BackVynils local y en dispositivos físicos es una etapa adicional, no está sustituida por la fixture.

El [recorrido con BackVynils local y un Samsung SM-S948B (API 37)](evidencias/sprint3/api-local-sm-s948b.md) verificó datos reales para HU01–HU08, incluidos los POST de álbum y track desde la app y la restricción de HU08 por rol. Incluye capturas y el [smoke test HTTP](../scripts/smoke_sprint3_api.py). La lista de tracks se corrigió después de observar un contador actualizado con filas recortadas ([issue #107](https://github.com/jhonospina2711/MISW4201-202615-Vinilos-Mobile-Grupo13/issues/107)).

## Inventario automatizado

| HU | Escenarios Espresso | Pruebas JVM específicas o compartidas |
|---|---|---|
| HU01 catálogo | datos, vacío, error/reintento, caché sin red | `AlbumRepositoryImplTest`, `AlbumDaoTest`, `AlbumMappersTest`, `UseCasesTest` |
| HU02 detalle álbum | datos relacionados y navegación, 404, sin tracks, error/reintento, rol | `AlbumRepositoryImplTest`, `AlbumMappersTest`, `UseCasesTest` |
| HU03 artistas | músicos/bandas y filtros, vacío, error/reintento | `ArtistRepositoryImplTest`, `ArtistServiceAdapterTest`, `ArtistMappersTest` |
| HU04 detalle artista | músico con premios, banda con integrantes, 404, error/reintento | `ArtistRepositoryImplTest`, `ArtistServiceAdapterTest`, `ArtistMappersTest` |
| HU05 coleccionistas | datos y navegación a HU06, vacío, error/reintento | `CollectorRepositoryImplTest`, `CollectorMappersTest` |
| HU06 detalle coleccionista | datos, favoritos, álbumes, comentarios, vacío, 404, error/reintento, navegación a HU02/HU04 | `CollectorRepositoryImplTest`, `CollectorMappersTest` |
| HU07 crear álbum | restricción visitante, validación, éxito y actualización, error, cancelar | `CreateAlbumUseCaseTest`, `AlbumValidatorTest`, `AlbumRepositoryImplTest` |
| HU08 agregar track | restricción visitante, validación, éxito y actualización, error/reintento, cancelar | `CreateTrackUseCaseTest`, `TrackValidatorTest`, `AlbumRepositoryImplTest` |

Los scripts son [`Sprint1EspressoTest.kt`](../app/src/androidTest/java/com/example/vinilosmobile/Sprint1EspressoTest.kt), [`CreateAlbumEspressoTest.kt`](../app/src/androidTest/java/com/example/vinilosmobile/CreateAlbumEspressoTest.kt) y [`FinalSprintEspressoTest.kt`](../app/src/androidTest/java/com/example/vinilosmobile/FinalSprintEspressoTest.kt). La fixture HTTP está en [`FixtureApiInterceptor.kt`](../app/src/androidTest/java/com/example/vinilosmobile/FixtureApiInterceptor.kt). Pasaron **80/80 pruebas JVM** y **32/32 escenarios Espresso** en Android 5.0.2 emulado (API 21), Pixel 6a emulado (API 33), emulador Android 9 (API 28) y Samsung SM-S948B físico (API 37). Después de los últimos cambios de accesibilidad se repitieron los 32 escenarios en [API 28](evidencias/sprint3/espresso-api28-final-accessibility-emulator.txt) y [API 37](evidencias/sprint3/espresso-api37-final-accessibility-sm-s948b.txt). Respaldos JVM: [`evidencias/sprint3/unit/`](evidencias/sprint3/unit/); [API 33](evidencias/sprint3/espresso-api33-final.xml.txt). En API 37, Espresso 3.7.0 corrigió la incompatibilidad de inyección de eventos de la versión anterior; para dos botones de HU07 se invoca el `performClick` de la vista en esa API tras verificar manualmente el toque físico. Android Lint terminó con **0 errores y 26 advertencias**; [XML](evidencias/sprint3/lint-debug.xml).

## Ejecución reproducible

```bash
./gradlew :app:testDebugUnitTest --no-configuration-cache
./gradlew :app:connectedDebugAndroidTest --no-configuration-cache
./gradlew :app:lintDebug --no-configuration-cache
```

La matriz se completó con **Android 5.0.2 (API 21)**: los 32 escenarios Espresso pasaron sobre el emulador `vinilos-ui` con la app instalada desde el APK de Debug. Evidencia en [espresso-api21-sprint3.txt](evidencias/sprint3/espresso-api21-sprint3.txt), con el veredicto `OK (32 tests)`. Con eso la app queda verificada en API 21, 28, 33 y 37, que cubre desde Lollipop hasta la versión más reciente probada.

## Pruebas de reconocimiento y exploración

La exploración sistemática recorre cada historia con esta matriz, registrando resultado y captura por dispositivo:

| Recorrido | Variantes que se fuerzan | Evidencia esperada |
|---|---|---|
| Navegación HU01–HU06 | visitante y coleccionista, pestañas, volver, rotación, fondo y regreso | Capturas, pasos y resultado por pantalla |
| Consultas | datos, lista vacía, 404, red lenta, sin red, 500, reintento | Mensajes en español, ausencia de bloqueos |
| Formularios HU07/HU08 | campos vacíos, caracteres largos, espacios, duración `7:21`/`7:99`, doble toque, cancelar, giro | Validación, una sola creación y estado posterior |
| Tamaño y configuración | API 21+, pantalla pequeña, letra grande, modo oscuro si aplica | Sin texto cortado, controles operables |

Se ejecutaron tres sesiones Monkey. Las dos primeras, en API 28 (200 y 300 eventos), salieron al launcher y a los ajustes rápidos, así que su cobertura fue limitada. La tercera se acotó al paquete con `-p com.example.vinilosmobile --pct-syskeys 0` y **600 eventos** sobre API 21 con BackVynils local levantado: recorrió **cinco actividades** —entrada, catálogo, detalle de álbum, detalle de artista y detalle de coleccionista— sin crashes ni ANR. Evidencia en [exploracion-aleatoria-api21-600-eventos.txt](evidencias/sprint3/exploracion-aleatoria-api21-600-eventos.txt). Véanse la [bitácora de exploración y las evidencias](EXPLORACION_SPRINT3.md). También se registró un recorrido sistemático de HU01–HU08 con API local en el Samsung.

Los defectos reproducibles se registran como issues `bug` con versión/commit, dispositivo/API, pasos, esperado, obtenido y evidencia. Un fix se cierra al repetir el escenario fallido y la regresión.

## Accesibilidad y usabilidad

Revisar HU01–HU08 con Accessibility Scanner en un dispositivo real o emulador con el servicio instalado. Por cada pantalla guardar captura del análisis, avisos sobre etiquetas, contraste y tamaño de blancos táctiles, corrección y segunda captura. Complementar con TalkBack y navegación por teclado: orden de foco, propósito de botones, textos de error anunciados, 200 % de tamaño de fuente y retorno de foco tras cerrar una hoja. Android recomienda objetivos táctiles de al menos 48 × 48 dp y combina herramientas automáticas con revisión manual ([guía oficial](https://developer.android.com/guide/topics/ui/accessibility/views/apps-views), [pruebas en Views](https://developer.android.com/guide/topics/ui/accessibility/views/testing-views)). Se ejecutó Scanner real en el Samsung, se corrigieron hallazgos en detalle de álbum, catálogo de coleccionistas, formulario HU07 y artistas, y las segundas instantáneas de esas pantallas terminaron sin sugerencias. Ver [reporte y capturas](ACCESIBILIDAD_SPRINT3.md).

## Perfilamiento, memoria y ANR

Perfilar en tres dispositivos físicos, repitiendo cada medición al menos tres veces para poder reportar mediana y dispersión. Comparar con el [perfilamiento del Sprint 2](DESEMPENO_SPRINT2.md) bajo el mismo dispositivo y condiciones. Registrar en cada HU: tiempo de apertura hasta contenido, CPU, memoria PSS/Java heap, caídas de frames, respuesta al volver y al reintento. Para HU07/HU08 incluir duración del POST y tiempo hasta ver la actualización. Para HU01 medir la emisión de caché y el refresco. Para HU03/HU05 medir el desplazamiento de listas. Para HU02/HU04/HU06 medir carga de relaciones e imágenes. Los tiempos de servidor y red deben separarse del tiempo de renderizado. Usar Android Profiler o `dumpsys`, y adjuntar trazas/capturas con dispositivo y versión ([guía oficial](https://developer.android.com/studio/profile/)). Registrar ANR si aparece; no inferir su ausencia solo de que Espresso terminó.

La app reduce trabajo de la interfaz con Retrofit/Room en dispatcher IO, `RecyclerView` para listas y Glide para imágenes. Los Fragments liberan su ViewBinding en `onDestroyView`; `viewModelScope` cancela trabajos al destruirse. Se obtuvieron lecturas puntuales de PSS para HU01–HU08 y tres arranques en el Samsung; el [reporte de perfilamiento](PERFILAMIENTO_SPRINT3.md) documenta resultados y límites. Estas decisiones y muestras no bastan por sí mismas para afirmar una mejora medida.

## Estado de evidencias

| Producto | Estado actual |
|---|---|
| Unitarias, integración y Espresso con fixtures | 80/80 JVM; 32/32 Espresso en API 28, API 33 y API 37, resultados guardados. Repetir Espresso tras cualquier cambio funcional posterior. |
| Matriz API 21, intermedio y reciente | API 21, API 28, API 33 y API 37 aprobadas |
| API local y recorrido físico | HU01–HU08 verificados en Samsung API 37; [bitácora y capturas](evidencias/sprint3/api-local-sm-s948b.md) |
| Accesibilidad con Accessibility Scanner | [Reporte](ACCESIBILIDAD_SPRINT3.md) y capturas antes/después; quedan estados alternos, fuente ampliada y revisión manual con TalkBack |
| Exploración sistemática y aleatoria | [Recorrido local y dos sesiones Monkey](EXPLORACION_SPRINT3.md); las sesiones tocaron áreas del sistema y requieren mayor cobertura dentro de la app |
| Perfilamiento de HU01–HU08 en tres físicos | [PSS puntual por historia](PERFILAMIENTO_SPRINT3.md) en un Samsung; faltan dos físicos y mediciones repetidas de CPU, latencia y frames |
| Reporte de defectos | [Issue #107](https://github.com/jhonospina2711/MISW4201-202615-Vinilos-Mobile-Grupo13/issues/107) documenta la lista de tracks recortada; [issue #108](https://github.com/jhonospina2711/MISW4201-202615-Vinilos-Mobile-Grupo13/issues/108) los hallazgos del Scanner; correcciones y regresión en PR #105 |
