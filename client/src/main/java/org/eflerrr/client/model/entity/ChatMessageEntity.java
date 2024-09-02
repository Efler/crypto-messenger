package org.eflerrr.client.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.eflerrr.client.model.MessageType;
import org.springframework.validation.annotation.Validated;

import java.time.OffsetDateTime;

@Entity
@Table(name = "chat_message")
@Data
@Validated
public class ChatMessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message_data", nullable = false, columnDefinition = "BYTEA")
    private byte[] message;

    @Column(name = "message_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private MessageType messageType;

    @ManyToOne(
            fetch = FetchType.EAGER)
    @JoinColumn(name = "client_from_id", nullable = false)
    private ClientEntity clientFrom;

    @ManyToOne(
            fetch = FetchType.EAGER)
    @JoinColumn(name = "client_to_id", nullable = false)
    private ClientEntity clientTo;

    @ManyToOne(
            fetch = FetchType.EAGER)
    @JoinColumn(name = "chat_name", nullable = false)
    private ChatEntity chat;

    @Column(name = "sent_time", nullable = false)
    private OffsetDateTime sentTime;

    @Column(name = "file_name")
    private String filename;

    @Column(name = "file_mime_type")
    private String mimeType;


}
