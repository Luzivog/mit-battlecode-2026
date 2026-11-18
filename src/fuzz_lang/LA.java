package fuzz_lang;

import java.util.Random;

// Arithmetic and Math. Every result is folded into the returned hash; caught exceptions call K.caught().
final class LA {
    static int h(int a, int b) {
        return Integer.rotateLeft(a, 7) * 31 + b;
    }

    static int h(int a, long b) {
        return h(a, (int) (b ^ (b >>> 32)));
    }

    static int h(int a, double d) {
        return h(a, Double.doubleToRawLongBits(d));
    }

    static int h(int a, float f) {
        return h(a, Float.floatToRawIntBits(f));
    }

    static int intArith(int s) {
        int r = 0;
        int a = Integer.MAX_VALUE - (s & 7), b = Integer.MIN_VALUE + (s & 3);
        r = h(r, a + 1);
        r = h(r, b - 1);
        r = h(r, a * 3);
        r = h(r, b * -1);
        r = h(r, Integer.MIN_VALUE / -1);
        r = h(r, Integer.MIN_VALUE % -1);
        r = h(r, -Integer.MIN_VALUE);
        r = h(r, Math.abs(Integer.MIN_VALUE));
        r = h(r, s / 7 + s % 7 + (-s) / 7 + (-s) % 7);
        r = h(r, (s % 13 - 6) / 4 + (s % 13 - 6) % 4);
        int x = s;
        x += 0x7fff_ffff;
        x *= 0x10001;
        x -= Integer.MIN_VALUE;
        x ^= x >>> 3;
        r = h(r, x);
        byte bb = (byte) s;
        bb += 300;
        short sh = (short) s;
        sh *= 1000;
        char ch = (char) s;
        ch += 70000;
        r = h(r, bb + sh + ch);
        r = h(r, ++x + x++ + --x - x--);
        return r;
    }

    static int longArith(int s) {
        int r = 1;
        long a = Long.MAX_VALUE - (s & 7), b = Long.MIN_VALUE + (s & 3);
        r = h(r, a + 1);
        r = h(r, b - 1);
        r = h(r, a * 3);
        r = h(r, Long.MIN_VALUE / -1);
        r = h(r, Long.MIN_VALUE % -1L);
        r = h(r, -Long.MIN_VALUE);
        r = h(r, Math.abs(Long.MIN_VALUE));
        long x = s * 0x9E3779B97F4A7C15L;
        r = h(r, x / 1000003 + x % 1000003);
        r = h(r, (long) s * s * s);
        r = h(r, (int) (x >> 7) + (int) x);
        r = h(r, Long.compare(x, -x) + Long.signum(x));
        return r;
    }

    static int divZero(int s) {
        int r = 2;
        int z = s % 4 == 0 ? (s & 7) + 1 : 0;
        try {
            r = h(r, 17 / z);
        } catch (ArithmeticException e) {
            K.caught();
            r = h(r, 1);
        }
        try {
            r = h(r, s % z);
        } catch (ArithmeticException e) {
            K.caught();
            r = h(r, 2);
        }
        long lz = z;
        try {
            r = h(r, 17L / lz);
        } catch (ArithmeticException e) {
            K.caught();
            r = h(r, 3);
        }
        try {
            r = h(r, (long) s % lz);
        } catch (ArithmeticException e) {
            K.caught();
            r = h(r, 4);
        }
        r = h(r, 1.0 / z);
        r = h(r, -1.0 / z);
        r = h(r, 0.0 / z);
        r = h(r, 5.0 % z);
        r = h(r, 1.0f / z);
        r = h(r, (double) s / 0.0);
        try {
            Math.floorDiv(s, z);
        } catch (ArithmeticException e) {
            K.caught();
            r = h(r, 5);
        }
        try {
            Math.floorMod((long) s, (long) z);
        } catch (ArithmeticException e) {
            K.caught();
            r = h(r, 6);
        }
        return r;
    }

    static int shifts(int s) {
        int r = 3;
        int x = s | 0x80000001;
        long y = (long) x << 20 | 0x8000000000000001L;
        for (int k = -70; k <= 70; k += 3 + (s & 3)) {
            r = h(r, x << k);
            r = h(r, x >> k);
            r = h(r, x >>> k);
            r = h(r, y << k);
            r = h(r, y >> k);
            r = h(r, y >>> k);
        }
        byte b = (byte) 0x80;
        r = h(r, b >>> 4);
        r = h(r, (b & 0xff) >>> 4);
        short sh = (short) -1;
        sh >>>= 10;
        r = h(r, sh);
        return r;
    }

    static int floatEdge(int s) {
        int r = 4;
        double nan = Double.NaN, pinf = Double.POSITIVE_INFINITY, ninf = Double.NEGATIVE_INFINITY, nz = -0.0;
        double v = (s & 1) == 0 ? nan : nz;
        r = h(r, v == v ? 1 : 0);
        r = h(r, nan != nan ? 1 : 0);
        r = h(r, 0.0 == -0.0 ? 1 : 0);
        r = h(r, Double.compare(0.0, -0.0));
        r = h(r, Double.compare(nan, pinf));
        r = h(r, Double.valueOf(0.0).equals(-0.0) ? 1 : 0);
        r = h(r, Double.valueOf(nan).equals(nan) ? 1 : 0);
        r = h(r, Math.min(-0.0, 0.0));
        r = h(r, Math.max(-0.0, 0.0));
        r = h(r, Math.min(nan, 1.0));
        r = h(r, Math.max(1.0f, Float.NaN));
        r = h(r, 1.0 / nz);
        r = h(r, pinf - pinf);
        r = h(r, ninf * 0);
        r = h(r, nan < 1 ? 1 : 0);
        r = h(r, nan > 1 ? 1 : 0);
        r = h(r, !(nan >= 1) ? 1 : 0);
        r = h(r, Math.abs(nz));
        r = h(r, -nz);
        r = h(r, nz + 0.0);
        r = h(r, Double.MIN_VALUE / 2);
        r = h(r, Double.MAX_VALUE * 2);
        r = h(r, Float.MIN_VALUE);
        r = h(r, (float) Double.MIN_VALUE);
        r = h(r, (float) 1e40);
        r = h(r, Math.sqrt(-1));
        r = h(r, Double.isNaN(v) ? 1 : 0);
        r = h(r, Double.isInfinite(pinf) ? 1 : 0);
        r = h(r, Double.hashCode(nz));
        r = h(r, Float.compare(Float.NaN, Float.NaN));
        r = h(r, Double.doubleToLongBits(nan));
        r = h(r, Float.floatToIntBits(Float.intBitsToFloat(0x7fc00001)));
        return r;
    }

    static int casts(int s) {
        int r = 5;
        double[] ds = {Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, 1e20, -1e20, 3.99, -3.99,
                2147483647.5, -2147483648.7, 9.3e18, -9.3e18, 0.5, -0.5, s * 1.37, 1e-300};
        for (double d : ds) {
            r = h(r, (int) d);
            r = h(r, (long) d);
            r = h(r, (float) d);
            float f = (float) d;
            r = h(r, (int) f);
            r = h(r, (long) f);
            r = h(r, (short) d);
            r = h(r, (byte) d);
            r = h(r, (char) d);
        }
        int[] is = {s, -s, 0x12345678, -129, 128, 65535, 65536, -1, Integer.MIN_VALUE};
        for (int i : is) {
            r = h(r, (byte) i);
            r = h(r, (char) i);
            r = h(r, (short) i);
            r = h(r, (float) i);
            r = h(r, (double) i);
            r = h(r, (long) i);
        }
        long big = 0x123456789abcdefL * (s | 1);
        r = h(r, (int) big);
        r = h(r, (float) big);
        r = h(r, (double) big);
        char c = (char) -1;
        r = h(r, c + 1);
        return r;
    }

    static int floatRound(int s) {
        int r = 6;
        r = h(r, 0.1f + 0.2f);
        r = h(r, 0.1 + 0.2);
        r = h(r, (double) 0.1f);
        r = h(r, (float) 0.1);
        r = h(r, 1e8f + 1);
        r = h(r, 16777217f);
        r = h(r, 1.0f / 3);
        r = h(r, 1.0 / 3);
        r = h(r, 0.1f * 3);
        r = h(r, 1e-45f / 2);
        double[] ds = {0.5, -0.5, 1.5, 2.5, -2.5, 0.49999999999999994, -0.49999999999999994, 1e19, -1e19,
                Double.NaN, 4503599627370497.0, s / 7.0};
        for (double d : ds) {
            r = h(r, Math.round(d));
            r = h(r, Math.round((float) d));
            r = h(r, Math.floor(d));
            r = h(r, Math.ceil(d));
            r = h(r, Math.rint(d));
            r = h(r, Math.signum(d));
        }
        r = h(r, Math.floorDiv(-7, 2) + Math.floorMod(-7, 2) + Math.floorDiv(s, -3) + Math.floorMod(s, -3));
        r = h(r, Math.ceilDiv(-7, 2) + Math.ceilDiv(s, 5));
        r = h(r, Math.copySign(3.0, -0.0));
        r = h(r, Math.ulp(1.0));
        r = h(r, Math.ulp(1e10f));
        r = h(r, Math.nextUp(1.0));
        r = h(r, Math.nextDown(1.0f));
        r = h(r, Math.nextAfter(1.0, 0));
        r = h(r, Math.getExponent(s + 0.5));
        r = h(r, Math.scalb(1.5, s % 40));
        r = h(r, Math.IEEEremainder(s, 7.5));
        r = h(r, Math.fma(1.1, 2.2, 3.3));
        float acc = 0;
        for (int i = 0; i < 20; i++) acc += 0.1f * i;
        r = h(r, acc);
        double dacc = 0;
        for (int i = 0; i < 20; i++) dacc = dacc * 1.0000001 + i / 3.0;
        r = h(r, dacc);
        return r;
    }

    static int mathFns(int s) {
        int r = 7;
        double x = (s % 1000) / 37.0;
        r = h(r, Math.sqrt(x));
        r = h(r, Math.sqrt(2.0));
        r = h(r, Math.cbrt(-x));
        r = h(r, Math.pow(x, 1.7));
        r = h(r, Math.pow(2, 0.5));
        r = h(r, Math.pow(0, 0));
        r = h(r, Math.pow(-1, Double.POSITIVE_INFINITY));
        r = h(r, Math.pow(-8, 1.0 / 3));
        r = h(r, Math.pow(1.0000001, 1e7));
        r = h(r, Math.pow(10, -s % 300));
        r = h(r, Math.pow(0.99, s % 2000));
        r = h(r, Math.exp(x));
        r = h(r, Math.log(x + 1));
        r = h(r, Math.log10(x + 1));
        r = h(r, Math.log1p(x));
        r = h(r, Math.expm1(x / 10));
        r = h(r, Math.sin(x));
        r = h(r, Math.cos(x));
        r = h(r, Math.tan(x));
        r = h(r, Math.asin(x / 30 - 0.5));
        r = h(r, Math.acos(x / 30 - 0.5));
        r = h(r, Math.atan(x));
        r = h(r, Math.atan2(x - 10, 3 - x));
        r = h(r, Math.sinh(x / 10));
        r = h(r, Math.cosh(x / 10));
        r = h(r, Math.tanh(x / 10));
        r = h(r, Math.hypot(x, 3));
        r = h(r, Math.toDegrees(x));
        r = h(r, Math.toRadians(x));
        r = h(r, Math.abs(-x) + Math.max(x, 2) + Math.min(x, 2));
        r = h(r, Math.abs((float) -x) + Math.max(1L, s) + Math.min(-3, s));
        return r;
    }

    static int strictMath(int s) {
        int r = 8;
        double x = (s % 997) / 31.0;
        r = h(r, StrictMath.sqrt(x));
        r = h(r, StrictMath.pow(x, 2.3));
        r = h(r, StrictMath.sin(x));
        r = h(r, StrictMath.cos(x));
        r = h(r, StrictMath.exp(x / 4));
        r = h(r, StrictMath.log(x + 2));
        r = h(r, StrictMath.atan2(x, 2));
        r = h(r, StrictMath.floor(-x));
        r = h(r, StrictMath.round(x * 3.5));
        r = h(r, StrictMath.abs(-s) + StrictMath.max(s, 3));
        return r;
    }

    static int exactMath(int s) {
        int r = 9;
        int[][] ops = {{Integer.MAX_VALUE, 1}, {Integer.MIN_VALUE, -1}, {s, s}, {65536, 65536}, {s, 3}};
        for (int[] o : ops) {
            try {
                r = h(r, Math.addExact(o[0], o[1]));
            } catch (ArithmeticException e) {
                K.caught();
                r = h(r, 11);
            }
            try {
                r = h(r, Math.multiplyExact(o[0], o[1]));
            } catch (ArithmeticException e) {
                K.caught();
                r = h(r, 12);
            }
            try {
                r = h(r, Math.subtractExact(o[0], o[1]));
            } catch (ArithmeticException e) {
                K.caught();
                r = h(r, 13);
            }
        }
        try {
            r = h(r, Math.toIntExact(1L << 40));
        } catch (ArithmeticException e) {
            K.caught();
            r = h(r, 14);
        }
        try {
            r = h(r, Math.negateExact(Integer.MIN_VALUE));
        } catch (ArithmeticException e) {
            K.caught();
            r = h(r, 15);
        }
        try {
            r = h(r, Math.absExact(Integer.MIN_VALUE + (s & 1)));
        } catch (ArithmeticException e) {
            K.caught();
            r = h(r, 16);
        }
        try {
            r = h(r, Math.incrementExact(Integer.MAX_VALUE - (s & 1)));
        } catch (ArithmeticException e) {
            K.caught();
            r = h(r, 17);
        }
        r = h(r, Math.clamp((long) s * 1000, -5000, 5000));
        r = h(r, Math.clamp(s / 3.0, -1.5, 2.5));
        r = h(r, Math.multiplyHigh((long) s << 33, 0x7fffffffffffL));
        return r;
    }

    static int intMethods(int s) {
        int r = 10;
        int x = s * 0x9E3779B1;
        r = h(r, Integer.bitCount(x));
        r = h(r, Integer.numberOfLeadingZeros(x));
        r = h(r, Integer.numberOfTrailingZeros(x));
        r = h(r, Integer.rotateLeft(x, s));
        r = h(r, Integer.rotateRight(x, -s));
        r = h(r, Integer.reverse(x));
        r = h(r, Integer.reverseBytes(x));
        r = h(r, Integer.highestOneBit(x));
        r = h(r, Integer.lowestOneBit(x));
        r = h(r, Integer.signum(x));
        r = h(r, Integer.toBinaryString(x).hashCode());
        r = h(r, Integer.toHexString(x).hashCode());
        r = h(r, Integer.toOctalString(x).hashCode());
        r = h(r, Integer.toString(x, 36).hashCode());
        r = h(r, Integer.toString(x, 99).hashCode());
        r = h(r, Integer.toUnsignedString(x).hashCode());
        r = h(r, Integer.compare(x, s));
        r = h(r, Integer.compareUnsigned(x, s));
        r = h(r, Integer.divideUnsigned(x, (s | 1)));
        r = h(r, Integer.remainderUnsigned(x, (s | 1)));
        r = h(r, Integer.parseInt(Integer.toString(x)));
        r = h(r, Integer.parseInt("-ff", 16));
        r = h(r, Integer.parseUnsignedInt("4294967295"));
        r = h(r, Integer.valueOf(x).hashCode());
        r = h(r, Integer.sum(x, s) + Integer.max(x, s) + Integer.min(x, s));
        r = h(r, Integer.MAX_VALUE + Integer.MIN_VALUE + Integer.SIZE + Integer.BYTES);
        r = h(r, Short.toUnsignedInt((short) x) + Byte.toUnsignedInt((byte) x));
        return r;
    }

    static int longMethods(int s) {
        int r = 11;
        long x = s * 0x9E3779B97F4A7C15L;
        r = h(r, Long.bitCount(x));
        r = h(r, Long.numberOfLeadingZeros(x));
        r = h(r, Long.numberOfTrailingZeros(x));
        r = h(r, Long.rotateLeft(x, s));
        r = h(r, Long.reverse(x));
        r = h(r, Long.reverseBytes(x));
        r = h(r, Long.highestOneBit(x));
        r = h(r, Long.toBinaryString(x).length());
        r = h(r, Long.toHexString(x).hashCode());
        r = h(r, Long.toString(x).hashCode());
        r = h(r, Long.toUnsignedString(x).hashCode());
        r = h(r, Long.parseLong("-9223372036854775808") == Long.MIN_VALUE ? 1 : 0);
        r = h(r, Long.hashCode(x));
        r = h(r, Long.divideUnsigned(x, 7) + Long.remainderUnsigned(x, 7));
        r = h(r, Long.compareUnsigned(x, -1));
        return r;
    }

    static int charOps(int s) {
        int r = 12;
        char[] cs = {'a', 'Z', '0', '9', ' ', '\n', 'é', '中', (char) (s & 0xffff), '_', '$'};
        for (char c : cs) {
            r = h(r, Character.isDigit(c) ? 1 : 0);
            r = h(r, Character.isLetter(c) ? 2 : 0);
            r = h(r, Character.isLetterOrDigit(c) ? 4 : 0);
            r = h(r, Character.isWhitespace(c) ? 8 : 0);
            r = h(r, Character.isUpperCase(c) ? 16 : 0);
            r = h(r, Character.toUpperCase(c));
            r = h(r, Character.toLowerCase(c));
            r = h(r, Character.getNumericValue(c));
            r = h(r, Character.digit(c, 16));
            r = h(r, c + 1);
            r = h(r, (char) (c + 1));
        }
        r = h(r, Character.forDigit(s & 15, 16));
        r = h(r, Character.toChars(0x1F600).length);
        r = h(r, Character.isSurrogate('\ud83d') ? 1 : 0);
        r = h(r, Character.compare('a', 'b'));
        return r;
    }

    static int random(int s) {
        int r = 13;
        Random a = new Random(s), b = new Random(s);
        r = h(r, a.nextInt() == b.nextInt() ? 1 : 0);
        r = h(r, a.nextInt(100));
        r = h(r, a.nextInt(1 << 20));
        r = h(r, a.nextInt(7, 99));
        r = h(r, a.nextLong());
        r = h(r, a.nextDouble());
        r = h(r, a.nextFloat());
        r = h(r, a.nextBoolean() ? 1 : 0);
        r = h(r, a.nextGaussian());
        a.setSeed(42);
        r = h(r, a.nextInt(1000));
        byte[] bs = new byte[7];
        a.nextBytes(bs);
        for (byte x : bs) r = h(r, x);
        try {
            a.nextInt(0);
        } catch (IllegalArgumentException e) {
            K.caught();
            r = h(r, 77);
        }
        Random c = new Random();
        r = h(r, c.nextInt(1000));
        r = h(r, Math.random());
        r = h(r, (int) (Math.random() * 100));
        return r;
    }
}
