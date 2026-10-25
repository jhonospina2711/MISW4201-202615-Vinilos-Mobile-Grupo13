# Selección de acceso

La Activity de entrada (`feature/entry/EntryActivity`) es el launcher. Su ViewModel lee el rol mediante `GetUserRoleUseCase`. Si no hay preferencia, muestra las opciones Visitante y Coleccionista con el componente compartido `coreui/molecules/RoleOptionView`. Si hay una selección guardada, abre directamente `MainActivity` y cierra la pantalla de entrada.

Al elegir una opción, `SetUserRoleUseCase` la guarda en Preferences DataStore antes de navegar. `HomeViewModel` vuelve a observar el rol desde DataStore; no se duplica como extra del Intent. El icono superior derecho de Home abre la misma pantalla de entrada en modo cambio y marca la opción actual. Al guardar, vuelve a Home. El botón flotante «Nuevo álbum» solo aparece para Coleccionista y, por ahora, muestra el aviso «Agregar próximamente».

La fuente de datos está en `data/preferences/`, el contrato y el enum en `domain/`, y la implementación del repositorio en `data/repository/`. El archivo de preferencias se llama `user_role`; los valores persistidos son `visitor` y `collector`. La versión de DataStore se mantiene en la serie 1.1 para conservar `minSdk = 21`.
