package org.eflerrr.client.model.event;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.html.Div;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.eflerrr.client.model.ChatInfo;

import java.util.List;

@Value
@EqualsAndHashCode(callSuper = false)
public class ChatUpdateEvent extends ComponentEvent<Div> {

    List<ChatInfo> updatedChats;

    public ChatUpdateEvent(List<ChatInfo> updatedChats) {
        super(new Div(), false);
        this.updatedChats = updatedChats;
    }
}
