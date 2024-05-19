package org.eflerrr.server.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.clients.admin.NewTopic;
import org.eflerrr.server.configuration.ApplicationConfig;
import org.eflerrr.server.exchangekey.DiffieHellman;
import org.eflerrr.server.service.dto.Chat;
import org.eflerrr.server.service.dto.ClientInfo;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final Map<String, Chat> chats = new HashMap<>();
    private final DiffieHellman diffieHellman;
    private final KafkaAdmin kafkaAdmin;
    private final ApplicationConfig config;

    public enum CipherAlgorithm {
        RC5,
        RC6
    }

    public Pair<
            Pair<BigInteger, BigInteger>,
            Pair<String, String>>
    createChat(
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

        return Pair.of(
                generatedParams,
                Pair.of(config.kafka().bootstrapServers(), topicName)
        );
    }

    private void createChat(String chatName, CipherAlgorithm algorithm) {
        createChat(chatName, algorithm, null);
    }


    public void checker() {     // TODO: for debug only, remove in production
        if (chats.isEmpty()) {
            log.info("No chats");
            return;
        }
        for (var chat : chats.values()) {
            log.info(chat.toString());
        }
    }

}
