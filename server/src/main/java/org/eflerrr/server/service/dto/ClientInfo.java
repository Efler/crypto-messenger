package org.eflerrr.server.service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

import java.math.BigInteger;

@Data
@Builder
public class ClientInfo {

    @Min(0)
    private Long id;
    @NotBlank
    private String name;
    @NotBlank
    private String host;
    @Min(1)
    private int port;
    private BigInteger publicKey;

}
