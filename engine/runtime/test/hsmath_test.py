#!/usr/bin/env python3
"""Check engine/runtime/hsmath.c against the JVM's Math.pow/exp/log/log10/sin/cos/tan, raw bits.

Compiles the C (plain and HSMATH_CHECK builds) and a Java oracle into ~/battlecode26-data/engine/scratch/hsmath/,
generates the inputs (cached there), runs the oracle under several JIT modes (which must agree with each other),
then compares every output bit pattern. Exits 0 only if everything matches. The HSMATH_CHECK build starts each call
from garbage registers, stack and upper lanes, and aborts on a memory access outside the tables and the stack.

usage: hsmath_test.py [--n N] [--n-other N] [--jobs J] [--keep]
"""
import argparse
import hashlib
import math
import os
import random
import re
import shutil
import struct
import subprocess
import sys
import time
from array import array
from concurrent.futures import ThreadPoolExecutor
from pathlib import Path

RUNTIME = Path(__file__).resolve().parent.parent
SCRATCH = Path.home() / 'battlecode26-data/engine/scratch/hsmath'
CFLAGS = ['-O2', '-ffp-contract=off', '-fno-fast-math', '-msse4.1']
JAVA_MODES = {
    'default': [],
    'Xint': ['-Xint'],
    'Xcomp': ['-Xcomp'],
    'Xcomp-c1': ['-Xcomp', '-XX:TieredStopAtLevel=1'],
    'Xcomp-c2': ['-Xcomp', '-XX:-TieredCompilation'],
}
GEN_VERSION = 1
CHUNK = 1 << 20  # records per read

# ---------------------------------------------------------------- oracle and driver sources

JAVA = r'''
import java.io.*;
import java.nio.*;
import java.nio.channels.FileChannel;
import java.nio.file.*;

public class HsOracle {
  static final int CHUNK = %(chunk)d;
  static double d(long b) { return Double.longBitsToDouble(b); }
  static long b(double d) { return Double.doubleToRawLongBits(d); }

  static void pow(LongBuffer in, LongBuffer out, int n) {
    for (int i = 0; i < n; i++) out.put(i, b(Math.pow(d(in.get(2 * i)), d(in.get(2 * i + 1)))));
  }
  // y a compile-time constant: C2 turns pow(x, 2.0) into x * x and pow(x, 0.5) into sqrt(x) for x > 0
  static void powc(LongBuffer in, LongBuffer out, int n) {
    for (int i = 0; i < n; i++) {
      double x = d(in.get(i));
      out.put(2 * i, b(Math.pow(x, 2.0)));
      out.put(2 * i + 1, b(Math.pow(x, 0.5)));
    }
  }
  static void exp(LongBuffer in, LongBuffer out, int n) { for (int i = 0; i < n; i++) out.put(i, b(Math.exp(d(in.get(i))))); }
  static void log(LongBuffer in, LongBuffer out, int n) { for (int i = 0; i < n; i++) out.put(i, b(Math.log(d(in.get(i))))); }
  static void log10(LongBuffer in, LongBuffer out, int n) { for (int i = 0; i < n; i++) out.put(i, b(Math.log10(d(in.get(i))))); }
  static void sin(LongBuffer in, LongBuffer out, int n) { for (int i = 0; i < n; i++) out.put(i, b(Math.sin(d(in.get(i))))); }
  static void cos(LongBuffer in, LongBuffer out, int n) { for (int i = 0; i < n; i++) out.put(i, b(Math.cos(d(in.get(i))))); }
  static void tan(LongBuffer in, LongBuffer out, int n) { for (int i = 0; i < n; i++) out.put(i, b(Math.tan(d(in.get(i))))); }

  // args: (fn in out)*
  public static void main(String[] a) throws IOException {
    for (int k = 0; k < a.length; k += 3) {
      String fn = a[k];
      int nin = fn.equals("pow") ? 2 : 1, nout = fn.equals("powc") ? 2 : 1;
      try (FileChannel ic = FileChannel.open(Path.of(a[k + 1]));
           FileChannel oc = FileChannel.open(Path.of(a[k + 2]), StandardOpenOption.CREATE, StandardOpenOption.WRITE,
                                             StandardOpenOption.TRUNCATE_EXISTING)) {
        ByteBuffer ib = ByteBuffer.allocateDirect(CHUNK * 8 * nin).order(ByteOrder.LITTLE_ENDIAN);
        ByteBuffer ob = ByteBuffer.allocateDirect(CHUNK * 8 * nout).order(ByteOrder.LITTLE_ENDIAN);
        while (true) {
          ib.clear();
          while (ib.hasRemaining() && ic.read(ib) > 0) { }
          ib.flip();
          int n = ib.remaining() / (8 * nin);
          if (n == 0) break;
          LongBuffer in = ib.asLongBuffer(), out = ob.clear().asLongBuffer();
          switch (fn) {
            case "pow": pow(in, out, n); break;
            case "powc": powc(in, out, n); break;
            case "exp": exp(in, out, n); break;
            case "log": log(in, out, n); break;
            case "log10": log10(in, out, n); break;
            case "sin": sin(in, out, n); break;
            case "cos": cos(in, out, n); break;
            case "tan": tan(in, out, n); break;
            default: throw new IllegalArgumentException(fn);
          }
          ob.limit(n * 8 * nout);
          while (ob.hasRemaining()) oc.write(ob);
        }
      }
    }
  }
}
'''

DRIVER = r'''
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "hsmath.h"
#ifdef HSMATH_CHECK
void bcx_hs_check_seed(uint64_t s);
#endif
#define CHUNK %(chunk)d
static double d(uint64_t b) { double v; memcpy(&v, &b, 8); return v; }
static uint64_t b(double v) { uint64_t u; memcpy(&u, &v, 8); return u; }
static uint64_t in[2 * CHUNK], out[2 * CHUNK];

/* args: [seed] (fn in out)* */
int main(int argc, char **argv) {
  int k = 1;
#ifdef HSMATH_CHECK
  bcx_hs_check_seed(strtoull(argv[k++], 0, 0));
#endif
  for (; k + 2 < argc; k += 3) {
    const char *fn = argv[k];
    int nin = !strcmp(fn, "pow") ? 2 : 1, nout = !strcmp(fn, "powc") ? 2 : 1;
    FILE *fi = fopen(argv[k + 1], "rb"), *fo = fopen(argv[k + 2], "wb");
    if (!fi || !fo) { perror(argv[k + 1]); return 2; }
    size_t n;
    while ((n = fread(in, 8 * (size_t)nin, CHUNK, fi)) > 0) {
      if (!strcmp(fn, "pow")) for (size_t i = 0; i < n; i++) out[i] = b(bcx_hs_pow(d(in[2 * i]), d(in[2 * i + 1])));
      else if (!strcmp(fn, "powc")) for (size_t i = 0; i < n; i++) {
        out[2 * i] = b(bcx_hs_pow(d(in[i]), 2.0));
        out[2 * i + 1] = b(bcx_hs_pow(d(in[i]), 0.5));
      }
%(single)s
      else { fprintf(stderr, "unknown function %%s\n", fn); return 2; }
      fwrite(out, 8 * (size_t)nout, n, fo);
    }
    fclose(fi);
    fclose(fo);
  }
  return 0;
}
'''

# ---------------------------------------------------------------- inputs

def bits(v):
    return struct.unpack('<Q', struct.pack('<d', v))[0]


# special doubles, as bit patterns
NANS = [0x7ff8000000000000, 0xfff8000000000000, 0x7ff0000000000001, 0xfff0000000000001, 0x7ff4000000000000,
        0x7ff8000000000123, 0xfff8dead0000beef, 0x7fffffffffffffff, 0xffffffffffffffff, 0x7ff7ffffffffffff]
SPECIAL = NANS + [
    0x0000000000000000, 0x8000000000000000, 0x7ff0000000000000, 0xfff0000000000000,   # zeros, infinities
    0x0000000000000001, 0x8000000000000001, 0x000fffffffffffff, 0x800fffffffffffff,   # subnormals
    0x0008000000000000, 0x0000000000123456, 0x0010000000000000, 0x8010000000000000,   # subnormal, min normal
    0x7fefffffffffffff, 0xffefffffffffffff, 0x0010000000000001, 0x7fe0000000000000,   # max normal
]


def common_values():
    v = [1.0, -1.0, 2.0, -2.0, 0.5, -0.5, 3.0, -3.0, 10.0, -10.0, 0.25, 1.5, -1.5, 0.1, 0.99, -0.99, 0.8,
         math.e, math.pi, -math.pi, 1e300, -1e300, 1e-300, -1e-300, 1e-310, 1e-320, 1e10, 1e-10, 1e22, 1e100,
         1 + 2**-52, 1 - 2**-53, -(1 + 2**-52), -(1 - 2**-53), 1 - 1 / 32, 1 - 1 / 32 - 2**-53, 1 + 1 / 16,
         1 + 1 / 16 - 2**-52, 1 - 1 / 32 + 2**-53, 0.9999, 1.0001, 2**-1022, 2**-1074, 2**1023, 1.7976931348623157e308,
         709.782712893384, 709.79, -745.1332191019411, -745.14, -708.3964185322641, 710.0, -750.0, 0.6931471805599453]
    return [bits(x) for x in v] + SPECIAL


def pow_edges():
    xs = common_values() + [bits(x) for x in (4.0, 1e4, -8.0, 0.75, 1 / 3, 2**-1060, -2**-1060)]
    ys = SPECIAL + [bits(y) for y in (
        1.0, -1.0, 2.0, -2.0, 0.5, -0.5, 3.0, -3.0, 4.0, 15.0, 0.25, 1.5, -1.5, 2.5, -2.5, 1 / 3, 0.1, 1e-5, -1e-5,
        1e10, -1e10, 1e300, -1e300, 2.0**31, 2.0**31 + 1, 2.0**52 - 1, 2.0**52, 2.0**52 + 1, 2.0**53 - 1, 2.0**53,
        2.0**53 + 2, 2.0**63, 2.0**64, -(2.0**53 - 1), -(2.0**52 + 1), 1023.0, 1024.0, 1074.0, 1075.0, -1022.0,
        -1074.0, -1075.0, -1076.0, 1023.99999, -1074.5, 1e-300, 2**-1074, -2**-1074, 2**-1022, 5e-324 * 3,
        0.5 + 2**-53, 0.5 - 2**-54, 2 + 2**-51, 2 - 2**-52, 1 + 2**-52, 1 - 2**-53)]
    out = array('Q')
    for x in xs:
        for y in ys:
            out.append(x)
            out.append(y)
    return out


def loguni(rng, lo, hi):
    return math.exp(rng.uniform(math.log(lo), math.log(hi)))


def pow_reasonable(rng, n):
    out = array('d')
    for i in range(n):
        k = i % 7
        if k == 0:
            x, y = math.ldexp(1 + rng.random(), rng.randint(-30, 30)), rng.uniform(-40, 40)
        elif k == 1:  # |x| in [1 - 1/32, 1 + 1/16): the stub's slower, more accurate path
            x, y = 1 + rng.uniform(-1 / 32, 1 / 16), rng.uniform(-3000, 3000)
        elif k == 2:  # negative x, integer y
            x, y = -math.ldexp(1 + rng.random(), rng.randint(-20, 20)), float(rng.randint(-80, 80))
        elif k == 3:  # results near overflow, underflow and in the subnormals
            x = math.ldexp(1 + rng.random(), rng.choice((-1, 1)) * rng.randint(0, 60))
            l2 = math.log2(x)
            y = rng.uniform(-1100, 1050) / l2 if l2 != 0 else 1.0
        elif k == 4:
            x, y = rng.uniform(0, 2), rng.uniform(-100, 100)
        elif k == 5:
            x, y = rng.uniform(0, 1e4), float(rng.randint(-20, 20))
        else:
            x, y = loguni(rng, 1e-300, 1e300), rng.uniform(-1.2, 1.2)
        out.append(x)
        out.append(y)
    return out


def single_reasonable(fn, rng, n):
    out = array('d')
    for i in range(n):
        k = i % 5
        s = rng.choice((-1.0, 1.0))
        if fn == 'exp':
            x = (rng.uniform(-750, 711), rng.uniform(-1, 1), s * loguni(rng, 1e-300, 1e-3), rng.uniform(-50, 50),
                 rng.uniform(700, 711) * s)[k]
        elif fn in ('log', 'log10'):
            x = (loguni(rng, 1e-310, 1e308), 1 + rng.uniform(-0.1, 0.1), rng.uniform(0, 100),
                 1 + rng.uniform(-1e-6, 1e-6), loguni(rng, 1e-320, 1e-300))[k]
        else:  # sin, cos, tan
            if k == 0:
                x = rng.uniform(-10, 10)
            elif k == 1:
                x = s * loguni(rng, 1e-12, 1e6)
            elif k == 2:
                x = s * loguni(rng, 1e6, 1e308)
            elif k == 3:  # near multiples of pi/2
                x = rng.randint(-10**6, 10**6) * (math.pi / 2) + rng.uniform(-1e-6, 1e-6)
            else:
                x = s * rng.uniform(0, 1e5)
        out.append(x)
    return out


def single_edges(fn):
    v = common_values() + [bits(x) for x in (
        math.pi / 2, math.pi / 4, 3 * math.pi / 4, 2 * math.pi, 1e6, 2.0**30, 2.0**31, 2.0**52, 2.0**64, 2.0**1000,
        1e-8, 1e-16, 1e-200, 0.7853981633974483, 1.5707963267948966, 3.141592653589793, 52.0 * math.pi,
        355.0, 104348.0, 1.0e300 * 1.7, 0.6931471805599453, 2.302585092994046)]
    out = array('Q')
    for x in v:
        out.append(x)
        out.append(x ^ 1)          # neighbours
        out.append((x + 1) & (2**64 - 1))
    return out


def rand_bits(rng, nwords):
    a = array('Q')
    a.frombytes(rng.randbytes(8 * nwords))
    if sys.byteorder != 'little':
        a.byteswap()
    return a


def write(path, a):
    tmp = path.with_suffix('.tmp')
    with open(tmp, 'wb') as f:
        a.tofile(f)
    os.replace(tmp, path)


def make_inputs(funcs, n, n_other):
    """[(name, fn, path, count)]: the test sets, generated once and cached."""
    d = SCRATCH / 'inputs'
    d.mkdir(parents=True, exist_ok=True)
    sets = []

    def add(name, fn, gen, count):
        p = d / ('v%d_%s_%s.bin' % (GEN_VERSION, name, count))
        if not p.exists():
            t = time.time()
            write(p, gen())
            print('  generated %s (%.1fs)' % (p.name, time.time() - t), flush=True)
        sets.append((name, fn, p, count))

    if 'pow' in funcs:
        e = pow_edges()
        add('pow.edges', 'pow', lambda: e, len(e) // 2)
        add('pow.bits', 'pow', lambda: rand_bits(random.Random(1), 2 * n), n)
        add('pow.reasonable', 'pow', lambda: pow_reasonable(random.Random(2), n), n)
        add('pow.0.99^r', 'pow', lambda: array('d', [v for r in range(5001) for v in (0.99, float(r))]), 5001)
        add('pow.(1-0.01)^(double)r', 'pow', lambda: array('d', [v for r in range(5001) for v in (1 - 0.01, float(r))]), 5001)
        rx = random.Random(3)
        add('pow.x^15', 'pow', lambda: array('d', [v for _ in range(10**6) for v in (rx.uniform(0, 1e4), 15.0)]), 10**6)
        add('pow.0.8^k', 'pow', lambda: array('d', [v for k in range(-100, 101) for v in (0.8, float(k))]), 201)
        add('pow.k^2', 'pow', lambda: array('d', [v for k in range(-10**5, 10**5 + 1) for v in (float(k), 2.0)]), 2 * 10**5 + 1)
        ex = sorted(set(e[0::2]))

        nc = min(n, 2 * 10**6)

        def cx():  # x for pow(x, 2.0) and pow(x, 0.5) with a constant y
            a = array('Q', ex)
            a.extend(rand_bits(random.Random(4), nc))
            a.frombytes(pow_reasonable(random.Random(5), nc)[::2].tobytes())
            return a
        add('pow.const-y(2.0,0.5)', 'powc', cx, len(ex) + 2 * nc)
    for fn in funcs:
        if fn == 'pow':
            continue
        e = single_edges(fn)
        add('%s.edges' % fn, fn, lambda: e, len(e))
        add('%s.bits' % fn, fn, lambda: rand_bits(random.Random('bits ' + fn), n_other), n_other)
        add('%s.reasonable' % fn, fn, lambda: single_reasonable(fn, random.Random('reasonable ' + fn), n_other), n_other)
    return sets


# ---------------------------------------------------------------- build and run

def sh(cmd, **kw):
    r = subprocess.run(cmd, capture_output=True, text=True, **kw)
    if r.returncode != 0:
        sys.exit('failed: %s\n%s%s' % (' '.join(map(str, cmd)), r.stdout, r.stderr))
    return r


def build(funcs):
    b = SCRATCH / 'build'
    b.mkdir(parents=True, exist_ok=True)
    single = '\n'.join(
        '      else if (!strcmp(fn, "%s")) for (size_t i = 0; i < n; i++) out[i] = b(bcx_hs_%s(d(in[i])));' % (f, f)
        for f in funcs if f != 'pow')
    (b / 'driver.c').write_text(DRIVER % {'chunk': CHUNK, 'single': single})
    cc = shutil.which('clang') or 'cc'
    exe = {}
    for name, extra in (('plain', []), ('check', ['-DHSMATH_CHECK'])):
        out = b / ('hsmath_%s' % name)
        sh([cc] + CFLAGS + extra + ['-Wall', '-Werror', '-I', RUNTIME, '-o', out, RUNTIME / 'hsmath.c', b / 'driver.c'])
        exe[name] = out
    (b / 'HsOracle.java').write_text(JAVA % {'chunk': CHUNK})
    sh(['javac', '-d', b, b / 'HsOracle.java'])
    return exe


def sha(p):
    h = hashlib.sha256()
    with open(p, 'rb') as f:
        while True:
            c = f.read(1 << 24)
            if not c:
                return h.hexdigest()
            h.update(c)


def compare(inp, fn, a, b, limit=5):
    """(mismatching outputs, examples)"""
    nin = 2 if fn == 'pow' else 1
    nout = 2 if fn == 'powc' else 1
    bad, ex = 0, []
    with open(inp, 'rb') as fi, open(a, 'rb') as fa, open(b, 'rb') as fb:
        while True:
            ca, cb = fa.read(8 * nout * CHUNK), fb.read(8 * nout * CHUNK)
            ci = fi.read(8 * nin * CHUNK)
            if len(ca) != len(cb):
                return -1, ['output lengths differ']
            if not ca:
                return bad, ex
            if ca != cb:
                wa, wb, wi = array('Q', ca), array('Q', cb), array('Q', ci)
                for i in range(len(wa)):
                    if wa[i] != wb[i]:
                        bad += 1
                        if len(ex) < limit:
                            r = i // nout
                            args = ', '.join('%016x' % wi[r * nin + j] for j in range(nin))
                            ex.append('%s(%s)%s: %016x vs %016x' % (fn, args, ' y=%s' % ('2.0', '0.5')[i % 2]
                                      if fn == 'powc' else '', wa[i], wb[i]))


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument('--n', type=int, default=10**7, help='random pow inputs per kind (bits, reasonable)')
    ap.add_argument('--n-other', type=int, default=10**7, help='random inputs per kind for the other functions')
    ap.add_argument('--jobs', type=int, default=2, help='processes at once (a JVM uses up to 2 cores)')
    ap.add_argument('--keep', action='store_true', help='keep the output files')
    args = ap.parse_args()

    funcs = re.findall(r'double\s+bcx_hs_(\w+)\s*\(', (RUNTIME / 'hsmath.h').read_text())
    jv = subprocess.run(['java', '-version'], capture_output=True, text=True).stderr.strip().splitlines()
    print('functions: %s' % ' '.join(funcs))
    print('java: %s' % ' | '.join(jv))
    print('cc: %s %s' % (shutil.which('clang') or 'cc', ' '.join(CFLAGS)))
    exe = build(funcs)
    sets = make_inputs(funcs, args.n, args.n_other)
    total = sum(c for _, _, _, c in sets)
    print('inputs: %d sets, %d calls per run' % (len(sets), total))

    outd = SCRATCH / 'out'
    if outd.exists():
        shutil.rmtree(outd)
    outd.mkdir(parents=True)
    runs = {}
    jvm = ['java', '-XX:+UseSerialGC', '-XX:CICompilerCount=2', '-Xmx1g', '-cp', SCRATCH / 'build', 'HsOracle']
    for m, flags in JAVA_MODES.items():
        runs['java-' + m] = jvm[:1] + flags + jvm[1:]
    runs['c-plain'] = [exe['plain']]
    runs['c-check'] = [exe['check'], '0x9e3779b97f4a7c15']

    def run(name):
        t = time.time()
        cmd = list(runs[name])
        for sname, fn, p, _ in sets:
            cmd += [fn, p, outd / ('%s.%s.out' % (name, sname))]
        sh(cmd)
        return name, time.time() - t

    with ThreadPoolExecutor(max_workers=max(1, args.jobs)) as ex:
        for name, dt in ex.map(run, runs):
            print('  ran %-14s %6.1fs' % (name, dt), flush=True)

    ok = True
    ref = 'java-default'
    print('\nJava modes against %s (the oracle must agree with itself):' % ref)
    for name in runs:
        if not name.startswith('java-') or name == ref:
            continue
        diffs = 0
        for sname, fn, p, _ in sets:
            a, b = outd / ('%s.%s.out' % (ref, sname)), outd / ('%s.%s.out' % (name, sname))
            if sha(a) != sha(b):
                bad, exs = compare(p, fn, a, b)
                diffs += bad
                print('  %s %s: %d differ, e.g. %s' % (name, sname, bad, '; '.join(exs)))
        print('  %-14s %s' % (name, 'identical' if diffs == 0 else '%d DIFFER' % diffs))
        ok &= diffs == 0

    print('\nC against %s, raw bits:' % ref)
    print('  %-30s %10s %10s %10s' % ('set', 'calls', 'c-plain', 'c-check'))
    for sname, fn, p, count in sets:
        cells = []
        for name in ('c-plain', 'c-check'):
            bad, exs = compare(p, fn, outd / ('%s.%s.out' % (ref, sname)), outd / ('%s.%s.out' % (name, sname)))
            cells.append(('ok' if bad == 0 else '%d BAD' % bad, exs))
            ok &= bad == 0
        n_out = count * (2 if fn == 'powc' else 1)
        print('  %-30s %10d %10s %10s' % (sname, n_out, cells[0][0], cells[1][0]))
        for _, exs in cells:
            for e in exs:
                print('      java vs c: ' + e)
    if not args.keep:
        shutil.rmtree(outd)
    print('\n%s' % ('ALL BITS MATCH' if ok else 'MISMATCH'))
    return 0 if ok else 1


if __name__ == '__main__':
    sys.exit(main())
