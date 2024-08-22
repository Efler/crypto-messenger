package org.eflerrr.server.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.eflerrr.encrypt.types.EncryptionAlgorithm;
import org.eflerrr.server.model.ClientSettings;

@Data
@Builder
public class CreateChatRequest {

    @NotBlank
    private String chatName;
    @NotNull
    private EncryptionAlgorithm encryptionAlgorithm;
    @NotNull
    private ClientSettings clientSettings;

}
