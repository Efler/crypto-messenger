package org.eflerrr.client.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.eflerrr.encrypt.types.EncryptionAlgorithm;
import org.springframework.validation.annotation.Validated;

@Entity
@Table(name = "chat")
@Data
@Validated
public class ChatEntity {

    @Id
    @Column(name = "chat_name", nullable = false)
    private String chatName;

    @Column(name = "encryption_algorithm", nullable = false)
    @Enumerated(EnumType.STRING)
    private EncryptionAlgorithm encryptionAlgorithm;

}
