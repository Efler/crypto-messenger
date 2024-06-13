package org.eflerrr.encrypt.types;

public enum EncryptionAlgorithm {

    RC5_16_12_16, RC5_16_12_24, RC5_16_12_32,
    RC5_32_12_16, RC5_32_12_24, RC5_32_12_32,
    RC5_64_12_16, RC5_64_12_24, RC5_64_12_32,
    RC5_DEFAULT,
    RC6_16_20_16, RC6_16_20_24, RC6_16_20_32,
    RC6_32_20_16, RC6_32_20_24, RC6_32_20_32,
    RC6_64_20_16, RC6_64_20_24, RC6_64_20_32,
    RC6_DEFAULT;

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
