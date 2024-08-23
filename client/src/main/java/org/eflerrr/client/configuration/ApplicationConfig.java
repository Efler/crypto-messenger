package org.eflerrr.client.configuration;


import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
        ChatList chatList,
        @NotBlank
        String chatEndpoint,
        @Valid
        Server server,
        @Valid
        FileUpload fileUpload,
        @Valid
        Encryption encryption,
        @Valid
        Kafka kafka
) {

    public record ChatList(
            //@Min(200) TODO! uncomment this line
            Duration updateInterval,
            @NotBlank
            String endpoint
    ) {
    }

    public record Server(
            @NotBlank
            String host,
            @Min(1024)
            @Max(49151)
            int port,
            @NotBlank
            String gatewayUrl
    ) {
    }

    public record FileUpload(
            boolean inMemory,
            @Min(1)
            @Max(10485760)  //  10 Mb
            int fileMaxSize
    ) {
    }

    public record Encryption(
            @Min(1)
            @Max(1024)
            int privateKeyBitLength
    ) {
    }

    public record Kafka(
            @Min(1)
            @Max(10485761)  //  10 Mb (file-max-size)  +  1 mb for message metadata  [producer prop]
            int maxRequestSize,
            @Min(1)
            @Max(10485761)  //  10 Mb (file-max-size)  +  1 mb for message metadata  [consumer prop]
            int fetchMaxBytes
    ) {
    }

}
