package org.eflerrr.client.client;

import lombok.extern.slf4j.Slf4j;
import org.eflerrr.client.configuration.ApplicationConfig;
import org.eflerrr.client.dto.response.ExchangePublicKeyResponse;
import org.eflerrr.client.model.ChatInfo;
import org.eflerrr.client.dto.request.CreateChatRequest;
import org.eflerrr.client.dto.response.CreateChatResponse;
import org.eflerrr.client.dto.response.JoinChatResponse;
import org.eflerrr.encrypt.types.EncryptionAlgorithm;
import org.eflerrr.encrypt.types.EncryptionMode;
import org.eflerrr.encrypt.types.PaddingType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigInteger;
import java.util.List;

@Component
@Slf4j
public class ServerClient {

    private final ApplicationConfig config;
    private final WebClient webClient;

    @Autowired
    public ServerClient(ApplicationConfig config) {
        this.webClient = WebClient.builder()
                .baseUrl(config.serverUrl())
                .build();
        this.config = config;
    }

    public List<ChatInfo> requestChatList() {
        log.info("Making a request to the server to get the chat list");
        var response = webClient.get()
                .uri(config.chatList().endpoint())
                .retrieve()
                .toEntityList(ChatInfo.class)
                .block();
        return response == null
                ? List.of()
                : response.getBody();
    }

    public CreateChatResponse createChat(
            long clientId,
            String clientName,
            String chatName,
            EncryptionAlgorithm algorithm,
            EncryptionMode mode,
            PaddingType padding
    ) {
        log.info("Making a request to the server to create a chat: {}, {}", chatName, algorithm);
        return webClient.post()
                .uri(config.chatEndpoint())
                .header("Client-Id", String.valueOf(clientId))
                .header("Client-Name", clientName)
                .header("Client-Host", config.server().host())
                .header("Client-Port", String.valueOf(config.server().port()))
                .header("Encryption-Mode", mode.name())
                .header("Padding-Type", padding.name())
                .bodyValue(CreateChatRequest.builder()
                        .chatName(chatName)
                        .encryptionAlgorithm(algorithm)
                        .build())
                .retrieve()
                .onStatus(HttpStatus.CONFLICT::equals, r ->
                        Mono.error(new IllegalArgumentException(
                                "Чат с именем '" + chatName + "' уже существует!")
                        )
                )
                .bodyToMono(CreateChatResponse.class)
                .block();
    }

    public JoinChatResponse joinChat(
            long clientId,
            String clientName,
            String chatName,
            EncryptionMode mode,
            PaddingType padding
    ) {
        log.info("Making a request to the server to join a chat: {}", chatName);
        return webClient.get()
                .uri(config.chatEndpoint())
                .header("Client-Id", String.valueOf(clientId))
                .header("Client-Name", clientName)
                .header("Client-Host", config.server().host())
                .header("Client-Port", String.valueOf(config.server().port()))
                .header("Chat-Name", chatName)
                .header("Encryption-Mode", mode.name())
                .header("Padding-Type", padding.name())
                .retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals, r ->
                        Mono.error(new IllegalArgumentException(String.format(
                                "Чата с именем '%s' не существует!",
                                chatName
                        )))
                )
                .onStatus(HttpStatus.FORBIDDEN::equals, r ->
                        Mono.error(new IllegalArgumentException(String.format(
                                "Чат с именем '%s' уже занят другими пользователями!",
                                chatName
                        )))
                )
                .onStatus(HttpStatus.CONFLICT::equals, r ->
                        Mono.error(new IllegalArgumentException(String.format(
                                "Клиент с именем '%s' уже присоединился к чату '%s'!",
                                clientName, chatName
                        )))
                )
                .bodyToMono(JoinChatResponse.class)
                .block();
    }

    public ExchangePublicKeyResponse exchangePublicKey(long clientId, String chatName, BigInteger publicKey) {
        log.info("Making a request to the server to exchange public key (chat: {})", chatName);
        return webClient.put()
                .uri(config.chatEndpoint())
                .header("Client-Id", String.valueOf(clientId))
                .header("Chat-Name", chatName)
                .bodyValue(publicKey)
                .retrieve()
                .onStatus(HttpStatus.FORBIDDEN::equals, r ->
                        Mono.error(new IllegalArgumentException(String.format(
                                "Чат с ID '%s' не находится в чате %s!",
                                clientId, chatName
                        )))
                )
                .onStatus(HttpStatus.NOT_FOUND::equals, r ->
                        Mono.error(new IllegalArgumentException(String.format(
                                "Чата с именем '%s' не существует!",
                                chatName
                        )))
                )
                .bodyToMono(ExchangePublicKeyResponse.class)
                .block();
    }

}
