package org.eflerrr.client.service;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventBus;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.shared.Registration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.eflerrr.client.configuration.ApplicationConfig;
import org.eflerrr.client.dao.ChatDao;
import org.eflerrr.client.dao.ClientDao;
import org.eflerrr.client.model.ClientSettings;
import org.eflerrr.client.model.entity.ChatMessage;
import org.eflerrr.client.model.event.IncomingMessageEvent;
import org.eflerrr.client.model.event.MateJoiningEvent;
import org.eflerrr.client.model.event.ReadyToChatEvent;
import org.eflerrr.client.model.event.ReceiveMatePublicKeyEvent;
import org.eflerrr.encrypt.manager.EncryptorManager;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ChatDao chatDao;
    private final ClientDao clientDao;
    private final ApplicationConfig config;
    private final ComponentEventBus eventBus = new ComponentEventBus(new Div());

    private EncryptorManager clientEncryptorManager;
    private EncryptorManager mateEncryptorManager;
    private KafkaProducer<String, ChatMessage> kafkaProducer;
    private KafkaConsumer<String, ChatMessage> kafkaConsumer;
    private final AtomicBoolean isConsuming = new AtomicBoolean(false);

    public <T extends ComponentEvent<?>> Registration attachListener(ComponentEventListener<T> listener, Class<T> eventType) {
        return eventBus.addListener(eventType, listener);
    }

    private void generateFinalKey() {
        var p = chatDao.getP();
        var privateKey = clientDao.getPrivateKey();
        var matePublicKey = chatDao.getMatePublicKey();

        chatDao.setFinalKey(
                matePublicKey.modPow(privateKey, p)
        );
    }

    private byte[] resizeFinalKey(int size) {
        var resized = new byte[size];
        var finalKeyBytes = chatDao.getFinalKey().toByteArray();
        for (int i = 0; i < size; i++) {
            resized[i] = finalKeyBytes[i % finalKeyBytes.length];
        }
        return resized;
    }

    public BigInteger getClientPublicKey() {
        var key = chatDao.getSelfPublicKey();
        if (key == null) {
            throw new IllegalStateException("Client has no public key!");
        }
        return key;
    }

    public void processMateJoining(ClientSettings mateSettings) {
        chatDao.setMateSettings(mateSettings);

        eventBus.fireEvent(new MateJoiningEvent(
                mateSettings.getClientName(),
                mateSettings.getEncryptionMode(),
                mateSettings.getPaddingType()
        ));
    }

    public void receiveMatePublicKey(BigInteger matePublicKey) {
        chatDao.setMatePublicKey(matePublicKey);
        eventBus.fireEvent(new ReceiveMatePublicKeyEvent());
    }

    public void setupEnvironment() {
        generateFinalKey();
        var resizedFinalKey = resizeFinalKey(chatDao.getEncryptionAlgorithm().getKeyLength());

        var selfSettings = chatDao.getSelfSettings();
        clientEncryptorManager = new EncryptorManager(
                resizedFinalKey,
                chatDao.getEncryptionAlgorithm(),
                selfSettings.getEncryptionMode(),
                selfSettings.getPaddingType(),
                selfSettings.getIV()
        );
        var mateSettings = chatDao.getMateSettings();
        mateEncryptorManager = new EncryptorManager(
                resizedFinalKey,
                chatDao.getEncryptionAlgorithm(),
                mateSettings.getEncryptionMode(),
                mateSettings.getPaddingType(),
                mateSettings.getIV()
        );

        kafkaProducer = new KafkaProducer<>(Map.of(
                "bootstrap.servers", chatDao.getKafkaInfo().getBootstrapServers(),
                "client.id", clientDao.getClientName(),
                "acks", "0",
                "max.request.size", String.valueOf(config.kafka().maxRequestSize())),
                new StringSerializer(), new JsonSerializer<>());

        var deserializer = new JsonDeserializer<>(ChatMessage.class);
        deserializer.configure(new HashMap<>() {{
            put(JsonDeserializer.TRUSTED_PACKAGES, "*");
            put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        }}, false);
        kafkaConsumer = new KafkaConsumer<>(Map.of(
                "bootstrap.servers", chatDao.getKafkaInfo().getBootstrapServers(),
                "client.id", clientDao.getClientName(),
                "group.id", clientDao.getClientId().toString(),
                "auto.offset.reset", "latest",
                "fetch.max.bytes", String.valueOf(config.kafka().fetchMaxBytes())),
                new StringDeserializer(), deserializer);
        startConsuming();

        eventBus.fireEvent(new ReadyToChatEvent());
    }

    public void sendMessage(ChatMessage chatMessage) {
        var messageBytes = chatMessage.getMessage();
        var encryptedMessage = clientEncryptorManager.encrypt(messageBytes);
        chatMessage.setMessage(encryptedMessage);
        chatMessage.setEncrypted(true);
        kafkaProducer.send(
                new ProducerRecord<>(chatDao.getKafkaInfo().getTopic(), chatMessage));
    }

    public void showMessage(ChatMessage chatMessage) {
        var encryptedMessage = chatMessage.getMessage();
        var decryptedMessage = chatMessage.getClientId() == clientDao.getClientId()
                ? clientEncryptorManager.decrypt(encryptedMessage)
                : mateEncryptorManager.decrypt(encryptedMessage);
        chatMessage.setMessage(decryptedMessage);
        chatMessage.setEncrypted(false);
        eventBus.fireEvent(new IncomingMessageEvent(chatMessage));
    }

    public ChatMessage.MessageType resolveMessageType(String fileMIMEType) {
        return fileMIMEType.startsWith("image/")
                ? ChatMessage.MessageType.IMAGE
                : ChatMessage.MessageType.TEXT;
    }

    private void startConsuming() {
        isConsuming.set(true);
        kafkaConsumer.subscribe(Collections.singleton(chatDao.getKafkaInfo().getTopic()));
        new Thread(() -> {
            log.info("Starting consumer...");
            try {
                while (isConsuming.get()) {
                    ConsumerRecords<String, ChatMessage> records =
                            kafkaConsumer.poll(Duration.ofMillis(100));
                    for (var record : records) {
                        showMessage(record.value());
                    }
                }
            } finally {
                log.info("Stopping consumer...");
                kafkaConsumer.close();
            }
        }).start();
    }

    public void stopConsuming() {
        isConsuming.set(false);
    }

}
