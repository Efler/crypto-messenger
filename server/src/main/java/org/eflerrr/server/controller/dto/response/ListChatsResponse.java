package org.eflerrr.server.controller.dto.response;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ListChatsResponse {

    @NotBlank
    private String chatName;
    @NotBlank
    private String cipherAlgorithm;

}
