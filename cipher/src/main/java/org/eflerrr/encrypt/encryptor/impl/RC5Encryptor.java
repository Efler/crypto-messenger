package org.eflerrr.encrypt.encryptor.impl;

import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;
import org.eflerrr.encrypt.encryptor.IEncryptor;
import org.eflerrr.utils.Utils;

import java.util.Map;

public class RC5Encryptor implements IEncryptor {

    @Getter
    protected final int blockLength;
    protected final int keyLength;
    protected final int rounds;
    protected long[] roundKeys;
    protected int roundKeysCount;
    protected Map<Integer, Pair<Long, Long>> constants = Map.of(
            16, Pair.of(0xB7E1L, 0x9E37L),
            32, Pair.of(0xB7E15163L, 0x9E3779B9L),
            64, Pair.of(0xB7E151628AED2A6BL, 0x9E3779B97F4A7C15L)
    );


    protected long[] getWords(byte[] key) {
        int wordLength = blockLength / 2 / Byte.SIZE;
        int countWords = (keyLength + wordLength - 1) / wordLength;
        long[] result = new long[countWords];
        for (int i = 0; i < countWords; i++) {
            result[i] = Utils.getBitsFrom(
                    key, i * wordLength * Byte.SIZE, wordLength * Byte.SIZE
            );
        }
        return result;
    }

    protected long[] getSubKeys(int countSubKeys) {
        int halfBlockLength = blockLength / 2;
        long p = constants.get(halfBlockLength).getLeft();
        long q = constants.get(halfBlockLength).getRight();
        long[] result = new long[countSubKeys];
        result[0] = p;
        for (int i = 1; i < countSubKeys; i++) {
            result[i] = Utils.additionMod(result[i - 1], q, halfBlockLength);
        }
        return result;
    }

    protected byte[][] splitTwoParts(byte[] block) {
        int halfBlockLengthInBytes = blockLength / 2 / Byte.SIZE;
        byte[][] result = new byte[2][halfBlockLengthInBytes];
        System.arraycopy(block, 0, result[0], 0, halfBlockLengthInBytes);
        System.arraycopy(block, halfBlockLengthInBytes, result[1], 0, halfBlockLengthInBytes);
        return result;
    }

    protected byte[] clayTwoParts(long left, long right, int sizeResult) {
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

    protected long[] expandKey(byte[] key, int countSubKeys) {
        long[] words = getWords(key);
        long[] subKeys = getSubKeys(countSubKeys);
        int sizeHalfBlockInBits = blockLength / 2;
        int countWordsSArray = subKeys.length;
        int countWords = words.length;
        int i = 0;
        int j = 0;
        long a = 0;
        long b = 0;
        for (int counter = 0; counter < 3 * Integer.max(countWordsSArray, countWords); counter++) {
            a = subKeys[i] = Utils.cycleLeftShift(
                    Utils.additionMod(Utils.additionMod(
                                    subKeys[i], a, sizeHalfBlockInBits),
                            b, sizeHalfBlockInBits),
                    3, sizeHalfBlockInBits);
            b = words[j] = Utils.cycleLeftShift(
                    Utils.additionMod(Utils.additionMod(
                                    words[j], a, sizeHalfBlockInBits),
                            b, sizeHalfBlockInBits),
                    Utils.additionMod(a, b, sizeHalfBlockInBits), sizeHalfBlockInBits);
            i = (i + 1) % countWordsSArray;
            j = (j + 1) % countWords;
        }
        return subKeys;
    }

    protected boolean checkBlockLength(int blockLength) {
        return blockLength == 32 || blockLength == 64 || blockLength == 128;
    }

    protected boolean checkKeyLength(int keyLength) {
        return keyLength > 0 && keyLength < 256;
    }

    protected boolean checkRounds(int rounds) {
        return rounds > 0 && rounds < 256;
    }


    public RC5Encryptor(int blockLength, int keyLength, int rounds) {
        if (!checkBlockLength(blockLength)) {
            throw new IllegalArgumentException("Invalid block length!");
        }
        if (!checkKeyLength(keyLength)) {
            throw new IllegalArgumentException("Invalid key length!");
        }
        if (!checkRounds(rounds)) {
            throw new IllegalArgumentException("Invalid rounds count!");
        }

        this.blockLength = blockLength;
        this.keyLength = keyLength;
        this.rounds = rounds;
        this.roundKeys = null;
        this.roundKeysCount = 2 * (rounds + 1);
    }

    public RC5Encryptor() {
        this(128, 16, 12);
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
                    Utils.cycleLeftShift((a ^ b), b, halfBlockLength),
                    roundKeys[2 * i], halfBlockLength);
            b = Utils.additionMod(
                    Utils.cycleLeftShift((a ^ b), a, halfBlockLength),
                    roundKeys[2 * i + 1], halfBlockLength);
        }
        return clayTwoParts(a, b, blockLength / Byte.SIZE);
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
                    a, halfBlockLength) ^ a;
            a = Utils.cycleRightShift(
                    Utils.subtractionMod(a, roundKeys[2 * i], halfBlockLength),
                    b, halfBlockLength) ^ b;
        }

        b = Utils.subtractionMod(b, roundKeys[1], halfBlockLength);
        a = Utils.subtractionMod(a, roundKeys[0], halfBlockLength);

        return clayTwoParts(a, b, blockLength / Byte.SIZE);
    }

    @Override
    public IEncryptor setKey(byte[] key) {
        if (key == null) {
            throw new NullPointerException("Key cannot be null!");
        }
        if (key.length != keyLength) {
            throw new IllegalArgumentException(
                    String.format("Invalid key length (%d != %d)!", key.length, keyLength)
            );
        }
        this.roundKeys = expandKey(key, roundKeysCount);
        return this;
    }

}
