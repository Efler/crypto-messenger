package org.eflerrr.encrypt.encryptor.impl;

import lombok.extern.slf4j.Slf4j;
import org.eflerrr.encrypt.manager.EncryptorManager;
import org.eflerrr.encrypt.types.EncryptionAlgorithm;
import org.eflerrr.encrypt.types.EncryptionMode;
import org.eflerrr.encrypt.types.PaddingType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.eflerrr.encrypt.types.EncryptionAlgorithm.*;
import static org.eflerrr.utils.Utils.bytesToHexString;
import static org.eflerrr.utils.Utils.generateIV;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@Slf4j
@SuppressWarnings("all")
class RC5EncryptorTest {

    private final String INITIAL_PATH;
    private final String ENCRYPTED_PATH;
    private final String DECRYPTED_PATH;

    public RC5EncryptorTest() throws URISyntaxException {
        INITIAL_PATH = Paths.get("src", "test", "resources", "initial.txt")
                .toAbsolutePath().toString();
        ENCRYPTED_PATH = Paths.get("src", "test", "resources", "encrypted.txt")
                .toAbsolutePath().toString();
        DECRYPTED_PATH = Paths.get("src", "test", "resources", "decrypted.txt")
                .toAbsolutePath().toString();
    }

    @BeforeEach
    public void logsToSeparate() {
        log.trace("--------------------------------------------------");
    }

    @AfterEach
    public void finalSeparate() {
        log.trace("--------------------------------------------------");
        log.trace("");
    }


    // -------- Data providers -------- //

    private static Stream<Arguments> WordLengthTest_16_provideFileTestData() {
        return Stream.of(new EncryptionAlgorithm[]{
                        RC5_16_12_16, RC5_16_12_24, RC5_16_12_32,
                })
                .flatMap(algorithm -> Stream.of(EncryptionMode.values())
                        .flatMap(mode -> Stream.of(PaddingType.values())
                                .flatMap(type -> Stream.of(Arguments.of(
                                        algorithm, mode, type
                                )))));
    }

    private static Stream<Arguments> WordLengthTest_32_provideFileTestData() {
        return Stream.of(new EncryptionAlgorithm[]{
                        RC5_32_12_16, RC5_32_12_24, RC5_32_12_32,
                })
                .flatMap(algorithm -> Stream.of(EncryptionMode.values())
                        .flatMap(mode -> Stream.of(PaddingType.values())
                                .flatMap(type -> Stream.of(Arguments.of(
                                        algorithm, mode, type
                                )))));
    }

    private static Stream<Arguments> WordLengthTest_64_provideFileTestData() {
        return Stream.of(new EncryptionAlgorithm[]{
                        RC5_64_12_16, RC5_64_12_24, RC5_64_12_32,
                })
                .flatMap(algorithm -> Stream.of(EncryptionMode.values())
                        .flatMap(mode -> Stream.of(PaddingType.values())
                                .flatMap(type -> Stream.of(Arguments.of(
                                        algorithm, mode, type
                                )))));
    }


    // -------- Choosing key size function -------- //

    private int getKeySize(EncryptionAlgorithm algorithm) {
        return switch (algorithm) {
            case RC5_16_12_16, RC5_32_12_16, RC5_64_12_16 -> 16;
            case RC5_16_12_24, RC5_32_12_24, RC5_64_12_24 -> 24;
            case RC5_16_12_32, RC5_32_12_32, RC5_64_12_32 -> 32;
            default -> 0;
        };
    }


    // -------- File async enctyption tests -------- //

    @ParameterizedTest
    @MethodSource("WordLengthTest_16_provideFileTestData")
    public void WordLengthTest_16(
            EncryptionAlgorithm algorithm,
            EncryptionMode mode,
            PaddingType type
    ) throws IOException {

        var KEY = generateIV(getKeySize(algorithm));
        var IV = generateIV(4);
        EncryptorManager manager = new EncryptorManager(KEY, algorithm, mode, type, IV);

        log.info("---  [ RC5 : WordLengthTest : 16 ]  ENCRYPTING TEXT MESSAGE FROM FILE ---");
        log.debug("Generated key: {}", bytesToHexString(KEY, "-"));
        log.info("Algorithm: {}", algorithm);
        log.info("Mode: {}", mode);
        log.info("Padding: {}", type);
        log.debug("Generated IV: {}", bytesToHexString(IV, "-"));

        try {

            log.info("Initial file: {}", INITIAL_PATH);
            manager.encryptFile(INITIAL_PATH, ENCRYPTED_PATH);
            log.info("Encrypted file: {}", ENCRYPTED_PATH);
            manager.decryptFile(ENCRYPTED_PATH, DECRYPTED_PATH);
            log.info("Decrypted file: {}", DECRYPTED_PATH);

            assertEquals(
                    new String(Files.readAllBytes(Paths.get(INITIAL_PATH)), StandardCharsets.UTF_8),
                    new String(Files.readAllBytes(Paths.get(DECRYPTED_PATH)), StandardCharsets.UTF_8)
            );

        } catch (IOException e) {
            log.error("Error while encrypting file test: {}", e.getMessage());
            fail();
        }

    }


    @ParameterizedTest
    @MethodSource("WordLengthTest_32_provideFileTestData")
    public void WordLengthTest_32(
            EncryptionAlgorithm algorithm,
            EncryptionMode mode,
            PaddingType type
    ) throws IOException {

        var KEY = generateIV(getKeySize(algorithm));
        var IV = generateIV(8);
        EncryptorManager manager = new EncryptorManager(KEY, algorithm, mode, type, IV);

        log.info("---  [ RC5 : WordLengthTest : 32 ]  ENCRYPTING TEXT MESSAGE FROM FILE ---");
        log.debug("Generated key: {}", bytesToHexString(KEY, "-"));
        log.info("Algorithm: {}", algorithm);
        log.info("Mode: {}", mode);
        log.info("Padding: {}", type);
        log.debug("Generated IV: {}", bytesToHexString(IV, "-"));

        try {

            log.info("Initial file: {}", INITIAL_PATH);
            manager.encryptFile(INITIAL_PATH, ENCRYPTED_PATH);
            log.info("Encrypted file: {}", ENCRYPTED_PATH);
            manager.decryptFile(ENCRYPTED_PATH, DECRYPTED_PATH);
            log.info("Decrypted file: {}", DECRYPTED_PATH);

            assertEquals(
                    new String(Files.readAllBytes(Paths.get(INITIAL_PATH)), StandardCharsets.UTF_8),
                    new String(Files.readAllBytes(Paths.get(DECRYPTED_PATH)), StandardCharsets.UTF_8)
            );

        } catch (IOException e) {
            log.error("Error while encrypting file test: {}", e.getMessage());
            fail();
        }

    }


    @ParameterizedTest
    @MethodSource("WordLengthTest_64_provideFileTestData")
    public void WordLengthTest_64(
            EncryptionAlgorithm algorithm,
            EncryptionMode mode,
            PaddingType type
    ) throws IOException {

        var KEY = generateIV(getKeySize(algorithm));
        var IV = generateIV(16);
        EncryptorManager manager = new EncryptorManager(KEY, algorithm, mode, type, IV);

        log.info("---  [ RC5 : WordLengthTest : 64 ]  ENCRYPTING TEXT MESSAGE FROM FILE ---");
        log.debug("Generated key: {}", bytesToHexString(KEY, "-"));
        log.info("Algorithm: {}", algorithm);
        log.info("Mode: {}", mode);
        log.info("Padding: {}", type);
        log.debug("Generated IV: {}", bytesToHexString(IV, "-"));

        try {

            log.info("Initial file: {}", INITIAL_PATH);
            manager.encryptFile(INITIAL_PATH, ENCRYPTED_PATH);
            log.info("Encrypted file: {}", ENCRYPTED_PATH);
            manager.decryptFile(ENCRYPTED_PATH, DECRYPTED_PATH);
            log.info("Decrypted file: {}", DECRYPTED_PATH);

            assertEquals(
                    new String(Files.readAllBytes(Paths.get(INITIAL_PATH)), StandardCharsets.UTF_8),
                    new String(Files.readAllBytes(Paths.get(DECRYPTED_PATH)), StandardCharsets.UTF_8)
            );

        } catch (IOException e) {
            log.error("Error while encrypting file test: {}", e.getMessage());
            fail();
        }

    }

}
