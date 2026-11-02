"""Copy an MP4 without the movie-level 'meta' box, fixing chunk offsets. moov must come before mdat."""
import struct, sys

src, dst = sys.argv[1], sys.argv[2]
d = bytearray(open(src, 'rb').read())

def boxes(start, end):
    o = start
    while o + 8 <= end:
        size, kind = struct.unpack('>I4s', d[o:o + 8])
        hdr = 8
        if size == 1:
            size, hdr = struct.unpack('>Q', d[o + 8:o + 16])[0], 16
        elif size == 0:
            size = end - o
        yield o, size, hdr, bytes(kind)
        o += size

top = list(boxes(0, len(d)))
moov = next(b for b in top if b[3] == b'moov')
mdat = next(b for b in top if b[3] == b'mdat')
assert moov[0] < mdat[0], 'moov must come before mdat'
m_off, m_size, m_hdr, _ = moov
meta = [b for b in boxes(m_off + m_hdr, m_off + m_size) if b[3] == b'meta']
if not meta:
    sys.exit('no movie-level meta box; nothing to do')
x_off, x_size = meta[0][0], meta[0][1]

def fix_offsets(start, end):
    for o, size, hdr, kind in boxes(start, end):
        if kind in (b'trak', b'mdia', b'minf', b'stbl'):
            fix_offsets(o + hdr, o + size)
        elif kind == b'stco':
            n = struct.unpack('>I', d[o + 12:o + 16])[0]
            for i in range(n):
                p = o + 16 + 4 * i
                struct.pack_into('>I', d, p, struct.unpack('>I', d[p:p + 4])[0] - x_size)
        elif kind == b'co64':
            n = struct.unpack('>I', d[o + 12:o + 16])[0]
            for i in range(n):
                p = o + 16 + 8 * i
                struct.pack_into('>Q', d, p, struct.unpack('>Q', d[p:p + 8])[0] - x_size)

fix_offsets(m_off + m_hdr, m_off + m_size)
struct.pack_into('>I', d, m_off, m_size - x_size)   # moov uses a 32-bit size here
out = d[:x_off] + d[x_off + x_size:]
open(dst, 'wb').write(out)
print('removed meta (%d bytes), wrote %s (%d bytes)' % (x_size, dst, len(out)))
