package org.eflerrr.client.model.entity;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;

//@Entity todo: make integration with DB
@Data
@NoArgsConstructor
@AllArgsConstructor
@Validated
public class ChatMessage {

    public enum MessageType {
        TEXT,
        IMAGE,
        FILE
    }

    @NotNull
    private byte[] message;

    private boolean isEncrypted;
    @NotNull
    private MessageType messageType;
    @Min(1)
    private long clientId;
    @NotNull
    private Optional<String> filename;
    @NotNull
    private Optional<String> mimeType;


}
