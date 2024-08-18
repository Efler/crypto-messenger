package org.eflerrr.server.service.dto;

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
public class ClientInfo {

    @Min(0)
    private Long id;
    @NotBlank
    private String name;
    @NotBlank
    private String host;
    @Min(1)
    private int port;
    @Min(1)
    private BigInteger publicKey;

    private EncryptionMode encryptionMode;
    private PaddingType paddingType;

}
