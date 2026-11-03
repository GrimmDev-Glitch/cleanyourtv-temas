<p align="center"><a href="README.md">Español</a> · <b>English</b></p>

# CleanYourTV Themes

Themes for your Android TV / Google TV: a home screen of your own, clean and customisable, with effects, sounds,
a video background and a music widget. Based on [cleanyourtv](https://github.com/Gathin23/cleanyourtv), which
also removes the ads and tracking from the TV.

> An **unofficial** themed version built from CleanYourTV. For the original project and its support, go to
> [github.com/Gathin23/cleanyourtv](https://github.com/Gathin23/cleanyourtv).

The first theme is **Edgerunners**, in a Cyberpunk style (see [`themes/edgerunners`](themes/edgerunners/README.md),
in Spanish).

> A fan-made theme. Not affiliated with or endorsed by CD PROJEKT RED, Studio Trigger or Netflix. This repo
> **does not include** any images, video or sounds from the series or the game: everyone adds their own.

## What the Edgerunners theme brings

- A **looping video background** (yours), darkened so everything stays readable; released when an app opens.
- All apps in **one sideways row**, each with a **neon edge** in the app's own colour.
- **Cut corners**, and a yellow frame with a cyan bar on the selected app.
- A **glitch** when an app opens and when the minute changes; the app's name **types itself in**.
- A **flickering neon** greeting, "system" labels and a CPU / memory / storage readout.
- A **music widget** (Spotify or any app): cover, song, progress bar, previous / pause / next.
- **Sounds** when moving and opening apps (yours, or synthetic ones included), with a switch in the panel.
- A **"Night City" screensaver**: a neon clock over your video. Back on the home screen opens it too.
- A boot **intro** ("BOOTING SYSTEM…"), and menus, quick panel and settings in the same style.
- **Wi-Fi signal bars** and the Rajdhani typeface in bold.

## Install it with Claude or Gemini (recommended)

The easiest way: copy the prompt below and paste it into an AI assistant **that can use your computer's
terminal**, such as [Claude Code](https://claude.com/claude-code), [Gemini CLI](https://github.com/google-gemini/gemini-cli)
or Codex. The assistant asks which TV you have, checks whether it is compatible and does everything with you step
by step, checking each change with screenshots from your TV.

> The ordinary browser chats (claude.ai, gemini.google.com) **will not work**: they cannot reach your TV. You need
> the terminal version, on a computer connected to the same Wi-Fi network as the TV.

```text
I want to install the "Edgerunners" theme from CleanYourTV Themes on my Android TV or TV box. Help me step by
step, working on my real TV over ADB, and check every change with screenshots taken from the TV. Talk to me in
plain English: I am not an expert. Ask me before anything that depends on my taste, before downloading anything
and before accepting any licence.

The project is at https://github.com/GrimmDev-Glitch/cleanyourtv-temas. Clone it (or download the ZIP if there is
no git) and work from that folder, using its scripts in tools/ and launcher/ rather than writing your own. Before
you start, read README.en.md, AGENTS.md, PROMPT.md and themes/edgerunners/README.md.

1. First things first
- Ask me which brand and model of TV or TV box I have, and which computer I use (Windows, Mac or Linux).
- Tell me whether it is compatible. It works on Android TV and Google TV with Android 8 or newer (Xiaomi, TCL,
  Hisense, Sony, Philips, onn, Chromecast with Google TV, Nvidia Shield and Android TV boxes). It does not work
  on Samsung (Tizen), LG (webOS), Roku, Fire TV, VIDAA or Vizio: if mine is one of those, explain it calmly and
  send me to https://cleanyour.tv/not-android, which turns off the tracking with the remote. If I do not know
  which system it runs, help me find out in the TV's menus.
- Look in examples/ for a folder for my model and read it: it has notes specific to that device.
- Explain how to turn on Developer options and USB debugging, and warn me right before the "Allow USB
  debugging?" prompt appears on the TV.

2. Connect and back up
- Connect as the "Connect" section of PROMPT.md says (port 5555, wireless debugging, the relay if needed).
  Confirm the Android version is 8 or newer (ro.build.version.sdk 26 or higher); if not, stop and tell me.
- Make the backup from the "Back up first" section of PROMPT.md before changing anything.

3. Clean-up (optional)
- Ask me whether I also want to remove ads and tracking. If I say yes, follow PROMPT.md to the letter (disable,
  never uninstall; small batches; ask me about my apps). If I say no, skip it.

4. Tools and installing the launcher
- On Windows: tools\preparar-herramientas.ps1, then tools\compilar-e-instalar.ps1 -TvHost <ip>. On Mac or
  Linux: install JDK 17, the Android SDK (platform 34 and build-tools 34) and tools/venv, and use
  HOME_KEY_SERVICE=1 launcher/build-and-install.sh.
- Follow everything in the "Launcher" section of PROMPT.md: check the TV accepts APKs, the minimum version, the
  Google TV setup wizard and its Wi-Fi network, and confirm with mFocusedApp that the launcher became Home.

5. My theme
- Copy themes/edgerunners to themes/mi-edgerunners (mi-* folders are personal and never go to GitHub).
- Ask me which apps I use and in which order I want them in the row, and put them in dock_apps in its
  config.json.
- Video background: ask me whether I have one and where it is. Check it is an MP4 with H.264, its resolution,
  length and size; run it through tools/strip_meta.py and save it as wallpaper.mp4 in my theme. Remind me to use
  my own material or material I have permission to use.
- Sounds: ask me whether I have my own. If so, analyse their volume to keep a single sound with no noise at the
  start (tools/trim_sounds.py, adjusting the times) and save them as sounds/ui_move and sounds/ui_open in my
  theme. If not, the launcher's synthetic sounds are used.
- Apply it with tools\theme.ps1 (Windows) or tools/theme.sh (Mac/Linux).
- Turn on the music widget (cmd notification allow_listener com.gathin.tvhome/.GuardRevival) and ask me whether
  I want "Night City" as the screensaver (screensaver_components com.gathin.tvhome/.ClockDream).
- Offer me the ad-blocking DNS as an optional extra, as PROMPT.md describes it.

6. Check
- Take and look at screenshots after every change. On many TVs the video and Netflix do not show in screenshots:
  then ask me what I see on screen.
- Ask me to try the real remote buttons (Home, YouTube, the gear, Back); a key sent over ADB does not prove
  them. If one does not work, see the buttons section of PROMPT.md.
- Restart the TV once and confirm everything is still the same.

7. Close up
- Close debugging as PROMPT.md says: revoke the authorisations, turn off USB and wireless debugging, and check
  with tools/adb-closed.py that the ports are closed, after the restart too.
- At the end, give me a summary of what you did and how to undo it (tools\theme.ps1 -Reset and
  docs/restore.md).
```

## What you need

- A TV or box with **Android TV / Google TV, Android 8 or newer** (Samsung, LG and Roku will not work).
  Tested on an **onn. 4K Streaming Box (Google TV 14)**.
- A **computer** on the same Wi-Fi network: on Windows the steps below are straightforward; on Mac or Linux,
  better use the prompt above.
- [Python 3](https://www.python.org/downloads/) and about 2 GB free for the build tools.

Everything is reversible: nothing is uninstalled and no root is used. Still, you do it at your own risk.

## Manual install, step by step (Windows)

### 1. Turn on debugging on the TV

On the TV: **Settings > System > About**, press OK **7 times** on "Build". Go back, open **Developer options** and
turn on **USB debugging**. Note the TV's IP address (**Settings > Network**).

### 2. Download this repo

Green **Code > Download ZIP** button on GitHub, and unzip it, for example into `D:\CleanYourTV Temas\`.
Open PowerShell in the repo folder (the one with this README).

### 3. Get the tools (once)

```
powershell -ExecutionPolicy Bypass -File tools\preparar-herramientas.ps1
```

Downloads Java 17 and the Android SDK into a `toolchain` folder next to the repo. It asks you to accept the
Android SDK licences.

### 4. (Optional, recommended) Clean up the TV

Removing telemetry, ads and Google's home screen is what the original project does. The easiest way is to paste
[`PROMPT.md`](PROMPT.md) into [Claude Code](https://claude.com/claude-code) (or another assistant with a terminal)
and follow it: it makes a backup, asks which apps you use and disables only the rest. Details in
[`README.cleanyourtv.md`](README.cleanyourtv.md).

### 5. Install the launcher

```
powershell -ExecutionPolicy Bypass -File tools\compilar-e-instalar.ps1 -TvHost <your-tv-ip>
```

The first time, the TV shows **"Allow USB debugging?"**: tick "Always allow" and accept. If your TV runs Google
TV, read the Google TV section in [`README.cleanyourtv.md`](README.cleanyourtv.md) first (the setup wizard can
take Home away from the launcher).

### 6. Add your video and sounds, and apply the theme

Read [`themes/edgerunners/README.md`](themes/edgerunners/README.md) (in Spanish): it says where `wallpaper.mp4`
and the sounds go. Then:

```
tools\theme.ps1 -TvHost <your-tv-ip> themes\edgerunners
```

### 7. Turn on the music widget and the screensaver

```
$env:TV_HOST='<your-tv-ip>'
tools\venv\Scripts\python.exe tools\tv.py 'cmd notification allow_listener com.gathin.tvhome/.GuardRevival'
tools\venv\Scripts\python.exe tools\tv.py 'settings put secure screensaver_components com.gathin.tvhome/.ClockDream'
```

The first lets the widget see what music is playing (it reads only the playback session, not your
notifications). The second makes "Night City" the screensaver.

### 8. Close debugging

In **Developer options**: "Revoke USB debugging authorisations", and turn off **USB debugging** (and "Wireless
debugging" if you turned it on). Leave the TV like that whenever you are not making changes.

## Make your own themes and switch between them

Each theme is a folder in `themes\` with its `config.json`, its `wallpaper.mp4` and its `sounds\` folder:

1. Copy `themes\edgerunners` to, say, `themes\mi-tema`.
2. Change the colours and options in `config.json` (every key is in [`themes/README.md`](themes/README.md)), and
   add another video and other sounds.
3. Apply it: `tools\theme.ps1 -TvHost <your-tv-ip> themes\mi-tema`. To go back, apply the other theme.

Folders whose name starts with **`mi-`** are personal: **they never go to GitHub** (see `.gitignore`), so you can
keep your videos, sounds and apps there without sharing them. To share a theme, give it a name without `mi-` and
leave out any copyrighted material.

Note: in this version the Rajdhani typeface, the menu style and the text sizes are shared by every theme; the neon
effects, the widget, the sounds and the screensaver come on with `"festival": "edgerunners"`.

## Undo

- Remove the theme (back to the launcher's normal look): `tools\theme.ps1 -TvHost <your-tv-ip> -Reset`
- Bring back the TV's original home screen and re-enable what was disabled: see [`docs/restore.md`](docs/restore.md).
- Back to Google's screensaver: **Settings > System > Screensaver** on the TV.

## Extra tools

| Script | What for |
|---|---|
| `tools\strip_meta.py` | Fixes an MP4 Android cannot read (without re-encoding). |
| `tools\trim_sounds.py` | Cuts your MP3s down to a single sound, with no noise at the start (needs `pip install miniaudio`). |
| `tools\make_sounds.py` | Makes the synthetic sounds included. |

## Credits and licences

- Based on [cleanyourtv](https://github.com/Gathin23/cleanyourtv) by Gathin, MIT licence ([`LICENSE`](LICENSE)).
  The changes in this repo are MIT too.
- The [Rajdhani](https://fonts.google.com/specimen/Rajdhani) typeface by Indian Type Foundry, SIL OFL 1.1
  ([`launcher/FONT-LICENSE-OFL-RAJDHANI.txt`](launcher/FONT-LICENSE-OFL-RAJDHANI.txt)).
- Cyberpunk, Cyberpunk 2077 and Cyberpunk: Edgerunners are trademarks of their owners; this is a non-profit fan
  project and includes none of their material.
