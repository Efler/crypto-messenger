package org.eflerrr.server.client;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigInteger;

@Component
@RequiredArgsConstructor
public class HttpClient {

    private static final String PUBLIC_KEY_URL = "http://%s:%d/chat/public-key";
    private static final String NOTIFY_CLIENT_LEAVING_URL = "http://%s:%d/chat/client-leaving";
    private final WebClient webClient = WebClient.create();

    public BigInteger getPublicKey(String host, int port) {
        return webClient.get()
                .uri(String.format(PUBLIC_KEY_URL, host, port))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        Mono.error(new IllegalStateException("Client does not have public key")))
                .bodyToMono(BigInteger.class)
                .block();
    }

    public void sendPublicKey(String host, int port, BigInteger publicKey) {
        webClient.post()
                .uri(String.format(PUBLIC_KEY_URL, host, port))
                .bodyValue(publicKey)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        Mono.error(new IllegalStateException("Client was not awaiting public key")))
                .bodyToMono(Void.class)
                .block();
    }

    public void notifyClientLeaving(String host, int port) {
        webClient.delete()
                .uri(String.format(NOTIFY_CLIENT_LEAVING_URL, host, port))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        Mono.error(new IllegalStateException("Client error occurred: " + response.statusCode()))
                )
                .bodyToMono(Void.class)
                .block();
    }

}
