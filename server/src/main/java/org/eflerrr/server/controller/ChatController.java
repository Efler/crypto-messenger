package org.eflerrr.server.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.eflerrr.server.controller.dto.request.CreateChatRequest;
import org.eflerrr.server.service.ChatService;
import org.eflerrr.server.service.dto.ClientInfo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@Validated
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<Object> createChat(
            @Min(0)
            @RequestHeader(value = "Client-Id")
            Long clientId,
            @NotBlank
            @RequestHeader(value = "Client-Name")
            String clientName,
            @NotBlank
            @RequestHeader(value = "Client-Host")
            String clientHost,
            @Min(1)
            @RequestHeader(value = "Client-Port")
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
                            .publicKey(null)
                            .build());
            return ResponseEntity
                    .ok(chatServiceResponse);

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<Object> joinChat(
            @Min(0)
            @RequestHeader(value = "Client-Id")
            Long clientId,
            @NotBlank
            @RequestHeader(value = "Client-Name")
            String clientName,
            @NotBlank
            @RequestHeader(value = "Client-Host")
            String clientHost,
            @Min(1)
            @RequestHeader(value = "Client-Port")
            int clientPort,
            @NotBlank
            @RequestHeader(value = "Chat-Name")
            String chatName
    ) {
        try {
            var chatServiceResponse = chatService.joinChat(
                    chatName,
                    ClientInfo.builder()
                            .id(clientId)
                            .name(clientName)
                            .host(clientHost)
                            .port(clientPort)
                            .publicKey(null)
                            .build());
            return ResponseEntity
                    .ok(chatServiceResponse);

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(e.getMessage());
        } catch (IllegalCallerException e) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(e.getMessage());
        }
    }

    @PutMapping
    public ResponseEntity<Object> exchangePublicKey(
            @Min(0)
            @RequestHeader(value = "Client-Id")
            Long clientId,
            @NotBlank
            @RequestHeader(value = "Chat-Name")
            String chatName,

            @Min(0)
            @RequestBody
            BigInteger publicKey
    ) {
        try {
            var exchangedPublicKey = chatService.exchangePublicKey(
                    chatName,
                    clientId,
                    publicKey
            );
            return ResponseEntity
                    .ok(exchangedPublicKey);
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch (IllegalCallerException e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(e.getMessage());
        }
    }

    @DeleteMapping
    public ResponseEntity<Object> leaveChat(
            @Min(0)
            @RequestHeader(value = "Client-Id")
            Long clientId,
            @NotBlank
            @RequestHeader(value = "Chat-Name")
            String chatName
    ) {
        try {
            chatService.leaveChat(chatName, clientId);
            return ResponseEntity
                    .ok()
                    .build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch (IllegalCallerException e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(e.getMessage());
        }
    }

    @GetMapping("/list")
    public ResponseEntity<List<String>> listChats() {
        return ResponseEntity
                .ok(chatService.listChats());
    }


    @PostMapping("/checker")
    public void checker() {     // TODO: for debug only, remove in production
        chatService.checker();
    }

}
