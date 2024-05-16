package org.eflerrr.encrypt.mode;

import org.eflerrr.encrypt.encryptor.IEncryptor;
import org.eflerrr.encrypt.mode.impl.*;

public class EncryptModes {

    public enum Mode {
        ECB,
        CBC, PCBC,
        CFB, OFB,
        CTR, RandomDelta
    }

    public static AEncryptMode getMode(
            Mode mode,
            IEncryptor encryptor,
            byte[] initializationVector) {
        int size = encryptor.getBlockLength() / Byte.SIZE;
        return switch (mode) {
            case ECB -> new ECBEncryptMode(encryptor, size, initializationVector);
            case CBC -> new CBCEncryptMode(encryptor, size, initializationVector);
            case PCBC -> new PCBCEncryptMode(encryptor, size, initializationVector);
            case CFB -> new CFBEncryptMode(encryptor, size, initializationVector);
            case OFB -> new OFBEncryptMode(encryptor, size, initializationVector);
            case CTR -> new CTREncryptMode(encryptor, size, initializationVector);
            case RandomDelta -> new RandomDeltaEncryptMode(encryptor, size, initializationVector);
        };
    }
}
