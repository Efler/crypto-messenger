package org.eflerrr.client.dto.response;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.eflerrr.client.model.DiffieHellmanParams;
import org.eflerrr.client.model.KafkaInfo;
import org.eflerrr.encrypt.types.EncryptionAlgorithm;
import org.springframework.validation.annotation.Validated;

@Data
@Builder
@Validated
public class JoinChatResponse {

    @Valid
    private DiffieHellmanParams diffieHellmanParams;
    @Valid
    private KafkaInfo kafkaInfo;
    @NotNull
    private EncryptionAlgorithm encryptionAlgorithm;
    @NotBlank
    private String mateName;

}
