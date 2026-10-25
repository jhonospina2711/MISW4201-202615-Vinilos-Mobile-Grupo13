# Arquitectura de Vinilos Mobile — Sprint 1

Implementación de HU01–HU04 en un único módulo Gradle `:app`. Se usan Kotlin, Android Views/XML, Material, MVVM y una separación de responsabilidades inspirada en Clean Architecture. `domain/` declara modelos, contratos de repositorio y casos de uso; `data/` implementa los contratos; `feature/` contiene las pantallas; `coreui/` reúne vistas y adapters compartidos. Hilt construye las dependencias en `di/`.

## Componentes y dependencias

```mermaid
flowchart LR
  subgraph UI[Presentación]
    Screens[Entry, Home, detalle de álbum y artista]
    VM[ViewModels con MutableLiveData privado]
    CoreUI[coreui: atoms, molecules, organisms]
    Screens --> VM
    Screens --> CoreUI
  end
  subgraph Domain[Dominio sin Android]
    UC[Casos de uso que exponen Flow]
    Contracts[Contratos Flow de DataResult y modelos]
    UC --> Contracts
  end
  subgraph Data[Datos]
    Repo[Repository emite DataResult en Flow]
    Adapter[ArtistServiceAdapter y mappers]
    Api[Retrofit + OkHttp]
    Room[Room: AlbumDao]
    Store[DataStore: rol]
    Repo --> Adapter --> Api
    Repo --> Room
    Repo --> Store
  end
  VM --> UC
  Contracts -. implementados por .-> Repo
  Api --> Backend[BackVynils REST]
  Hilt[Hilt: módulos di] -. provee .-> VM
  Hilt -. provee .-> Repo
  Hilt -. provee .-> Api
  Hilt -. provee .-> Room
  Hilt -. provee .-> Store
```

Una pantalla envía eventos al ViewModel y observa su `LiveData` (`viewLifecycleOwner` en Fragments, Activity como propietario en Activities). El ViewModel recoge con `collect` el `Flow<DataResult<T>>` del caso de uso y actualiza un `MutableLiveData` privado; expone únicamente `LiveData` de estados sellados (`Loading`, `Content`, `Empty`, `Error`, `Cancelled` y, en los detalles, `NotFound`). El Repository emite `DataResult.Loading`, `Success`, `Error` o `Cancelled`, y el caso de uso transmite ese flujo sin introducir Android en `domain/`. `Success` puede llegar más de una vez en álbumes: primero caché y después red. `Error` lleva `DataError.Request` con una categoría segura para UI o `DataError.NotFound`; si ya hay álbum guardado, el ViewModel conserva ese contenido ante un error de red. `LiveData` queda solo en presentación. DTO y entidades Room no llegan a la UI. El `NetworkErrorMapper` transforma fallos técnicos en categorías para mensajes legibles. Los interceptores añaden cabeceras comunes y registran solo datos básicos en Debug; Release desactiva ese registro.

`Cancelled` representa una solicitud interrumpida de forma independiente mientras el ViewModel sigue activo y permite mostrar Reintentar. Una cancelación del `viewModelScope` por cierre de pantalla se propaga como `CancellationException`; no se intenta emitir ni pintar un estado después de destruir la pantalla. Esto sigue la [transparencia de excepciones de Kotlin Flow](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/catch.html) y el consumo de [LiveData según el ciclo de vida de Views](https://developer.android.com/topic/architecture/views/ui-layer). La preferencia de rol continúa como `Flow<UserRole?>` porque es una fuente continua de valores, no una operación de carga de las cuatro HU.

## Relaciones principales

```mermaid
classDiagram
  class AlbumCatalogViewModel
  class ArtistCatalogViewModel
  class AlbumDetailViewModel
  class ArtistDetailViewModel
  class GetAlbumsUseCase
  class GetAlbumUseCase
  class GetArtistsUseCase
  class GetArtistUseCase
  class AlbumRepository
  class ArtistRepository
  class AlbumRepositoryImpl
  class ArtistRepositoryImpl
  class AlbumDao
  class ArtistServiceAdapter
  class AlbumApiService
  class ArtistApiService
  AlbumCatalogViewModel --> GetAlbumsUseCase
  AlbumDetailViewModel --> GetAlbumUseCase
  ArtistCatalogViewModel --> GetArtistsUseCase
  ArtistDetailViewModel --> GetArtistUseCase
  GetAlbumsUseCase --> AlbumRepository
  GetAlbumUseCase --> AlbumRepository
  GetArtistsUseCase --> ArtistRepository
  GetArtistUseCase --> ArtistRepository
  AlbumRepository <|.. AlbumRepositoryImpl
  ArtistRepository <|.. ArtistRepositoryImpl
  AlbumRepositoryImpl --> AlbumApiService
  AlbumRepositoryImpl --> AlbumDao
  ArtistRepositoryImpl --> ArtistServiceAdapter
  ArtistServiceAdapter --> ArtistApiService
```

`ArtistKey` combina ID y tipo (`MUSICIAN` o `BAND`) para elegir el endpoint correcto. `GetUserRoleUseCase` y `SetUserRoleUseCase` acceden al contrato de rol; DataStore queda encapsulado en `UserRolePreferencesDataSource`.

## Navegación

```mermaid
flowchart TD
  Launch[Inicio de la app] --> Entry[EntryActivity]
  Entry -->|sin rol| Choice[Visitante o Coleccionista]
  Choice -->|guardar en DataStore| Home[MainActivity]
  Entry -->|rol guardado| Home
  Home --> Pager[ViewPager2 + menú inferior]
  Pager --> Albums[AlbumCatalogFragment]
  Pager --> Artists[ArtistsFragment]
  Pager --> Collectors[CollectorsFragment]
  Albums -->|ID| AlbumDetail[AlbumDetailActivity]
  Artists -->|ArtistKey| ArtistDetail[ArtistDetailActivity]
  Home -->|cambiar rol| Entry
```

Las tres secciones de Home se cambian con menú o deslizamiento. Los detalles viven en Activities propias y reciben el identificador, no un DTO completo. El rol persistido es la fuente de verdad: el FAB de álbum en Home y el de track en detalle solo aparecen para Coleccionista. Sus acciones son avisos de “próximamente” en este sprint.

## Flujo de artistas y conexión

```mermaid
sequenceDiagram
  actor Usuario
  participant Vista as ArtistsFragment
  participant VM as ArtistCatalogViewModel
  participant UC as GetArtistsUseCase
  participant Repo as ArtistRepositoryImpl
  participant Adapter as ArtistServiceAdapter
  participant API as ArtistApiService
  participant Server as BackVynils
  participant Detail as ArtistDetailActivity
  Usuario->>Vista: Abre Artistas
  Vista->>VM: observe(state, viewLifecycleOwner) y reload()
  VM->>UC: invoke()
  UC->>Repo: getArtists() retorna Flow de DataResult
  Repo-->>UC: Loading
  UC-->>VM: Flow emite Loading
  VM-->>Vista: LiveData muestra Loading
  Repo->>Adapter: getArtists()
  par Consultas concurrentes
    Adapter->>API: getMusicians()
    API->>Server: GET /musicians
    Server-->>API: músicos JSON
  and
    Adapter->>API: getBands()
    API->>Server: GET /bands
    Server-->>API: bandas JSON
  end
  alt Ambas respuestas correctas
    Adapter->>Adapter: DTO a Artist, combinar y ordenar
    Adapter-->>Repo: List<Artist>
    Repo-->>UC: Success(List de Artist)
    UC-->>VM: Flow emite Success
    VM->>VM: collect y actualiza MutableLiveData
    VM-->>Vista: observer recibe Content o Empty
    Vista-->>Usuario: Lista y filtros locales
    opt Selecciona un artista
      Usuario->>Vista: Toca fila
      Vista->>Detail: abre con ArtistKey
      Note over Detail,Server: Solicita el detalle y los premios cuando existen
    end
  else Falla una consulta o no hay red
    Server--xAPI: error
    Repo-->>UC: Error(DataError.Request)
    UC-->>VM: Flow emite Error
    VM->>VM: actualiza MutableLiveData con Error
    VM-->>Vista: observer muestra Error y Reintentar
    Note over Repo,Server: Artistas no tienen caché Room en Sprint 1
  else Solicitud cancelada independientemente
    Repo-->>UC: Cancelled
    UC-->>VM: Flow emite Cancelled
    VM-->>Vista: observer muestra Cancelled y Reintentar
  end
```

La lista pide músicos y bandas en paralelo. El Repository emite la lista combinada cuando ambos endpoints responden; no muestra una lista parcial con solo músicos o bandas. El filtro Todos/Músicos/Bandas opera sobre la lista recibida, sin nuevas solicitudes. Los detalles usan el mismo flujo `Flow → collect → MutableLiveData → observe` y se consultan por tipo e ID; una consulta auxiliar a `/performerprizes` completa el nombre de premios cuando el backend no lo incluye. Si esa consulta auxiliar falla, el detalle principal sigue visible. **No se implementó modo offline de artistas**: sin red aparece estado de error y opción de reintento.

## Room y funcionamiento sin red

Room usa `VinilosDatabase` versión 1, esquema exportado a `app/schemas/`, y una tabla `albums`. `AlbumEntity` guarda `id`, posición del catálogo, marca `inCatalog`, nombre, artistas como JSON, género, año, portada, sello y descripción. `AlbumDao.replaceAll` reemplaza el catálogo en una transacción; `upsert` actualiza un detalle sin añadirlo al catálogo si no estaba allí. Al iniciar una consulta, el Repository lee Room y emite de inmediato los datos guardados si existen. Después pide `GET /albums` o `GET /albums/{id}`, convierte el DTO a dominio, actualiza Room y emite la versión de red. Si la red falla, la emisión guardada permanece visible; sin copia local se emite un error. `AlbumCatalog.source = CACHE` permite avisar al usuario que ve datos guardados. Un 404 de detalle termina en `NotFound` aunque se haya emitido antes una copia local.

```mermaid
sequenceDiagram
  actor Usuario
  participant Vista as AlbumCatalogFragment
  participant VM as AlbumCatalogViewModel
  participant UC as GetAlbumsUseCase
  participant Repo as AlbumRepositoryImpl
  participant API as AlbumApiService
  participant Room as AlbumDao
  Usuario->>Vista: Abre Álbumes
  Vista->>VM: observe(state, viewLifecycleOwner)
  VM->>UC: invoke()
  UC->>Repo: getAlbums() retorna Flow de DataResult
  Repo-->>UC: Loading
  UC-->>VM: Flow emite Loading
  VM-->>Vista: LiveData muestra Loading
  Repo->>Room: getAll()
  opt Hay álbumes guardados
    Room-->>Repo: AlbumEntity[]
    Repo-->>UC: Success(AlbumCatalog CACHE)
    UC-->>VM: Flow emite Success CACHE
    VM->>VM: collect y actualiza MutableLiveData
    VM-->>Vista: observer muestra Content guardado
  end
  Repo->>API: GET /albums
  alt Respuesta correcta
    API-->>Repo: AlbumDto[]
    Repo->>Room: replaceAll(AlbumEntity[])
    Repo-->>UC: Success(AlbumCatalog REMOTE)
    UC-->>VM: Flow emite Success REMOTE
    VM->>VM: collect y actualiza MutableLiveData
    VM-->>Vista: observer muestra Content o Empty actualizado
  else Error de red o servidor
    API--xRepo: excepción
    Repo-->>UC: Error(DataError.Request)
    UC-->>VM: Flow emite Error
    alt Ya se emitió caché
      Note over Repo,Vista: Se conserva Content guardado
    else No hay datos guardados
      VM->>VM: actualiza MutableLiveData con Error
      VM-->>Vista: observer muestra Error y Reintentar
    end
  else Solicitud cancelada independientemente
    Repo-->>UC: Cancelled
    UC-->>VM: Flow emite Cancelled
    VM-->>Vista: observer conserva caché o muestra Reintentar
  end
```

El detalle de álbum usa el mismo flujo y puede emitir el detalle guardado antes del detalle nuevo. La caché de álbumes es **parcial**: la entidad actual no guarda tracks, comentarios ni fecha completa. Un detalle sin red puede mostrar solo los campos persistidos. DataStore es separado de Room y guarda únicamente `selected_user_role` (`visitor` o `collector`), para conservar la elección entre aperturas. Su `Flow` se recoge en los ViewModels de Entry, Home y detalle; Entry usa `first()` para decidir la navegación inicial y Home observa cambios posteriores.

## Tecnologías y decisiones

| Tecnología o decisión | Uso y motivo |
|---|---|
| Un módulo `:app`; paquetes `feature`, `domain`, `data`, `di`, `coreui` | Mantiene límites claros sin el coste de módulos Gradle adicionales para cuatro HU. |
| Kotlin + Android Views/XML + Material | Implementa los diseños en el stack acordado y conserva soporte desde API 21. |
| `coreui/atoms`, `molecules`, `organisms`, `models` | Reúne vistas reutilizables; “atomic design” guía la organización, no añade un módulo ni sustituye XML. |
| Repository y casos de uso con `Flow<DataResult<T>>` | Transmiten carga, éxitos, errores tipados y cancelación independiente; permiten emitir primero Room y después red sin bloquear la pantalla. |
| ViewModel + LiveData + estados sellados | `collect` transforma emisiones de dominio en estados observables con el ciclo de vida de la pantalla. |
| Hilt + KSP | Provee servicios, repositorios, Room, DataStore y ViewModels sin construirlos en Activities. |
| Retrofit + Gson + OkHttp | Un cliente y una URL configurables para BackVynils; errores y cabeceras centralizados. |
| Room | Copia local del catálogo y detalles básicos de álbumes para fallos de conexión. |
| DataStore Preferences | Persiste el rol sin usar la base relacional para una sola preferencia. |
| ViewPager2 + RecyclerView + Glide | Navegación entre secciones, listas eficientes y carga de imágenes. |

Las versiones exactas están en [`gradle/libs.versions.toml`](../gradle/libs.versions.toml). La URL base se puede configurar con `-PvinilosApiBaseUrl=.../`; en el emulador Debug, `10.0.2.2` apunta al Docker local. Las pruebas unitarias con JUnit y MockK cubren casos de uso, repositorios, adaptación y mapeo de DTO, interceptor, errores y DataStore; Robolectric verifica `AlbumDao` con Room real en memoria. Espresso también usa Room en memoria y recorre las cuatro HU con respuestas HTTP controladas. Véase [pruebas Sprint 1](PRUEBAS_SPRINT1.md).
