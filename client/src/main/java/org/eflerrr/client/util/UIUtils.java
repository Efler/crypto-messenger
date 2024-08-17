package org.eflerrr.client.util;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.function.Consumer;

@Component
public class UIUtils {

    public static boolean executeWithLockUI(
            Optional<UI> ui, ComponentEvent<Div> event, Consumer<ComponentEvent<Div>> code) {
        if (ui.isPresent()) {
            var currUI = ui.get();
            currUI.getSession().lock();
            currUI.access(() -> code.accept(event));
            currUI.getSession().unlock();
            return true;
        }
        return false;
    }

}
