# API local y recorrido en teléfono físico — 21 de noviembre de 2026

## Entorno

- Dispositivo: Samsung SM-S948B, Android 17 (API 37), conectado por USB.
- Backend: [BackVynils](https://github.com/TheSoftwareDesignLab/BackVynils), commit `7178f83`, NestJS y PostgreSQL 13.
- Inicio: `docker compose -f docs/backvynils-compose.local.yml up -d --build`; ambos servicios quedaron activos y la base sana.
- API publicada solo en `127.0.0.1:3000` del Mac. El teléfono llegó por `adb reverse tcp:3000 tcp:3000`.
- APK probado: Debug compilado con `./gradlew :app:assembleDebug -PvinilosApiBaseUrl=http://127.0.0.1:3000/ --no-configuration-cache`. El APK Release entregado usa la URL HTTPS pública y no forma parte de esta prueba local.

## Contrato de la API

`python3 scripts/smoke_sprint1_api.py` pasó HU01–HU04: cuatro álbumes iniciales, detalle con tracks, músicos, bandas y premios.

`python3 scripts/smoke_sprint3_api.py --create-track` pasó:

| Historia | Resultado |
|---|---|
| HU05 | `GET /collectors` HTTP 200, dos registros |
| HU06 | `GET /collectors/100` y `GET /collectors/100/albums` HTTP 200, un álbum relacionado |
| HU08 | `GET /albums/100/tracks` HTTP 200; `POST /albums/100/tracks` creó un track de prueba y una segunda lectura lo devolvió |

El script es de solo lectura salvo que se indique `--create-track`; esa opción escribe en la base local de Docker. Tras el recorrido de la app, `python3 scripts/smoke_sprint3_api.py` volvió a pasar y encontró cuatro tracks en el álbum 100.

## Recorrido de la app

1. El catálogo mostró cuatro álbumes, incluido «Buscando América».
2. HU05 mostró Manolo Bellon y Jaime Monsalve con sus contadores. [Captura del listado](hu05-api-local-sm-s948b.png).
3. Al seleccionar Manolo, HU06 mostró teléfono, correo, favorito, álbum y comentario. [Captura del detalle](hu06-api-local-sm-s948b.png).
4. Con rol Visitante, el álbum no mostró «Agregar track». Al elegir Coleccionista, sí lo mostró.
5. En HU08 se ingresó `VinilosMovilQA` y `03:24`; la app cerró el formulario tras guardar. La lista pasó a cuatro filas y mostró el nuevo track. [Captura de HU08](hu08-api-local-sm-s948b.png).

Durante la primera ejecución, el contador marcó cuatro, pero el detalle solo dibujó las dos filas iniciales. Se corrigió el contenedor del detalle a `NestedScrollView` y se solicitó una nueva medición de la lista tras `submitList`. La repetición en el mismo teléfono mostró las cuatro filas, incluidas las dos creadas para prueba. El problema y su corrección se documentan para la regresión del PR.

## Límites de esta evidencia

- Una prueba manual en un teléfono y el smoke test HTTP no reemplazan Espresso ni la matriz desde API 21.
- La API publicada `https://backvynils-q6yc.onrender.com/albums` agotó 12 segundos sin responder en la comprobación de este día; el APK Release no quedó validado con datos públicos.
- Los tracks de prueba permanecen en el volumen local `backvynils_vinyls_data`; no se escribieron en el servicio compartido.
