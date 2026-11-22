# Reconocimiento y exploración sistemática — Sprint 3

## Entornos

- Samsung SM-S948B físico, Android 17/API 37, APK Debug apuntando a BackVynils local en Docker mediante `adb reverse`.
- Emulador Android 9/API 28 para regresión y eventos aleatorios.
- Fecha: 21 y 22 de noviembre de 2026, hora Colombia.

## Recorrido sistemático ejecutado

| Secuencia | Resultado observado | Evidencia |
|---|---|---|
| HU01: abrir catálogo con API local | Cinco álbumes tras crear uno de prueba; carátulas y contador visibles | [Bitácora API local](evidencias/sprint3/api-local-sm-s948b.md) |
| HU02: abrir álbum y revisar tracks/comentarios | Detalle y cuatro tracks visibles tras corregir la altura de lista | [Captura HU08 y defecto #107](evidencias/sprint3/hu08-api-local-sm-s948b.png) |
| HU03/HU04: abrir artistas y detalle de Rubén Blades | Músico, dos álbumes y premio visibles; regreso operativo | Grabación de Accessibility Scanner y recorrido en el Samsung |
| HU05/HU06: abrir Coleccionistas y Manolo Bellon | Dos coleccionistas; favorito, álbum y comentario en detalle; navegación de ida/vuelta | [Capturas HU05/HU06](evidencias/sprint3/api-local-sm-s948b.md) |
| HU07: crear álbum con datos válidos | POST real; catálogo pasó de cuatro a cinco; detalle del álbum creado | [Captura HU07](evidencias/sprint3/hu07-api-local-sm-s948b.png) |
| HU08: visitante y coleccionista | Visitante sin acción de agregar; coleccionista pudo crear track `VinilosMovilQA` (`03:24`) y verlo en la lista | [Bitácora API local](evidencias/sprint3/api-local-sm-s948b.md) |

El recorrido de Scanner añadió captura de catálogo, detalle de álbum, hoja de track, listado y detalle de coleccionistas. Las instantáneas de HU07 y el análisis de artistas se documentan en [Accesibilidad Sprint 3](ACCESIBILIDAD_SPRINT3.md). Los estados vacío, 404, error, reintento, validación y rol están automatizados con las fixtures Espresso; no se presentan aquí como exploración manual de la API local.

## Reconocimiento aleatorio ejecutado

En el emulador API 28 se ejecutó `adb shell monkey -p com.example.vinilosmobile -s 20261122 --throttle 250 -v 200`. El [registro íntegro](evidencias/sprint3/monkey-api28-seed20261122.txt) muestra **200 eventos inyectados**, sin mensaje de crash ni ANR y con cierre normal de Monkey. El [estado final](evidencias/sprint3/monkey-api28-final.png) fue el launcher Android; algunos eventos salieron de la app. Por ello, esta corrida confirma que no hubo un fallo detectado durante esos eventos, pero **no demuestra cobertura aleatoria de todas las historias**. La semilla permite repetirla.

Se hizo una segunda corrida de **300 eventos** con semilla `2026112202`, 80 % toques y 20 % gestos, sin teclas de sistema ni cambio de aplicación configurados. El [log](evidencias/sprint3/monkey-api28-seed2026112202.txt) terminó normalmente sin crash ni ANR. La [captura final](evidencias/sprint3/monkey-api28-seed2026112202-final.png) muestra Vinilos en estado de error de conexión, con la cortina de ajustes rápidos abierta: los toques llegaron al área del sistema y activaron el modo avión. Se restauró la conectividad al terminar. Esta corrida ejercitó una pérdida inesperada de red sin cierre de la app, pero tampoco permite atribuir cobertura completa de HU01–HU08.

## Tercera sesión aleatoria acotada a la app

Fecha: 22 de noviembre de 2026. Emulador Android 5.0.2 (API 21), APK de Debug contra BackVynils local por `10.0.2.2:3000`.

Comando:

```bash
adb shell monkey -p com.example.vinilosmobile --throttle 200 --pct-syskeys 0 --pct-appswitch 5 -v 600
```

| Dato | Resultado |
|---|---|
| Eventos inyectados | 600 |
| Actividades alcanzadas | `EntryActivity` (10), `MainActivity` (1), `AlbumDetailActivity` (4), `ArtistDetailActivity` (2), `CollectorDetailActivity` (1) |
| Crashes | 0 |
| ANR | 0 |

Las dos sesiones anteriores se habían salido de la app porque no restringían el paquete ni excluían las teclas de sistema. Acotar el paquete con `-p` y usar `--pct-syskeys 0` mantuvo la exploración dentro de las pantallas de Vinilos y permitió llegar a los tres detalles. La bitácora completa está en `docs/evidencias/sprint3/exploracion-aleatoria-api21-600-eventos.txt`.

## Defectos y límites

- [Issue #107](https://github.com/jhonospina2711/MISW4201-202615-Vinilos-Mobile-Grupo13/issues/107): tracks creados no visibles por altura antigua; corregido y repetido en Samsung/fixtures.
- [Issue #108](https://github.com/jhonospina2711/MISW4201-202615-Vinilos-Mobile-Grupo13/issues/108): sugerencias de Scanner; correcciones y segundas instantáneas documentadas.
- Faltan sesiones sistemáticas de red lenta/sin red, giro, fuente 200 %, pantalla pequeña y abandono/reapertura de formularios contra API real. La suite Espresso cubre parte de las variantes de datos/errores, pero no reemplaza esos recorridos físicos.
