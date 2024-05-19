package org.eflerrr.server.controller.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KafkaInfo {

    @NotBlank
    String bootstrapServers;
    @NotBlank
    String topic;

}
