package org.eflerrr.client.model.event;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.html.Div;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.eflerrr.client.model.ChatMessage;

@Value
@EqualsAndHashCode(callSuper = false)
public class IncomingMessageEvent extends ComponentEvent<Div> {

    ChatMessage chatMessage;

    public IncomingMessageEvent(ChatMessage chatMessage) {
        super(new Div(), false);
        this.chatMessage = chatMessage;
    }

}
