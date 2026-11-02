"""Writes the Edgerunners skin's interface sounds as 16-bit mono WAVs: synthesised, no samples.

    python -I work\\make_sounds.py launcher\\app\\src\\main\\res\\raw

A synth / glitch character instead of plain beeps: band-limited square waves, fast pitch sweeps, a bit crusher and
sample-rate reduction for the digital grit, and a noise burst for the "data access" on opening an app.
"""
import math, random, struct, sys, wave, os

RATE = 44100
random.seed(2077)   # the same files on every run

def square(phase):
    # First five odd harmonics: a square wave without the harsh aliasing of a hard one.
    return sum(math.sin(phase * k) / k for k in (1, 3, 5, 7, 9)) * (4 / math.pi) * 0.8

def sweep(f0, f1, ms, amp, decay, wave_fn=square):
    n = int(RATE * ms / 1000)
    out, phase = [], 0.0
    for i in range(n):
        t = i / RATE
        f = f0 * (f1 / f0) ** (i / n)        # exponential glide
        phase += 2 * math.pi * f / RATE
        env = math.exp(-decay * t) * min(1.0, i / (RATE * 0.0015))
        out.append(amp * env * wave_fn(phase))
    return out

def noise(ms, amp, decay):
    n = int(RATE * ms / 1000)
    out, last = [], 0.0
    for i in range(n):
        t = i / RATE
        last = 0.6 * last + 0.4 * random.uniform(-1, 1)   # gentle low-pass
        out.append(amp * math.exp(-decay * t) * last)
    return out

def crush(samples, bits=6, hold=3):
    """Bit depth reduction plus sample-and-hold: the 'digital' grit."""
    levels = 2 ** (bits - 1)
    out, held = [], 0.0
    for i, s in enumerate(samples):
        if i % hold == 0:
            held = round(s * levels) / levels
        out.append(held)
    return out

def mix(*tracks):
    n = max(len(t) for t in tracks)
    return [sum(t[i] for t in tracks if i < len(t)) for i in range(n)]

def pad(ms):
    return [0.0] * int(RATE * ms / 1000)

def write(path, samples):
    peak = max(abs(s) for s in samples) or 1
    gain = min(1.0, 0.9 / peak)
    with wave.open(path, 'wb') as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(RATE)
        w.writeframes(b''.join(struct.pack('<h', int(s * gain * 32767)) for s in samples))

def ring(samples, freq):
    """Ring modulation by a square wave: the metallic, robotic edge."""
    return [s * (1 if math.sin(2 * math.pi * freq * i / RATE) >= 0 else -1) for i, s in enumerate(samples)]

def stutter(samples, start_ms, slice_ms, repeats):
    """Repeats one slice a few times, fading, like a signal that skips: the glitch."""
    a = int(RATE * start_ms / 1000)
    b = a + int(RATE * slice_ms / 1000)
    piece = samples[a:b]
    out = samples[:b]
    for r in range(repeats):
        out += [s * (0.85 ** (r + 1)) for s in piece]
    return out + samples[b:]

def blips(count, ms, lo, hi, amp):
    """A burst of very short blips at random pitches: data streaming past."""
    out = []
    for _ in range(count):
        f = random.uniform(lo, hi)
        out += sweep(f, f, ms, amp, 10)
    return out

out = sys.argv[1]
os.makedirs(out, exist_ok=True)

# Moving between tiles: a low square blip gliding down, ring-modulated, stuttering, crushed.
move = sweep(700, 160, 55, 0.6, 40)
move = [0.6 * s + 0.4 * r for s, r in zip(move, ring(move, 95))]
move = crush(stutter(move, 4, 6, 2), bits=4, hold=4)
write(os.path.join(out, 'ui_move.wav'), move)

# Opening an app: a low arpeggio that stutters, a burst of data blips, then a bass drop under crushed noise.
arp = []
for f in (165, 247, 330):
    note = sweep(f, f * 1.03, 34, 0.5, 20)
    arp += stutter(note, 2, 5, 1)
arp = crush(ring(arp, 70), bits=4, hold=3)
data = crush(blips(8, 9, 300, 1100, 0.3), bits=4, hold=5)
tail = mix(crush(noise(170, 0.3, 16), bits=4, hold=5),
           sweep(90, 32, 220, 0.75, 12, wave_fn=math.sin))
write(os.path.join(out, 'ui_open.wav'), arp + data + tail)
print('wrote ui_move.wav, ui_open.wav to', out)
