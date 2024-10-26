package org.eflerrr.client.dto.response;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import org.eflerrr.client.model.ClientSettings;

import java.math.BigInteger;

@Data
@Builder
public class ExchangePublicKeyResponse {

    @Min(1)
    private BigInteger matePublicKey;
    @NotBlank
    private ClientSettings mateSettings;

}
