# Estrategia y resultados de pruebas — Sprint 3

## Alcance y criterio de aceptación

El objetivo es verificar las ocho historias HU01–HU08 del MVP, las tres historias que faltaban (HU05, HU06 y HU08) y la regresión de HU01–HU04/HU07. Se ejecutan pruebas JVM para reglas y mapeos, Espresso con HTTP controlado para el flujo real de la interfaz, y sesiones manuales para accesibilidad, reconocimiento y desempeño. Una historia se acepta cuando sus escenarios de datos, vacío, error, navegación y rol que apliquen pasan y no hay defectos críticos abiertos.

Las fixtures Espresso sustituyen la respuesta HTTP, pero recorren Retrofit, DTO, repositorio, caso de uso, ViewModel, Activity/Fragment y los recursos de interfaz reales. Los tests no escriben en el backend compartido. La verificación contra BackVynils local y en dispositivos físicos es una etapa adicional, no está sustituida por la fixture.

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

Los scripts son [`Sprint1EspressoTest.kt`](../app/src/androidTest/java/com/example/vinilosmobile/Sprint1EspressoTest.kt), [`CreateAlbumEspressoTest.kt`](../app/src/androidTest/java/com/example/vinilosmobile/CreateAlbumEspressoTest.kt) y [`FinalSprintEspressoTest.kt`](../app/src/androidTest/java/com/example/vinilosmobile/FinalSprintEspressoTest.kt). La fixture HTTP está en [`FixtureApiInterceptor.kt`](../app/src/androidTest/java/com/example/vinilosmobile/FixtureApiInterceptor.kt). En la corrida del 21 de noviembre (hora Colombia) pasaron **80/80 pruebas JVM** y **32/32 escenarios Espresso** en Pixel 6a emulado, API 33. Los respaldos están en [`evidencias/sprint3/unit/`](evidencias/sprint3/unit/) y [`evidencias/sprint3/espresso-api33.xml`](evidencias/sprint3/espresso-api33.xml). Android Lint terminó con **0 errores y 26 advertencias**; [XML](evidencias/sprint3/lint-debug.xml).

## Ejecución reproducible

```bash
./gradlew :app:testDebugUnitTest --no-configuration-cache
./gradlew :app:connectedDebugAndroidTest --no-configuration-cache
./gradlew :app:lintDebug --no-configuration-cache
```

Para la matriz de Android, repetir `connectedDebugAndroidTest` en un dispositivo API 21 (Android L), uno intermedio y uno reciente. Registrar modelo, API, fecha, número de pruebas, fallas, captura o XML. Si un dispositivo no está disponible, dejarlo pendiente; un XML generado en otra API no demuestra compatibilidad con L.

## Pruebas de reconocimiento y exploración

La exploración sistemática recorre cada historia con esta matriz, registrando resultado y captura por dispositivo:

| Recorrido | Variantes que se fuerzan | Evidencia esperada |
|---|---|---|
| Navegación HU01–HU06 | visitante y coleccionista, pestañas, volver, rotación, fondo y regreso | Capturas, pasos y resultado por pantalla |
| Consultas | datos, lista vacía, 404, red lenta, sin red, 500, reintento | Mensajes en español, ausencia de bloqueos |
| Formularios HU07/HU08 | campos vacíos, caracteres largos, espacios, duración `7:21`/`7:99`, doble toque, cancelar, giro | Validación, una sola creación y estado posterior |
| Tamaño y configuración | API 21+, pantalla pequeña, letra grande, modo oscuro si aplica | Sin texto cortado, controles operables |

Para reconocimiento aleatorio, ejecutar una sesión cronometrada con un orden de acciones generado por semilla registrada (por ejemplo, `seed=20261121`): elegir al azar pestaña, abrir un elemento visible, volver, cambiar rol, girar pantalla, activar/desactivar red, abrir/cerrar formularios y escribir variantes de texto. Guardar semilla, duración, dispositivo, secuencia, capturas y defectos. No se debe afirmar que la sesión ocurrió hasta registrar el recorrido real.

Los defectos reproducibles se registran como issues `bug` con versión/commit, dispositivo/API, pasos, esperado, obtenido y evidencia. Un fix se cierra al repetir el escenario fallido y la regresión.

## Accesibilidad y usabilidad

Revisar HU01–HU08 con Accessibility Scanner en un dispositivo real o emulador con el servicio instalado. Por cada pantalla guardar captura del análisis, avisos sobre etiquetas, contraste y tamaño de blancos táctiles, corrección y segunda captura. Complementar con TalkBack y navegación por teclado: orden de foco, propósito de botones, textos de error anunciados, 200 % de tamaño de fuente y retorno de foco tras cerrar una hoja. Android recomienda objetivos táctiles de al menos 48 × 48 dp y combina herramientas automáticas con revisión manual ([guía oficial](https://developer.android.com/guide/topics/ui/accessibility/views/apps-views), [pruebas en Views](https://developer.android.com/guide/topics/ui/accessibility/views/testing-views)). Lint y Espresso por sí solos no sustituyen el reporte pedido de Accessibility Scanner.

## Perfilamiento, memoria y ANR

Perfilar en tres dispositivos físicos, repitiendo cada medición al menos tres veces para poder reportar mediana y dispersión. Comparar con el [perfilamiento del Sprint 2](DESEMPENO_SPRINT2.md) bajo el mismo dispositivo y condiciones. Registrar en cada HU: tiempo de apertura hasta contenido, CPU, memoria PSS/Java heap, caídas de frames, respuesta al volver y al reintento. Para HU07/HU08 incluir duración del POST y tiempo hasta ver la actualización. Para HU01 medir la emisión de caché y el refresco. Para HU03/HU05 medir el desplazamiento de listas. Para HU02/HU04/HU06 medir carga de relaciones e imágenes. Los tiempos de servidor y red deben separarse del tiempo de renderizado. Usar Android Profiler o `dumpsys`, y adjuntar trazas/capturas con dispositivo y versión ([guía oficial](https://developer.android.com/studio/profile/)). Registrar ANR si aparece; no inferir su ausencia solo de que Espresso terminó.

La app reduce trabajo de la interfaz con Retrofit/Room en dispatcher IO, `RecyclerView` para listas y Glide para imágenes. Los Fragments liberan su ViewBinding en `onDestroyView`; `viewModelScope` cancela trabajos al destruirse. Estas decisiones se verifican con perfilamiento y revisión de ciclo de vida, no bastan por sí mismas para afirmar una mejora medida.

## Estado de evidencias

| Producto | Estado actual |
|---|---|
| Unitarias, integración y Espresso con fixtures | 80/80 JVM y 32/32 Espresso en API 33; XML guardados. Repetir Espresso tras cualquier cambio funcional posterior. |
| Matriz API 21, intermedio y reciente | Pendiente de ejecutar en cada dispositivo |
| Accesibilidad con Accessibility Scanner | Pendiente de instalar/ejecutar y guardar capturas |
| Exploración sistemática y aleatoria | Guion definido; falta bitácora de ejecución |
| Perfilamiento de HU01–HU08 en tres físicos | Pendiente de dispositivos y mediciones |
| Reporte de defectos | Crear issues para hallazgos reproducibles y enlazar su verificación |
