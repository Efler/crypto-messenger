package org.eflerrr.server.controller.dto.response;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.eflerrr.encrypt.types.EncryptionMode;
import org.eflerrr.encrypt.types.PaddingType;

import java.math.BigInteger;

@Data
@Builder
public class ExchangePublicKeyResponse {

    @Min(1)
    private BigInteger matePublicKey;
    @NotBlank
    private String mateName;
    @NotNull
    private EncryptionMode mateMode;
    @NotNull
    private PaddingType matePadding;

}
