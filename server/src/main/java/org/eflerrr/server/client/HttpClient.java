package org.eflerrr.server.client;

import lombok.RequiredArgsConstructor;
import org.eflerrr.server.configuration.ApplicationConfig;
import org.eflerrr.server.model.ClientSettings;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigInteger;

@Component
@RequiredArgsConstructor
public class HttpClient {

    private static final String CLIENT_URL_TEMPLATE = "%s://%s:%d%s%s";
    private final WebClient webClient = WebClient.create();
    private final ApplicationConfig config;

    private enum ClientRequestType {
        PUBLIC_KEY,
        NOTIFY_EXIT
    }

    private String buildUri(String host, int port, ClientRequestType type) {
        return String.format(CLIENT_URL_TEMPLATE,
                config.clients().protocol(),
                host,
                port,
                config.clients().rootEndpoint(),
                switch (type) {
                    case PUBLIC_KEY -> config.clients().publicKeyEndpoint();
                    case NOTIFY_EXIT -> config.clients().notifyExitEndpoint();
                });
    }

    public BigInteger getPublicKey(
            String host, int port, ClientSettings mateSettings) {
        return webClient.put()
                .uri(buildUri(host, port, ClientRequestType.PUBLIC_KEY))
                .bodyValue(mateSettings)
                .retrieve()
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        Mono.error(new IllegalStateException("Client does not have public key")))
                .bodyToMono(BigInteger.class)
                .block();
    }

    public void sendPublicKey(String host, int port, BigInteger matePublicKey) {
        webClient.post()
                .uri(buildUri(host, port, ClientRequestType.PUBLIC_KEY))
                .bodyValue(matePublicKey)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        Mono.error(new IllegalStateException("Client was not awaiting public key")))
                .bodyToMono(Void.class)
                .block();
    }

    public void notifyClientExit(String host, int port) {
        webClient.delete()
                .uri(buildUri(host, port, ClientRequestType.NOTIFY_EXIT))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        Mono.error(new IllegalStateException("Client error occurred: " + response.statusCode()))
                )
                .bodyToMono(Void.class)
                .block();
    }

}
