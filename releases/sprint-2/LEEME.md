# APK del Sprint 2 — Vinilos Mobile

Hay dos archivos porque el backend de TSDC usa un certificado que Android 5.0 no puede validar. Cada uno sirve para un escenario distinto.

## Cuál usar

| Archivo | Tamaño | Cuándo usarlo |
|---|---:|---|
| **`vinilos-sprint2-release.apk`** | 2,7 MB | **Instalación en un celular físico (Android 7 o superior).** Apunta al backend publicado de TSDC y trae los datos reales sin configurar nada. |
| `vinilos-sprint2-debug.apk` | 12 MB | **Emulador Android 5.0 con el backend local levantado**, siguiendo el README. Es el que se usó para verificar la app en Lollipop. |

### Por qué existen los dos

El backend publicado (`https://backvynils-q6yc.onrender.com`) responde correctamente, pero su certificado lo emite **Google Trust Services (ECDSA)**, y **Android 5.0 no valida esa cadena**. En un dispositivo moderno funciona; en Lollipop, la app muestra su estado de error y permite reintentar, sin cerrarse.

Por eso:

- El APK de **Release** apunta al backend publicado y sirve desde Android 7.
- El APK de **Debug** apunta a `http://10.0.2.2:3000/` y permite verificar la app en el emulador API 21 con BackVynils local, que es el escenario documentado en el README.

## Datos de los archivos

| Dato | Release | Debug |
|---|---|---|
| Archivo | `vinilos-sprint2-release.apk` | `vinilos-sprint2-debug.apk` |
| SHA-256 | `b6e8dcb511b6dc7c…` | `7f6829a1306a0332…` |
| Paquete | `com.example.vinilosmobile` | `com.example.vinilosmobile` |
| `minSdk` | 21 (Android 5.0) | 21 (Android 5.0) |
| Firma | Clave de depuración | Clave de depuración |
| URL por defecto | `https://backvynils-q6yc.onrender.com/` | `http://10.0.2.2:3000/` |

**Ojo:** no se pueden tener instalados los dos a la vez con el mismo paquete. Desinstala uno antes de instalar el otro.

## Instalación

```bash
adb install releases/sprint-2/vinilos-sprint2-release.apk
```

O abre el archivo directamente en el dispositivo y confirma la instalación.

## Historias incluidas

HU01, HU02, HU03, HU04 y **HU07**. Son las cinco que exige la iteración 2.

## Verificación

| Entorno | Variante | Resultado |
|---|---|---|
| Emulador Android 5.0.2 (API 21) | Debug + BackVynils local | Catálogo con 4 álbumes, artistas y detalle con datos reales. Sin excepciones fatales. |
| Emulador Android 5.0.2 (API 21) | Release | Instala y abre; no alcanza el backend publicado por la cadena de certificados descrita arriba. |
| Emulador API 35 | Debug | 22 escenarios Espresso aprobados (HU07 y regresión de HU01–HU04). |

## Cómo verificar la integridad

```bash
shasum -a 256 releases/sprint-2/vinilos-sprint2-release.apk
```
