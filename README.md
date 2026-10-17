# Vinilos Mobile — Grupo 13

Aplicación móvil Android de **Vinilos**, desarrollada en Kotlin para el proyecto transversal del curso Ingeniería de Software para Aplicaciones Móviles (MISO). Consume la API REST de [BackVynils](https://github.com/TheSoftwareDesignLab/BackVynils).

La aplicación permite consultar el catálogo de álbumes, el listado de artistas y el listado de coleccionistas, con el detalle de cada uno. En el rol de coleccionista permite además crear álbumes y asociarles tracks.

> **Estado del repositorio:** contiene el proyecto Android base (una actividad de ejemplo). Las ocho historias de usuario del MVP se implementan en los sprints 1 a 3. El avance y la documentación están en la [wiki del proyecto](https://github.com/jhonospina2711/MISW4201-202615-Vinilos-Mobile-Grupo13/wiki).

---

## 1. Requisitos

| Requisito | Versión |
|---|---|
| Android Studio | Versión estable reciente, con el SDK de Android instalado |
| JDK | 11 o superior |
| Android SDK Platform | 37 (`compileSdk` y `targetSdk`) |
| Android SDK Build-Tools | Los que Android Studio instala por defecto |
| Gradle | 9.6.0 — lo descarga el wrapper, no hace falta instalarlo |
| Android Gradle Plugin | 9.4.1 |
| Versión mínima del dispositivo | Android 7.0 (API 24) |

No se necesita instalar Gradle ni Kotlin aparte: el proyecto usa el *wrapper* incluido (`gradlew`) y el plugin de Android declarado en `gradle/libs.versions.toml`.

## 2. Configuración local

1. Clona el repositorio:

   ```bash
   git clone https://github.com/jhonospina2711/MISW4201-202615-Vinilos-Mobile-Grupo13.git
   cd MISW4201-202615-Vinilos-Mobile-Grupo13
   ```

2. Abre la carpeta raíz en Android Studio y acepta la sincronización de Gradle. La primera vez descarga Gradle 9.6.0 y todas las dependencias, así que puede tardar varios minutos.

3. Si vas a compilar desde la terminal en lugar de Android Studio, crea el archivo `local.properties` en la raíz con la ruta de tu SDK de Android:

   ```properties
   sdk.dir=/ruta/a/tu/Android/sdk
   ```

   Android Studio genera este archivo automáticamente. Está en `.gitignore`, así que **no** se sube al repositorio y cada integrante debe tener el suyo.

## 3. Compilar y ejecutar

| Acción | Comando |
|---|---|
| Compilar el APK de depuración | `./gradlew assembleDebug` |
| Compilar e instalar en un dispositivo o emulador conectado | `./gradlew installDebug` |
| Ejecutar las pruebas unitarias | `./gradlew test` |
| Ejecutar las pruebas instrumentadas (requiere dispositivo o emulador) | `./gradlew connectedAndroidTest` |
| Limpiar los artefactos de compilación | `./gradlew clean` |

En Windows usa `gradlew.bat` en lugar de `./gradlew`.

El APK de depuración queda en `app/build/outputs/apk/debug/app-debug.apk`.

## 4. Estructura del proyecto

```
.
├── app/                          Módulo de la aplicación
│   ├── build.gradle.kts          Configuración del módulo (SDK, dependencias)
│   └── src/
│       ├── main/
│       │   ├── java/…/           Código Kotlin de la aplicación
│       │   ├── res/              Recursos: layouts, temas, textos, iconos
│       │   └── AndroidManifest.xml
│       ├── test/                 Pruebas unitarias (JUnit)
│       └── androidTest/          Pruebas instrumentadas (Espresso)
├── gradle/
│   ├── libs.versions.toml        Catálogo de versiones de dependencias
│   └── wrapper/                  Wrapper de Gradle
├── build.gradle.kts              Build raíz
├── settings.gradle.kts           Módulos y repositorios
└── gradle.properties             Propiedades de Gradle
```

## 5. Dependencias

Las versiones se declaran centralizadas en `gradle/libs.versions.toml`.

| Dependencia | Versión | Uso |
|---|---|---|
| `androidx.core:core-ktx` | 1.10.1 | Extensiones de Kotlin para Android |
| `androidx.appcompat:appcompat` | 1.6.1 | Compatibilidad hacia atrás |
| `com.google.android.material:material` | 1.10.0 | Componentes de Material Design |
| `androidx.activity:activity-ktx` | 1.8.0 | APIs de `Activity` para Kotlin |
| `androidx.constraintlayout:constraintlayout` | 2.1.4 | Layouts con `ConstraintLayout` |
| `junit:junit` | 4.13.2 | Pruebas unitarias |
| `androidx.test.ext:junit` | 1.1.5 | Pruebas instrumentadas |
| `androidx.test.espresso:espresso-core` | 3.5.1 | Pruebas de interfaz |

## 6. Conexión con el backend

La aplicación consume la API REST de **BackVynils**:

- Repositorio del backend: <https://github.com/TheSoftwareDesignLab/BackVynils>

La URL base de la API y la configuración del cliente HTTP se definen durante el Sprint 1, junto con la primera historia que consume el servicio. Para que la aplicación pueda hacer peticiones de red necesita el permiso `android.permission.INTERNET` en el `AndroidManifest.xml`, que todavía no está declarado.

## 7. Flujo de trabajo en Git

El repositorio sigue GitFlow:

| Rama | Propósito |
|---|---|
| `main` | Versiones liberadas |
| `releases` | Preparación de la entrega de cada iteración |
| `develop` | Integración del trabajo del equipo |
| `feature/HUxx-descripcion` | Una funcionalidad por historia de usuario |

Cada funcionalidad entra a `develop` por *pull request* aprobado por otro integrante, con **squash and merge**. Las reglas completas están en la wiki: [Flujo de trabajo en Git](https://github.com/jhonospina2711/MISW4201-202615-Vinilos-Mobile-Grupo13/wiki/9.Flujo-de-trabajo-en-Git).

## 8. Documentación del proyecto

Toda la documentación vive en la wiki del repositorio:

| Documento | Enlace |
|---|---|
| Inicio | [Wiki](https://github.com/jhonospina2711/MISW4201-202615-Vinilos-Mobile-Grupo13/wiki) |
| Backlog del producto | [2. Backlog del producto](https://github.com/jhonospina2711/MISW4201-202615-Vinilos-Mobile-Grupo13/wiki/2.Backlog-del-producto) |
| Diseño de interfaz (UI/UX) | [3. Diseño de interfaz de usuario](https://github.com/jhonospina2711/MISW4201-202615-Vinilos-Mobile-Grupo13/wiki/3.Dise%C3%B1o-de-interfaz-de-usuario-(UI-UX)) |
| Organización y acuerdos | [4. Organización y acuerdos del equipo](https://github.com/jhonospina2711/MISW4201-202615-Vinilos-Mobile-Grupo13/wiki/4.Organizaci%C3%B3n-y-acuerdos-del-equipo) |
| Flujo de trabajo en Git | [9. Flujo de trabajo en Git](https://github.com/jhonospina2711/MISW4201-202615-Vinilos-Mobile-Grupo13/wiki/9.Flujo-de-trabajo-en-Git) |
| Iteraciones | [Sprint 1](https://github.com/jhonospina2711/MISW4201-202615-Vinilos-Mobile-Grupo13/wiki/6.Sprint-1) · [Sprint 2](https://github.com/jhonospina2711/MISW4201-202615-Vinilos-Mobile-Grupo13/wiki/7.Sprint-2) · [Sprint 3](https://github.com/jhonospina2711/MISW4201-202615-Vinilos-Mobile-Grupo13/wiki/8.Sprint-3) |
| Tablero de trabajo | [GitHub Projects](https://github.com/jhonospina2711/projects/1) |

## 9. Equipo

| Integrante | Rol |
|---|---|
| Juan Miguel López | Representante del Product Owner |
| Jhon Gabriel Ospina Orozco | Desarrollo backend; estructura de la wiki y del repositorio |
| David Santiago Combita Plazas | Desarrollo móvil |
| Nicolay Cortez | Diseño UI/UX y desarrollo de la interfaz |

Curso MISW4201 — Ingeniería de Software para Aplicaciones Móviles — Universidad de los Andes.
