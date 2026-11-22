# APK del Sprint 3 — Vinilos Mobile

Estos APK se compilaron desde la rama `feature/sprint-3-final` con las ocho historias HU01–HU08. **Aún son candidatos de release:** falta integrar por PR a `develop`, promover a `main`, ejecutar la matriz final de dispositivos y crear el tag. No se deben presentar como release publicado hasta completar esos pasos.

| Archivo | Tamaño aproximado | URL de API por defecto | Uso |
|---|---:|---|---|
| `vinilos-sprint3-release.apk` | 2,8 MB | `https://backvynils-q6yc.onrender.com/` | Instalación en Android moderno cuando el servicio publicado esté disponible |
| `vinilos-sprint3-debug.apk` | 10 MB | `http://10.0.2.2:3000/` | Emulador con BackVynils local levantado según el README |

Ambos usan el paquete `com.example.vinilosmobile`, `minSdk 21` y firma de depuración académica. No pueden coexistir instalados; `adb install -r` reemplaza una variante por la otra si comparten firma.

## Integridad

```text
Release SHA-256  96eb70c198b539eb0c7e9a9fbab1f9d76303c4890d27909365a61a89b732c493
Debug SHA-256    c56786eb1bc2b6d672575d217c4d1b0794863c131b987ec1f1652927f0376dac
```

Comprobar con `shasum -a 256 releases/sprint-3/vinilos-sprint3-release.apk releases/sprint-3/vinilos-sprint3-debug.apk`.

## Verificación disponible

- `assembleRelease` y `assembleDebug`: correctos.
- 80 pruebas JVM y 32 escenarios Espresso: correctos; Espresso se ejecutó en API 28, API 33 y en Samsung físico API 37. La regresión tras las correcciones finales de accesibilidad pasó 32/32 en API 28 y API 37. Resultados en [`docs/evidencias/sprint3/`](../../docs/evidencias/sprint3/).
- Lint Debug: terminó sin errores y con 26 avisos, principalmente versiones de dependencias y recursos no usados. Se guarda el XML para su revisión.
- El APK Release se instaló y abrió en API 33. `am start -W` informó arranque en frío de **440 ms** para `EntryActivity`; `dumpsys meminfo` informó **33.442 KB de PSS** en esa pantalla. Son mediciones puntuales de emulador, no un perfilamiento de las ocho historias ni de dispositivos físicos.
- El 21 de noviembre de 2026, la comprobación HTTPS `GET /albums` contra el servicio publicado agotó 20 s sin bytes de respuesta. El APK puede instalarse y abrir, pero la carga de datos reales por esa URL no quedó verificada en esta corrida. La app sí se recorrió contra BackVynils local en Samsung con un APK Debug compilado para `127.0.0.1` y `adb reverse`; ese APK de prueba difiere del Debug entregable que usa `10.0.2.2`. Repetir la comprobación publicada antes del release.

## Instalación

```bash
adb install -r releases/sprint-3/vinilos-sprint3-release.apk
```

Para el APK Debug, iniciar BackVynils local antes de abrir la app y usar un emulador Android. Para probar en un celular físico con backend local, compilar desde el código con `-PvinilosApiBaseUrl=...` y una dirección alcanzable desde el celular; `10.0.2.2` solo funciona en el emulador.
