package org.eflerrr.client.controller;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.eflerrr.client.model.ClientSettings;
import org.eflerrr.client.service.ChatService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;

@RestController
@RequestMapping("/client-chat")
@RequiredArgsConstructor
@Validated
public class ClientChatController {

    private final ChatService chatService;

    @PutMapping("/public-key")
    public ResponseEntity<Object> publicKey(
            @NotNull
            @RequestBody
            ClientSettings mateSettings
    ) {
        chatService.processMateJoining(mateSettings);
        try {
            return ResponseEntity
                    .ok(chatService.getClientPublicKey());
        } catch (IllegalStateException e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }

    @PostMapping("/public-key")
    public void receiveMatePublicKey(
            @Min(1)
            @RequestBody
            BigInteger matePublicKey
    ) {
        chatService.receiveMatePublicKey(matePublicKey);
    }

}
