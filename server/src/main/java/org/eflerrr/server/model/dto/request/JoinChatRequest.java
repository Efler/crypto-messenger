package org.eflerrr.server.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.eflerrr.server.model.ClientSettings;

@Data
@Builder
public class JoinChatRequest {

    @NotBlank
    private String chatName;
    @NotNull
    private ClientSettings clientSettings;

}
