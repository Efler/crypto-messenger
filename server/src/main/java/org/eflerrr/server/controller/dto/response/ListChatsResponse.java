package org.eflerrr.server.controller.dto.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.eflerrr.encrypt.types.EncryptionAlgorithm;

@Data
@Builder
public class ListChatsResponse {

    @NotBlank
    private String chatName;
    @NotNull
    private EncryptionAlgorithm encryptionAlgorithm;

}
