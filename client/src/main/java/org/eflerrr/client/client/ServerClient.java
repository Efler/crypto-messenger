package org.eflerrr.client.client;

import lombok.extern.slf4j.Slf4j;
import org.eflerrr.client.client.dto.ChatInfo;
import org.eflerrr.client.client.dto.request.CreateChatRequest;
import org.eflerrr.client.client.dto.response.CreateChatResponse;
import org.eflerrr.client.configuration.ApplicationConfig;
import org.eflerrr.encrypt.types.EncryptionAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

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
            long clientId, String clientName, String chatName, EncryptionAlgorithm algorithm
    ) {
        log.info("Making a request to the server to create a chat: {}, {}", chatName, algorithm);
        return webClient.post()
                .uri(config.chatEndpoint())
                .header("Client-Id", String.valueOf(clientId))
                .header("Client-Name", clientName)
                .header("Client-Host", config.server().host())
                .header("Client-Port", String.valueOf(config.server().port()))
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

}
