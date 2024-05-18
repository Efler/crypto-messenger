package org.eflerrr.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.security.SecureRandom;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Utils {

    public static final SecureRandom secureRandom = new SecureRandom();

    public static long cycleLeftShift(long x, long shift, long k) {
        shift = shift % k;
        long mask = (k < 64 ? (1L << k) : 0L) - 1;
        return ((x << shift) | (x >>> (k - shift))) & mask;
    }

    public static long cycleRightShift(long x, long shift, long k) {
        shift = shift % k;
        long mask = (k < 64 ? (1L << k) : 0L) - 1;
        return ((x >>> shift) | (x << (k - shift))) & mask;
    }

    public static byte[] xorBits(byte[] x, byte[] y) {
        if (x == null || y == null) {
            throw new NullPointerException("Blocks cannot be null!");
        }
        var size = Math.min(x.length, y.length);
        var res = new byte[size];
        for (int i = 0; i < size; i++) {
            res[i] = (byte) (x[i] ^ y[i]);
        }
        return res;
    }

    public static String bytesToHexString(byte[] bytes, String separator) {
        var builder = new StringBuilder();

        for (int i = 0; i < bytes.length; i++) {
            builder.append(String.format("%02X", bytes[i]));
            if (i != bytes.length - 1) {
                builder.append(separator);
            }
        }
        return builder.toString();
    }

    public static String bytesToHexString(byte[] bytes) {
        return bytesToHexString(bytes, "");
    }

    public static String bytesToBinaryString(byte[] bytes, String separator) {
        var builder = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            builder.append(
                    String.format("%8s", Integer.toBinaryString(bytes[i] & 0xFF))
                            .replace(' ', '0')
            );
            if (i != bytes.length - 1) {
                builder.append(separator);
            }
        }
        return builder.toString();
    }

    public static String bytesToBinaryString(byte[] bytes) {
        return bytesToBinaryString(bytes, "");
    }

    public static byte[] generateIV(int n) {
        byte[] res = new byte[n];
        secureRandom.nextBytes(res);
        return res;
    }

    public static long bytesToLong(byte[] bytesValue) {
        long result = 0L;
        for (byte byteValue : bytesValue) {
            int signBit = (byteValue >> (Byte.SIZE - 1)) & 1;
            long longValue = byteValue & ((1 << (Byte.SIZE - 1)) - 1);
            if (signBit == 1) {
                longValue |= 1 << (Byte.SIZE - 1);
            }
            result = (result << Byte.SIZE) | longValue;
        }
        return result;
    }

    public static long additionMod(long first, long second, int numBits) {
        long result = 0;
        long reminder = 0;
        for (int i = 0; i < numBits; i++) {
            long tempSum = ((first >> i) & 1) ^ ((second >> i) & 1) ^ reminder;
            reminder = (((first >> i) & 1) + ((second >> i) & 1) + reminder) >> 1;
            result |= tempSum << i;
        }
        return result;
    }

    public static long subtractionMod(long first, long second, int numBits) {
        return additionMod(first, ~second + 1, numBits);
    }

    public static long multiplicationMod(long first, long second, int numBits) {
        long result = 0;
        for (int i = 0; i < numBits; i++) {
            if (((first >> i) & 1) == 1) {
                result = additionMod(result, second << i, numBits);
            }
        }
        return result;
    }

    public static long getBitsFrom(byte[] bytes, int from, int countBits) {
        byte[] result = new byte[(countBits + Byte.SIZE - 1) / Byte.SIZE];
        for (int i = 0; i < countBits; i++) {
            if (from + i >= bytes.length * Byte.SIZE) {
                setOneBit(result, i / countBits, false);
            } else {
                setOneBit(result, i, getOneBit(bytes, from + i) == 1);
            }
        }
        return bytesToLong(result);
    }

    public static int getOneBit(byte[] bytes, int indexBit) {
        return (bytes[indexBit / Byte.SIZE] >> (Byte.SIZE - indexBit % Byte.SIZE - 1)) & 1;
    }

    public static void setOneBit(byte[] bytes, int indexBit, boolean valueBit) {
        if (valueBit) {
            bytes[indexBit / Byte.SIZE] |= (byte) (1 << (Byte.SIZE - indexBit % Byte.SIZE - 1));
        } else {
            bytes[indexBit / Byte.SIZE] &= (byte) ~(1 << (Byte.SIZE - indexBit % Byte.SIZE - 1));
        }
    }

}
