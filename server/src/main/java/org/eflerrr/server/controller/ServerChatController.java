package org.eflerrr.server.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.eflerrr.encrypt.types.EncryptionMode;
import org.eflerrr.encrypt.types.PaddingType;
import org.eflerrr.server.controller.dto.request.CreateChatRequest;
import org.eflerrr.server.controller.dto.response.ListChatsResponse;
import org.eflerrr.server.service.ChatService;
import org.eflerrr.server.service.dto.ClientInfo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.util.List;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@Validated
public class ServerChatController {

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
                    createChatRequest.getEncryptionAlgorithm(),
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
            String chatName,
            @NotNull
            @RequestHeader(value = "Encryption-Mode")
            EncryptionMode mode,
            @NotNull
            @RequestHeader(value = "Padding-Type")
            PaddingType padding
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
                            .build(),
                    mode, padding);
            return ResponseEntity
                    .ok(chatServiceResponse);

        } catch (InvalidKeyException e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
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
    public ResponseEntity<List<ListChatsResponse>> listChats() {
        var chatList = chatService.listChats();
        List<ListChatsResponse> responseList = chatList.isEmpty()
                ? List.of() : chatList.stream()
                .map(chat -> ListChatsResponse.builder()
                        .chatName(chat.getKey())
                        .encryptionAlgorithm(chat.getValue())
                        .build())
                .toList();
        return ResponseEntity
                .ok(responseList);
    }


    @PostMapping("/checker")
    public void checker() {     // TODO: for debug only, remove in production
        chatService.checker();
    }

}
