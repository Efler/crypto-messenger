package org.eflerrr.client.configuration;


import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app-configs", ignoreUnknownFields = false)
public record ApplicationConfig(

        @NotBlank
        String serverUrl

) {
}
