package org.eflerrr.client.client.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.eflerrr.encrypt.types.EncryptionAlgorithm;

@Data
@Builder
public class CreateChatRequest {

    @NotBlank
    private String chatName;
    @NotNull
    private EncryptionAlgorithm encryptionAlgorithm;

}
