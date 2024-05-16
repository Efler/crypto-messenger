package org.eflerrr;

import lombok.extern.slf4j.Slf4j;
import org.eflerrr.encrypt.encryptor.impl.RC5Encryptor;
import org.eflerrr.encrypt.manager.EncryptorManager;
import org.eflerrr.encrypt.mode.EncryptModes;
import org.eflerrr.padding.Paddings;

import java.io.IOException;
import java.nio.file.Paths;

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
                        (byte) 0x00, (byte) 0xDD, (byte) 0x8E, (byte) 0x0F,
                },
                new RC5Encryptor(64, 10, 50),
                EncryptModes.Mode.RandomDelta,
                Paddings.PaddingType.ZEROZ,
                new byte[]{
                        (byte) 0xA1, (byte) 0x52, (byte) 0x03, (byte) 0x04,
                        (byte) 0x05, (byte) 0x06, (byte) 0x09, (byte) 0xDC,
                        (byte) 0x09, (byte) 0x6A, (byte) 0x0B, (byte) 0x0C,
                        (byte) 0x0D, (byte) 0x0E, (byte) 0xFF, (byte) 0x10,
                }
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
