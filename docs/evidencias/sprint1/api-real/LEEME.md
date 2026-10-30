# Consumo real de la API en Android 5.0.2 (API 21)

Evidencia de que la aplicación consume la API REST de BackVynils con **datos reales**, no solo con respuestas simuladas. Complementa las pruebas automatizadas, que usan respuestas HTTP controladas.

## Entorno

| Elemento | Valor |
|---|---|
| Dispositivo | Emulador `vinilos-ui`, Android **5.0.2 (API 21)**, arm64-v8a, 1080×1920 |
| APK | `releases/sprint-1/vinilos-sprint1.apk` (variante Debug, SHA-256 `2f279618…`) |
| URL base de la app | `http://10.0.2.2:3000/` (alias del emulador hacia el `localhost` del equipo) |
| Backend | [BackVynils](https://github.com/TheSoftwareDesignLab/BackVynils) en el commit `7178f83` |
| Despliegue del backend | `docs/backvynils-compose.local.yml` — NestJS + PostgreSQL 13 en Docker |
| Semilla | Migración `1611260351811-DataSetup`, ejecutada automáticamente al iniciar |

## Respuestas verificadas del backend

| Endpoint | Estado | Contenido |
|---|---|---|
| `GET /albums` | 200 | 4 álbumes |
| `GET /musicians` | 200 | — |
| `GET /bands` | 200 | — |
| `GET /collectors` | 200 | — |

## Recorrido capturado

| Captura | Pantalla | Qué demuestra |
|---|---|---|
| `01-entrada-rol.png` | Selección de rol | La app abre sin errores y ofrece Visitante o Coleccionista |
| `02-catalogo-4-albumes.png` | Catálogo (HU01) | «**4 álbumes en el catálogo**» y las tarjetas de *Buscando América* y *Poeta del pueblo*, con artista, género y año traídos de la API |
| `03-artistas.png` | Listado de artistas (HU03) | Chips Todos/Músicos/Bandas y las filas de **Rubén Blades** (Músico · 2 álbumes) y **Queen** (Banda · 2 álbumes) |
| `04-detalle-album.png` | Detalle de álbum (HU02) | *Buscando América*, sello Elektra, fecha 1 de agosto de 1984, descripción completa y **Tracks: 2** |

## Resultado

- **Cero excepciones fatales** durante todo el recorrido.
- La app muestra datos que solo pueden venir del backend: nombres de artistas, sello, fecha y descripción del álbum.
- El estado vacío o de error aparece cuando la API no está disponible, y la app no se cierra.

## Limitación

Esta verificación se hizo contra el **backend local**. El servicio publicado en `https://backvynils-q6yc.onrender.com/` no responde, y ese fallo está registrado en el issue [#48](https://github.com/jhonospina2711/MISW4201-202615-Vinilos-Mobile-Grupo13/issues/48). El defecto es del despliegue, no de la app: el mismo código de BackVynils, en local, responde 200 en todos los endpoints.

## Cómo reproducirlo

```bash
# 1. Backend (desde la raíz del repositorio de la app)
cd docs
docker compose -f backvynils-compose.local.yml up -d

# 2. Comprobar que responde
curl -s http://localhost:3000/albums | head -c 200

# 3. Emulador con API 21 e instalar el APK
adb install -r ../releases/sprint-1/vinilos-sprint1.apk
adb shell monkey -p com.example.vinilosmobile -c android.intent.category.LAUNCHER 1
```
