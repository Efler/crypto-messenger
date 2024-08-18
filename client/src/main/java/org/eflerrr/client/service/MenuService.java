package org.eflerrr.client.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eflerrr.client.client.ServerClient;
import org.eflerrr.client.configuration.ApplicationConfig;
import org.eflerrr.client.dao.ChatDao;
import org.eflerrr.client.dao.ClientDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.SecureRandom;

@Service
@Getter
@Slf4j
public class MenuService {

    private final static SecureRandom RANDOM = new SecureRandom();

    private final ServerClient serverClient;
    private final ClientDao clientDao;
    private final ChatDao chatDao;

    private void generatePublicKey() {
        log.info("Generating public key");
        clientDao.setPublicKey(
                chatDao.getG().modPow(clientDao.getPrivateKey(), chatDao.getP())
        );
        log.warn("Public key: {}", clientDao.getPublicKey());  // TODO!
    }


    @Autowired
    public MenuService(
            ServerClient serverClient,
            ClientDao clientDao,
            ChatDao chatDao,
            ApplicationConfig config) {
        this.serverClient = serverClient;
        this.clientDao = clientDao;
        this.clientDao.setPrivateKey(
                new BigInteger(config.encryption().privateKeyBitLength(), RANDOM));
        this.chatDao = chatDao;
    }

    public void createChat() {
        var chatName = chatDao.getChatName();
        var clientName = clientDao.getClientName();
        var clientId = clientDao.getClientId();
        var algorithm = chatDao.getEncryptionAlgorithm();
        var mode = chatDao.getEncryptionMode();
        var padding = chatDao.getPaddingType();

        log.info("Creating chat with name {}", chatName);
        var serverResponse = serverClient.createChat(
                clientId, clientName, chatName, algorithm, mode, padding);
        chatDao.setG(serverResponse.getDiffieHellmanParams().getG());
        chatDao.setP(serverResponse.getDiffieHellmanParams().getP());
        chatDao.setKafkaInfo(serverResponse.getKafkaInfo());

        generatePublicKey();
    }

    public void joinChat() {
        var chatName = chatDao.getChatName();
        var clientName = clientDao.getClientName();
        var clientId = clientDao.getClientId();
        var mode = chatDao.getEncryptionMode();
        var padding = chatDao.getPaddingType();

        log.info("Joining chat with name {}", chatName);
        var serverResponse = serverClient.joinChat(clientId, clientName, chatName, mode, padding);
        chatDao.setEncryptionAlgorithm(serverResponse.getEncryptionAlgorithm());
        chatDao.setG(serverResponse.getDiffieHellmanParams().getG());
        chatDao.setP(serverResponse.getDiffieHellmanParams().getP());
        chatDao.setKafkaInfo(serverResponse.getKafkaInfo());

        generatePublicKey();

        var exchangeResponse = serverClient.exchangePublicKey(clientId, chatName, clientDao.getPublicKey());
        chatDao.setMateName(exchangeResponse.getMateName());
        chatDao.setMateEncryptionMode(exchangeResponse.getMateMode());
        chatDao.setMatePaddingType(exchangeResponse.getMatePadding());
        chatDao.setMatePublicKey(exchangeResponse.getMatePublicKey());
    }

    public void generateClientId() {
        var clientName = clientDao.getClientName();
        long result = clientName.hashCode();
        for (char c : clientName.toCharArray()) {
            result = 31 * result + c;
        }
        clientDao.setClientId(Math.abs(result));
    }

    public boolean validateChatName(String chatName) {
        return chatName != null
                && !chatName.trim().isEmpty()
                && chatName.matches("[a-zA-Z0-9-]+");
    }

}
