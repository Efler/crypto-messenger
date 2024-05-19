package org.eflerrr.server.controller.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.math.BigInteger;

@Data
@Builder
public class DiffieHellmanParams {

    @NotNull
    private BigInteger g;
    @NotNull
    private BigInteger p;

}
