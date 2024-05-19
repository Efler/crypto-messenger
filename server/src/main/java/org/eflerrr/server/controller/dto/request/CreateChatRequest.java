package org.eflerrr.server.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.eflerrr.server.service.ChatService;

@Data
@Builder
public class CreateChatRequest {

    @NotBlank
    private String chatName;
    @NotNull
    private ChatService.CipherAlgorithm cipherAlgorithm;

}
