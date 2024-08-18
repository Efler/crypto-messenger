package org.eflerrr.client.encryption;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eflerrr.client.configuration.ApplicationConfig;
import org.eflerrr.encrypt.manager.EncryptorManager;
import org.eflerrr.encrypt.types.EncryptionAlgorithm;
import org.eflerrr.encrypt.types.EncryptionMode;
import org.eflerrr.encrypt.types.PaddingType;
import org.springframework.stereotype.Component;

import java.math.BigInteger;

@Component
@Slf4j
@RequiredArgsConstructor
public class DualChatMessageEncryptor {

    private final ApplicationConfig config;
    private EncryptorManager encryptor;
    private EncryptorManager decryptor;

    public DualChatMessageEncryptor configureEncryptor(
            BigInteger key,
            EncryptionMode mode,
            PaddingType type,
            byte[] IV,
            EncryptionAlgorithm algorithm) {
        this.encryptor = new EncryptorManager(
                key.toByteArray(), algorithm, mode, type, IV
        );
        return this;
    }

    public DualChatMessageEncryptor configureDecryptor(
            BigInteger key,
            EncryptionMode mode,
            PaddingType type,
            byte[] IV,
            EncryptionAlgorithm algorithm
    ) {
        this.decryptor = new EncryptorManager(
                key.toByteArray(), algorithm, mode, type, IV
        );
        return this;
    }

    public byte[] encrypt(byte[] message) {
        return encryptor.encryptAsync(
                message, config.encryption().threadsCount());
    }

    public byte[] decrypt(byte[] encryptedMessage) {
        return decryptor.decryptAsync(
                encryptedMessage, config.encryption().threadsCount()
        );
    }

}
