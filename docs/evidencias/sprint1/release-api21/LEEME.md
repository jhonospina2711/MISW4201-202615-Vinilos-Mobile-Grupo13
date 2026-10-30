# Verificación del APK de Release en Android 5.0.2 (API 21)

Evidencia del cierre del issue [#47](https://github.com/jhonospina2711/MISW4201-202615-Vinilos-Mobile-Grupo13/issues/47): la variante Release se cerraba al iniciar por la configuración de R8.

## Construcción

```bash
./gradlew :app:assembleRelease
```

| Dato | Valor |
|---|---|
| Resultado | `BUILD SUCCESSFUL in 2m 18s` |
| Tarea de minificación | `:app:minifyReleaseWithR8` |
| APK generado | `app/build/outputs/apk/release/app-release.apk` |
| Tamaño de Release | **2,5 MB** (el APK de Debug es de 11 MB) |
| Firma | Clave de depuración (`signingConfig = signingConfigs.getByName("debug")`) |

## Instalación y arranque

| Paso | Resultado |
|---|---|
| Dispositivo | Emulador Android 5.0.2, API 21, arm64-v8a |
| `adb install app-release.apk` | `Success` (instalación limpia, previa desinstalación) |
| Apertura desde el launcher (`monkey -c android.intent.category.LAUNCHER`) | `EntryActivity` queda en foco; la app **no** vuelve al launcher |
| `IllegalAccessError` en Logcat | **No aparece** |
| `FATAL EXCEPTION` en Logcat | **0** |
| Navegación: tap en *Visitante* | Abre `feature.home.MainActivity` |
| Proceso tras la navegación | Vivo |

## Qué cambió respecto al defecto reportado

El issue se reprodujo con `optimization { packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**") }`. Esa restricción dejaba fuera clases del runtime de Kotlin, y R8 renombraba `kotlin.sequences.d`, lo que producía:

```
java.lang.IllegalAccessError: Class kotlin.sequences.d extended by class k30 is inaccessible
```

La configuración actual conserva `optimization.enable = true` pero **elimina** el `packageScope` personalizado, y la app abre correctamente.

## Limitación

Sin backend disponible (ver issue [#48](https://github.com/jhonospina2711/MISW4201-202615-Vinilos-Mobile-Grupo13/issues/48)), la verificación cubre instalación, arranque, navegación y renderizado de pantallas. No cubre la carga de datos reales.
