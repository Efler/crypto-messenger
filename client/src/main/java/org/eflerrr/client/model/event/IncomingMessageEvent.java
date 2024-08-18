package org.eflerrr.client.model.event;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.html.Div;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.eflerrr.client.model.entity.ChatMessage;

@Getter
@EqualsAndHashCode(callSuper = false)
@ToString
public class IncomingMessageEvent extends ComponentEvent<Div> {

    private final ChatMessage chatMessage;

    public IncomingMessageEvent(ChatMessage chatMessage) {
        super(new Div(), false);
        this.chatMessage = chatMessage;
    }

}
