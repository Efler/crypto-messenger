package org.eflerrr.server.model.dto.response;

import jakarta.validation.Valid;
import lombok.Builder;
import lombok.Data;
import org.eflerrr.server.model.DiffieHellmanParams;
import org.eflerrr.server.model.KafkaInfo;
import org.springframework.validation.annotation.Validated;

@Data
@Builder
@Validated
public class JoinChatResponse {

    @Valid
    private DiffieHellmanParams diffieHellmanParams;
    @Valid
    private KafkaInfo kafkaInfo;

}
