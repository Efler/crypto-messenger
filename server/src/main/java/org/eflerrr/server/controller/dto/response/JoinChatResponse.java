package org.eflerrr.server.controller.dto.response;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.eflerrr.server.controller.dto.DiffieHellmanParams;
import org.eflerrr.server.controller.dto.KafkaInfo;
import org.eflerrr.server.service.ChatService;
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
    private ChatService.CipherAlgorithm cipherAlgorithm;

}
