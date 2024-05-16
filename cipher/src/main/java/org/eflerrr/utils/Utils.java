package org.eflerrr.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.security.SecureRandom;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Utils {

    public static final SecureRandom secureRandom = new SecureRandom();

    public static byte[] permutation(
            byte[] block, int[] pBlock, boolean reversedOrder, boolean isOneIndexed
    ) {
        if (block == null || pBlock == null) {
            throw new NullPointerException("Blocks cannot be null!");
        }

        byte[] res = new byte[(pBlock.length + 7) / 8];
        int currBitIndex = 0;

        for (var i : pBlock) {
            int position = i - (isOneIndexed ? 1 : 0);
            int bitOffset = reversedOrder ? position % 8 : 7 - position % 8;
            int resOffset = 7 - currBitIndex % 8;
            int blockIndex = position / 8;
            if (blockIndex >= block.length) {
                throw new IndexOutOfBoundsException(String.format("P-block index %d is out of bounds!", i));
            }
            int resIndex = currBitIndex / 8;

            boolean value = ((block[blockIndex] & 0xFF) & (1 << bitOffset)) != 0;
            res[resIndex] = (byte) (value
                    ? (res[resIndex] & 0xFF) | (1 << resOffset)
                    : res[resIndex] & ~(1 << resOffset));
            currBitIndex++;
        }

        return res;
    }

    public static byte[] permutation(byte[] block, int[] pBlock, boolean reversedOrder) {
        return permutation(block, pBlock, reversedOrder, true);
    }

    public static byte[] permutation(byte[] block, int[] pBlock) {
        return permutation(block, pBlock, false, true);
    }

    public static long cycleLeftShift(long x, int bitsNum, long k) {
        long shift = Math.abs(k % bitsNum);
        return (x << shift) | ((x & (((1L << shift) - 1) << (bitsNum - shift))) >>> (bitsNum - shift));
    }

    public static long cycleRightShift(long x, int bitsNum, long k) {
        long shift = Math.abs(k % bitsNum);
        return (x >>> shift) | ((x & ((1L << shift) - 1)) << (bitsNum - shift));
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

    public static byte[] substitution(byte[] block) {
        if (block == null) {
            throw new NullPointerException("Block cannot be null!");
        }
        if (block.length != 6) {
            throw new IllegalArgumentException(
                    String.format("Invalid block size (%d != 6)!", block.length)
            );
        }

        byte[] result = new byte[4];

        long tmpBlock = 0;
        for (var b : block) {
            tmpBlock = (b & 0xFF) | (tmpBlock << 8);
        }

        for (int i = 0; i < 8; i++) {
            int[] bitsArr = new int[6];
            int sixBits = (int) ((tmpBlock >> (6 * (8 - i - 1))) & 0xFF);
            for (int j = 0; j < 6; j++) {
                bitsArr[j] = (sixBits >> (5 - j)) & 1;
            }

            int row = (bitsArr[0] << 1) | bitsArr[5];
            int col = (bitsArr[1] << 3) | (bitsArr[2] << 2) | (bitsArr[3] << 1) | (bitsArr[4]);
//            int value = S_BLOCKS[i][row][col];  // TODO: make S_BLOCKS as an argument

//            result[i / 2] |= (byte) ((i & 1) != 0 ? value : value << 4);
        }

        return result;
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

    public static byte[] longToBytes(long number, int k) {
        byte[] result = new byte[k];
        for (int i = k - 1; i >= 0; i--) {
            result[i] = (byte) (number & ((1 << Long.BYTES) - 1));
            number >>= Byte.SIZE;
        }
        return result;
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
