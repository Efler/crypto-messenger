package org.eflerrr.client.dto.response;

import jakarta.validation.Valid;
import lombok.Builder;
import lombok.Data;
import org.eflerrr.client.model.DiffieHellmanParams;
import org.eflerrr.client.model.KafkaInfo;
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
