package org.eflerrr.server.model;

import lombok.Data;
import org.eflerrr.encrypt.types.EncryptionMode;
import org.eflerrr.encrypt.types.PaddingType;

@Data
public class ClientSettings {

    private String clientName;
    private EncryptionMode encryptionMode;
    private PaddingType paddingType;
    private byte[] IV;
    private Boolean isCreator;
    private Long sessionId;

}
