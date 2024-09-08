package org.eflerrr.client.configuration;


import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "app-configs", ignoreUnknownFields = false)
public record ApplicationConfig(
        @NotBlank
        String host,

        @Min(1024)
        @Max(49151)
        int port,

        @NotBlank
        String gatewayEndpoint,

        @Bean
        @Valid
        Server server,

        @Valid
        FileUpload fileUpload,

        @Valid
        Encryption encryption,

        @Valid
        Kafka kafka,

        @Valid
        Database database
) {

    public record Server(
            @NotNull
            Protocol protocol,

            @NotBlank
            String host,

            @Min(1024)
            @Max(49151)
            int port,

            @Valid
            Chat chat,

            @Valid
            ChatList chatList
    ) {
        public enum Protocol {
            HTTP,
            HTTPS;

            @Override
            public String toString() {
                return name().toLowerCase();
            }
        }

        public record Chat(
                @NotBlank
                String endpoint
        ) {
        }

        public record ChatList(
                @NotBlank
                String endpoint,

                @DurationMin(millis = 500)
                Duration updateInterval
        ) {
        }
    }

    public record FileUpload(
            boolean inMemory,

            @Min(1)
            @Max(15728640)  //  15 Mb
            int maxFileSize
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
            @Max(15728641)  //  15 Mb (file-max-size)  +  1 mb for message metadata  [producer prop]
            int maxRequestSize,

            @Min(1)
            @Max(15728641)  //  15 Mb (file-max-size)  +  1 mb for message metadata  [consumer prop]
            int fetchMaxBytes
    ) {
    }

    public record Database(
            Messages messages
    ) {
        public record Messages(
                boolean saveEncrypted
        ) {
        }
    }

}
