# BackVynils para desarrollo

## Estado del servicio publicado

La [guía de TSDC](https://thesoftwaredesignlab.github.io/SE4MACodelabs/jetpack-mvvm/index.html) publica `https://backvynils-q6yc.onrender.com`. El 24 de octubre de 2026, `GET /albums` seguía respondiendo HTTP `500`. El [repositorio BackVynils](https://github.com/TheSoftwareDesignLab/BackVynils) no anuncia otra URL activa en su README. La variante Release tiene esa URL como valor predeterminado, por lo que requiere un backend funcional antes de usarse para la entrega; puede sobrescribirse con `-PvinilosApiBaseUrl=https://servidor/`.

El [controlador de álbumes](https://github.com/TheSoftwareDesignLab/BackVynils/blob/master/src/album/album.controller.ts) no declara un guard ni una cabecera de autenticación para `GET /albums`; el [interceptor asociado](https://github.com/TheSoftwareDesignLab/BackVynils/blob/master/src/interceptors/interceptor.ts) solo traduce errores de negocio. Con el código publicado, no parece necesario un access token para esa consulta. El `500` indica un fallo interno del despliegue; si existe una configuración adicional en Render, no está documentada en ese repositorio.

## Copia local con Docker

El backend está clonado como carpeta hermana de este repositorio: `../BackVynils`. La configuración [backvynils-compose.local.yml](backvynils-compose.local.yml) construye su [Dockerfile](https://github.com/TheSoftwareDesignLab/BackVynils/blob/master/Dockerfile), usa PostgreSQL 13, espera a que la base esté sana y publica la API solo en `127.0.0.1:3000`. Usa un volumen Docker para la base, sin crear `pgdata/` en el checkout del backend.

Desde la raíz de Vinilos Mobile:

```bash
git clone https://github.com/TheSoftwareDesignLab/BackVynils.git ../BackVynils # solo si falta la carpeta
docker compose -f docs/backvynils-compose.local.yml up --build -d
curl http://localhost:3000/albums
```

Para consultar estado y logs, usar `docker compose -f docs/backvynils-compose.local.yml ps` y `docker compose -f docs/backvynils-compose.local.yml logs -f web`. Para detener los contenedores, `docker compose -f docs/backvynils-compose.local.yml down`; el volumen de PostgreSQL permanece para conservar los datos.

El 24 de octubre de 2026, esta copia local respondió HTTP `200` en `GET /albums` con cuatro álbumes iniciales.

La variante Debug usa `http://10.0.2.2:3000/` por defecto. `10.0.2.2` apunta al computador desde el emulador; `localhost` dentro de Android apunta al propio dispositivo y produce `ConnectException`. Para otra URL, compilar con `bash gradlew :app:assembleDebug -PvinilosApiBaseUrl=https://servidor/`. En un dispositivo físico conectado por USB, ejecutar `adb reverse tcp:3000 tcp:3000` y compilar con `-PvinilosApiBaseUrl=http://127.0.0.1:3000/`. El puerto de Docker solo escucha en el equipo local. El tráfico HTTP local está permitido solo en Debug mediante `app/src/debug/AndroidManifest.xml`.

En este equipo, el primer `docker compose` se quedó esperando al gestor de credenciales de Docker Desktop. Para descargar las imágenes públicas se usó una configuración temporal de Docker sin `credsStore`; no se modificó la configuración ni las credenciales normales del usuario.
