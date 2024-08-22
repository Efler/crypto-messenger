package org.eflerrr.server.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.eflerrr.server.model.ClientInfo;
import org.eflerrr.server.model.dto.request.CreateChatRequest;
import org.eflerrr.server.model.dto.request.JoinChatRequest;
import org.eflerrr.server.model.dto.response.ListChatsResponse;
import org.eflerrr.server.service.ChatService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.util.List;

import static org.eflerrr.utils.Utils.bytesToBinaryString;
import static org.eflerrr.utils.Utils.bytesToHexString;

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
            @RequestHeader(value = "Client-Host")
            String clientHost,
            @Min(1)
            @Max(65535)
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
                            .settings(createChatRequest.getClientSettings())
                            .id(clientId)
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

    @PatchMapping
    public ResponseEntity<Object> joinChat(
            @Min(0)
            @RequestHeader(value = "Client-Id")
            Long clientId,
            @NotBlank
            @RequestHeader(value = "Client-Host")
            String clientHost,
            @Min(1)
            @Max(65535)
            @RequestHeader(value = "Client-Port")
            int clientPort,

            @NotNull
            @RequestBody
            JoinChatRequest joinChatRequest
    ) {
        try {
            var chatServiceResponse = chatService.joinChat(
                    joinChatRequest.getChatName(),
                    ClientInfo.builder()
                            .settings(joinChatRequest.getClientSettings())
                            .id(clientId)
                            .host(clientHost)
                            .port(clientPort)
                            .publicKey(null)
                            .build());
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

            @Min(1)
            @RequestBody
            BigInteger publicKey
    ) {
        try {
            var exchangeResponse = chatService.exchangePublicKey(
                    chatName,
                    clientId,
                    publicKey
            );
            return ResponseEntity
                    .ok(exchangeResponse);
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

    @GetMapping("/is-registered/{chat-name}")
    public ResponseEntity<Boolean> isChatRegistered(
            @NotBlank
            @PathVariable("chat-name")
            String chatName
    ) {
        return ResponseEntity
                .ok(chatService.isChatRegistered(chatName));
    }

    @PostMapping("/checker")        // TODO: for debug only, remove in production
    public void checker() {     // TODO: for debug only, remove in production
        chatService.checker();
    }

}
