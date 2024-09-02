package org.eflerrr.client.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.eflerrr.encrypt.types.EncryptionMode;
import org.eflerrr.encrypt.types.PaddingType;
import org.springframework.validation.annotation.Validated;

import java.util.HashSet;
import java.util.Set;

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

//    TODO!!!
//    @ToString.Exclude
//    @EqualsAndHashCode.Exclude()
//    @OneToMany(
//            mappedBy = "clientFrom",
//            fetch = FetchType.EAGER)
//    private Set<ChatMessageEntity> sentMessages = new HashSet<>();
//
//    @ToString.Exclude
//    @EqualsAndHashCode.Exclude()
//    @OneToMany(
//            mappedBy = "clientTo",
//            fetch = FetchType.EAGER)
//    private Set<ChatMessageEntity> receivedMessages = new HashSet<>();
//
//    public void addSentMessage(ChatMessageEntity message) {
//        message.setClientFrom(this);
//        sentMessages.add(message);
//    }
//
//    public void addReceivedMessage(ChatMessageEntity message) {
//        message.setClientTo(this);
//        sentMessages.add(message);
//    }

}
