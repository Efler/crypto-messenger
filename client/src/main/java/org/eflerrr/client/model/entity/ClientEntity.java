package org.eflerrr.client.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.eflerrr.encrypt.types.EncryptionMode;
import org.eflerrr.encrypt.types.PaddingType;
import org.springframework.validation.annotation.Validated;

@Entity
@Table(name = "client")
@Data
@Validated
public class ClientEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id")
    private Long sessionId;

    @Column(name = "client_name", nullable = false)
    private String clientName;

    @Column(name = "encryption_mode", nullable = false)
    @Enumerated(EnumType.STRING)
    private EncryptionMode encryptionMode;

    @Column(name = "padding_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private PaddingType paddingType;

    @Column(name = "iv", nullable = false, columnDefinition = "BYTEA")
    private byte[] IV;

}
