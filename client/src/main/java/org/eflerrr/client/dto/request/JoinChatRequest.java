package org.eflerrr.client.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.eflerrr.client.model.ClientSettings;

@Data
@Builder
public class JoinChatRequest {

    @NotBlank
    private String chatName;
    @NotNull
    private ClientSettings clientSettings;

}
