"""Cuts the owner's MP3s down to the launcher's two interface sounds and writes them as WAVs into res/raw.

    ..\\toolchain\\audio-venv\\Scripts\\python.exe -I work\\trim_sounds.py "<Switching Apps Sound.mp3>" "<Open Sound.mp3>" launcher\\app\\src\\main\\res\\raw

Switching Apps Sound.mp3 repeats one tick about every 150 ms: keep one, the one at 395-505 ms with silence before it.
Open Sound.mp3 has background hiss from the start up to 910 ms: keep 905-1280 ms, the opening sound itself.
Each piece gets a short fade in and out (no click) and is normalised to -1 dBFS.
"""
import struct, sys, wave, os
import miniaudio

RATE = 44100

def load(path):
    d = miniaudio.decode_file(path, output_format=miniaudio.SampleFormat.SIGNED16, nchannels=1, sample_rate=RATE)
    return [s / 32768 for s in d.samples]

def cut(samples, start_ms, end_ms, fade_in_ms, fade_out_ms):
    a, b = int(RATE * start_ms / 1000), int(RATE * end_ms / 1000)
    piece = samples[a:b]
    fi, fo = int(RATE * fade_in_ms / 1000), int(RATE * fade_out_ms / 1000)
    for i in range(min(fi, len(piece))):
        piece[i] *= i / fi
    for i in range(min(fo, len(piece))):
        piece[-1 - i] *= i / fo
    return piece

def write(path, samples):
    peak = max(abs(s) for s in samples) or 1
    gain = 0.89 / peak   # -1 dBFS
    with wave.open(path, 'wb') as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(RATE)
        w.writeframes(b''.join(struct.pack('<h', int(s * gain * 32767)) for s in samples))
    print('wrote %s (%d ms, gain x%.1f)' % (path, len(samples) * 1000 // RATE, gain))

switching, opening, out = sys.argv[1], sys.argv[2], sys.argv[3]
os.makedirs(out, exist_ok=True)
write(os.path.join(out, 'ui_move.wav'), cut(load(switching), 395, 505, 2, 25))
write(os.path.join(out, 'ui_open.wav'), cut(load(opening), 905, 1280, 4, 80))
