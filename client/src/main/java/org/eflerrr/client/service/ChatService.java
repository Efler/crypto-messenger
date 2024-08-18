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
import org.eflerrr.client.model.entity.ChatMessage;
import org.eflerrr.client.model.event.IncomingMessageEvent;
import org.eflerrr.client.model.event.MateJoiningEvent;
import org.eflerrr.client.model.event.ReadyToChatEvent;
import org.eflerrr.client.model.event.ReceiveMatePublicKeyEvent;
import org.eflerrr.client.util.BytesSerializer;
import org.eflerrr.encrypt.manager.EncryptorManager;
import org.eflerrr.encrypt.types.EncryptionMode;
import org.eflerrr.encrypt.types.PaddingType;
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

    private final ApplicationConfig config;
    private final ChatDao chatDao;
    private final ClientDao clientDao;
    private final ComponentEventBus eventBus = new ComponentEventBus(new Div());

    private final byte[] IV = new byte[]{   // TODO: MAKE FROM UI!
            (byte) 0x04, (byte) 0x01, (byte) 0x02, (byte) 0x1C,
            (byte) 0xF4, (byte) 0x65, (byte) 0x83, (byte) 0x07,
            (byte) 0xAA, (byte) 0x09, (byte) 0x1A, (byte) 0x0B,
            (byte) 0x00, (byte) 0xDD, (byte) 0x8E, (byte) 0x0F
    };
    private EncryptorManager clientEncryptorManager;
    private EncryptorManager mateEncryptorManager;
    private KafkaProducer<String, ChatMessage> kafkaProducer;
    private KafkaConsumer<String, ChatMessage> kafkaConsumer;
    private final AtomicBoolean isConsuming = new AtomicBoolean(false);

    public <T extends ComponentEvent<?>> Registration attachListener(ComponentEventListener<T> listener, Class<T> eventType) {
        return eventBus.addListener(eventType, listener);
    }

    public BigInteger getClientPublicKey() {
        if (clientDao.getPublicKey() == null) {
            throw new IllegalStateException("Client has no public key!");
        }
        return clientDao.getPublicKey();
    }

    public void processMateJoining(
            String mateName, EncryptionMode mateMode, PaddingType matePadding) {
        chatDao.setMateName(mateName);
        chatDao.setMateEncryptionMode(mateMode);
        chatDao.setMatePaddingType(matePadding);

        eventBus.fireEvent(new MateJoiningEvent(
                mateName, mateMode, matePadding
        ));
    }

    public void receiveMatePublicKey(BigInteger matePublicKey) {
        chatDao.setMatePublicKey(matePublicKey);
        eventBus.fireEvent(new ReceiveMatePublicKeyEvent());
    }

    public void generateFinalKey() {
        var p = chatDao.getP();
        var privateKey = clientDao.getPrivateKey();
        var matePublicKey = chatDao.getMatePublicKey();

        clientDao.setFinalKey(
                matePublicKey.modPow(privateKey, p)
        );
        // TODO!
        var tmp = new byte[16];
        var finalKeyArr = clientDao.getFinalKey().toByteArray();
        for (int i = 0; i < 16; i++) {
            tmp[i] = finalKeyArr[i % finalKeyArr.length];
        }
        log.warn("tmp = {}", tmp);

        clientEncryptorManager = new EncryptorManager(
//                clientDao.getFinalKey().toByteArray(),
                tmp, // TODO!
                chatDao.getEncryptionAlgorithm(),
                chatDao.getEncryptionMode(),
                chatDao.getPaddingType(),
                IV
        );
        mateEncryptorManager = new EncryptorManager(
//                clientDao.getFinalKey().toByteArray(),
                tmp, // TODO!
                chatDao.getEncryptionAlgorithm(),
                chatDao.getMateEncryptionMode(),
                chatDao.getMatePaddingType(),
                IV
        );

        kafkaProducer = new KafkaProducer<>(Map.of(
                "bootstrap.servers", chatDao.getKafkaInfo().getBootstrapServers(),
                "client.id", clientDao.getClientName(),
                "acks", "0"),
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
                "auto.offset.reset", "latest"),
                new StringDeserializer(), deserializer);
        startConsuming();

        eventBus.fireEvent(new ReadyToChatEvent());
    }

    public void sendMessage(ChatMessage chatMessage) {
        var messageBytes = chatMessage.getMessage();
        var encryptedMessage = clientEncryptorManager.encryptAsync(
                messageBytes, config.encryption().threadsCount());
        chatMessage.setMessage(encryptedMessage);
        chatMessage.setEncrypted(true);
        kafkaProducer.send(
                new ProducerRecord<>(chatDao.getKafkaInfo().getTopic(), chatMessage));
    }

    public void showMessage(ChatMessage chatMessage) {
        var encryptedMessage = chatMessage.getMessage();
        var decryptedMessage = chatMessage.getClientId() == clientDao.getClientId()
                ? clientEncryptorManager.decryptAsync(encryptedMessage, config.encryption().threadsCount())
                : mateEncryptorManager.decryptAsync(encryptedMessage, config.encryption().threadsCount());
        chatMessage.setMessage(decryptedMessage);
        chatMessage.setEncrypted(false);
        eventBus.fireEvent(new IncomingMessageEvent(chatMessage));
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
