package org.eflerrr.encrypt.encryptor.impl;

import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;

import static org.eflerrr.utils.Utils.*;

public class RC6Encryptor extends RC5Encryptor {

    protected double log2(double digit) {
        return Math.log(digit) / Math.log(2);
    }

    protected byte[][] splitFourParts(byte[] block) {
        int quarterBlockLengthInBytes = blockLength / 4 / Byte.SIZE;
        byte[][] result = new byte[4][quarterBlockLengthInBytes];
        for (int i = 0; i < 4; i++) {
            System.arraycopy(block, quarterBlockLengthInBytes * i,
                    result[i], 0, quarterBlockLengthInBytes);
        }
        return result;
    }

    protected byte[] clayFourParts(long a, long b, long c, long d, int sizeResult) {
        var firstPart = clayTwoParts(a, b, sizeResult / 2);
        var secondPart = clayTwoParts(c, d, sizeResult / 2);
        byte[] result = new byte[sizeResult];
        System.arraycopy(firstPart, 0, result, 0, sizeResult / 2);
        System.arraycopy(secondPart, 0, result, sizeResult / 2, sizeResult / 2);
        return result;
    }

    @Override
    protected boolean checkBlockLength(int blockLength) {
        return blockLength == 64 || blockLength == 128 || blockLength == 256;
    }


    public RC6Encryptor(int blockLength, int keyLength, int rounds) {
        super(blockLength, keyLength, rounds);
        this.roundKeysCount = 2 * (rounds + 2);
        this.constants = Map.of(
                32, Pair.of(0xB7E1L, 0x9E37L),
                64, Pair.of(0xB7E15163L, 0x9E3779B9L),
                128, Pair.of(0xB7E151628AED2A6BL, 0x9E3779B97F4A7C15L)
        );
    }

    public RC6Encryptor() {
        this(256, 16, 20);
    }

    @Override
    public byte[] encrypt(byte[] block) {
        if (roundKeys == null) {
            throw new NullPointerException("Round keys are not configured before encryption!");
        }
        int quarterBlockLength = blockLength / 4;

        var parts = splitFourParts(block);
        long a = bytesToLong(parts[0]);
        long b = additionMod(bytesToLong(parts[1]), roundKeys[0], quarterBlockLength);
        long c = bytesToLong(parts[2]);
        long d = additionMod(bytesToLong(parts[3]), roundKeys[1], quarterBlockLength);

        for (int i = 1; i <= rounds; i++) {
            long t = cycleLeftShift(
                    multiplicationMod(b,
                            additionMod(
                                    additionMod(b, b, quarterBlockLength),
                                    1L, quarterBlockLength),
                            quarterBlockLength),
                    (int) log2(quarterBlockLength), quarterBlockLength
            );
            long u = cycleLeftShift(
                    multiplicationMod(d,
                            additionMod(
                                    additionMod(d, d, quarterBlockLength),
                                    1L, quarterBlockLength),
                            quarterBlockLength),
                    (int) log2(quarterBlockLength), quarterBlockLength
            );
            a = additionMod(
                    cycleLeftShift(a ^ t, u, quarterBlockLength),
                    roundKeys[2 * i], quarterBlockLength);
            c = additionMod(
                    cycleLeftShift(c ^ u, t, quarterBlockLength),
                    roundKeys[2 * i + 1], quarterBlockLength);

            long tmp = a;
            a = b;
            b = c;
            c = d;
            d = tmp;
        }

        a = additionMod(a, roundKeys[2 * rounds + 2], quarterBlockLength);
        c = additionMod(c, roundKeys[2 * rounds + 3], quarterBlockLength);

        return clayFourParts(a, b, c, d, blockLength / Byte.SIZE);
    }

    @Override
    public byte[] decrypt(byte[] block) {
        if (roundKeys == null) {
            throw new NullPointerException("Round keys are not configured before decryption!");
        }
        int quarterBlockLength = blockLength / 4;

        var parts = splitFourParts(block);
        long a = subtractionMod(bytesToLong(parts[0]), roundKeys[2 * rounds + 2], quarterBlockLength);
        long b = bytesToLong(parts[1]);
        long c = subtractionMod(bytesToLong(parts[2]), roundKeys[2 * rounds + 3], quarterBlockLength);
        long d = bytesToLong(parts[3]);

        for (int i = rounds; i >= 1; i--) {
            long tmp = d;
            d = c;
            c = b;
            b = a;
            a = tmp;

            long u = cycleLeftShift(
                    multiplicationMod(d,
                            additionMod(
                                    additionMod(d, d, quarterBlockLength),
                                    1L, quarterBlockLength),
                            quarterBlockLength),
                    (int) log2(quarterBlockLength), quarterBlockLength
            );
            long t = cycleLeftShift(
                    multiplicationMod(b,
                            additionMod(
                                    additionMod(b, b, quarterBlockLength),
                                    1L, quarterBlockLength),
                            quarterBlockLength),
                    (int) log2(quarterBlockLength), quarterBlockLength
            );

            c = cycleRightShift(
                    subtractionMod(c, roundKeys[2 * i + 1], quarterBlockLength),
                    t, quarterBlockLength
            ) ^ u;
            a = cycleRightShift(
                    subtractionMod(a, roundKeys[2 * i], quarterBlockLength),
                    u, quarterBlockLength
            ) ^ t;
        }

        d = subtractionMod(d, roundKeys[1], quarterBlockLength);
        b = subtractionMod(b, roundKeys[0], quarterBlockLength);

        return clayFourParts(a, b, c, d, blockLength / Byte.SIZE);
    }

}
