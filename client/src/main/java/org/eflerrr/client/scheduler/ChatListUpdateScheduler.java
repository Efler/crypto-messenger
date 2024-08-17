package org.eflerrr.client.scheduler;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventBus;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.shared.Registration;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.eflerrr.client.client.ServerClient;
import org.eflerrr.client.dto.ChatInfo;
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

    @Getter
    @EqualsAndHashCode(callSuper = false)
    @ToString
    public static class ChatUpdateEvent extends ComponentEvent<Div> {

        private final List<ChatInfo> updatedChats;

        public ChatUpdateEvent(List<ChatInfo> updatedChats) {
            super(new Div(), false);
            this.updatedChats = updatedChats;
        }
    }

    @Scheduled(fixedRateString = "#{@chatList.updateInterval}")
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

    public ChatListUpdateScheduler pause() {
        isRunning = false;
        return this;
    }

    public ChatListUpdateScheduler start() {
        isRunning = true;
        return this;
    }

    public ChatListUpdateScheduler clearPreviousList() {
        previousList = null;
        return this;
    }

}
