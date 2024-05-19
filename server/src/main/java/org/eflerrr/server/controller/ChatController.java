package org.eflerrr.server.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.eflerrr.server.controller.dto.DiffieHellmanParams;
import org.eflerrr.server.controller.dto.KafkaInfo;
import org.eflerrr.server.controller.dto.request.CreateChatRequest;
import org.eflerrr.server.controller.dto.response.CreateChatResponse;
import org.eflerrr.server.service.ChatService;
import org.eflerrr.server.service.dto.ClientInfo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@Validated
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<Object> createChat(
            @Min(0)
            @RequestHeader(value = "Client-Id", required = true)
            Long clientId,
            @NotBlank
            @RequestHeader(value = "Client-Name", required = true)
            String clientName,
            @NotBlank
            @RequestHeader(value = "Client-Host", required = true)
            String clientHost,
            @Min(1)
            @RequestHeader(value = "Client-Port", required = true)
            int clientPort,

            @Valid
            @RequestBody
            CreateChatRequest createChatRequest
    ) {
        try {
            var chatServiceResponse = chatService.createChat(
                    createChatRequest.getChatName(),
                    createChatRequest.getCipherAlgorithm(),
                    ClientInfo.builder()
                            .id(clientId)
                            .name(clientName)
                            .host(clientHost)
                            .port(clientPort)
                            .build()
            );
            var generatedParams = chatServiceResponse.getLeft();
            var kafkaInfo = chatServiceResponse.getRight();
            return ResponseEntity.ok(CreateChatResponse.builder()
                    .diffieHellmanParams(DiffieHellmanParams.builder()
                            .g(generatedParams.getLeft())
                            .p(generatedParams.getRight())
                            .build())
                    .kafkaInfo(KafkaInfo.builder()
                            .bootstrapServers(kafkaInfo.getLeft())
                            .topic(kafkaInfo.getRight())
                            .build())
                    .build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(e.getMessage());
        }
    }


    @PostMapping("/checker")
    public void checker() {     // TODO: for debug only, remove in production
        chatService.checker();
    }

}
