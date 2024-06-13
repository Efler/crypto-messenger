package org.eflerrr.client.service;

import lombok.extern.slf4j.Slf4j;
import org.eflerrr.client.client.ServerClient;
import org.eflerrr.client.client.dto.KafkaInfo;
import org.eflerrr.encrypt.types.EncryptionAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.SecureRandom;

@Service
@Slf4j
public class MenuService {

    private final static int PRIVATE_KEY_BIT_LENGTH = 1024;
    private final static SecureRandom RANDOM = new SecureRandom();
    private final BigInteger privateKey;
    private final ServerClient serverClient;
    private BigInteger publicKey;
    private KafkaInfo kafkaInfo;
    private String clientName;
    private long clientId;


    private long generateClientId(String clientName) {
        var result = clientName.hashCode();
        for (char c : clientName.toCharArray()) {
            result = 31 * result + c;
        }
        return Math.abs(result);
    }

    private BigInteger generatePublicKey(BigInteger g, BigInteger p) {
        log.info("Generating public key");
        return g.modPow(privateKey, p);
    }


    @Autowired
    public MenuService(ServerClient serverClient) {
        this.serverClient = serverClient;
        this.privateKey = new BigInteger(PRIVATE_KEY_BIT_LENGTH, RANDOM);
    }

    public void setCredentials(String clientName) {
        this.clientName = clientName;
        this.clientId = generateClientId(clientName);
    }

    public void createChat(String chatName, EncryptionAlgorithm algorithm) {
        log.info("Creating chat with name {}", chatName);
        var serverResponse = serverClient.createChat(clientId, clientName, chatName, algorithm);
        this.publicKey = generatePublicKey(
                serverResponse.getDiffieHellmanParams().getG(),
                serverResponse.getDiffieHellmanParams().getP()
        );
        this.kafkaInfo = serverResponse.getKafkaInfo();
    }

}
