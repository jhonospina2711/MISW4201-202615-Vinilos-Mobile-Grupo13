# Estrategia de pruebas — Sprint 1

Documento de estrategia, casos y resultados de prueba para las historias HU01–HU04 de Vinilos Mobile. Revisión: 30 de octubre de 2026. Rama de trabajo: `feature/feedback-semana-3`.

---

## 1. Objetivo y alcance

El objetivo es demostrar, con evidencia repetible, que las cuatro historias del Sprint 1 cumplen su comportamiento funcional en las capas de presentación, dominio, datos y red, y que los estados de error, vacío y reintento son observables por el usuario.

**Dentro del alcance**

- HU01 — Consultar el catálogo de álbumes, incluida la copia local cuando falla la red.
- HU02 — Consultar el detalle de un álbum con tracks, comentarios y acciones según el rol.
- HU03 — Consultar el listado de artistas con filtros de músicos y bandas.
- HU04 — Consultar el detalle de un músico o de una banda.
- Pruebas unitarias JVM, pruebas de integración con Room real en memoria, escenarios E2E automatizados con Espresso, contrato de API contra BackVynils local, recorrido manual de aceptación y verificación de consumo real de la API con datos.
- Matriz de entornos con Android API 28, API 31, API 21 y API 33.
- **Suite completa en Android 5.0.2 (API 21)**: los 15 escenarios Espresso se ejecutaron y aprobaron en Lollipop, además de la verificación de humo sobre el APK de entrega.

**Fuera del alcance de este sprint**

- Historias posteriores (coleccionistas, creación de álbumes y de tracks), que aún no están implementadas.
- Pruebas de rendimiento, de carga, de seguridad y de accesibilidad profunda.
- La ejecución de la suite en API 33, que se interrumpió por desconexión del emulador.
- Validación contra el backend HTTPS publicado: está bloqueada por el defecto #48 y no sustituye la verificación local.
- La ejecución en API 33 no se considera una batería válida porque el emulador se desconectó durante la corrida.

---

## 2. Estrategia de pruebas

La estrategia combina pruebas automatizadas rápidas en la JVM, pruebas de integración con dependencias reales acotadas, escenarios E2E sobre la interfaz real y una aceptación manual mínima sobre backend y dispositivo reales. Cada nivel responde a una pregunta distinta y ninguno se usa como sustituto de otro.

### 2.1. Tipos, niveles y técnicas (TNT)

**Niveles de prueba**

| Nivel | Qué responde | Alcance en el Sprint 1 | Herramientas | Volumen |
|---|---|---|---|---|
| Unitario | ¿La lógica aislada hace lo que debe? | Repositorios con colaboradores simulados, casos de uso, mappers, adaptador de artistas, interceptor, mapeo de errores y preferencias de rol. | JUnit 4, MockK, corrutinas de test. | 43 pruebas en 10 archivos |
| Integración | ¿Las piezas reales encajan entre sí? | `AlbumDao` contra Room real en memoria; módulos Hilt de test que sustituyen red, repositorios y base de datos; contrato de API contra BackVynils local. | Robolectric API 28, Room en memoria, Hilt Testing, script de contrato. | 3 pruebas de DAO más 4 verificaciones de contrato |
| Sistema / E2E | ¿El usuario completa el flujo en la app real? | 15 escenarios sobre `MainActivity`, `AlbumDetailActivity` y `ArtistDetailActivity`, con Retrofit, repositorios, ViewModels y vistas reales. | Espresso, `ActivityScenario`, `FixtureApiInterceptor`, `VinilosTestRunner`. | 15 escenarios |
| Aceptación | ¿El incremento sirve en el entorno de entrega? | Recorrido manual M01–M04 en API 31 con backend Docker; revisión de release. | APK Debug/Release, backend local, dispositivo o emulador. | 4 recorridos manuales |

**Tipos de prueba**

| Tipo | Propósito | Dónde se aplica |
|---|---|---|
| Funcional | Verificar que cada historia entrega los datos y las acciones esperadas. | Escenarios E2E, casos de uso y repositorios. |
| Regresión | Detectar rupturas al cambiar repositorios, mapeo o interfaz. | Suite Espresso determinista y suite unitaria en cada cambio. |
| Contrato | Confirmar que la API real conserva rutas, códigos e identificadores que la app consume. | `scripts/smoke_sprint1_api.py` contra BackVynils local. |
| Usabilidad básica | Confirmar que los estados vacío, error, reintento y aviso de caché son legibles y que las acciones visibles corresponden al rol. | Aserciones Espresso sobre estados y recorrido manual M01–M04. |

**Técnicas de diseño de casos**

| Técnica | Aplicación concreta |
|---|---|
| Particiones de equivalencia | Catálogo con datos frente a catálogo vacío; artista músico frente a banda; rol Visitante frente a Coleccionista; respuesta 200, 404 y 500. |
| Valores límite | Identificadores válidos 100 y 101 frente al inexistente 999; álbum con tracks frente a álbum sin tracks; primer y último registro del orden por identificador; fecha de nacimiento, de creación y de premiación presentes o ausentes. |
| Transición de estados | Carga → éxito, carga → vacío, carga → error → reintento → éxito; emisión de la copia Room antes de la red; cancelación del ciclo de vida sin emitir estado a una pantalla destruida. |
| Casos de error | HTTP 404, HTTP 500, timeout, fallo de conexión, JSON o registros inválidos descartados y error ya traducido que no se vuelve a mapear. |

### 2.2. Distribución del esfuerzo

| Nivel | Esfuerzo | Sustento |
|---|---:|---|
| Unitario | 40 % | Es la red de seguridad más barata y de mayor volumen: 43 de las 46 pruebas unitarias se ejecutan en la JVM sin emulador. |
| Integración | 20 % | Requiere mantener Room real, los módulos Hilt de test y el contrato con BackVynils local, pero cubre pocas piezas por corrida. |
| Sistema / E2E | 30 % | Los 15 flujos Espresso exigen emulador, fixtures deterministas y repetición en más de una API; son la evidencia principal de comportamiento visible. |
| Aceptación | 10 % | El recorrido manual y la revisión de release dependen de backend y dispositivo reales y se reservan para el cierre del incremento. |
| **Total** | **100 %** | — |

La distribución concentra el esfuerzo en unitarias y E2E porque son la evidencia repetible del día a día, y reserva la aceptación manual para lo que solo puede validarse con el backend y el dispositivo de entrega.

### 2.3. Enfoque por capas

| Capa | Qué se verifica | Pruebas asociadas |
|---|---|---|
| ViewModel y casos de uso | Delegación sin pérdida de identificador, tipo, origen o rol; exposición del flujo de rol persistido. Los ViewModels se ejercitan de forma indirecta desde Espresso. | `UseCasesTest`, escenarios E2E |
| Repositorio | Emisión de caché y red, resultados sellados de carga, éxito, error y cancelación, reemplazo de caché, 404, timeout y propagación de cancelación. | `AlbumRepositoryImplTest`, `ArtistRepositoryImplTest`, `UserRoleRepositoryImplTest` |
| Red y mapeo | Cabecera JSON, unión de músicos y bandas, descarte de registros inválidos, resolución de premios, transformación de DTO y traducción de errores. | `RequestHeadersInterceptorTest`, `NetworkErrorMapperTest`, `ArtistServiceAdapterTest`, `AlbumMappersTest`, `ArtistMappersTest` |
| Persistencia | Consultas SQL, orden del catálogo, exclusión de filas de detalle, `replaceAll`, `upsert` y lectura/escritura del rol. | `AlbumDaoTest`, `UserRolePreferencesDataSourceTest` |
| Interfaz | Navegación, datos visibles, estados vacío y error, reintento, aviso de caché y visibilidad de acciones por rol. | 15 escenarios E2E de `Sprint1EspressoTest` |

### 2.4. Datos y fixtures

- **`FixtureApiInterceptor`** intercepta OkHttp y responde por ruta con datos deterministas: dos álbumes, un músico, una banda, el detalle del álbum 100 con dos tracks y un comentario, el detalle del músico 100 con premio y el detalle de la banda 101 con integrantes. Permite forzar códigos por ruta (`responseCodes`), activar catálogos vacíos (`emptyAlbums`, `emptyArtists`), simular un álbum sin tracks (`albumWithoutTracks`) y registrar las rutas solicitadas (`requestedPaths`) para comprobar qué endpoints se consultaron.
- **Room en memoria** se inyecta con `EspressoDatabaseModule`, de modo que cada corrida parte de una base limpia y la caché de HU01 puede probarse sin residuos entre escenarios.
- **Módulos Hilt de test** (`EspressoNetworkModule`, `EspressoRepositoryModule`, `EspressoDatabaseModule`) sustituyen las implementaciones de producción mediante `@UninstallModules`; los repositorios, ViewModels y vistas siguen siendo los reales.
- **`FakeUserRoleRepository`** controla el rol sin depender de DataStore y permite verificar la visibilidad de acciones para Visitante y Coleccionista.
- **`VinilosTestRunner`** registra `HiltTestApplication` como aplicación de instrumentación.
- En la JVM, `AlbumDaoTest` usa Room real en memoria bajo Robolectric API 28; el resto de las pruebas unitarias usa MockK para aislar colaboradores.

---

## 3. Entornos y matriz de dispositivos

| Entorno | API | Dispositivo o emulador | Resultado | Estado |
|---|---|---|---|---|
| JVM local con Robolectric | 28 (configuración de Robolectric) | Host de desarrollo | 46 unitarias ejecutadas, 0 fallos | Aprobado |
| Emulador Android 9 | 28 | `Vinilos_API28` (`emulator-5554`) | 15 E2E ejecutadas, 0 fallos | Aprobado |
| Emulador Android 12 | 31 | `Huawei(AVD)` (`emulator-5556`) | 15 E2E ejecutadas, 0 fallos | Aprobado |
| Emulador Android 13 | 33 | `Pixel_6a` | Ejecución interrumpida por desconexión del emulador; reporte parcial | No válido |
| Emulador o dispositivo Android 5.0 | 21 | No disponible en el equipo | No ejecutado | Pendiente |
| Backend local con Docker | No aplica | `127.0.0.1:3000` | 4 HU verificadas por contrato | Aprobado |
| Backend HTTPS publicado | No aplica | Servicio de Render | `GET /albums` responde HTTP 500 | Bloqueado (#48) |

La ejecución de API 28 es previa a la migración de los repositorios a `Flow<DataResult<T>>`; la de API 31 corresponde al build actual. Ambas se conservan como evidencia histórica. En API 21 se ejecutó la batería completa: los 15 escenarios Espresso aprobaron en Android 5.0.2 mediante `am instrument`, y además se verificó el APK de entrega de forma manual (instalación, arranque, navegación y estados de error). El detalle está en la sección 6 y las capturas en `evidencias/sprint1/api21/`.

---

## 4. Escenarios E2E

Los 15 escenarios se ejecutan sobre Activities reales con Retrofit, repositorios, ViewModels, Room en memoria y vistas reales. La red se sustituye únicamente en el límite HTTP mediante `FixtureApiInterceptor`. Todos los escenarios viven en [`Sprint1EspressoTest.kt`](../app/src/androidTest/java/com/example/vinilosmobile/Sprint1EspressoTest.kt).

### 4.1. HU01 — Consultar catálogo de álbumes

| ID | Escenario | Precondición | Pasos | Resultado esperado | Automatizado en |
|---|---|---|---|---|---|
| E2E-01 | Catálogo cargado desde Retrofit y repositorio | App instalada; `/albums` responde 200 con dos álbumes | 1. Abrir el catálogo.<br>2. Esperar la carga.<br>3. Revisar la grilla. | Se muestra «Buscando América»; la grilla contiene «A Night at the Opera»; se registra `GET /albums`. | `hu01_catalogLoadsThroughRetrofitAndRepository` |
| E2E-02 | Catálogo vacío muestra estado vacío | `emptyAlbums = true` | 1. Abrir el catálogo.<br>2. Esperar la respuesta vacía. | Se muestra el mensaje de catálogo vacío (`catalog_empty`). | `hu01_emptyCatalogShowsEmptyState` |
| E2E-03 | Error del servidor permite reintentar | `/albums` responde 500 | 1. Abrir el catálogo.<br>2. Esperar el error.<br>3. Restaurar la respuesta 200.<br>4. Pulsar «Reintentar». | Se muestra el error de catálogo (`catalog_error_title`) y, tras el reintento, «Buscando América». | `hu01_serverErrorCanRetry` |
| E2E-04 | Fallo de red muestra la copia de Room | Primera apertura con 200 que llena Room; luego `/albums` responde 500 | 1. Abrir el catálogo y esperar los datos.<br>2. Forzar el fallo de red.<br>3. Reabrir el catálogo. | Aparece el aviso de caché (`cachedNotice`) y la grilla conserva «Buscando América» desde Room. | `hu01_networkFailureShowsRoomCache` |

### 4.2. HU02 — Consultar detalle de un álbum

| ID | Escenario | Precondición | Pasos | Resultado esperado | Automatizado en |
|---|---|---|---|---|---|
| E2E-05 | La tarjeta abre el detalle con tracks y comentarios | Rol Coleccionista; `/albums` y `/albums/100` responden 200 | 1. Abrir el catálogo.<br>2. Tocar «Buscando América».<br>3. Revisar tracks y comentarios.<br>4. Revisar el botón de agregar track.<br>5. Volver. | El detalle muestra el nombre, el track «Decisiones», el comentario «Gran álbum» y el FAB de track visible; se registra `GET /albums/100` y se vuelve al catálogo. | `hu02_cardOpensAlbumActivityWithTracksAndComments` |
| E2E-06 | Álbum inexistente muestra no encontrado y Visitante no tiene acción | Rol Visitante; `/albums/999` responde 404 | 1. Abrir el detalle del álbum 999. | Se muestra el mensaje de álbum no encontrado (`album_not_found`) y el FAB de track no es visible. | `hu02_missingAlbumShowsNotFoundAndVisitorHasNoAction` |
| E2E-07 | Álbum sin tracks muestra mensaje de vacío | `albumWithoutTracks = true` | 1. Abrir el detalle del álbum 100. | Se muestra el nombre del álbum y el mensaje de álbum sin tracks (`album_no_tracks`). | `hu02_albumWithoutTracksShowsEmptyMessage` |
| E2E-08 | Error del detalle permite reintentar | `/albums/100` responde 500 | 1. Abrir el detalle del álbum 100.<br>2. Esperar el error.<br>3. Restaurar la respuesta 200.<br>4. Pulsar «Reintentar». | Se muestra el error de detalle (`album_detail_error`) y, tras el reintento, «Buscando América». | `hu02_serverErrorCanRetry` |

### 4.3. HU03 — Consultar listado de artistas

| ID | Escenario | Precondición | Pasos | Resultado esperado | Automatizado en |
|---|---|---|---|---|---|
| E2E-09 | El listado filtra músicos y bandas | `/musicians` y `/bands` responden 200 | 1. Abrir la app.<br>2. Entrar a Artistas.<br>3. Esperar «Queen».<br>4. Filtrar Bandas.<br>5. Filtrar Músicos. | Bandas muestra «Queen» y no «Rubén Blades»; Músicos muestra «Rubén Blades»; se registran `GET /musicians` y `GET /bands`. | `hu03_artistListFiltersMusiciansAndBands` |
| E2E-10 | Listado vacío muestra estado vacío | `emptyArtists = true` | 1. Abrir la app.<br>2. Entrar a Artistas. | Se muestra el mensaje de artistas vacío (`artists_empty`). | `hu03_emptyArtistsShowEmptyState` |
| E2E-11 | Error del listado permite reintentar | `/bands` responde 500 | 1. Entrar a Artistas.<br>2. Esperar el error.<br>3. Restaurar la respuesta 200.<br>4. Pulsar «Reintentar». | Se muestra el error de artistas (`artists_error`) y, tras el reintento, «Queen». | `hu03_serverErrorCanRetry` |

### 4.4. HU04 — Consultar detalle de un artista

| ID | Escenario | Precondición | Pasos | Resultado esperado | Automatizado en |
|---|---|---|---|---|---|
| E2E-12 | El detalle de músico muestra álbumes y premio | `/musicians/100` y `/performerprizes` responden 200 | 1. Entrar a Artistas.<br>2. Tocar «Rubén Blades».<br>3. Revisar fecha, álbumes y premios.<br>4. Volver. | Se muestra la etiqueta de fecha, «Buscando América» en la lista de álbumes y «Grammy Award» en premios; se registran `GET /musicians/100` y `GET /performerprizes`. | `hu04_musicianDetailDisplaysAlbumsAndPrize` |
| E2E-13 | El detalle de banda muestra integrantes y fecha de creación | `/bands/101` responde 200; se abre con `ArtistKey(101, BAND)` | 1. Abrir el detalle de la banda 101.<br>2. Revisar integrantes y fecha. | Se muestra «Queen», «Freddie Mercury» en integrantes y la etiqueta de fecha de creación (`artist_creation_date`); se registra `GET /bands/101`. | `hu04_bandDetailDisplaysMembersAndCreationDate` |
| E2E-14 | Artista inexistente muestra no encontrado | `/musicians/999` responde 404 | 1. Abrir el detalle del músico 999. | Se muestra el mensaje de artista no encontrado (`artist_not_found`). | `hu04_missingArtistShowsNotFound` |
| E2E-15 | Error del detalle de artista permite reintentar | `/musicians/100` responde 500 | 1. Abrir el detalle del músico 100.<br>2. Esperar el error.<br>3. Restaurar la respuesta 200.<br>4. Pulsar «Reintentar». | Se muestra el error de detalle (`artist_detail_error`) y, tras el reintento, «Rubén Blades». | `hu04_serverErrorCanRetry` |

---

## 5. Casos de prueba por historia de usuario

Cada tabla relaciona los escenarios E2E con las pruebas unitarias o de integración que cubren la misma regla. Las clases compartidas entre historias se listan en cada una, pero se contabilizan una sola vez en la sección 6.

### 5.1. HU01 — Catálogo de álbumes

Criterios de aceptación: **CA-HU01.1** el catálogo muestra los álbumes vigentes de `/albums`; **CA-HU01.2** sin red se conserva la última copia local con aviso; **CA-HU01.3** una respuesta vacía muestra estado vacío; **CA-HU01.4** un error muestra mensaje y permite reintentar.

| Caso | Tipo | Qué verifica | Criterio |
|---|---|---|---|
| E2E-01 | Sistema / E2E | Carga del catálogo a través de Retrofit, repositorio y grilla real. | CA-HU01.1 |
| E2E-02 | Sistema / E2E | Estado vacío cuando la API devuelve una lista sin elementos. | CA-HU01.3 |
| E2E-03 | Sistema / E2E | Error 500 visible y reintento exitoso. | CA-HU01.4 |
| E2E-04 | Sistema / E2E | Copia de Room visible cuando falla la red. | CA-HU01.2 |
| `AlbumRepositoryImplTest` (13) | Unitario | Emisión de caché y red, reemplazo de Room, 404, fallo sin caché y cancelación. | CA-HU01.1, CA-HU01.2, CA-HU01.4 |
| `AlbumDaoTest` (3) | Integración | Consulta del catálogo, exclusión de filas de detalle, orden, `replaceAll` y `upsert`. | CA-HU01.1, CA-HU01.2 |
| `AlbumMappersTest` (3) | Unitario | DTO válido, descarte de registros inválidos y round-trip con Room. | CA-HU01.1 |
| `UseCasesTest::getAlbumsPreservesSourceInformation` (1) | Unitario | El caso de uso conserva la información de origen del catálogo. | CA-HU01.1 |
| `NetworkErrorMapperTest` (3) | Unitario transversal | Conexión, timeout, permisos y servidor traducidos a fallos tipados. | CA-HU01.4 |

### 5.2. HU02 — Detalle de un álbum

Criterios de aceptación: **CA-HU02.1** la tarjeta abre el detalle con datos, tracks y comentarios; **CA-HU02.2** un identificador inexistente muestra no encontrado; **CA-HU02.3** un álbum sin tracks muestra un mensaje explícito; **CA-HU02.4** un error permite reintentar; **CA-HU02.5** la acción de agregar track solo es visible para Coleccionista.

| Caso | Tipo | Qué verifica | Criterio |
|---|---|---|---|
| E2E-05 | Sistema / E2E | Navegación por ID, tracks, comentarios, FAB de Coleccionista y retorno. | CA-HU02.1, CA-HU02.5 |
| E2E-06 | Sistema / E2E | 404 de álbum y ausencia de acción para Visitante. | CA-HU02.2, CA-HU02.5 |
| E2E-07 | Sistema / E2E | Mensaje de álbum sin tracks. | CA-HU02.3 |
| E2E-08 | Sistema / E2E | Error 500 del detalle y reintento exitoso. | CA-HU02.4 |
| `AlbumRepositoryImplTest` (13, compartida con HU01) | Unitario | Detalle desde red, detalle desde caché, 404 sin datos obsoletos y cancelación. | CA-HU02.1, CA-HU02.2 |
| `AlbumMappersTest` (3, compartida con HU01) | Unitario | Detalle válido, relaciones filtradas y campos ausentes en la copia Room. | CA-HU02.1 |
| `UseCasesTest::getAlbumPassesSelectedId` (1) | Unitario | El caso de uso entrega el ID seleccionado al repositorio. | CA-HU02.1 |
| `UserRolePreferencesDataSourceTest` (2) y `UserRoleRepositoryImplTest` (2) | Unitario transversal | Lectura, escritura y flujo del rol que habilita el FAB. | CA-HU02.5 |

### 5.3. HU03 — Listado de artistas

Criterios de aceptación: **CA-HU03.1** el listado combina músicos y bandas; **CA-HU03.2** los filtros separan cada tipo; **CA-HU03.3** un listado vacío muestra estado vacío; **CA-HU03.4** un error muestra mensaje y permite reintentar.

| Caso | Tipo | Qué verifica | Criterio |
|---|---|---|---|
| E2E-09 | Sistema / E2E | Listado combinado y filtros de Bandas y Músicos. | CA-HU03.1, CA-HU03.2 |
| E2E-10 | Sistema / E2E | Estado vacío del listado. | CA-HU03.3 |
| E2E-11 | Sistema / E2E | Error 500 y reintento exitoso. | CA-HU03.4 |
| `ArtistRepositoryImplTest` (5) | Unitario | Listado desde el adaptador, detalle por tipo e ID, timeout, 404 y cancelación. | CA-HU03.1, CA-HU03.4 |
| `ArtistServiceAdapterTest` (5) | Unitario | Unión ordenada de músicos y bandas, descarte de registros inválidos y detalle por tipo. | CA-HU03.1, CA-HU03.2 |
| `ArtistMappersTest` (3) | Unitario | Artista inválido descartado y transformación de músico y banda. | CA-HU03.1 |
| `UseCasesTest::getArtistsPassesCatalog` (1) | Unitario | El caso de uso entrega el catálogo de artistas. | CA-HU03.1 |
| `RequestHeadersInterceptorTest` (1) | Unitario transversal | Cabecera JSON y una sola delegación a OkHttp. | CA-HU03.1 |

### 5.4. HU04 — Detalle de un artista

Criterios de aceptación: **CA-HU04.1** el detalle de músico muestra fecha, álbumes y premios; **CA-HU04.2** el detalle de banda muestra fecha de creación e integrantes; **CA-HU04.3** un identificador inexistente muestra no encontrado; **CA-HU04.4** un error permite reintentar.

| Caso | Tipo | Qué verifica | Criterio |
|---|---|---|---|
| E2E-12 | Sistema / E2E | Detalle de músico con álbumes y premio resuelto desde el catálogo de premios. | CA-HU04.1 |
| E2E-13 | Sistema / E2E | Detalle de banda con integrantes y fecha de creación. | CA-HU04.2 |
| E2E-14 | Sistema / E2E | 404 de artista. | CA-HU04.3 |
| E2E-15 | Sistema / E2E | Error 500 del detalle de artista y reintento exitoso. | CA-HU04.4 |
| `ArtistRepositoryImplTest` (5, compartida con HU03) | Unitario | Detalle por tipo e ID, timeout, 404 y cancelación. | CA-HU04.1, CA-HU04.2, CA-HU04.3 |
| `ArtistServiceAdapterTest` (5, compartida con HU03) | Unitario | Resolución del premio, endpoint de banda, integrantes y fallo de premios que no oculta el detalle. | CA-HU04.1, CA-HU04.2 |
| `ArtistMappersTest` (3, compartida con HU03) | Unitario | Fecha de nacimiento, fecha de creación, álbumes y miembros válidos. | CA-HU04.1, CA-HU04.2 |
| `UseCasesTest::getArtistPassesTypeAndId` (1) | Unitario | El caso de uso entrega tipo e ID del artista. | CA-HU04.1, CA-HU04.2 |
| `NetworkErrorMapperTest` (3, compartida) | Unitario transversal | Timeout y error de servidor tipados para el detalle. | CA-HU04.4 |

### 5.5. Mapa de cobertura unitaria

| Archivo | Pruebas | Historias cubiertas |
|---|---:|---|
| `AlbumRepositoryImplTest` | 13 | HU01, HU02 |
| `ArtistRepositoryImplTest` | 5 | HU03, HU04 |
| `UserRoleRepositoryImplTest` | 2 | Transversal (acceso y acciones por rol) |
| `UserRolePreferencesDataSourceTest` | 2 | Transversal (acceso y acciones por rol) |
| `RequestHeadersInterceptorTest` | 1 | Transversal (red) |
| `ArtistServiceAdapterTest` | 5 | HU03, HU04 |
| `NetworkErrorMapperTest` | 3 | HU01, HU02, HU04 |
| `AlbumDaoTest` | 3 | HU01 |
| `ArtistMappersTest` | 3 | HU03, HU04 |
| `AlbumMappersTest` | 3 | HU01, HU02 |
| `UseCasesTest` | 6 | HU01 (1), HU02 (1), HU03 (1), HU04 (1), acceso (2) |
| **Total** | **46** | — |

---

## 6. Resultados de la ejecución

La evidencia principal es esta tabla. Los archivos JUnit XML se conservan como respaldo verificable de cada corrida y se enlazan en la última columna.

| Conjunto | Entorno | Ejecutadas | Aprobadas | Fallidas | Duración o fecha | Evidencia |
|---|---|---:|---:|---:|---|---|
| `AlbumRepositoryImplTest` | JVM local | 13 | 13 | 0 | 0,47 s · 2026-10-25 | [XML](evidencias/sprint1/unit/TEST-com.example.vinilosmobile.data.repository.AlbumRepositoryImplTest.xml) |
| `ArtistRepositoryImplTest` | JVM local | 5 | 5 | 0 | 0,36 s · 2026-10-25 | [XML](evidencias/sprint1/unit/TEST-com.example.vinilosmobile.data.repository.ArtistRepositoryImplTest.xml) |
| `UserRoleRepositoryImplTest` | JVM local | 2 | 2 | 0 | 0,04 s · 2026-10-25 | [XML](evidencias/sprint1/unit/TEST-com.example.vinilosmobile.data.repository.UserRoleRepositoryImplTest.xml) |
| `UserRolePreferencesDataSourceTest` | JVM local | 2 | 2 | 0 | 0,11 s · 2026-10-25 | [XML](evidencias/sprint1/unit/TEST-com.example.vinilosmobile.data.preferences.UserRolePreferencesDataSourceTest.xml) |
| `RequestHeadersInterceptorTest` | JVM local | 1 | 1 | 0 | 0,15 s · 2026-10-25 | [XML](evidencias/sprint1/unit/TEST-com.example.vinilosmobile.data.network.interceptors.RequestHeadersInterceptorTest.xml) |
| `ArtistServiceAdapterTest` | JVM local | 5 | 5 | 0 | 0,96 s · 2026-10-25 | [XML](evidencias/sprint1/unit/TEST-com.example.vinilosmobile.data.network.ArtistServiceAdapterTest.xml) |
| `NetworkErrorMapperTest` | JVM local | 3 | 3 | 0 | 0,23 s · 2026-10-25 | [XML](evidencias/sprint1/unit/TEST-com.example.vinilosmobile.data.network.errors.NetworkErrorMapperTest.xml) |
| `AlbumDaoTest` | JVM + Robolectric API 28 | 3 | 3 | 0 | 5,80 s · 2026-10-25 | [XML](evidencias/sprint1/unit/TEST-com.example.vinilosmobile.data.local.AlbumDaoTest.xml) |
| `ArtistMappersTest` | JVM local | 3 | 3 | 0 | 0,003 s · 2026-10-25 | [XML](evidencias/sprint1/unit/TEST-com.example.vinilosmobile.data.mappers.ArtistMappersTest.xml) |
| `AlbumMappersTest` | JVM local | 3 | 3 | 0 | 0,02 s · 2026-10-25 | [XML](evidencias/sprint1/unit/TEST-com.example.vinilosmobile.data.mappers.AlbumMappersTest.xml) |
| `UseCasesTest` | JVM local | 6 | 6 | 0 | 0,11 s · 2026-10-25 | [XML](evidencias/sprint1/unit/TEST-com.example.vinilosmobile.domain.usecase.UseCasesTest.xml) |
| **Total unitarias** | **JVM local** | **46** | **46** | **0** | **8,24 s (suma de suites) · 2026-10-25** | [Directorio](evidencias/sprint1/unit/) |
| **E2E Espresso** | **Android 9 / API 28** | **15** | **15** | **0** | **10,54 s · 2026-10-24** | [XML](evidencias/sprint1/espresso-api28.xml) |
| **E2E Espresso** | **Android 12 / API 31** | **15** | **15** | **0** | **17,12 s · 2026-10-25** | [XML](evidencias/sprint1/espresso-api31.xml) |
| Contrato de API | BackVynils local (Docker) | 4 verificaciones (HU01–HU04) | 4 | 0 | 2026-10-24 · duración no registrada | [Script](scripts/smoke_sprint1_api.py) |
| **Consumo real de la API** | **Android 5.0.2 / API 21 + BackVynils local** | **4 pantallas** | **4** | **0** | 2026-10-30 | [Capturas y registro](evidencias/sprint1/api-real/) |
| Aceptación manual M01–M04 | Android 12 / API 31 | 4 | 4 | 0 | Fecha no registrada | Sin registro adjunto |
| **E2E Espresso** | **Android 5.0.2 / API 21 (arm64)** | **15** | **15** | **0** | 2026-10-30 | [XML](evidencias/sprint1/espresso-api21.xml) · [log](evidencias/sprint1/espresso-api21.txt) |
| Compilación y arranque del APK de Release | Android 5.0.2 / API 21 (arm64) | build + 2 pantallas | 3 | 0 | 2026-10-30 | [Evidencia](evidencias/sprint1/release-api21/) |
| Humo manual sobre el APK de entrega | Android 5.0.2 / API 21 (arm64) | 2 pantallas + 2 pestañas | 4 | 0 | 2026-10-30 | [Capturas](evidencias/sprint1/api21/) |
| E2E Espresso | Android 13 / API 33 | No registrado | No registrado | No registrado | Ejecución interrumpida | Reporte parcial descartado |

### 6.1. Resultado por escenario E2E

| Escenario | API 28 | API 31 | Resultado |
|---|---|---|---|
| E2E-01 Catálogo desde Retrofit y repositorio | Aprobado | Aprobado | Cumple |
| E2E-02 Catálogo vacío | Aprobado | Aprobado | Cumple |
| E2E-03 Error de catálogo con reintento | Aprobado | Aprobado | Cumple |
| E2E-04 Copia de Room ante fallo de red | Aprobado | Aprobado | Cumple |
| E2E-05 Detalle de álbum con tracks y comentarios | Aprobado | Aprobado | Cumple |
| E2E-06 Álbum inexistente y Visitante sin acción | Aprobado | Aprobado | Cumple |
| E2E-07 Álbum sin tracks | Aprobado | Aprobado | Cumple |
| E2E-08 Error de detalle de álbum con reintento | Aprobado | Aprobado | Cumple |
| E2E-09 Filtros de músicos y bandas | Aprobado | Aprobado | Cumple |
| E2E-10 Listado de artistas vacío | Aprobado | Aprobado | Cumple |
| E2E-11 Error de artistas con reintento | Aprobado | Aprobado | Cumple |
| E2E-12 Detalle de músico con álbumes y premio | Aprobado | Aprobado | Cumple |
| E2E-13 Detalle de banda con integrantes y fecha | Aprobado | Aprobado | Cumple |
| E2E-14 Artista inexistente | Aprobado | Aprobado | Cumple |
| E2E-15 Error de detalle de artista con reintento | Aprobado | Aprobado | Cumple |

### 6.2. Reproducción

```bash
bash gradlew :app:testDebugUnitTest --no-configuration-cache
bash gradlew :app:connectedDebugAndroidTest --no-configuration-cache
python3 scripts/smoke_sprint1_api.py http://127.0.0.1:3000/
```

El reporte HTML de cada corrida queda en `app/build/reports/` y los XML en `app/build/test-results/`. Las copias versionadas en `docs/evidencias/sprint1/` son el respaldo de las cifras de esta sección.

---

## 7. Defectos y riesgos

| ID | Título | Tipo | Impacto | Estado | Mitigación |
|---|---|---|---|---|---|
| #47 | Release se cierra al iniciar por configuración R8 | Defecto | Impedía validar la variante Release; la app se cerraba al abrir. | **Resuelto y verificado el 2026-10-30**: `assembleRelease` compila, el APK de 2,5 MB instala y abre en API 21, sin `IllegalAccessError`. Evidencia en `evidencias/sprint1/release-api21/`. | Mantener `optimization.enable = true` sin `packageScope` personalizado, que era la causa. |
| #48 | El backend de entrega no responde y bloquea la validación contra el servicio publicado | Defecto | No se pueden validar las cuatro HU contra el servicio de entrega. | Abierto. | Validar funcionalidad contra BackVynils local con Docker; permitir sobrescribir la URL base con `-PvinilosApiBaseUrl`; no aceptar la consulta en producción hasta que el servicio responda 200. |
| Emulador API 33 inestable | La corrida en API 33 se interrumpió por desconexión del emulador | Sin impacto en la entrega | La batería válida se ejecutó en API 21, API 28 y API 31. | N/A | Repetir en API 33 si se requiere ampliar la matriz, sin contarla hasta que termine. |
| URL base por entorno | Dependencia de `10.0.2.2`, `adb reverse` o `-PvinilosApiBaseUrl` | Riesgo | Una configuración incorrecta produce fallos de conexión que parecen defectos de la app. | Vigente. | Documentar la URL por entorno; usar `adb reverse` en dispositivo físico; verificar la URL antes de cada corrida manual. |

---

## 8. Criterios de entrada y salida

### 8.1. Criterios de entrada

- El proyecto compila y las dependencias de Gradle están resueltas.
- Existe un backend disponible (BackVynils local con Docker) o, para la suite automatizada, los fixtures HTTP están configurados.
- Hay un emulador o dispositivo con la API objetivo conectado y visible en `adb devices`.
- El APK Debug está instalado y la base de datos de prueba parte limpia.
- Los datos semilla incluyen al menos un álbum, un músico y una banda.
- Los defectos conocidos están registrados como issues con etiqueta `bug`.

### 8.2. Criterios de salida

Una historia se considera aceptada cuando:

1. Todas sus pruebas unitarias pasan (46 de 46 en el sprint).
2. Todos sus escenarios E2E pasan en la API objetivo (15 de 15 por corrida).
3. El contrato de API responde correctamente para sus endpoints contra BackVynils local (4 de 4 verificaciones).
4. El recorrido manual asociado (M01–M04) está aprobado en el entorno de aceptación.
5. No tiene defectos abiertos de severidad alta sin mitigación documentada.
6. La evidencia XML de la corrida está archivada en `docs/evidencias/sprint1/`.

El sprint se considera cerrado cuando el backend de entrega responde correctamente (#48). La suite Espresso ya se ejecutó en API 21, API 28 y API 31, y la variante Release abre sin cierre. La variante Release ya abre sin cierre. La verificación de humo en Lollipop ya está hecha; quedan #47 y #48, así que el cierre es parcial y así debe reportarse.

---

## 9. Trazabilidad con la planeación

El tablero de GitHub Projects no expone en el repositorio identificadores de tarea estables; por eso la columna «Tarea» usa el nombre de la unidad de trabajo del Sprint 1 y la columna de issues distingue los defectos registrados de los que no tienen número. Los únicos issues con identificador verificable en el repositorio son #47 y #48.

| HU | Tarea de planeación | Escenarios E2E | Pruebas unitarias | Issue(s) de GitHub |
|---|---|---|---|---|
| HU01 — Consultar catálogo de álbumes | Catálogo de álbumes con caché Room | E2E-01 a E2E-04 | `AlbumRepositoryImplTest` (13), `AlbumDaoTest` (3), `AlbumMappersTest` (3), `UseCasesTest::getAlbumsPreservesSourceInformation` (1), `NetworkErrorMapperTest` (3) | Implementación: no registrado. Defectos relacionados: #47 (Release), #48 (backend) |
| HU02 — Consultar detalle de un álbum | Detalle de álbum con tracks, comentarios y acciones por rol | E2E-05 a E2E-08 | `AlbumRepositoryImplTest` (13), `AlbumMappersTest` (3), `UseCasesTest::getAlbumPassesSelectedId` (1), pruebas de rol (4) | Implementación: no registrado. Defectos relacionados: #47, #48 |
| HU03 — Consultar listado de artistas | Listado de artistas con filtros de músicos y bandas | E2E-09 a E2E-11 | `ArtistRepositoryImplTest` (5), `ArtistServiceAdapterTest` (5), `ArtistMappersTest` (3), `UseCasesTest::getArtistsPassesCatalog` (1), `RequestHeadersInterceptorTest` (1) | Implementación: no registrado. Defectos relacionados: #47, #48 |
| HU04 — Consultar detalle de un artista | Detalle de músico y banda con premios e integrantes | E2E-12 a E2E-15 | `ArtistRepositoryImplTest` (5), `ArtistServiceAdapterTest` (5), `ArtistMappersTest` (3), `UseCasesTest::getArtistPassesTypeAndId` (1), `NetworkErrorMapperTest` (3) | Implementación: no registrado. Defectos relacionados: #47, #48 |
| Transversal — acceso, red y calidad | Rol persistido, cabeceras, mapeo de errores y contrato de API | E2E-05, E2E-06 | `UserRoleRepositoryImplTest` (2), `UserRolePreferencesDataSourceTest` (2), `RequestHeadersInterceptorTest` (1), `NetworkErrorMapperTest` (3), `UseCasesTest` (2 de rol) | #47, #48 |

**Cobertura de la planeación:** las cuatro historias tienen escenarios E2E, pruebas unitarias y criterios de aceptación asociados. El único pendiente de calidad del sprint es la resolución de #48; ninguno se presenta como aprobado en este documento.
