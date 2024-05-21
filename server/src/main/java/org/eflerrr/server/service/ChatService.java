package org.eflerrr.server.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.eflerrr.server.client.HttpClient;
import org.eflerrr.server.configuration.ApplicationConfig;
import org.eflerrr.server.controller.dto.DiffieHellmanParams;
import org.eflerrr.server.controller.dto.KafkaInfo;
import org.eflerrr.server.controller.dto.response.CreateChatResponse;
import org.eflerrr.server.controller.dto.response.JoinChatResponse;
import org.eflerrr.server.exchangekey.DiffieHellman;
import org.eflerrr.server.service.dto.Chat;
import org.eflerrr.server.service.dto.ClientInfo;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final Map<String, Chat> chats = new ConcurrentHashMap<>();
    private final DiffieHellman diffieHellman;
    private final HttpClient httpClient;
    private final KafkaAdmin kafkaAdmin;
    private final AdminClient kafkaTopicsDeleter;
    private final ApplicationConfig config;

    public static final String CHAT_NOT_EXIST_ERROR_MESSAGE = "Chat with name %s does not exist";

    public enum CipherAlgorithm {
        RC5,
        RC6
    }


    public CreateChatResponse createChat(
            String chatName, CipherAlgorithm algorithm, ClientInfo creator
    ) {
        for (var existingName : chats.keySet()) {
            if (chatName.equals(existingName)) {
                throw new IllegalArgumentException("Chat with name " + chatName + " already exists");
            }
        }

        var topicName = chatName + config.kafka().topicsPostfix();
        var chatTopic = new NewTopic(
                topicName, 1,
                config.kafka().topicsReplicationFactor()
        );
        kafkaAdmin.createOrModifyTopics(chatTopic);

        HashMap<Long, ClientInfo> clients = null;
        if (creator != null) {
            clients = new HashMap<>();
            clients.put(creator.getId(), creator);
        }

        var generatedParams = diffieHellman.generateParams(config.diffieHellman().bitLength());
        var g = generatedParams.getLeft();
        var p = generatedParams.getRight();

        Chat chat = Chat.builder()
                .name(chatName)
                .cipherAlgorithm(algorithm)
                .clients(clients)
                .clientsCount(creator == null ? 0 : 1)
                .creatorId(creator == null ? null : creator.getId())
                .kafkaTopic(chatTopic)
                .g(g)
                .p(p)
                .build();
        chats.put(chatName, chat);

        return CreateChatResponse.builder()
                .diffieHellmanParams(DiffieHellmanParams.builder()
                        .g(g).p(p)
                        .build())
                .kafkaInfo(KafkaInfo.builder()
                        .bootstrapServers(config.kafka().bootstrapServers())
                        .topic(topicName)
                        .build())
                .build();
    }

    private void createChat(String chatName, CipherAlgorithm algorithm) {
        createChat(chatName, algorithm, null);
    }

    public JoinChatResponse joinChat(String chatName, ClientInfo client) {
        var chat = chats.get(chatName);
        if (chat == null) {
            throw new IllegalArgumentException(String.format(CHAT_NOT_EXIST_ERROR_MESSAGE, chatName));
        }
        if (chat.getClientsCount() == 2) {
            throw new IllegalStateException(String.format("Chat with name %s is full", chatName));
        }
        if (chat.getClients().containsKey(client.getId())) {
            throw new IllegalCallerException(String.format("Client with id %d already in chat", client.getId()));
        }

        chat.getClients().put(client.getId(), client);
        chat.setClientsCount(chat.getClientsCount() + 1);

        var creator = chat.getClients().get(chat.getCreatorId());
        var publicKey = httpClient.getPublicKey(creator.getHost(), creator.getPort());
        creator.setPublicKey(publicKey);

        return JoinChatResponse.builder()
                .diffieHellmanParams(DiffieHellmanParams.builder()
                        .g(chat.getG())
                        .p(chat.getP())
                        .build())
                .kafkaInfo(KafkaInfo.builder()
                        .bootstrapServers(config.kafka().bootstrapServers())
                        .topic(chat.getKafkaTopic().name())
                        .build())
                .cipherAlgorithm(chat.getCipherAlgorithm())
                .build();
    }

    public BigInteger exchangePublicKey(String chatName, Long clientId, BigInteger publicKey) {
        var chat = chats.get(chatName);
        if (chat == null) {
            throw new IllegalArgumentException(String.format(CHAT_NOT_EXIST_ERROR_MESSAGE, chatName));
        }
        if (!chat.getClients().containsKey(clientId)) {
            throw new IllegalCallerException(String.format("Client with id %d is not in chat", clientId));
        }

        var creator = chat.getClients().get(chat.getCreatorId());
        var creatorPublicKey = creator.getPublicKey();

        chat.getClients().get(clientId).setPublicKey(publicKey);
        httpClient.sendPublicKey(creator.getHost(), creator.getPort(), publicKey);

        return creatorPublicKey;
    }

    @SneakyThrows
    public void leaveChat(String chatName, Long clientId) {
        var chat = chats.get(chatName);
        if (chat == null) {
            throw new IllegalArgumentException(String.format(CHAT_NOT_EXIST_ERROR_MESSAGE, chatName));
        }
        if (!chat.getClients().containsKey(clientId)) {
            throw new IllegalCallerException(String.format("Client with id %d is not in chat", clientId));
        }

        chat.getClients().remove(clientId);
        chat.setClientsCount(chat.getClientsCount() - 1);

        if (chat.getClientsCount() == 0) {
            chats.remove(chatName);
            kafkaTopicsDeleter.deleteTopics(Collections.singletonList(chat.getKafkaTopic().name()))
                    .all().get();
        } else if (clientId.equals(chat.getCreatorId())) {
            var newCreator = chat.getClients().get(chat.getClients().keySet().iterator().next());
            chat.setCreatorId(newCreator.getId());
            newCreator.setPublicKey(null);
            httpClient.notifyClientLeaving(newCreator.getHost(), newCreator.getPort());
        }
    }

    public List<String> listChats() {
        return List.copyOf(chats.keySet());
    }


    public void checker() {     // TODO: for debug only, remove in production
        if (chats.isEmpty()) {
            log.info("No chats");
            return;
        }
        for (var chat : chats.values()) {
            log.info("Chat:");
            log.info(chat.toString());
            log.info("Clients:");
            for (var client : chat.getClients().values()) {
                log.info(client.toString());
            }
        }
    }

}
