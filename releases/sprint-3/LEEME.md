# APK del Sprint 3 — Vinilos Mobile

Estos APK se compilaron desde la rama `feature/sprint-3-final` con las ocho historias HU01–HU08. **Aún son candidatos de release:** falta integrar por PR a `develop`, promover a `main`, ejecutar la matriz final de dispositivos y crear el tag. No se deben presentar como release publicado hasta completar esos pasos.

| Archivo | Tamaño aproximado | URL de API por defecto | Uso |
|---|---:|---|---|
| `vinilos-sprint3-release.apk` | 2,8 MB | `https://backvynils-q6yc.onrender.com/` | Instalación en Android moderno cuando el servicio publicado esté disponible |
| `vinilos-sprint3-debug.apk` | 10 MB | `http://10.0.2.2:3000/` | Emulador con BackVynils local levantado según el README |

Ambos usan el paquete `com.example.vinilosmobile`, `minSdk 21` y firma de depuración académica. No pueden coexistir instalados; `adb install -r` reemplaza una variante por la otra si comparten firma.

## Integridad

```text
Release SHA-256  99365e74bcf0c721aeb272df0798c67cbf0705b698062265e436ca8b10e25a50
Debug SHA-256    a4f9bdd4aea97cb1f9c2cb8054c975b44a161cb3f2043c436973a0e430e07d72
```

Comprobar con `shasum -a 256 releases/sprint-3/vinilos-sprint3-release.apk releases/sprint-3/vinilos-sprint3-debug.apk`.

## Verificación disponible

- `assembleRelease` y `assembleDebug`: correctos.
- 80 pruebas JVM y 32 escenarios Espresso: correctos; Espresso se ejecutó en Pixel 6a emulado con Android 13 (API 33). XML en [`docs/evidencias/sprint3/`](../../docs/evidencias/sprint3/).
- Lint Debug: terminó sin errores y con 26 avisos, principalmente versiones de dependencias y recursos no usados. Se guarda el XML para su revisión.
- El APK Release se instaló y abrió en API 33. `am start -W` informó arranque en frío de **440 ms** para `EntryActivity`; `dumpsys meminfo` informó **33.442 KB de PSS** en esa pantalla. Son mediciones puntuales de emulador, no un perfilamiento de las ocho historias ni de dispositivos físicos.
- El 21 de noviembre de 2026, la comprobación HTTPS `GET /albums` contra el servicio publicado agotó 20 s sin bytes de respuesta. El APK puede instalarse y abrir, pero la carga de datos reales por esa URL no quedó verificada en esta corrida. Repetir antes del release.

## Instalación

```bash
adb install -r releases/sprint-3/vinilos-sprint3-release.apk
```

Para el APK Debug, iniciar BackVynils local antes de abrir la app y usar un emulador Android. Para probar en un celular físico con backend local, compilar desde el código con `-PvinilosApiBaseUrl=...` y una dirección alcanzable desde el celular; `10.0.2.2` solo funciona en el emulador.
