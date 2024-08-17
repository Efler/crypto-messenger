package org.eflerrr.client.service;

import com.vaadin.flow.component.ComponentEventBus;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.shared.Registration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eflerrr.client.dao.ChatDao;
import org.eflerrr.client.dao.ClientDao;
import org.eflerrr.client.model.event.MateJoiningEvent;
import org.eflerrr.encrypt.types.EncryptionMode;
import org.eflerrr.encrypt.types.PaddingType;
import org.springframework.stereotype.Service;

import java.math.BigInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ChatDao chatDao;
    private final ClientDao clientDao;
    private final ComponentEventBus eventBus = new ComponentEventBus(new Div());

    public Registration attachListener(ComponentEventListener<MateJoiningEvent> listener) {
        return eventBus.addListener(MateJoiningEvent.class, listener);
    }

    public BigInteger getClientPublicKey() {
        if (clientDao.getPublicKey() == null) {
            throw new IllegalStateException("Client has no public key!");
        }
        return clientDao.getPublicKey();
    }

    public void processMateJoining(
            String mateName, EncryptionMode mateMode, PaddingType matePadding) {
        eventBus.fireEvent(new MateJoiningEvent(
                mateName, mateMode, matePadding
        ));
    }

}
