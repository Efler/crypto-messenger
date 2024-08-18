package org.eflerrr.client.dao;

import lombok.Data;
import org.eflerrr.client.model.KafkaInfo;
import org.eflerrr.encrypt.types.EncryptionAlgorithm;
import org.eflerrr.encrypt.types.EncryptionMode;
import org.eflerrr.encrypt.types.PaddingType;
import org.springframework.stereotype.Component;

import java.math.BigInteger;

@Component
@Data
public class ChatDao {
    private String chatName;
    private String mateName;
    private EncryptionAlgorithm encryptionAlgorithm;
    private EncryptionMode encryptionMode;
    private EncryptionMode mateEncryptionMode;
    private PaddingType paddingType;
    private PaddingType matePaddingType;
    private BigInteger matePublicKey;
    private BigInteger g;
    private BigInteger p;
    private KafkaInfo kafkaInfo;
}
