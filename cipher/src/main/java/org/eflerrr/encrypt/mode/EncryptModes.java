package org.eflerrr.encrypt.mode;

import org.eflerrr.encrypt.encryptor.IEncryptor;
import org.eflerrr.encrypt.mode.impl.*;
import org.eflerrr.encrypt.types.EncryptionMode;

public class EncryptModes {

    public static AEncryptMode getMode(
            EncryptionMode mode,
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
