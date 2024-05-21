package org.eflerrr.client.client;

import lombok.extern.slf4j.Slf4j;
import org.eflerrr.client.client.dto.ChatInfo;
import org.eflerrr.client.configuration.ApplicationConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
@Slf4j
public class ChatListUpdateServerClient {

    private final String chatListEndpoint;
    private final WebClient webClient;

    @Autowired
    public ChatListUpdateServerClient(ApplicationConfig config) {
        this.webClient = WebClient.builder()
                .baseUrl(config.serverUrl())
                .build();
        this.chatListEndpoint = config.chatList().endpoint();
    }

    public List<ChatInfo> requestChatList() {
        log.info("Making a request to the server to get the chat list");
        var response = webClient.get()
                .uri(chatListEndpoint)
                .retrieve()
                .toEntityList(ChatInfo.class)
                .block();
        return response == null
                ? List.of()
                : response.getBody();
    }

}
