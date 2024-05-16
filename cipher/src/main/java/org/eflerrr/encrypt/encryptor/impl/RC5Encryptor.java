package org.eflerrr.encrypt.encryptor.impl;

import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;
import org.eflerrr.encrypt.encryptor.IEncryptor;
import org.eflerrr.utils.Utils;

import java.util.Map;

public class RC5Encryptor implements IEncryptor {

    private static final Map<Integer, Pair<Long, Long>> RC5_CONSTANTS = Map.of(
            16, Pair.of(0xB7E1L, 0x9E37L),
            32, Pair.of(0xB7E15163L, 0x9E3779B9L),
            64, Pair.of(0xB7E151628AED2A6BL, 0x9E3779B97F4A7C15L)
    );
    @Getter
    private final int blockLength;
    private final int keyLength;
    private final int rounds;
    private long[] roundKeys;


    private long[] getWords(byte[] key) {
        int wordLength = blockLength / 2;
        int countWords = (keyLength + wordLength - 1) / wordLength;
        long[] result = new long[countWords];
        for (int i = 0; i < countWords; i++) {
            result[i] = Utils.getBitsFrom(key, i * wordLength, wordLength);
        }
        return result;
    }

    private long[] getSubKeys() {
        int halfBlockLength = blockLength / 2;
        int countWordsSArray = 2 * (rounds + 1);
        long p = RC5_CONSTANTS.get(halfBlockLength).getLeft();
        long q = RC5_CONSTANTS.get(halfBlockLength).getRight();
        long[] result = new long[countWordsSArray];
        result[0] = p;
        for (int i = 1; i < countWordsSArray; i++) {
            result[i] = Utils.additionMod(result[i - 1], q, halfBlockLength);
        }
        return result;
    }

    private byte[][] splitTwoParts(byte[] block) {
        int halfBlockLengthInBytes = blockLength / 2 / Byte.SIZE;
        byte[][] result = new byte[2][halfBlockLengthInBytes];
        System.arraycopy(block, 0, result[0], 0, halfBlockLengthInBytes);
        System.arraycopy(block, halfBlockLengthInBytes, result[1], 0, halfBlockLengthInBytes);
        return result;
    }

    private byte[] clayTwoParts(long left, long right) {
        int sizeResult = blockLength / Byte.SIZE;
        byte[] leftResult = new byte[sizeResult / 2];
        byte[] rightResult = new byte[sizeResult / 2];
        for (int i = 0; i < sizeResult / 2; i++) {
            leftResult[sizeResult / 2 - i - 1] = (byte) ((left >> (i * Byte.SIZE)) & ((1 << Byte.SIZE) - 1));
            rightResult[sizeResult / 2 - i - 1] = (byte) ((right >> (i * Byte.SIZE)) & ((1 << Byte.SIZE) - 1));
        }
        byte[] result = new byte[sizeResult];
        System.arraycopy(leftResult, 0, result, 0, sizeResult / 2);
        System.arraycopy(rightResult, 0, result, sizeResult / 2, sizeResult / 2);
        return result;
    }

    private long[] expandKey(byte[] key) {
        long[] words = getWords(key);
        long[] subKeys = getSubKeys();
        int sizeHalfBlockInBits = blockLength / 2;
        int countWordsSArray = subKeys.length;
        int countWords = words.length;
        int i = 0;
        int j = 0;
        long a = 0;
        long b = 0;
        for (int counter = 0; counter < 3 * Integer.max(countWordsSArray, countWords); counter++) {
            a = subKeys[i] = Utils.cycleLeftShift(Utils.additionMod(Utils.additionMod(subKeys[i], a, sizeHalfBlockInBits), b, sizeHalfBlockInBits), sizeHalfBlockInBits, 3);
            b = words[j] = Utils.cycleLeftShift(Utils.additionMod(Utils.additionMod(subKeys[i], a, sizeHalfBlockInBits), b, sizeHalfBlockInBits), sizeHalfBlockInBits, Utils.additionMod(a, b, sizeHalfBlockInBits));
            i = (i + 1) % countWordsSArray;
            j = (j + 1) % countWords;
        }
        return subKeys;
    }


    public RC5Encryptor(int blockLength, int keyLength, int rounds) {
        if (blockLength != 32 && blockLength != 64 && blockLength != 128) {
            throw new IllegalArgumentException("Invalid block length!");
        }
        if (keyLength <= 0 || keyLength >= 256) {
            throw new IllegalArgumentException("Invalid key length!");
        }
        if (rounds <= 0 || rounds >= 256) {
            throw new IllegalArgumentException("Illegal rounds' count!");
        }

        this.blockLength = blockLength;
        this.keyLength = keyLength;
        this.rounds = rounds;
        this.roundKeys = null;
    }

    public RC5Encryptor() {
        this(128, 128, 12);
    }

    @Override
    public byte[] encrypt(byte[] block) {
        if (roundKeys == null) {
            throw new NullPointerException("Round keys are not configured before encryption!");
        }
        int halfBlockLength = blockLength / 2;
        var halfParts = splitTwoParts(block);

        long a = Utils.additionMod(Utils.bytesToLong(halfParts[0]), roundKeys[0], halfBlockLength);
        long b = Utils.additionMod(Utils.bytesToLong(halfParts[1]), roundKeys[1], halfBlockLength);

        for (int i = 1; i < rounds; i++) {
            a = Utils.additionMod(
                    Utils.cycleLeftShift((a ^ b), halfBlockLength, b),
                    roundKeys[2 * i], halfBlockLength);
            b = Utils.additionMod(
                    Utils.cycleLeftShift((a ^ b), halfBlockLength, a),
                    roundKeys[2 * i + 1], halfBlockLength);
        }
        return clayTwoParts(a, b);
    }

    @Override
    public byte[] decrypt(byte[] block) {
        if (roundKeys == null) {
            throw new NullPointerException("Round keys are not configured before decryption!");
        }
        int halfBlockLength = blockLength / 2;
        var halfParts = splitTwoParts(block);

        long a = Utils.bytesToLong(halfParts[0]);
        long b = Utils.bytesToLong(halfParts[1]);

        for (int i = rounds - 1; i >= 1; i--) {
            b = Utils.cycleRightShift(
                    Utils.subtractionMod(b, roundKeys[2 * i + 1], halfBlockLength),
                    halfBlockLength, a) ^ a;
            a = Utils.cycleRightShift(
                    Utils.subtractionMod(a, roundKeys[2 * i], halfBlockLength),
                    halfBlockLength, b) ^ b;
        }

        b = Utils.subtractionMod(b, roundKeys[1], halfBlockLength);
        a = Utils.subtractionMod(a, roundKeys[0], halfBlockLength);

        return clayTwoParts(a, b);
    }

    @Override
    public IEncryptor setKey(byte[] key) {
        if (key == null) {
            throw new NullPointerException("Key cannot be null!");
        }
        this.roundKeys = expandKey(key);
        return this;
    }

}
