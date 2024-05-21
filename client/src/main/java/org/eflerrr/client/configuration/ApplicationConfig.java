package org.eflerrr.client.configuration;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "app-configs", ignoreUnknownFields = false)
public record ApplicationConfig(

        @NotBlank
        String serverUrl,
        @Bean
        @Valid
        ChatList chatList

) {

    public record ChatList(

            //@Min(200) TODO! uncomment this line
            Duration updateInterval,
            @NotBlank
            String endpoint

    ) {
    }

}
