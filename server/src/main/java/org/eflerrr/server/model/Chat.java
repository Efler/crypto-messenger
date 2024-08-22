package org.eflerrr.server.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.apache.kafka.clients.admin.NewTopic;
import org.eflerrr.encrypt.types.EncryptionAlgorithm;

import java.math.BigInteger;
import java.util.Map;

@Data
@Builder
public class Chat {

    @NotBlank
    private String name;

    @NotNull
    private EncryptionAlgorithm encryptionAlgorithm;

    private Map<Long, ClientInfo> clients;

    @Min(0)
    @Max(2)
    private int clientsCount;

    private Long creatorId;

    @NotNull
    private NewTopic kafkaTopic;

    @NotNull
    private BigInteger g;

    @NotNull
    private BigInteger p;

}
