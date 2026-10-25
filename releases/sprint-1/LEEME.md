# APK del Sprint 1 — Vinilos Mobile

## Archivo

| Dato | Valor |
|---|---|
| Archivo | `vinilos-sprint1.apk` |
| Tamaño | 11 MB |
| SHA-256 | `2f279618eebbebe35840f615eb41242a55cf04302b7f5557779461e582dfe9e1` |
| Paquete | `com.example.vinilosmobile` |
| Versión | `1.0` (versionCode 1) |
| SDK mínimo | 21 (Android 5.0 Lollipop) |
| Historias incluidas | HU01, HU02, HU03 y HU04 |

## Instalación

1. Descarga `vinilos-sprint1.apk` desde este repositorio.
2. En el dispositivo, habilita **Instalar apps desconocidas** para el navegador o el explorador de archivos con el que abras el APK.
3. Abre el archivo y confirma la instalación.
4. Si instalas por `adb`:

   ```bash
   adb install releases/sprint-1/vinilos-sprint1.apk
   ```

Si ya hay una versión anterior instalada con la misma firma, se actualiza sin desinstalar. Si el dispositivo rechaza la instalación por firma, desinstala primero la versión previa.

## Cómo se generó

El APK se construyó desde la rama `sprint-1` con:

```bash
./gradlew :app:assembleDebug
```

## Advertencias conocidas

- **Firma de depuración.** El APK está firmado con la clave de *debug*, no con una clave de release. Sirve para instalar y probar en dispositivos físicos, no para distribuir.
- **Backend.** La app consume la API de BackVynils por HTTPS. El backend publicado responde HTTP 500 y ese fallo está reportado en el issue [#48](https://github.com/jhonospina2711/MISW4201-202615-Vinilos-Mobile-Grupo13/issues/48). La construcción Release se cierra al iniciar por la configuración de R8, reportado en el issue [#47](https://github.com/jhonospina2711/MISW4201-202615-Vinilos-Mobile-Grupo13/issues/47).
- **API 21.** La batería de pruebas se ejecutó en API 28 y API 31. Falta la ejecución en API 21.

## Verificar la integridad

```bash
shasum -a 256 releases/sprint-1/vinilos-sprint1.apk
```

Debe imprimir el SHA-256 de la tabla de arriba.
