# Vinilos Mobile — Grupo 13

Aplicación móvil Android de **Vinilos**, desarrollada en Kotlin para el proyecto transversal del curso Ingeniería de Software para Aplicaciones Móviles (MISO). Consume la API REST de [BackVynils](https://github.com/TheSoftwareDesignLab/BackVynils).

La aplicación permite consultar el catálogo de álbumes, el listado de artistas y el listado de coleccionistas, con el detalle de cada uno. En el rol de coleccionista permite además crear álbumes y asociarles tracks.

> **Estado de `sprint-1`:** un módulo `:app`, navegación inferior con tres Fragments y gesto horizontal, catálogo HU01 con Hilt, Repository y caché Room, y un detalle inicial de álbum. Artistas y Coleccionistas tienen pantallas iniciales. El avance y la documentación del producto están en la [wiki del proyecto](https://github.com/jhonospina2711/MISW4201-202615-Vinilos-Mobile-Grupo13/wiki).

Al abrir por primera vez, la app permite elegir Visitante o Coleccionista y guarda la selección con DataStore. Las siguientes aperturas entran directamente a Inicio. La arquitectura de ese flujo está descrita en [docs/ACCESO.md](docs/ACCESO.md).

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
| Versión mínima del dispositivo | Android 5.0 (API 21) |

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
│       │   │   ├── coreui/       Componentes Android Views reutilizables
│       │   │   │   ├── atoms/    Vistas elementales
│       │   │   │   ├── molecules/ Tarjetas y adapters reutilizables
│       │   │   │   ├── organisms/ Estados de contenido reutilizables
│       │   │   │   └── models/   Modelos de presentación compartidos
│       │   │   ├── domain/       Modelos, contratos y casos de uso
│       │   │   ├── data/         Network/Retrofit, Room, mappers y repositorios
│       │   │   ├── di/           Módulos Hilt de red, base de datos y repositorios
│       │   │   └── feature/      Home, detalles y ViewModels por pantalla
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
| `com.google.android.material:material` | 1.13.0 | Componentes Material 3 para Android Views |
| `androidx.activity:activity-ktx` | 1.8.0 | APIs de `Activity` para Kotlin |
| `androidx.constraintlayout:constraintlayout` | 2.1.4 | Layouts con `ConstraintLayout` |
| `androidx.viewpager2:viewpager2` | 1.1.0 | Transición animada y gesto horizontal entre secciones |
| `androidx.recyclerview:recyclerview` | 1.4.0 | Grilla de álbumes |
| `androidx.fragment:fragment-ktx` | 1.8.9 | Fragments y delegados de ViewModel |
| `androidx.lifecycle:*` | 2.9.4 | ViewModel, coroutines y estados observables |
| `com.squareup.retrofit2:*` | 3.0.0 | API REST y conversión JSON |
| `com.github.bumptech.glide:glide` | 4.16.0 | Carga de portadas |
| `com.squareup.okhttp3:okhttp` y `logging-interceptor` | 4.12.0 | Cliente compartido, interceptores y registro básico en Debug |
| `com.google.dagger:hilt-android` | 2.59.2 | Inyección de dependencias |
| `com.google.devtools.ksp` | 2.3.6 | Procesamiento de Hilt y Room |
| `androidx.room:*` | 2.7.2 | Caché local de álbumes |
| `junit:junit` | 4.13.2 | Pruebas unitarias |
| `androidx.test.ext:junit` | 1.1.5 | Pruebas instrumentadas |
| `androidx.test.espresso:espresso-core` | 3.5.1 | Pruebas de interfaz |

## 6. Conexión con el backend

La aplicación consume la API REST de **BackVynils**:

- Repositorio del backend: <https://github.com/TheSoftwareDesignLab/BackVynils>

El cliente Retrofit y el permiso `android.permission.INTERNET` ya están configurados. Debug apunta por defecto al backend Docker del emulador (`http://10.0.2.2:3000/`); para otra URL, pasar `-PvinilosApiBaseUrl=https://servidor/` a Gradle. El Repository intenta la red primero y usa la copia de Room si falla. Sin datos guardados muestra un error general traducido al español. Room usa esquema versión 1 y se conserva el mínimo de Android 5.0 (API 21). El estado del despliegue de TSDC y los pasos para Docker local están en [Backend para desarrollo](docs/BACKEND.md).

## 7. Flujo de trabajo en Git

El repositorio sigue GitFlow:

| Rama | Propósito |
|---|---|
| `main` | Versiones liberadas |
| `sprint-1` | Rama de trabajo solicitada para la base y HU01 |
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
