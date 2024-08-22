package org.eflerrr.server.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.math.BigInteger;

@Data
@Builder
public class ClientInfo {

    @Min(0)
    private Long id;
    @NotBlank
    private String host;
    @Min(1)
    private int port;
    @Min(1)
    private BigInteger publicKey;
    @NotNull
    private ClientSettings settings;

}
