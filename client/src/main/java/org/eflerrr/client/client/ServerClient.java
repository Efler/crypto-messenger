package org.eflerrr.client.client;

import lombok.extern.slf4j.Slf4j;
import org.eflerrr.client.configuration.ApplicationConfig;
import org.eflerrr.client.dto.request.CreateChatRequest;
import org.eflerrr.client.dto.request.JoinChatRequest;
import org.eflerrr.client.dto.response.CreateChatResponse;
import org.eflerrr.client.dto.response.ExchangePublicKeyResponse;
import org.eflerrr.client.dto.response.JoinChatResponse;
import org.eflerrr.client.model.ChatInfo;
import org.eflerrr.client.model.ClientSettings;
import org.eflerrr.encrypt.types.EncryptionAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigInteger;
import java.util.List;

import static org.eflerrr.utils.Utils.bytesToBinaryString;
import static org.eflerrr.utils.Utils.bytesToHexString;

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
            String chatName,
            EncryptionAlgorithm algorithm,
            ClientSettings clientSettings
    ) {
        return webClient.post()
                .uri(config.chatEndpoint())
                .header("Client-Id", String.valueOf(clientId))
                .header("Client-Host", config.server().host())
                .header("Client-Port", String.valueOf(config.server().port()))
                .bodyValue(CreateChatRequest.builder()
                        .chatName(chatName)
                        .encryptionAlgorithm(algorithm)
                        .clientSettings(clientSettings)
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
            String chatName,
            ClientSettings clientSettings
    ) {
        log.info("Making a request to the server to join a chat: {}", chatName);
        return webClient.patch()
                .uri(config.chatEndpoint())
                .header("Client-Id", String.valueOf(clientId))
                .header("Client-Host", config.server().host())
                .header("Client-Port", String.valueOf(config.server().port()))
                .bodyValue(JoinChatRequest.builder()
                        .chatName(chatName)
                        .clientSettings(clientSettings)
                        .build())
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
                                clientSettings.getClientName(), chatName
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

    public boolean isChatRegistered(String chatName) {
        log.info("Making a request to the server to check if chat is registered (chat: {})", chatName);
        return Boolean.TRUE.equals(
                webClient.get()
                        .uri(config.chatEndpoint() + "/is-registered/{chat-name}", chatName)
                        .retrieve()
                        .bodyToMono(Boolean.class)
                        .block()
        );
    }

    public void exitChat(long clientId, String chatName) {
        log.info("Making a request to the server to exit chat (chat: {})", chatName);
        webClient.delete()
                .uri(config.chatEndpoint())
                .header("Client-Id", String.valueOf(clientId))
                .header("Chat-Name", chatName)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, r ->
                        r.bodyToMono(String.class)
                                .flatMap(errorMessage ->
                                        Mono.error(new IllegalArgumentException(errorMessage)))
                )
                .bodyToMono(Void.class)
                .block();
    }

}
