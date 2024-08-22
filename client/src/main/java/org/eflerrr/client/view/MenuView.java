package org.eflerrr.client.view;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.ComboBoxVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.theme.lumo.Lumo;
import lombok.extern.slf4j.Slf4j;
import org.eflerrr.client.dao.ChatDao;
import org.eflerrr.client.dao.ClientDao;
import org.eflerrr.client.model.ChatInfo;
import org.eflerrr.client.model.ClientSettings;
import org.eflerrr.client.model.event.ChatUpdateEvent;
import org.eflerrr.client.scheduler.ChatListUpdateScheduler;
import org.eflerrr.client.service.MenuService;
import org.eflerrr.client.util.UIUtils;
import org.eflerrr.encrypt.types.EncryptionAlgorithm;
import org.eflerrr.encrypt.types.EncryptionMode;
import org.eflerrr.encrypt.types.PaddingType;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

import static org.eflerrr.utils.Utils.bytesToHexString;

@Route("menu")
@PageTitle("Menu")
@CssImport("./styles/menu/menu-styles.css")
@Slf4j
public class MenuView extends VerticalLayout implements HasUrlParameter<String> {

    private final static String INVALID_INPUT_NOTIFICATION_TEXT = "Некорректные параметры для создания чата";
    private final static String INVALID_SETTINGS_NOTIFICATION_TEXT = "Некорректные настройки шифрования";
    private final static String DUPLICATED_CHAT_NOTIFICATION_TEXT_TEMPLATE = "Чат с именем '%s' уже существует";

    private final MenuService menuService;
    private final ChatListUpdateScheduler scheduler;
    private final ClientDao clientDao;
    private final ChatDao chatDao;
    private final Map<String, EncryptionAlgorithm> registeredChats;

    private final Notification errorNotification = new Notification(
            "Неизвестная ошибка, попробуйте позже!",
            5000, Notification.Position.BOTTOM_CENTER
    );
    private final VerticalLayout listLayout;
    private final ComboBox<EncryptionAlgorithm> createBox;
    private final TextField createNameField;
    private final Div darkenLayout;
    private final VerticalLayout settingsLayout;
    private final ComboBox<EncryptionMode> settingsModeBox;
    private final ComboBox<PaddingType> settingsPaddingBox;
    private final TextField ivTextField;
    private final Button generateIVButton;
    private Registration registration;


    private void onGenerateIVButtonClick(ClickEvent<Button> ignored) {
        ivTextField.setValue(bytesToHexString(menuService.generateIV(), "-"));
    }

    private void enableDarkenLayout() {
        this.add(darkenLayout);
        darkenLayout.getClassNames().add("darken");
    }

    private void disableDarkenLayout() {
        this.remove(darkenLayout);
        darkenLayout.getClassNames().remove("darken");
    }

    private void showSettings() {
        enableDarkenLayout();
        this.add(settingsLayout);
        settingsLayout.getClassNames().add("show-settings");
        onGenerateIVButtonClick(null);
    }

    private void hideSettings() {
        this.remove(settingsLayout);
        settingsLayout.getClassNames().remove("show-settings");
        disableDarkenLayout();
    }

    private void onCreateChatButtonClick() {
        var chosenName = createNameField.getValue();
        var chosenAlgo = createBox.getValue();
        if (menuService.validateChatName(chosenName) && chosenAlgo != null) {
            if (!menuService.isChatRegistered(chosenName)) {
                chatDao.getSelfSettings().setIsCreator(true);
                chatDao.setChatName(chosenName);
                chatDao.setEncryptionAlgorithm(chosenAlgo);

                showSettings();
            } else {
                errorNotification.setText(String.format(
                        DUPLICATED_CHAT_NOTIFICATION_TEXT_TEMPLATE, chosenName));
                errorNotification.open();
            }
        } else {
            errorNotification.setText(INVALID_INPUT_NOTIFICATION_TEXT);
            errorNotification.open();
        }
    }

    private void onJoinChatButtonClick(String chosenName) {
        chatDao.getSelfSettings().setIsCreator(false);
        chatDao.setChatName(chosenName);
        chatDao.setEncryptionAlgorithm(registeredChats.get(chosenName));

        showSettings();
    }

    private void onSettingsConfirmClick() {
        var chosenMode = settingsModeBox.getValue();
        var chosenPadding = settingsPaddingBox.getValue();
        if (chosenMode != null && chosenPadding != null) {
            chatDao.getSelfSettings().setEncryptionMode(chosenMode);
            chatDao.getSelfSettings().setPaddingType(chosenPadding);

            if (chatDao.getSelfSettings().getIsCreator()) {
                try {
                    menuService.createChat();
                    getUI().ifPresent(ui -> ui.navigate("chat/" + chatDao.getChatName()));
                } catch (IllegalArgumentException e) {
                    errorNotification.setText(e.getMessage());
                    errorNotification.open();
                }
            } else {
                try {
                    menuService.joinChat();
                    getUI().ifPresent(ui -> ui.navigate("chat/" + chatDao.getChatName()));
                } catch (IllegalArgumentException e) {
                    errorNotification.setText(e.getMessage());
                    errorNotification.open();
                }
            }


        } else {
            errorNotification.setText(INVALID_SETTINGS_NOTIFICATION_TEXT);
            errorNotification.open();
        }
    }

    private void onSettingsCloseClick() {
        chatDao.setChatName(null);
        chatDao.setEncryptionAlgorithm(null);
        chatDao.getSelfSettings().setIV(null);
        chatDao.getSelfSettings().setIsCreator(false);

        hideSettings();
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


    @Autowired
    public MenuView(
            MenuService menuService,
            ChatListUpdateScheduler scheduler,
            ChatDao chatDao,
            ClientDao clientDao) {
        this.menuService = menuService;
        this.scheduler = scheduler;
        this.chatDao = chatDao;
        this.clientDao = clientDao;
        this.registeredChats = new HashMap<>();

        this.darkenLayout = new Div();
        darkenLayout.setClassName("darken-layout");

        this.settingsLayout = new VerticalLayout();
        settingsLayout.setClassName("settings-layout");
        settingsLayout.setSpacing(false);
        settingsLayout.setPadding(false);
        settingsLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        settingsLayout.setAlignItems(Alignment.CENTER);

        var settingsHeader = new H3("Настройте режим шифрования, тип набивки блоков и вектор инициализации");
        settingsHeader.setClassName("settings-header");
        this.settingsModeBox = new ComboBox<>();
        settingsModeBox.setPlaceholder("Режим...");
        settingsModeBox.setItems(EncryptionMode.values());
        settingsModeBox.setItemLabelGenerator(EncryptionMode::toString);
        settingsModeBox.addClassName("settings-mode-box");
        settingsModeBox.addThemeVariants(ComboBoxVariant.LUMO_ALIGN_CENTER);
        this.settingsPaddingBox = new ComboBox<>();
        settingsPaddingBox.setPlaceholder("Тип набивки...");
        settingsPaddingBox.setItems(PaddingType.values());
        settingsPaddingBox.setItemLabelGenerator(PaddingType::toString);
        settingsPaddingBox.addClassName("settings-padding-box");
        settingsPaddingBox.addThemeVariants(ComboBoxVariant.LUMO_ALIGN_CENTER);
        var settingsInputLayout = new HorizontalLayout();
        settingsInputLayout.setAlignItems(Alignment.CENTER);
        settingsInputLayout.getElement().getThemeList().add(Lumo.DARK);
        settingsInputLayout.setClassName("settings-input-layout");
        settingsInputLayout.add(
                settingsModeBox,
                settingsPaddingBox
        );
        this.ivTextField = new TextField();
        ivTextField.setClassName("settings-iv-field");
        ivTextField.setReadOnly(true);
        ivTextField.setValue("Генерируем IV...");
        ivTextField.addThemeVariants(TextFieldVariant.LUMO_ALIGN_CENTER);
        this.generateIVButton = new Button("Сгенерировать новый IV");
        generateIVButton.setClassName("settings-generate-iv-button");
        generateIVButton.addClickListener(this::onGenerateIVButtonClick);
        var settingsButton = new Button("Применить");
        settingsButton.addClassName("settings-confirm-button");
        settingsButton.addClickListener(e -> onSettingsConfirmClick());
        Button closeButton = new Button("Закрыть");
        closeButton.addClassName("settings-close-button");
        closeButton.addClickListener(e -> onSettingsCloseClick());
        settingsLayout.add(
                settingsHeader,
                settingsInputLayout,
                ivTextField,
                generateIVButton,
                settingsButton,
                closeButton
        );


        setClassName("menu-layout");
        setSizeFull();
        setSpacing(false);
        setPadding(false);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);

        H2 listHeader = new H2("Присоединиться к комнате");
        listHeader.setClassName("headers");
        var chatLoadingLabel = new Div("Загрузка списка комнат...");
        chatLoadingLabel.setClassName("loading-chats-label");
        listLayout = new VerticalLayout();
        listLayout.setClassName("list-layout");
        listLayout.setAlignItems(Alignment.CENTER);
        listLayout.add(
                listHeader,
                chatLoadingLabel
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
        createNameField = new TextField();
        createNameField.setPlaceholder("Название комнаты...");
        createNameField.addClassName("create-name-field");
        createNameField.addThemeVariants(TextFieldVariant.LUMO_ALIGN_CENTER);
        createNameField.getElement().setAttribute("autocomplete", "off");
        createBox = new ComboBox<>();
        createBox.setPlaceholder("Алгоритм...");
        createBox.setItems(EncryptionAlgorithm.values());
        createBox.setItemLabelGenerator(EncryptionAlgorithm::toString);
        createBox.addClassName("create-box");
        createBox.addThemeVariants(ComboBoxVariant.LUMO_ALIGN_CENTER);
        var createButton = new Button("Создать");
        createButton.addClassName("create-button");
        createButton.addClickListener(e -> onCreateChatButtonClick());
        var createInputLayout = new HorizontalLayout();
        createInputLayout.setAlignItems(Alignment.CENTER);
        createInputLayout.getElement().getThemeList().add(Lumo.DARK);
        createInputLayout.setClassName("create-input-layout");
        createInputLayout.add(
                createNameField,
                createBox
        );
        VerticalLayout createLayout = new VerticalLayout();
        createLayout.setClassName("create-layout");
        createLayout.setAlignItems(Alignment.CENTER);
        createLayout.add(
                createHeader,
                createInputLayout,
                createButton
        );

        Div divider = new Div();
        divider.setClassName("divider");

        Div orLabel = new Div();
        orLabel.setClassName("or-label");
        orLabel.setText("или");

        var mockLayout = new VerticalLayout();
        createLayout.setAlignItems(Alignment.CENTER);
        mockLayout.setClassName("mock-layout");

        HorizontalLayout mainLayout = new HorizontalLayout();
        mainLayout.setSizeFull();
        mainLayout.setSpacing(false);
        mainLayout.setMargin(false);
        mainLayout.setPadding(false);
        mainLayout.add(
                leftLayout,
                divider,
                mockLayout,
                createLayout
        );
        mainLayout.setAlignItems(Alignment.STRETCH);

        add(
                mainLayout,
                orLabel
        );

        scheduler.start();
    }

    public void onChatListUpdate(ChatUpdateEvent chatUpdateEvent) {
        log.info("Updating chat list in UI");

        UIUtils.executeWithLockUI(getUI(), chatUpdateEvent, (rawEvent) -> {

            var chatList = ((ChatUpdateEvent) rawEvent).getUpdatedChats();
            listLayout.removeAll();
            registeredChats.clear();
            if (chatList.isEmpty()) {

                var noChatsLabel = new Div("Нет доступных комнат :(");
                noChatsLabel.setClassName("no-chats-label");
                listLayout.add(noChatsLabel);

            } else {

                for (ChatInfo chatInfo : chatList) {
                    Button chatButton = new Button(chatInfo.getChatName());
                    chatButton.addClassName("list-chat-button");
                    chatButton.addClickListener(e -> onJoinChatButtonClick(e.getSource().getText()));

                    Button chatAlgorithm = new Button(chatInfo.getEncryptionAlgorithm().toString());
                    chatAlgorithm.setClassName("list-chat-algorithm");

                    var rowLayout = new HorizontalLayout();
                    rowLayout.setClassName("list-chat-row-layout");
                    rowLayout.setAlignItems(Alignment.CENTER);
                    rowLayout.setSpacing(false);
                    rowLayout.setPadding(false);
                    rowLayout.setMargin(false);
                    rowLayout.add(
                            chatButton,
                            chatAlgorithm
                    );
                    listLayout.add(rowLayout);
                    registeredChats.put(chatInfo.getChatName(), chatInfo.getEncryptionAlgorithm());
                }

            }
        });
    }

    @Override
    public void setParameter(BeforeEvent event, String clientName) {
        clientDao.setClientName(clientName);
        chatDao.setSelfSettings(new ClientSettings());
        chatDao.getSelfSettings().setClientName(clientName);

        menuService.generateClientId();
    }

}
