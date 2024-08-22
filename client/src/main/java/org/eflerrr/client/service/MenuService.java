package org.eflerrr.client.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eflerrr.client.client.ServerClient;
import org.eflerrr.client.configuration.ApplicationConfig;
import org.eflerrr.client.dao.ChatDao;
import org.eflerrr.client.dao.ClientDao;
import org.eflerrr.utils.Utils;
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
        chatDao.setSelfPublicKey(
                chatDao.getG().modPow(clientDao.getPrivateKey(), chatDao.getP())
        );
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
        var clientId = clientDao.getClientId();
        var chatName = chatDao.getChatName();
        var algorithm = chatDao.getEncryptionAlgorithm();
        var selfSettings = chatDao.getSelfSettings();

        log.info("Creating chat with name {}", chatName);
        var serverResponse = serverClient.createChat(
                clientId, chatName, algorithm, selfSettings
        );
        chatDao.setG(serverResponse.getDiffieHellmanParams().getG());
        chatDao.setP(serverResponse.getDiffieHellmanParams().getP());
        chatDao.setKafkaInfo(serverResponse.getKafkaInfo());

        generatePublicKey();
    }

    public void joinChat() {
        var clientId = clientDao.getClientId();
        var chatName = chatDao.getChatName();
        var selfSettings = chatDao.getSelfSettings();

        log.info("Joining chat with name {}", chatName);
        var serverResponse = serverClient.joinChat(clientId, chatName, selfSettings);
        chatDao.setG(serverResponse.getDiffieHellmanParams().getG());
        chatDao.setP(serverResponse.getDiffieHellmanParams().getP());
        chatDao.setKafkaInfo(serverResponse.getKafkaInfo());

        generatePublicKey();

        var exchangeResponse = serverClient.exchangePublicKey(clientId, chatName, chatDao.getSelfPublicKey());
        chatDao.setMatePublicKey(exchangeResponse.getMatePublicKey());
        chatDao.setMateSettings(exchangeResponse.getMateSettings());
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

    public byte[] generateIV() {
        var generated = Utils.generateIV(chatDao.getEncryptionAlgorithm().getBlockLength() / 8);
        chatDao.getSelfSettings().setIV(generated);
        return generated;
    }

    public boolean isChatRegistered(String chatName) {
        return serverClient.isChatRegistered(chatName);
    }

}
