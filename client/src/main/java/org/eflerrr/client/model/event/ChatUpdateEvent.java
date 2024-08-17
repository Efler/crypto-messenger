package org.eflerrr.client.model.event;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.html.Div;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.eflerrr.client.model.ChatInfo;

import java.util.List;

@Getter
@EqualsAndHashCode(callSuper = false)
@ToString
public class ChatUpdateEvent extends ComponentEvent<Div> {

    private final List<ChatInfo> updatedChats;

    public ChatUpdateEvent(List<ChatInfo> updatedChats) {
        super(new Div(), false);
        this.updatedChats = updatedChats;
    }
}
