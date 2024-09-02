package org.eflerrr.client.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.eflerrr.encrypt.types.EncryptionAlgorithm;
import org.springframework.validation.annotation.Validated;

import java.util.HashSet;
import java.util.Set;

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

//    TODO!!!
//    @ToString.Exclude
//    @EqualsAndHashCode.Exclude()
//    @OneToMany(
//            mappedBy = "chat",
//            fetch = FetchType.EAGER)
//    private Set<ChatMessageEntity> messages = new HashSet<>();
//
//    public void addMessage(ChatMessageEntity message) {
//        message.setChat(this);
//        messages.add(message);
//    }

}
