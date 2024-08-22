package org.eflerrr.server.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.eflerrr.encrypt.types.EncryptionAlgorithm;
import org.eflerrr.server.client.HttpClient;
import org.eflerrr.server.configuration.ApplicationConfig;
import org.eflerrr.server.model.Chat;
import org.eflerrr.server.model.ClientInfo;
import org.eflerrr.server.model.DiffieHellmanParams;
import org.eflerrr.server.model.KafkaInfo;
import org.eflerrr.server.model.dto.response.CreateChatResponse;
import org.eflerrr.server.model.dto.response.ExchangePublicKeyResponse;
import org.eflerrr.server.model.dto.response.JoinChatResponse;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final Map<String, Chat> chats = new ConcurrentHashMap<>();
    private final DiffieHellmanService diffieHellmanService;
    private final HttpClient httpClient;
    private final KafkaAdmin kafkaAdmin;
    private final AdminClient kafkaTopicsDeleter;
    private final ApplicationConfig config;

    public static final String CHAT_NOT_EXIST_ERROR_MESSAGE = "Chat with name %s does not exist";


    public CreateChatResponse createChat(
            String chatName, EncryptionAlgorithm algorithm, ClientInfo creator
    ) {
        if (chats.containsKey(chatName)) {
            throw new IllegalArgumentException("Chat with name " + chatName + " already exists");
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

        var generatedParams = diffieHellmanService.generateParams(config.diffieHellman().bitLength());
        var g = generatedParams.getLeft();
        var p = generatedParams.getRight();

        Chat chat = Chat.builder()
                .name(chatName)
                .encryptionAlgorithm(algorithm)
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

    public JoinChatResponse joinChat(String chatName, ClientInfo client) throws InvalidKeyException {
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

        try {
            var creator = chat.getClients().get(chat.getCreatorId());
            var creatorPublicKey = httpClient.getPublicKey(
                    creator.getHost(),
                    creator.getPort(),
                    client.getSettings());
            creator.setPublicKey(creatorPublicKey);
        } catch (IllegalStateException e) {
            throw new InvalidKeyException((String.format(
                    "Creator of the chat '%s' did not send his public key!", chatName)));
        }

        chat.getClients().put(client.getId(), client);
        chat.setClientsCount(chat.getClientsCount() + 1);

        return JoinChatResponse.builder()
                .diffieHellmanParams(DiffieHellmanParams.builder()
                        .g(chat.getG())
                        .p(chat.getP())
                        .build())
                .kafkaInfo(KafkaInfo.builder()
                        .bootstrapServers(config.kafka().bootstrapServers())
                        .topic(chat.getKafkaTopic().name())
                        .build())
                .build();
    }

    public ExchangePublicKeyResponse exchangePublicKey(String chatName, Long clientId, BigInteger publicKey) {
        var chat = chats.get(chatName);
        if (chat == null) {
            throw new IllegalArgumentException(String.format(CHAT_NOT_EXIST_ERROR_MESSAGE, chatName));
        }
        if (!chat.getClients().containsKey(clientId)) {
            throw new IllegalCallerException(String.format("Client with id %d is not in the chat", clientId));
        }

        var creator = chat.getClients().get(chat.getCreatorId());

        chat.getClients().get(clientId).setPublicKey(publicKey);
        httpClient.sendPublicKey(creator.getHost(), creator.getPort(), publicKey);

        return ExchangePublicKeyResponse.builder()
                .matePublicKey(creator.getPublicKey())
                .mateSettings(creator.getSettings())
                .build();
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

    public List<Pair<String, EncryptionAlgorithm>> listChats() {
        var result = new ArrayList<Pair<String, EncryptionAlgorithm>>();
        for (var chat : chats.values()) {
            result.add(Pair.of(
                    chat.getName(),
                    chat.getEncryptionAlgorithm())
            );
        }
        return result;
    }

    public boolean isChatRegistered(String chatName) {
        var isRegistered = chats.containsKey(chatName);
        log.info("Processing isChatRegistered for chat '{}', {}", chatName, isRegistered ? "YES" : "NO");
        return isRegistered;
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
