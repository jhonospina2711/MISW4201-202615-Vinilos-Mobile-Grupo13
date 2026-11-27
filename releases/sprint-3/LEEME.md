# APK del Sprint 3 — Vinilos Mobile

Estos APK se compilan desde `main` con las ocho historias HU01–HU08 y son los **assets del release `v3.0.0-sprint3-entrega`**. Incluyen las correcciones de la semana 8: objetivos táctiles de 48 dp, caché de formateadores y diagramas coherentes con el código.

| Archivo | Tamaño aproximado | URL de API por defecto | Uso |
|---|---:|---|---|
| `vinilos-sprint3-release.apk` | 2,9 MB | `https://backvynils-q6yc.onrender.com/` | Instalación en Android moderno cuando el servicio publicado esté disponible |
| `vinilos-sprint3-debug.apk` | 11 MB | `http://10.0.2.2:3000/` | Emulador con BackVynils local levantado según el README |

Ambos usan el paquete `com.example.vinilosmobile`, `minSdk 21` y firma de depuración académica. No pueden coexistir instalados; `adb install -r` reemplaza una variante por la otra si comparten firma.

Se entregan **dos APK** por una razón concreta: el certificado ECDSA del backend publicado no se valida en Android 5.0, así que el APK de Release —que apunta a la API pública por HTTPS— se usa en Android moderno, y el de Debug —que apunta a BackVynils local por HTTP— se usa para verificar en API 21 con el backend en Docker. La app es la misma; cambia la URL base y la firma.

## Integridad

```text
Release SHA-256  4733a73fd8cc28b57385a55f6bac9ccccb1242ea4566ab0e21d884fabecf3150
Debug SHA-256    2c5f892777a97cce100581193f98962bec7fc339213236b746922351b7df98f2
```

Comprobar con `shasum -a 256 releases/sprint-3/vinilos-sprint3-release.apk releases/sprint-3/vinilos-sprint3-debug.apk`.

## Verificación

- `assembleRelease` y `assembleDebug`: correctos, y `lintDebug` termina con **0 errores y 13 avisos** (recursos sin usar, atributos que solo aplican desde API 23, dependencias con versión más nueva y sugerencias de formato). No hay `NewApi`.
- **80 de 80** pruebas unitarias JVM aprobadas.
- **32 de 32** escenarios Espresso en **API 21, 28, 33 y 37**: 128 ejecuciones aprobadas del mismo conjunto. En Android 5.0.2 corrieron los 32 completos.
- **34 de 34** pruebas en Firebase Test Lab sobre API 28 y API 33 (32 escenarios + 2 auditorías de dispositivo); auditoría aprobada también en un Galaxy S24 físico con API 36.
- Reconocimiento aleatorio en API 21: 600 eventos acotados al paquete, cinco actividades, **0 crashes y 0 ANR**.
- Accesibilidad: Accessibility Scanner en cinco pantallas; objetivos táctiles corregidos a 48 dp y verificados con una auditoría instrumentada en seis pantallas —**0 elementos por debajo del mínimo**— en emulador API 21 y en Galaxy S24 físico.
- Perfilamiento: arranque en frío de `EntryActivity` con mediana de **214 ms** y PSS por historia en un Samsung SM-S948B físico. Faltan dos dispositivos.

Los resultados completos están en la wiki, en [Sprint 3](https://github.com/jhonospina2711/MISW4201-202615-Vinilos-Mobile-Grupo13/wiki/8.Sprint-3), y en [`docs/evidencias/sprint3/`](../../docs/evidencias/sprint3/).

## Instalación

```bash
adb install -r releases/sprint-3/vinilos-sprint3-release.apk
```

Para el APK Debug, iniciar BackVynils local antes de abrir la app y usar un emulador Android. Para probar en un celular físico con backend local, compilar desde el código con `-PvinilosApiBaseUrl=...` y una dirección alcanzable desde el celular; `10.0.2.2` solo funciona en el emulador.
