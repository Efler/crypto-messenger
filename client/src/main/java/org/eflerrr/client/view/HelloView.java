package org.eflerrr.client.view;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.Lumo;

@Route("/hello")
@PageTitle("Hello")
@CssImport("./styles/hello/hello-styles.css")
public class HelloView extends VerticalLayout {

    private static final String HEADER_TEXT = "Приветик, как тебя зовут?";
    private static final String NAME_FIELD_PLACEHOLDER = "Введи имя...";
    private static final String SUBMIT_BUTTON_TEXT = "Ввод";
    private static final String NOTIFICATION_TEXT = "Некорректное имя: только латинские буквы и цифры, друг!";


    private boolean isValidUsername(String username) {
        return username != null
                && !username.trim().isEmpty()
                && username.matches("[a-zA-Z0-9]+");
    }

    public HelloView() {

        getElement().getThemeList().add(Lumo.DARK);
        addClassName("hello-view");
        setSpacing(false);

        H1 header = new H1(HEADER_TEXT);
        header.addClassName("header");

        TextField nameField = new TextField();
        nameField.setPlaceholder(NAME_FIELD_PLACEHOLDER);
        nameField.addClassName("name-field");
        nameField.addThemeVariants(TextFieldVariant.LUMO_ALIGN_CENTER);
        nameField.getElement().setAttribute("autocomplete", "off");

        Notification notification = new Notification(
                NOTIFICATION_TEXT, 5000, Notification.Position.BOTTOM_CENTER);

        Button submitButton = new Button(SUBMIT_BUTTON_TEXT);
        submitButton.addClassName("submit-button");
        submitButton.addClickListener(event -> {
            String username = nameField.getValue();
            if (isValidUsername(username)) {
                getUI().ifPresent(ui -> ui.navigate("menu/" + username));
            } else {
                notification.open();
            }
        });
        submitButton.addClickShortcut(Key.ENTER);

        add(
                header,
                nameField,
                submitButton
        );

    }

}
