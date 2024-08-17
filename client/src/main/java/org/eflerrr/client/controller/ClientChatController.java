package org.eflerrr.client.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.eflerrr.client.service.ChatService;
import org.eflerrr.encrypt.types.EncryptionMode;
import org.eflerrr.encrypt.types.PaddingType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/client-chat")
@RequiredArgsConstructor
@Validated
public class ClientChatController {

    private final ChatService chatService;

    @GetMapping("/public-key")
    public ResponseEntity<Object> publicKey(
            @NotBlank
            @RequestHeader(value = "Mate-Name")
            String mateName,
            @NotNull
            @RequestHeader(value = "Encryption-Mode")
            EncryptionMode mateMode,
            @NotNull
            @RequestHeader(value = "Padding-Type")
            PaddingType matePadding
    ) {
        chatService.processMateJoining(mateName, mateMode, matePadding);
        try {
            return ResponseEntity
                    .ok(chatService.getClientPublicKey());
        } catch (IllegalStateException e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }

}
