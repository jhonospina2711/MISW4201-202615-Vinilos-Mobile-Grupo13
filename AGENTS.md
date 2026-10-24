# Reglas de desarrollo de Vinilos Mobile

## Estructura

- Mantener un solo módulo Gradle, `:app`. Organizar por responsabilidad dentro de `app/src/main/java/com/example/vinilosmobile/`.
- `feature/home/` contiene `MainActivity`, el adapter de las tres secciones, sus Fragments de inicio y contratos de navegación. Los detalles y futuras Activities viven en su propia carpeta de `feature/`.
- `data/network/` contiene servicios Retrofit, modelos DTO, interceptores y mapeo de errores. `data/local/` contiene Room. `data/repository/` coordina ambas fuentes. `di/` contiene los módulos Hilt.
- `domain/` contiene modelos, contratos de repositorio y casos de uso. No depende de clases Android, Retrofit, OkHttp ni Room.
- `coreui/` contiene componentes de Android Views compartidos en `atoms/`, `molecules/`, `organisms/` y `models/`. Los XML permanecen en `res/` y usan nombres descriptivos con prefijo `vinilos_` si son compartidos.

## Archivos y modelos

- Cada clase, interfaz, objeto o enum de nivel superior tiene su propio archivo Kotlin con el mismo nombre. Esto incluye Activities, Fragments, ViewModels, adapters, contratos de navegación y excepciones. Las funciones de extensión relacionadas pueden agruparse en un archivo de mappers.
- Todo DTO, entidad Room, modelo de dominio o modelo de presentación va en la carpeta `models/` de su capa. No pasar DTOs o entidades Room directamente a Views ni adapters.
- Actualizar paquetes, imports, Manifest y referencias XML al mover una pantalla o vista.

## Flujo de datos e interfaz

- La pantalla usa Android Views/XML, MVVM, Hilt y casos de uso. El Fragment observa `LiveData` con `viewLifecycleOwner`; el ViewModel mantiene `MutableLiveData` privado y expone `LiveData` público.
- Representar los estados excluyentes de la pantalla con una interfaz o clase sellada en `models/`. El Fragment pinta estados y envía eventos; no crea Retrofit, DAOs ni repositorios.
- Las peticiones pasan por un `OkHttpClient` compartido y Retrofit provistos por Hilt. Añadir cabeceras comunes en un interceptor; traducir excepciones en `NetworkErrorMapper`. No mostrar mensajes técnicos del backend en la interfaz.
- Registrar solo información básica de peticiones durante Debug. No registrar cuerpos ni datos sensibles; mantener el registro desactivado en Release.
- Para HU01, consultar red primero, actualizar Room con el catálogo recibido y usar la copia guardada solo si falla la consulta. Mostrar al usuario cuándo ve datos guardados.

## Configuración

- La URL base se pasa mediante `-PvinilosApiBaseUrl=...`, siempre con `/` final. No agregar URLs de entornos personales ni credenciales al repositorio.
- Si cambia el esquema de Room, incrementar su versión, crear una migración y conservar el esquema exportado en `app/schemas/`.
- Mantener la carpeta `spec/` local fuera de Git; los acuerdos que debe compartir el equipo van en este archivo, `README.md` o `docs/`.
