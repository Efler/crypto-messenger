package org.eflerrr.client.dao;

import lombok.Data;
import org.eflerrr.client.model.ClientSettings;
import org.eflerrr.client.model.KafkaInfo;
import org.eflerrr.client.model.entity.ChatEntity;
import org.eflerrr.encrypt.types.EncryptionAlgorithm;
import org.springframework.stereotype.Component;

import java.math.BigInteger;

@Component
@Data
public class ChatDao {

    private String chatName;
    private ClientSettings selfSettings;
    private ClientSettings mateSettings;
    private EncryptionAlgorithm encryptionAlgorithm;
    private BigInteger selfPublicKey;
    private BigInteger matePublicKey;
    private BigInteger finalKey;
    private BigInteger g;
    private BigInteger p;
    private KafkaInfo kafkaInfo;
    private ChatEntity chatEntity;

    public void clear() {
        chatName = null;
        selfSettings = null;
        mateSettings = null;
        encryptionAlgorithm = null;
        selfPublicKey = null;
        matePublicKey = null;
        finalKey = null;
        g = null;
        p = null;
        kafkaInfo = null;
        chatEntity = null;
    }

}
