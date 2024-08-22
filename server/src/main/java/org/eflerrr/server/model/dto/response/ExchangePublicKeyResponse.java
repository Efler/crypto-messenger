package org.eflerrr.server.model.dto.response;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.eflerrr.server.model.ClientSettings;

import java.math.BigInteger;

@Data
@Builder
public class ExchangePublicKeyResponse {

    @Min(1)
    private BigInteger matePublicKey;
    @NotNull
    private ClientSettings mateSettings;

}
