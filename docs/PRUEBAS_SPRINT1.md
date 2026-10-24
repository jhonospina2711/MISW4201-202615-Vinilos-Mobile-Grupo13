# Estrategia y resultados de pruebas — Sprint 1

Revisión: 24 de octubre de 2026. Alcance: HU01 catálogo de álbumes, HU02 detalle de álbum, HU03 listado de artistas y HU04 detalle de artista.

## Estrategia

1. **E2E automatizadas con Espresso:** probar navegación, estados y datos visibles desde Activities/Fragments reales. Un interceptor HTTP de prueba devuelve respuestas deterministas a Retrofit; se ejecutan los mappers, Service Adapter, Repository, casos de uso y ViewModels reales. Room se crea en memoria y el rol se controla con un repositorio de prueba. Esto evita depender de Docker o de datos compartidos para la batería de regresión. Los tests viven en [`Sprint1EspressoTest.kt`](../app/src/androidTest/java/com/example/vinilosmobile/Sprint1EspressoTest.kt).
2. **Contrato de la API real:** ejecutar [`smoke_sprint1_api.py`](../scripts/smoke_sprint1_api.py) contra BackVynils local. Hace solicitudes de solo lectura a `/albums`, `/albums/{id}`, `/musicians`, `/bands`, ambos detalles y `/performerprizes`; valida códigos 200, JSON e identificadores para las cuatro HU.
3. **E2E manuales:** recorrer la app Debug con el backend Docker real y confirmar datos, navegación y retorno. Repetir los casos críticos en un dispositivo con Android 5.0/API 21 y otro Android posterior antes del release.
4. **Release:** compilar, verificar firma, instalar y abrir el APK optimizado. Ejecutar las cuatro HU contra el backend HTTPS definitivo antes de publicar un APK de entrega.

Una HU se acepta cuando pasan sus casos Espresso, los escenarios manuales acordados y la consulta al backend de entrega, sin defectos abiertos de severidad alta. Los fallos se registran como issues con etiqueta `bug`, pasos de reproducción, entorno, resultado esperado y real.

## Inventario automatizado

| HU | Casos Espresso | Qué comprueban |
|---|---:|---|
| HU01 | 4 | Catálogo desde Retrofit, vacío, error y reintento, copia Room cuando falla la red. |
| HU02 | 4 | Navegación con ID, tracks y comentarios, FAB de coleccionista, 404, álbum sin tracks, error y reintento. |
| HU03 | 3 | Combinación de músicos y bandas, filtros, vacío, error y reintento. |
| HU04 | 4 | Detalle de músico con premio y álbumes, banda con integrantes, 404, error y reintento. |

La batería usa respuestas HTTP de prueba. Por sí sola no demuestra disponibilidad del servicio publicado ni compatibilidad con API 21.

## Guiones E2E manuales

| Caso | Pasos | Resultado esperado |
|---|---|---|
| M01 / HU01 | Instalar Debug, abrir sin rol guardado, elegir Visitante y entrar a Álbumes. | Se muestra el número y la grilla de álbumes devueltos por `/albums`; el FAB de creación está oculto. |
| M02 / HU02 | Tocar un álbum de M01, revisar datos, tracks y comentarios, volver al catálogo. | Se abre una Activity con el ID seleccionado; datos de `/albums/{id}` y botón Volver funcional. El FAB de track solo aparece para Coleccionista. |
| M03 / HU03 | Abrir Artistas, alternar Todos, Músicos y Bandas. | La lista combina `/musicians` y `/bands`; cada filtro conserva solo su tipo. |
| M04 / HU04 | Abrir un músico y una banda desde M03, revisar álbumes, premios e integrantes cuando existan, volver. | El detalle corresponde al ID y tipo seleccionados; los datos son los de la API real. |
| M05 / errores | Desconectar o detener el backend; abrir listados y detalles, pulsar Reintentar después de restaurarlo. | Mensaje legible; HU01 muestra el catálogo guardado si lo tiene. |
| M06 / Release | Instalar el APK Release en un dispositivo limpio, escoger rol y consultar las cuatro HU con la URL HTTPS final. | La app abre sin cierre y las cuatro consultas responden correctamente. |

## Ejecución y evidencia

| Entorno o comando | Resultado |
|---|---|
| Emulador Medium_Phone, Android 12/API 31, `bash gradlew :app:connectedDebugAndroidTest --no-configuration-cache` | **15 pruebas, 0 fallos, 0 errores, 0 omitidas.** [JUnit XML](evidencias/sprint1/espresso-api31.xml). |
| `python3 scripts/smoke_sprint1_api.py` contra Docker local | **4 HU aprobadas:** 4 álbumes; detalle de álbum 100 con 2 tracks; endpoints de músicos y bandas; detalles y premios. |
| M01–M04 manuales en API 31 con Debug y Docker | **Aprobadas.** Visitante vio 4 álbumes; detalle de Buscando América con 2 tracks y 1 comentario; filtros mostraron Rubén Blades y Queen; ambos detalles cargaron y el músico mostró Grammy Award. La banda del conjunto de datos local no tiene integrantes. |
| `bash gradlew :app:assembleRelease --no-configuration-cache` | **Compilación correcta.** APK de 4,1 MB; firma v1/v2 verificada; minSdk 21. Se instaló y abrió en API 31 después de corregir la configuración de R8. Una instalación limpia mostró la selección de rol y luego un error legible por el HTTP 500, sin cierre de la app. SHA-256: `40d0f32defdc0959fcfcc307cbc6c784aae8e7cb0051cc6aaa6b7ec344da20e7`. |
| API 33, Pixel_6a | Ejecución instrumentada interrumpida por desconexión del emulador; el reporte quedó parcial y **no cuenta como batería aprobada**. |
| Android 5.0/API 21 | **Pendiente.** No hay dispositivo ni imagen API 21 instalados en este equipo; `minSdk = 21` y compilación correcta no sustituyen esta ejecución. |
| Backend HTTPS publicado | **Bloqueado:** `GET /albums` continúa respondiendo HTTP 500. Release abre y muestra el catálogo Room guardado si existe, pero no puede validar las cuatro HU contra ese servicio. |

Defectos registrados: [#47, cierre al iniciar Release por R8](https://github.com/jhonospina2711/MISW4201-202615-Vinilos-Mobile-Grupo13/issues/47), corregido en `sprint-1` y pendiente de integrar al release; [#48, HTTP 500 del backend HTTPS](https://github.com/jhonospina2711/MISW4201-202615-Vinilos-Mobile-Grupo13/issues/48), abierto.

## Comandos para repetir

```bash
bash gradlew :app:connectedDebugAndroidTest --no-configuration-cache
python3 scripts/smoke_sprint1_api.py http://127.0.0.1:3000/
bash gradlew :app:assembleRelease --no-configuration-cache
```

El script de API asume datos semilla con al menos un álbum, músico y banda. El emulador usa `10.0.2.2` para conectarse al Docker del computador; un dispositivo físico requiere otra URL alcanzable o `adb reverse`.

## Pendientes de calidad

- Ejecutar M01–M06 y la batería Espresso en Android API 21, con un dispositivo físico o imagen compatible disponible para el equipo.
- Reparar o sustituir el backend HTTPS y repetir M06 sin depender de Room.
- Configurar una firma Release del equipo antes del APK final. La variante actual usa la clave Debug para facilitar la instalación de desarrollo.
- Integrar la corrección del issue #47 y cerrar ese defecto tras verificar el release final; resolver el issue #48 antes de aceptar la consulta en producción.
