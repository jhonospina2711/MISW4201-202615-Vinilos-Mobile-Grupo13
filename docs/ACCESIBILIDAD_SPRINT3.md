# Revisión de accesibilidad — Sprint 3

## Entorno y método

- Fecha: 22 de noviembre de 2026, hora Colombia.
- Samsung SM-S948B físico, Android 17/API 37; APK Debug de `feature/sprint-3-final` conectado a BackVynils local mediante `adb reverse`.
- Accessibility Scanner de Google instalado desde Play Store y servicio activado por el dueño del teléfono.
- Se hicieron instantáneas antes y después de corregir el detalle de álbum, y una grabación de navegación. Las sugerencias son hallazgos automáticos de las pantallas capturadas: no equivalen a una auditoría completa con TalkBack ni a pruebas con usuarios.

## Hallazgos y correcciones

| Pantalla | Antes | Corrección | Resultado |
|---|---|---|---|
| Detalle de álbum HU02/HU08 | 3 sugerencias: barra de título con altura fija y texto ampliable; sello repetido en chip y campo; contadores de tracks y comentarios con descripción idéntica | Barras de detalle con `wrap_content` y altura mínima; chip decorativo excluido del foco; cada contador anuncia su sección y el título se marca como encabezado | Segunda instantánea: **sin sugerencias** |
| Listado de coleccionistas HU05 | Grabación: descripción «Coleccionistas» repetida entre el título y la navegación inferior | El título visual conserva el diseño y anuncia «Listado de coleccionistas» | Segunda instantánea: **sin sugerencias** |
| Formulario de crear álbum HU07 | 2 sugerencias: barra con altura fija y dos iconos de menú con idéntica descripción | Barra con altura flexible y descripciones «Elegir género» y «Elegir sello discográfico» | Segunda instantánea: **sin sugerencias** |
| Listado de artistas HU03 | Grabación: título «Artistas» repetido en la navegación inferior | El encabezado visual anuncia «Listado de artistas» | Segunda instantánea: **sin sugerencias** |
| Detalle de artista HU04 | Grabación: contraste del retrato de reserva 2,05:1 y rótulo «Álbumes» duplicado con la sección | Colores existentes del tema aplicados al retrato de reserva; campo renombrado «Total de álbumes» | Segunda instantánea: **sin sugerencias** |

La captura inicial del detalle está en [vista general](evidencias/sprint3/accessibility-scanner-album-detail-overview-sm-s948b.png) y [lista de sugerencias](evidencias/sprint3/accessibility-scanner-album-detail-findings-sm-s948b.png). La [segunda instantánea](evidencias/sprint3/accessibility-scanner-album-detail-after-sm-s948b.png) muestra cero sugerencias. El hallazgo y el arreglo están vinculados al [issue #108](https://github.com/jhonospina2711/MISW4201-202615-Vinilos-Mobile-Grupo13/issues/108).

La grabación produjo **7 capturas**. El [resumen](evidencias/sprint3/accessibility-scanner-seven-screens-overview-sm-s948b.png) muestra cero sugerencias en seis y una en la sexta; el [detalle](evidencias/sprint3/accessibility-scanner-seven-screens-finding-sm-s948b.png) identifica la descripción duplicada. Según las miniaturas, cubrió detalle de álbum, hoja de agregar track, catálogo de álbumes, listado y detalle de coleccionista. Aunque se navegó también a otras vistas, la grabación no conservó muestras suficientes de artistas y crear álbum para considerarlas analizadas.

La [segunda instantánea de Coleccionistas](evidencias/sprint3/accessibility-scanner-collectors-after-sm-s948b.png) también muestra cero sugerencias.

El formulario HU07 tiene [captura inicial](evidencias/sprint3/accessibility-scanner-album-form-overview-sm-s948b.png), [detalle de los dos avisos](evidencias/sprint3/accessibility-scanner-album-form-findings-sm-s948b.png) y [segunda instantánea sin sugerencias](evidencias/sprint3/accessibility-scanner-album-form-after-sm-s948b.png).

La grabación de Artistas produjo un aviso en el listado y dos en el detalle: [lista inicial](evidencias/sprint3/accessibility-scanner-artists-list-sm-s948b.png) y [detalle de los hallazgos](evidencias/sprint3/accessibility-scanner-artists-findings-sm-s948b.png). Las segundas instantáneas del [listado](evidencias/sprint3/accessibility-scanner-artists-list-after-sm-s948b.png) y [detalle](evidencias/sprint3/accessibility-scanner-artist-detail-after-sm-s948b.png) muestran cero sugerencias.

## Pendiente de revisión manual

- Ejecutar Scanner sobre estados vacío/error y letra ampliada.
- Recorrer con TalkBack el orden de foco, las etiquetas de botones, los errores de formularios, la navegación de regreso y el foco tras cerrar la hoja de track.
- Probar ampliación de fuente al 200 % y objetivos táctiles en una pantalla pequeña.
- Obtener retroalimentación de usuarios reales sobre lectura y manejo.

Estas tareas quedan separadas de la prueba automática para no presentar la ausencia de sugerencias de Scanner como garantía de accesibilidad completa.
