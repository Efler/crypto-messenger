package org.eflerrr.client.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.eflerrr.encrypt.types.EncryptionAlgorithm;

@Getter
@Setter
@EqualsAndHashCode
@Builder
public class ChatInfo {

    @NotBlank
    private String chatName;
    @NotNull
    private EncryptionAlgorithm encryptionAlgorithm;

}
