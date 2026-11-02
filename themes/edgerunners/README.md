# Tema Edgerunners

Un tema estilo Cyberpunk: neón amarillo y cian, esquinas cortadas en diagonal, glitch al abrir apps y al cambiar
de minuto, widget de música, protector de pantalla "Night City" y menús con el mismo estilo.

Esta carpeta trae solo la configuración (`config.json`). **El video y los sonidos los pones tú**, porque el arte de
la serie y del juego tiene derechos de autor y no se puede compartir aquí.

## Lo que va en esta carpeta

```
themes/edgerunners/
├── config.json          ← ya viene (colores, fila de apps, efectos)
├── wallpaper.mp4        ← COLOCA AQUÍ TU VIDEO (opcional)
└── sounds/              ← COLOCA AQUÍ TUS SONIDOS (opcional)
    ├── ui_move.wav      ← suena al moverte entre apps y menús
    └── ui_open.wav      ← suena al abrir una app
```

Sin video, el fondo queda oscuro. Sin sonidos, el launcher usa sus propios "bips" sintéticos (libres).

### El video (`wallpaper.mp4`)

- Formato **MP4 con video H.264**, 720p o 1080p, sin audio (o se ignora).
- Corto: **10 a 60 segundos**, idealmente con un final que empate con el inicio para que el loop no se note.
- Que pese **menos de 20 MB**.
- Solo se reproduce mientras estás en el inicio; al abrir una app se libera.

Algunos editores guardan un bloque de datos que Android no sabe leer (el fondo se queda oscuro). Pásalo antes por
este arreglo, que no vuelve a codificar ni cambia la imagen:

```
python -I tools\strip_meta.py "C:\ruta\a\tu-video.mp4" themes\edgerunners\wallpaper.mp4
```

### Los sonidos (`sounds/`)

- Nombres exactos: `ui_move` y `ui_open`, en **.wav, .mp3 u .ogg**.
- Cortos: `ui_move` de 0,1 s aprox., `ui_open` de menos de medio segundo.
- Si tu archivo tiene silencio, ruido o el sonido repetido, `tools\trim_sounds.py` lo recorta (ver sus
  instrucciones en las primeras líneas).

Usa material propio, libre (por ejemplo con licencia CC0) o que tengas permiso de usar.

## Aplicarlo a la TV

Con la depuración activada en la TV (ver el README principal):

```
tools\theme.ps1 -TvHost <ip-de-tu-tv> themes\edgerunners
```

El script manda la configuración, el video y los sonidos, y reinicia el inicio.

## Cambiar las apps de la fila

Agrega a `config.json` la lista en el orden que quieras (las que no tengas instaladas se saltan):

```json
"dock_apps": ["com.google.android.youtube.tv", "com.netflix.ninja", "com.spotify.tv.android"]
```

También puedes reordenarlas desde la TV: mantén OK sobre una app y elige **Mover**.
