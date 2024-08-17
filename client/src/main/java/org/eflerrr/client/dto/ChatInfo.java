package org.eflerrr.client.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
@Builder
public class ChatInfo {

    @NotBlank
    private String chatName;
    @NotBlank
    private String encryptionAlgorithm;

}
