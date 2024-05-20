package org.eflerrr.server.configuration;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.AdminClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
@RequiredArgsConstructor
public class AdminClientConfig {

    @Value("${spring.kafka.admin.client-id}")
    private String kafkaAdminClientId;
    private final ApplicationConfig config;

    @Bean
    public AdminClient adminClient() {
        var props = new Properties();
        props.put("bootstrap.servers", config.kafka().bootstrapServers());
        props.put("client.id", kafkaAdminClientId + "-topics-deleter");

        return AdminClient.create(props);
    }

}
