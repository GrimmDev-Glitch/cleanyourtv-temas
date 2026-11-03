# CleanYourTV Temas

Temas para tu Android TV / Google TV: una pantalla de inicio propia, limpia y personalizable, con efectos,
sonidos, fondo de video y widget de música. Basado en [cleanyourtv](https://github.com/Gathin23/cleanyourtv),
que además quita la publicidad y el rastreo de la TV.

> Versión **no oficial** con temas, hecha a partir de CleanYourTV. Para el proyecto original y su soporte, ve a
> [github.com/Gathin23/cleanyourtv](https://github.com/Gathin23/cleanyourtv).

El primer tema es **Edgerunners**, de estilo Cyberpunk (ver [`themes/edgerunners`](themes/edgerunners/README.md)).

> Tema hecho por fans. No está afiliado ni respaldado por CD PROJEKT RED, Studio Trigger ni Netflix. Este repo
> **no incluye** imágenes, videos ni sonidos de la serie o del juego: cada quien pone los suyos.

## Qué trae el tema Edgerunners

- Fondo de **video en loop** (el tuyo), oscurecido para que se lea todo; se libera al abrir una app.
- Todas las apps en **una fila** que se desplaza, con un **borde neón** del color de cada app.
- **Esquinas cortadas** en diagonal, marco amarillo con barra cian en la app seleccionada.
- **Glitch** al abrir una app y al cambiar de minuto; el nombre de la app se **escribe letra por letra**.
- Saludo con brillo de **neón que parpadea**, etiquetas de "sistema" y lectura de CPU / memoria / almacenamiento.
- **Widget de música** (Spotify o cualquier app): portada, canción, barra de progreso, anterior / pausa / siguiente.
- **Sonidos** al moverte y al abrir apps (los tuyos o unos sintéticos incluidos), con interruptor en el panel.
- Protector de pantalla **"Night City"**: reloj neón sobre tu video. También se abre con Atrás en el inicio.
- **Intro** al encender ("INICIANDO SISTEMA…") y menús, panel rápido y ajustes con el mismo estilo.
- Barras de **señal Wi-Fi**, tipografía Rajdhani en negrita.

## Instálalo con Claude o Gemini (recomendado)

La forma más fácil: copia el prompt de abajo y pégalo en un asistente de IA **que pueda usar la terminal de tu
computadora**, como [Claude Code](https://claude.com/claude-code), [Gemini CLI](https://github.com/google-gemini/gemini-cli)
o Codex. El asistente te pregunta qué TV tienes, revisa si es compatible y hace todo contigo paso a paso,
comprobando cada cambio con capturas de tu TV.

> Los chats normales del navegador (claude.ai, gemini.google.com) **no sirven**: no pueden conectarse a tu TV.
> Necesitas la versión de terminal, en una computadora conectada a la misma red Wi-Fi que la TV.

```text
Quiero instalar el tema "Edgerunners" de CleanYourTV Temas en mi televisor o TV box con Android. Ayúdame paso a
paso, trabajando en mi TV real por ADB, y comprueba cada cambio con capturas de pantalla sacadas de la TV.
Háblame en español y en palabras sencillas: no soy experto. Pregúntame antes de cualquier cosa que dependa de mis
gustos, antes de descargar algo y antes de aceptar licencias.

El proyecto está en https://github.com/GrimmDev-Glitch/cleanyourtv-temas. Clónalo (o descarga el ZIP si no hay
git) y trabaja desde esa carpeta, usando sus scripts de tools/ y launcher/ en vez de escribir los tuyos. Antes de
empezar lee README.md, AGENTS.md, PROMPT.md y themes/edgerunners/README.md.

1. Lo primero
- Pregúntame qué marca y modelo de televisor o TV box tengo, y qué computadora uso (Windows, Mac o Linux).
- Dime si es compatible. Funciona en Android TV y Google TV con Android 8 o más nuevo (Xiaomi, TCL, Hisense,
  Sony, Philips, onn, Chromecast con Google TV, Nvidia Shield y cajas Android TV). No funciona en Samsung
  (Tizen), LG (webOS), Roku, Fire TV, VIDAA ni Vizio: si es una de esas, explícamelo con calma y mándame a
  https://cleanyour.tv/not-android, que quita el rastreo con el control remoto. Si no sé qué sistema tiene,
  ayúdame a averiguarlo en los menús de la TV.
- Busca en examples/ si hay una carpeta para mi modelo y léela: trae avisos propios de ese equipo.
- Explícame cómo activar las Opciones para desarrolladores y la Depuración por USB, y avísame justo antes de que
  aparezca en la TV el aviso "¿Permitir la depuración USB?".

2. Conectar y respaldar
- Conéctate como dice la sección "Connect" de PROMPT.md (puerto 5555, depuración inalámbrica, relay si hace
  falta). Confirma que la versión de Android es 8 o mayor (ro.build.version.sdk 26 o más); si no, detente y
  dímelo.
- Haz el respaldo de la sección "Back up first" de PROMPT.md antes de cambiar nada.

3. Limpieza (opcional)
- Pregúntame si además quiero quitar publicidad y rastreo. Si digo que sí, sigue PROMPT.md al pie de la letra
  (desactivar, nunca desinstalar; por tandas pequeñas; preguntarme por mis apps). Si digo que no, sáltalo.

4. Herramientas e instalación del launcher
- En Windows: tools\preparar-herramientas.ps1 y luego tools\compilar-e-instalar.ps1 -TvHost <ip>. En Mac o
  Linux: instala JDK 17, el Android SDK (plataforma 34 y build-tools 34) y tools/venv, y usa
  HOME_KEY_SERVICE=1 launcher/build-and-install.sh.
- Respeta todo lo de la sección "Launcher" de PROMPT.md: comprobar que la TV acepta APKs, la versión mínima,
  el asistente de configuración de Google TV y su red Wi-Fi, y confirmar con mFocusedApp que el launcher quedó
  como inicio.

5. Mi tema
- Copia themes/edgerunners a themes/mi-edgerunners (las carpetas mi-* son personales y no se suben a GitHub).
- Pregúntame qué apps uso y en qué orden las quiero en la fila, y ponlas en dock_apps de su config.json.
- Video de fondo: pregúntame si tengo uno y su ruta. Revisa que sea MP4 con H.264, su resolución, duración y
  peso; pásalo por tools/strip_meta.py y guárdalo como wallpaper.mp4 en mi tema. Recuérdame usar material propio
  o con permiso.
- Sonidos: pregúntame si tengo los míos. Si sí, analiza su volumen para quedarte con un solo sonido sin ruido
  al inicio (tools/trim_sounds.py, ajustando los tiempos) y guárdalos como sounds/ui_move y sounds/ui_open en mi
  tema. Si no, se usan los sintéticos que trae el launcher.
- Aplícalo con tools\theme.ps1 (Windows) o tools/theme.sh (Mac/Linux).
- Activa el widget de música (cmd notification allow_listener com.gathin.tvhome/.GuardRevival) y pregúntame si
  quiero "Night City" como protector de pantalla (screensaver_components com.gathin.tvhome/.ClockDream).
- Ofréceme el DNS que bloquea anuncios como extra opcional, tal como lo describe PROMPT.md.

6. Comprobar
- Saca capturas y revísalas después de cada cambio. En muchas TVs el video y Netflix no salen en las capturas:
  en ese caso pregúntame qué veo en la pantalla.
- Pídeme que pruebe los botones reales del control (Inicio, YouTube, engranaje, Atrás); una tecla enviada por
  ADB no basta para comprobarlos. Si alguno no funciona, revisa la sección de botones de PROMPT.md.
- Reinicia la TV una vez y confirma que todo sigue igual.

7. Cerrar
- Cierra la depuración como dice PROMPT.md: revocar autorizaciones, apagar la depuración USB e inalámbrica, y
  comprobar con tools/adb-closed.py que los puertos quedaron cerrados, también después de reiniciar.
- Al final dame un resumen de lo que hiciste y de cómo deshacerlo (tools\theme.ps1 -Reset y docs/restore.md).
```

## Qué necesitas

- Una TV o caja con **Android TV / Google TV, Android 8 o más nuevo** (Samsung, LG y Roku no sirven).
  Probado en una **onn. 4K Streaming Box (Google TV 14)**.
- Una **computadora** en la misma red Wi-Fi: con Windows los pasos de abajo son directos; con Mac o Linux, mejor
  usa el prompt de arriba.
- [Python 3](https://www.python.org/downloads/) y unos 2 GB libres para las herramientas de compilación.

Todo es reversible: no se desinstala nada ni se usa root. Aun así, lo haces bajo tu propia responsabilidad.

## Instalación manual paso a paso (Windows)

### 1. Activa la depuración en la TV

En la TV: **Configuración > Sistema > Acerca de**, pulsa OK **7 veces** sobre "Compilación". Vuelve atrás, entra a
**Opciones para desarrolladores** y activa **Depuración por USB**. Anota la IP de la TV
(**Configuración > Red**).

### 2. Descarga este repo

Botón verde **Code > Download ZIP** en GitHub y descomprímelo, por ejemplo en `D:\CleanYourTV Temas\`.
Abre PowerShell en la carpeta del repo (la que tiene este README).

### 3. Prepara las herramientas (una sola vez)

```
powershell -ExecutionPolicy Bypass -File tools\preparar-herramientas.ps1
```

Descarga Java 17 y el Android SDK en una carpeta `toolchain` junto al repo. Te pedirá aceptar las licencias del
Android SDK.

### 4. (Opcional, recomendado) Limpia la TV

Quitar telemetría, anuncios y el inicio de Google es el proyecto original. La forma más fácil es pegar
[`PROMPT.md`](PROMPT.md) en [Claude Code](https://claude.com/claude-code) (u otro asistente con terminal) y
seguirlo: hace un respaldo, te pregunta qué apps usas y desactiva solo lo que no. Detalles en
[`README.cleanyourtv.md`](README.cleanyourtv.md).

### 5. Instala el launcher

```
powershell -ExecutionPolicy Bypass -File tools\compilar-e-instalar.ps1 -TvHost <ip-de-tu-tv>
```

La primera vez la TV muestra **"¿Permitir la depuración USB?"**: marca "Permitir siempre" y acepta.
Si tu TV es Google TV, revisa antes la sección de Google TV en [`README.cleanyourtv.md`](README.cleanyourtv.md)
(el asistente de configuración puede quitarle el inicio al launcher).

### 6. Pon tu video y tus sonidos, y aplica el tema

Lee [`themes/edgerunners/README.md`](themes/edgerunners/README.md): ahí se explica dónde colocar
`wallpaper.mp4` y los sonidos. Luego:

```
tools\theme.ps1 -TvHost <ip-de-tu-tv> themes\edgerunners
```

### 7. Activa el widget de música y el protector de pantalla

```
$env:TV_HOST='<ip-de-tu-tv>'
tools\venv\Scripts\python.exe tools\tv.py 'cmd notification allow_listener com.gathin.tvhome/.GuardRevival'
tools\venv\Scripts\python.exe tools\tv.py 'settings put secure screensaver_components com.gathin.tvhome/.ClockDream'
```

El primero deja que el widget vea qué música suena (solo lee la sesión de reproducción, no tus notificaciones).
El segundo pone "Night City" como protector de pantalla.

### 8. Cierra la depuración

En **Opciones para desarrolladores**: "Revocar autorizaciones de depuración USB" y desactiva **Depuración por USB**
(y "Depuración inalámbrica" si la activaste). Deja la TV así cuando no estés haciendo cambios.

## Crear tus propios temas y cambiar entre ellos

Cada tema es una carpeta en `themes\` con su `config.json`, su `wallpaper.mp4` y su carpeta `sounds\`:

1. Copia `themes\edgerunners` a, por ejemplo, `themes\mi-tema`.
2. Cambia los colores y opciones en `config.json` (todas las claves están en [`themes/README.md`](themes/README.md)),
   pon otro video y otros sonidos.
3. Aplícalo: `tools\theme.ps1 -TvHost <ip-de-tu-tv> themes\mi-tema`. Para volver: aplica el otro tema.

Las carpetas que empiezan con **`mi-`** son personales: **nunca se suben a GitHub** (ver `.gitignore`), así que
puedes guardar ahí tus videos, sonidos y apps sin compartirlos. Para compartir un tema, ponle un nombre sin `mi-`
y deja fuera el material con derechos de autor.

Ojo: en esta versión, la tipografía Rajdhani, el estilo de los menús y los tamaños de letra son comunes a todos
los temas; los efectos neón, el widget, los sonidos y el protector se activan con `"festival": "edgerunners"`.

## Deshacer

- Quitar el tema (vuelve al aspecto normal del launcher): `tools\theme.ps1 -TvHost <ip-de-tu-tv> -Reset`
- Volver al inicio original de la TV y reactivar lo desactivado: ver [`docs/restore.md`](docs/restore.md).
- Volver al protector de Google: **Configuración > Sistema > Protector de pantalla** en la TV.

## Herramientas extra

| Script | Para qué |
|---|---|
| `tools\strip_meta.py` | Arregla un MP4 que Android no puede leer (sin volver a codificar). |
| `tools\trim_sounds.py` | Recorta tus MP3 a un solo sonido, sin ruido al inicio (necesita `pip install miniaudio`). |
| `tools\make_sounds.py` | Genera los sonidos sintéticos incluidos. |

## Créditos y licencias

- Basado en [cleanyourtv](https://github.com/Gathin23/cleanyourtv) de Gathin, licencia MIT ([`LICENSE`](LICENSE)).
  Los cambios de este repo también son MIT.
- Tipografía [Rajdhani](https://fonts.google.com/specimen/Rajdhani) de Indian Type Foundry, licencia SIL OFL 1.1
  ([`launcher/FONT-LICENSE-OFL-RAJDHANI.txt`](launcher/FONT-LICENSE-OFL-RAJDHANI.txt)).
- Cyberpunk, Cyberpunk 2077 y Cyberpunk: Edgerunners son marcas de sus dueños; este es un proyecto de fans sin fines
  de lucro y no incluye su material.
