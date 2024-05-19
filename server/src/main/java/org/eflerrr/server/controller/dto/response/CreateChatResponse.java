package org.eflerrr.server.controller.dto.response;

import jakarta.validation.Valid;
import lombok.Builder;
import lombok.Data;
import org.eflerrr.server.controller.dto.DiffieHellmanParams;
import org.eflerrr.server.controller.dto.KafkaInfo;
import org.springframework.validation.annotation.Validated;

@Data
@Builder
@Validated
public class CreateChatResponse {

    @Valid
    private DiffieHellmanParams diffieHellmanParams;
    @Valid
    private KafkaInfo kafkaInfo;

}
