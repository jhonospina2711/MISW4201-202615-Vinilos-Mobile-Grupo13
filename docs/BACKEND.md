# BackVynils para desarrollo

## Estado del servicio publicado

La [guía de TSDC](https://thesoftwaredesignlab.github.io/SE4MACodelabs/jetpack-mvvm/index.html) publica `https://backvynils-q6yc.onrender.com`. El 24 de octubre de 2026, `GET /albums` respondió HTTP `500` con `{"statusCode":500,"message":"Internal server error"}`. El [repositorio BackVynils](https://github.com/TheSoftwareDesignLab/BackVynils) no anuncia otra URL activa en su README; su entorno Postman de Heroku tiene la dirección vacía. Por ahora no se usa esa URL como base predeterminada de la app.

El [controlador de álbumes](https://github.com/TheSoftwareDesignLab/BackVynils/blob/master/src/album/album.controller.ts) no declara un guard ni una cabecera de autenticación para `GET /albums`; el [interceptor asociado](https://github.com/TheSoftwareDesignLab/BackVynils/blob/master/src/interceptors/interceptor.ts) solo traduce errores de negocio. Con el código publicado, no parece necesario un access token para esa consulta. El `500` indica un fallo interno del despliegue; si existe una configuración adicional en Render, no está documentada en ese repositorio.

## Levantar una copia local

El repositorio incluye [Dockerfile](https://github.com/TheSoftwareDesignLab/BackVynils/blob/master/Dockerfile) y [docker-compose.yml](https://github.com/TheSoftwareDesignLab/BackVynils/blob/master/docker-compose.yml). Compose expone la API en el puerto `3000`, inicia PostgreSQL y configura las variables `DB_HOST`, `DB_USER`, `DB_PASS` y `DB_NAME`. La app NestJS ejecuta migraciones y carga datos iniciales al iniciar.

1. Iniciar Docker Desktop y comprobar `docker version`.
2. Clonar BackVynils fuera de este repositorio, por ejemplo como carpeta hermana: desde la carpeta de Vinilos Mobile, ejecutar `cd ..` y luego `git clone https://github.com/TheSoftwareDesignLab/BackVynils.git`.
3. En el `docker-compose.yml` de BackVynils, fijar `db.image` a `postgres:13` antes de crear la base. El archivo original usa `postgres` sin versión y el Dockerfile usa Node 12; son configuraciones antiguas que pueden requerir ajustes en equipos nuevos.
4. Desde la carpeta de BackVynils, ejecutar `docker compose up --build -d` y revisar `docker compose logs web`. Si `web` arranca antes de que PostgreSQL acepte conexiones, ejecutar `docker compose restart web` y volver a revisar el log.
5. Confirmar que `http://localhost:3000/albums` devuelve JSON antes de configurar Android. Si devuelve `500`, revisar primero los logs de `web` y `db`.

Para un emulador Android, compilar la app con `bash gradlew :app:assembleDebug -PvinilosApiBaseUrl=http://10.0.2.2:3000/`. `10.0.2.2` apunta al host desde el emulador. Para un dispositivo físico, usar la IP LAN del computador y permitir el acceso al puerto 3000. El tráfico HTTP local está permitido solo en Debug mediante `app/src/debug/AndroidManifest.xml`; Release debe usar HTTPS.

No se inició una copia local en esta revisión: el comando `docker version` no pudo conectar con el daemon de Docker del equipo.
