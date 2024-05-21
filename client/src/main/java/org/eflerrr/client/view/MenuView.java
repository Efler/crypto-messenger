package org.eflerrr.client.view;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;
import lombok.extern.slf4j.Slf4j;
import org.eflerrr.client.client.dto.ChatInfo;
import org.eflerrr.client.scheduler.ChatListUpdateScheduler;
import org.springframework.beans.factory.annotation.Autowired;

@Route("menu")
@PageTitle("Menu")
@CssImport("./styles/menu/menu-styles.css")
@Slf4j
public class MenuView extends VerticalLayout implements HasUrlParameter<String> {

    private final ChatListUpdateScheduler scheduler;
    private final VerticalLayout listLayout;
    private Registration registration;
    private String clientName;
    private long clientId;

    @Autowired
    public MenuView(ChatListUpdateScheduler scheduler) {
        this.scheduler = scheduler;

        setClassName("menu-layout");
        setSizeFull();
        setSpacing(false);
        setPadding(false);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);

        H2 listHeader = new H2("Присоединиться к комнате");
        listHeader.setClassName("headers");
        var cahtLoadingLabel = new Div("Загрузка списка комнат...");
        cahtLoadingLabel.setClassName("loading-chats-label");
        listLayout = new VerticalLayout();
        listLayout.setClassName("list-layout");
        listLayout.setAlignItems(Alignment.CENTER);
        listLayout.add(
                listHeader,
                cahtLoadingLabel
        );
        var leftLayout = new VerticalLayout();
        leftLayout.setClassName("left-layout");
        leftLayout.setAlignItems(Alignment.CENTER);
        leftLayout.add(
                listHeader,
                listLayout
        );

        H2 createHeader = new H2("Создать новую комнату");
        createHeader.setClassName("headers");
        VerticalLayout createLayout = new VerticalLayout();
        createLayout.setClassName("create-layout");
        createLayout.setAlignItems(Alignment.CENTER);
        createLayout.add(
                createHeader
        );

        Div divider = new Div();
        divider.setClassName("divider");

        Div orLabel = new Div();
        orLabel.setClassName("or-label");
        orLabel.setText("или");

        HorizontalLayout mainLayout = new HorizontalLayout();
        mainLayout.setSizeFull();
        mainLayout.setSpacing(false);
        mainLayout.setMargin(false);
        mainLayout.setPadding(false);
        mainLayout.add(
                leftLayout,
                divider,
                createLayout
        );
        mainLayout.setAlignItems(Alignment.STRETCH);

        add(
                mainLayout,
                orLabel
        );

        scheduler.start();
    }

    public void onChatListUpdate(ChatListUpdateScheduler.ChatUpdateEvent event) {
        log.info("Updating chat list in UI");

        if (getUI().isPresent()) {
            var currUI = getUI().get();
            currUI.getSession().lock();
            currUI.access(() -> {
                listLayout.removeAll();
                if (event.getUpdatedChats().isEmpty()) {

                    var noChatsLabel = new Div("Нет доступных комнат :(");
                    noChatsLabel.setClassName("no-chats-label");
                    listLayout.add(noChatsLabel);

                } else {

                    for (ChatInfo chatInfo : event.getUpdatedChats()) {
                        Button chatButton = new Button(chatInfo.toString());
                        chatButton.addClassName("list-chat-button");
                        chatButton.addClickListener(e -> {
                            getUI().ifPresent(ui -> ui.navigate(
                                    "chat/" + clientName + "/" + chatInfo.getChatName()
                            ));
                        });
                        chatButton.addClickShortcut(Key.ENTER);
                        listLayout.add(chatButton);
                    }

                }
            });
            currUI.getSession().unlock();
        }
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        log.info("Attaching to the scheduler");
        registration = scheduler.attachListener(this::onChatListUpdate);
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        log.info("Detaching from the scheduler");
        registration.remove();
        scheduler.clearPreviousList().pause();
    }

    @Override
    public void setParameter(BeforeEvent event, String parameter) {
        this.clientName = parameter;
        this.clientId = generateClientId(clientName);
    }

    private long generateClientId(String clientName) {
        var result = clientName.hashCode();
        for (char c : clientName.toCharArray()) {
            result = 31 * result + c;
        }
        return Math.abs(result);
    }

}
