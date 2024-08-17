package org.eflerrr.server.client;

import lombok.RequiredArgsConstructor;
import org.eflerrr.encrypt.types.EncryptionMode;
import org.eflerrr.encrypt.types.PaddingType;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigInteger;

@Component
@RequiredArgsConstructor
public class HttpClient {

    private static final String PUBLIC_KEY_URL = "http://%s:%d/client-chat/public-key"; // TODO: replace to configs, no hardcode!
    private static final String NOTIFY_CLIENT_LEAVING_URL = "http://%s:%d/client-chat/client-leaving"; // TODO: replace to configs, no hardcode!
    private final WebClient webClient = WebClient.create();

    public BigInteger getPublicKey(
            String host, int port, String mateName, EncryptionMode mateMode, PaddingType matePadding) {
        return webClient.get()
                .uri(String.format(PUBLIC_KEY_URL, host, port))
                .header("Mate-Name", mateName)
                .header("Encryption-Mode", mateMode.name())
                .header("Padding-Type", matePadding.name())
                .retrieve()
                .onStatus(HttpStatusCode::is5xxServerError, response ->
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
