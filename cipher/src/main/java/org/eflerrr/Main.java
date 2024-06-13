package org.eflerrr;

import lombok.extern.slf4j.Slf4j;
import org.eflerrr.encrypt.manager.EncryptorManager;
import org.eflerrr.encrypt.mode.EncryptModes;
import org.eflerrr.encrypt.types.EncryptionMode;
import org.eflerrr.encrypt.types.PaddingType;

import java.io.IOException;
import java.nio.file.Paths;

import static org.eflerrr.encrypt.types.EncryptionAlgorithm.RC6_DEFAULT;
import static org.eflerrr.utils.Utils.generateIV;

@Slf4j
@SuppressWarnings("all")
public class Main {

    public static void main(String[] args) {
        var inputFile = "text.txt";
        var encryptedFile = inputFile.substring(0, inputFile.lastIndexOf('.'))
                + "_encrypted"
                + inputFile.substring(inputFile.lastIndexOf('.'));
        var decryptedFile = inputFile.substring(0, inputFile.lastIndexOf('.'))
                + "_decrypted"
                + inputFile.substring(inputFile.lastIndexOf('.'));

        var manager = new EncryptorManager(
                new byte[]{
                        (byte) 0x04, (byte) 0x01, (byte) 0x02, (byte) 0x1C,
                        (byte) 0xF4, (byte) 0x65, (byte) 0x83, (byte) 0x07,
                        (byte) 0xAA, (byte) 0x09, (byte) 0x1A, (byte) 0x0B,
                        (byte) 0x00, (byte) 0xDD, (byte) 0x8E, (byte) 0x0F
                },
                RC6_DEFAULT,
                EncryptionMode.RandomDelta,
                PaddingType.ISO10126,
                generateIV(32)
        );

        try {
            log.info("initial file: {}", inputFile);
            log.info("encrypting file...");
            manager.encryptFileAsync(
                    Paths.get("cipher", "src", "main", "resources", inputFile)
                            .toAbsolutePath().toString(),
                    Paths.get("cipher", "src", "main", "resources", encryptedFile)
                            .toAbsolutePath().toString(),
                    7);
            log.info("encrypted: {}", encryptedFile);
        } catch (IOException e) {
            log.error("EncryptFileAsync Error! Message: {}", e.getMessage());
        }

        try {
            log.info("decrypting file...");
            manager.decryptFileAsync(
                    Paths.get("cipher", "src", "main", "resources", encryptedFile)
                            .toAbsolutePath().toString(),
                    Paths.get("cipher", "src", "main", "resources", decryptedFile)
                            .toAbsolutePath().toString(),
                    7);
            log.info("decrypted: {}", decryptedFile);
        } catch (IOException e) {
            log.error("DecryptFileAsync Error! Message : {}", e.getMessage());
        }
    }

}
