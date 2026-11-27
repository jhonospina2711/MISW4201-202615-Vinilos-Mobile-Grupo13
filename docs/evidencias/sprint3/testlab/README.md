# Evidencias de Firebase Test Lab — 26 de noviembre de 2026

Ejecuciones hechas con `gcloud firebase test android run` sobre el APK de Debug y el APK de instrumentación del commit entregado. Test Lab creó su propio bucket; **no se modificó ningún recurso existente del proyecto de GCP**.

| Archivo | Contenido |
|---|---|
| `api28-mediumphone-34.txt` | Suite completa en Android 9 / API 28 (`MediumPhone.arm`, virtual): 34/34 |
| `api33-mediumphone-34.txt` | Suite completa en Android 13 / API 33 (`MediumPhone.arm`, virtual): 34/34 |
| `sc-51e-audit-36.txt` | Auditoría de dispositivo en Samsung Galaxy S24 (SC-51E), físico, API 36 |

## Qué prueba cada archivo

Los dos primeros son el resultado de las **34 pruebas**: los 32 escenarios E2E de las ocho historias más las 2 pruebas de `DeviceAuditTest` (objetivos táctiles y perfil). Cero fallos y cero errores en cada dispositivo.

El tercero es el log de la auditoría en un equipo físico: recorre seis pantallas, mide el alto y ancho efectivos de cada elemento interactivo y falla si alguno queda por debajo de 48 dp. También registra el consumo de memoria del proceso (RSS y pico) y los frames dibujados por pantalla.

## Límites de estas mediciones

- `DeviceAuditTest` corre **con el arnés de pruebas cargado** (Hilt de test, fixtures y runner), así que la memoria es un **límite superior** respecto de la app sola.
- La métrica es **RSS**, no PSS: sirve para comparar dispositivos medidos con este método, no para comparar con la tabla de PSS del Samsung SM-S948B.
- Es **una muestra por pantalla**; no es una mediana de tres repeticiones.
- Los frames son acumulados de `dumpsys gfxinfo` durante la prueba, no una ventana de desplazamiento controlada.
- El **Infinix SMART 8 (API 33)** que se intentó usar primero falló por **infraestructura de Test Lab** (`Inconclusive`), no por la app; se reintentó en otro modelo.
- Esto **no sustituye** la revisión con TalkBack ni el Accessibility Scanner: mide tamaños y memoria, no orden de foco ni anuncios del lector de pantalla.

## Cómo reproducirlo

```bash
gcloud firebase test android run \
  --type instrumentation \
  --app app/build/outputs/apk/debug/app-debug.apk \
  --test app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk \
  --test-targets "class com.example.vinilosmobile.DeviceAuditTest" \
  --device model=SC-51E,version=36,locale=es_ES,orientation=portrait \
  --timeout 15m
```

Sin `--test-targets` se ejecuta la suite completa, que es como se obtuvieron los archivos de API 28 y API 33.
