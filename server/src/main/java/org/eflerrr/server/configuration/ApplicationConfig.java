package org.eflerrr.server.configuration;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app-configs", ignoreUnknownFields = false)
public record ApplicationConfig(
        @NotBlank
        String host,

        @Min(1024)
        @Max(49151)
        int port,

        @Valid
        Clients clients,

        @Valid
        Kafka kafka,

        @Valid
        DiffieHellman diffieHellman
) {

    public record Clients(
            @NotNull
            Protocol protocol,

            @NotBlank
            String rootEndpoint,

            @NotBlank
            String publicKeyEndpoint,

            @NotBlank
            String notifyExitEndpoint
    ) {
        public enum Protocol {
            HTTP,
            HTTPS;

            @Override
            public String toString() {
                return name().toLowerCase();
            }
        }
    }

    public record Kafka(
            @NotBlank
            String bootstrapServers,

            @Min(1)
            short topicsReplicationFactor,

            @NotNull
            String topicsPostfix
    ) {
    }

    public record DiffieHellman(
            @Min(1)
            int bitLength
    ) {
    }

}
