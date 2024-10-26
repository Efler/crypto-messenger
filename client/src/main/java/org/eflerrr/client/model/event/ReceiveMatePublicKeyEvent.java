package org.eflerrr.client.model.event;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.html.Div;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = false)
@ToString
public class ReceiveMatePublicKeyEvent extends ComponentEvent<Div> {

    public ReceiveMatePublicKeyEvent() {
        super(new Div(), false);
    }

}
