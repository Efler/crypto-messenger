package org.eflerrr.server.controller.dto.response;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.eflerrr.encrypt.types.EncryptionAlgorithm;
import org.eflerrr.server.controller.dto.DiffieHellmanParams;
import org.eflerrr.server.controller.dto.KafkaInfo;
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

}
