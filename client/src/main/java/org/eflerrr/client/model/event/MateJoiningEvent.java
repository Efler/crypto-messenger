package org.eflerrr.client.model.event;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.html.Div;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.eflerrr.encrypt.types.EncryptionMode;
import org.eflerrr.encrypt.types.PaddingType;

@Value
@EqualsAndHashCode(callSuper = false)
public class MateJoiningEvent extends ComponentEvent<Div> {

    String mateName;
    EncryptionMode mateMode;
    PaddingType matePadding;

    public MateJoiningEvent(String mateName, EncryptionMode mateMode, PaddingType matePadding) {
        super(new Div(), false);
        this.mateName = mateName;
        this.mateMode = mateMode;
        this.matePadding = matePadding;
    }

}
