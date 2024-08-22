package org.eflerrr.encrypt.types;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.eflerrr.encrypt.encryptor.IEncryptor;
import org.eflerrr.encrypt.encryptor.impl.RC5Encryptor;
import org.eflerrr.encrypt.encryptor.impl.RC6Encryptor;

@Getter
@AllArgsConstructor
public enum EncryptionAlgorithm {

    RC5_16_12_16(32, 16, 12),
    RC5_16_12_24(32, 24, 12),
    RC5_16_12_32(32, 32, 12),
    RC5_32_12_16(64, 16, 12),
    RC5_32_12_24(64, 24, 12),
    RC5_32_12_32(64, 32, 12),
    RC5_64_12_16(128, 16, 12),
    RC5_64_12_24(128, 24, 12),
    RC5_64_12_32(128, 32, 12),
    RC5_DEFAULT(128, 16, 12),
    RC6_16_20_16(64, 16, 20),
    RC6_16_20_24(64, 24, 20),
    RC6_16_20_32(64, 32, 20),
    RC6_32_20_16(128, 16, 20),
    RC6_32_20_24(128, 24, 20),
    RC6_32_20_32(128, 32, 20),
    RC6_64_20_16(256, 16, 20),
    RC6_64_20_24(256, 24, 20),
    RC6_64_20_32(256, 32, 20),
    RC6_DEFAULT(256, 16, 20);

    private final int blockLength;
    private final int keyLength;
    private final int rounds;

    public IEncryptor createEncryptorInstance() {
        return switch (this) {
            case RC5_16_12_16,
                 RC5_16_12_24,
                 RC5_16_12_32,
                 RC5_32_12_16,
                 RC5_32_12_24,
                 RC5_32_12_32,
                 RC5_64_12_16,
                 RC5_64_12_24,
                 RC5_64_12_32,
                 RC5_DEFAULT -> new RC5Encryptor(this.blockLength, this.keyLength, this.rounds);
            case RC6_16_20_16,
                 RC6_16_20_24,
                 RC6_16_20_32,
                 RC6_32_20_16,
                 RC6_32_20_24,
                 RC6_32_20_32,
                 RC6_64_20_16,
                 RC6_64_20_24,
                 RC6_64_20_32,
                 RC6_DEFAULT -> new RC6Encryptor(this.blockLength, this.keyLength, this.rounds);
        };
    }

    @Override
    public String toString() {
        if (super.toString().contains("DEFAULT")) {
            return super.toString()
                    .replace("_DEFAULT", "-default");
        } else {
            return super.toString()
                    .replaceFirst("_", "-")
                    .replace("_", "/");
        }
    }

}
