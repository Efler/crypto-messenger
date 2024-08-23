package org.eflerrr.encrypt.manager;

import org.eflerrr.encrypt.encryptor.IEncryptor;
import org.eflerrr.encrypt.mode.AEncryptMode;
import org.eflerrr.encrypt.types.EncryptionAlgorithm;
import org.eflerrr.encrypt.types.EncryptionMode;
import org.eflerrr.encrypt.types.PaddingType;
import org.eflerrr.padding.IPadding;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.eflerrr.encrypt.mode.EncryptModes.getMode;
import static org.eflerrr.padding.Paddings.getPadding;

public class EncryptorManager {

    protected final AEncryptMode kernelMode;
    protected final IPadding padding;
    protected final int blockLength;

    public EncryptorManager(
            byte[] key,
            IEncryptor encryptor,
            EncryptionMode mode,
            PaddingType type,
            byte[] initializationVector) {
        kernelMode = getMode(mode, encryptor.setKey(key), initializationVector);
        padding = getPadding(type);
        blockLength = encryptor.getBlockLength() / Byte.SIZE;
    }

    public EncryptorManager(
            byte[] key,
            EncryptionAlgorithm algorithm,
            EncryptionMode mode,
            PaddingType type,
            byte[] initializationVector) {
        this(key, algorithm.createEncryptorInstance(), mode, type, initializationVector);
    }

    public byte[] encrypt(byte[] plain) {
        var withPadding = padding.makePadding(plain, blockLength);
        return kernelMode.encrypt(withPadding);
    }

    public byte[] decrypt(byte[] encoded) {
        return padding.undoPadding(kernelMode.decrypt(encoded));
    }

    public void encryptFile(String inputFile, String outputFile) throws IOException {
        var data = Files.readAllBytes(Paths.get(inputFile));
        var encrypted = encrypt(data);
        Files.write(Paths.get(outputFile), encrypted);
    }

    public void decryptFile(String inputFile, String outputFile) throws IOException {
        var data = Files.readAllBytes(Paths.get(inputFile));
        var decrypted = decrypt(data);
        Files.write(Paths.get(outputFile), decrypted);
    }

}
