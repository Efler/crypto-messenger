package org.eflerrr.client.scheduler;

import com.vaadin.flow.component.ComponentEventBus;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.shared.Registration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eflerrr.client.client.ServerClient;
import org.eflerrr.client.model.ChatInfo;
import org.eflerrr.client.model.event.ChatUpdateEvent;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@EnableScheduling
@Slf4j
public class ChatListUpdateScheduler {

    private final ServerClient serverClient;
    private final ComponentEventBus eventBus = new ComponentEventBus(new Div());
    private boolean isRunning = false;
    private List<ChatInfo> previousList = null;

    @Scheduled(fixedRateString = "#{@server.chatList.updateInterval}")
    public void updateChatList() {
        if (isRunning) {
            log.info("Updating chat list in scheduler");
            var chats = serverClient.requestChatList();
            if (previousList == null || !previousList.equals(chats)) {
                log.info("Invoking an event for chat list update");
                previousList = chats;
                var event = new ChatUpdateEvent(chats);
                eventBus.fireEvent(event);
            }
        }
    }

    public Registration attachListener(ComponentEventListener<ChatUpdateEvent> listener) {
        return eventBus.addListener(ChatUpdateEvent.class, listener);
    }

    public void pause() {
        isRunning = false;
    }

    public void start() {
        isRunning = true;
    }

    public ChatListUpdateScheduler clearPreviousList() {
        previousList = null;
        return this;
    }

}
