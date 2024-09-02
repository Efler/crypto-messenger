package org.eflerrr.client.view;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.SucceededEvent;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.theme.lumo.Lumo;
import lombok.extern.slf4j.Slf4j;
import org.eflerrr.client.configuration.ApplicationConfig;
import org.eflerrr.client.dao.ChatDao;
import org.eflerrr.client.dao.ClientDao;
import org.eflerrr.client.model.ChatMessage;
import org.eflerrr.client.model.MessageType;
import org.eflerrr.client.model.event.*;
import org.eflerrr.client.model.uploadbuffer.UploadBuffer;
import org.eflerrr.client.service.ChatService;
import org.eflerrr.client.util.UIUtils;
import org.eflerrr.encrypt.types.EncryptionMode;
import org.eflerrr.encrypt.types.PaddingType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.vaadin.olli.FileDownloadWrapper;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Route("chat")
@PageTitle("Chat")
@CssImport("./styles/chat/chat-styles.css")
@Slf4j
public class ChatView extends HorizontalLayout implements HasUrlParameter<String> {

    private static final String MATE_NAME_TEMPLATE = "Имя: %s";
    private static final String MATE_MODE_TEMPLATE = "Режим: %s";
    private static final String MATE_PADDING_TEMPLATE = "Набивка: %s";
    private static final String MATE_ABSENT_PLACEHOLDER = "ожидаем...";
    private static final String MESSAGES_SEPARATOR = "– – – – – – –";

    private static final String SCROLL_JS_SCRIPT =
            "requestAnimationFrame(() => { this.scrollTop = this.scrollHeight; });";
    private static final String SCROLL_UI_CALLBACK_TEMPLATE =
            "const img = document.createElement('img');"
                    + "img.src = '%s';"
                    + "img.onload = function() {"
                    + "  const container = document.querySelector('.messages-layout');"
                    + "  container.scrollTop = container.scrollHeight;"
                    + "};";

    private final H2 statusHeader;
    private final H2 encryptionAlgorithmLabel;
    private final H3 clientPropsHeader;
    private final H3 clientPropsNameLabel;
    private final H3 clientPropsEncrtyptionModeLabel;
    private final H3 clientPropsPaddingTypeLabel;
    private final VerticalLayout clientPropsLayout;
    private final H3 matePropsHeader;
    private final H3 matePropsNameLabel;
    private final H3 matePropsEncryptionModeLabel;
    private final H3 matePropsPaddingTypeLabel;
    private final VerticalLayout matePropsLayout;
    private final Button exitButton;
    private final VerticalLayout statusLayout;
    private final VerticalLayout chatLayout;
    private final VerticalLayout loadingLayout;
    private final VerticalLayout loadingPatch;
    private final H3 loadingLabel;
    private final Image loadingSpinner;
    private final VerticalLayout messagesLayout;
    private final HorizontalLayout inputPanelLayout;
    private final TextField inputTextField;
    private final Button sendButton;
    private final Button fileButton;
    private final Upload fileUpload;
    private final UploadBuffer uploadBuffer;

    private final ChatService chatService;
    private final ChatDao chatDao;
    private final ClientDao clientDao;
    private final ApplicationConfig config;
    private final Set<Registration> eventRegistrations;


    private void onSendButtonClick(ClickEvent<Button> ignored) {
        var message = inputTextField.getValue();
        if (!message.isEmpty()) {
            inputTextField.clear();
            chatService.sendMessage(
                    new ChatMessage(
                            message.getBytes(StandardCharsets.UTF_8),
                            false,
                            MessageType.TEXT,
                            clientDao.getClientId(),
                            Optional.empty(),
                            Optional.empty()
                    ));
        }
    }

    private void onExitButtonClick(ClickEvent<Button> ignored) {
        try {
            chatService.exitChat();
        } catch (IllegalStateException ex) {
            log.warn(ex.getLocalizedMessage());
        }

        getUI().ifPresent(ui -> ui.navigate("menu/" + clientDao.getClientName()));
    }

    private void onFileUpload(SucceededEvent event) {
        String fileName = event.getFileName();
        InputStream fileInputStream = uploadBuffer.getInputStream(fileName);
        File file = null;

        try {
            if (!config.fileUpload().inMemory()) {
                file = uploadBuffer.getFileData(fileName).getFile();
            }
            var messageType = chatService.resolveMessageType(event.getMIMEType());
            chatService.sendMessage(
                    new ChatMessage(
                            fileInputStream.readAllBytes(),
                            false,
                            messageType,
                            clientDao.getClientId(),
                            Optional.of(fileName),
                            Optional.of(event.getMIMEType())
                    ));
        } catch (IOException ex) {
            log.warn(ex.getLocalizedMessage());
        }
        if (file != null) {
            log.info(String.valueOf(file.delete()));
        }
    }

    private void lockInput() {
        inputPanelLayout.setEnabled(false);
        fileButton.setEnabled(false);
    }

    private void unlockInput() {
        inputPanelLayout.setEnabled(true);
        fileButton.setEnabled(true);
    }

    private String resolveFilename(ChatMessage message) {
        var maybeMIMEType = message.getMimeType();
        return message.getFilename().orElse("blank." +
                (maybeMIMEType.map(s -> s.substring(s.indexOf('/')))
                        .orElse(""))
        );
    }

    private Button buildFileMessageButton(String fileName, String mimeType, boolean isSelf) {
        var fileNameLabel = new H3(fileName);
        fileNameLabel.setClassName("file-message-inner-filename-label");
        var typeLabel = new H3(mimeType);
        typeLabel.setClassName("file-message-inner-mime-type-label");

        var verticalLayout = new VerticalLayout();
        verticalLayout.setClassName("file-message-inner-vertical");
        verticalLayout.add(
                fileNameLabel,
                typeLabel
        );

        var iconLayout = new VerticalLayout(VaadinIcon.FILE_TEXT.create());
        iconLayout.setClassName("file-message-inner-icon");

        var horizontalLayout = new HorizontalLayout();
        horizontalLayout.setClassName("file-message-inner-horizontal");
        horizontalLayout.add(
                iconLayout,
                verticalLayout
        );

        var button = new Button(horizontalLayout);
        button.setClassName(isSelf ? "file-message-self" : "file-message-mate");
        return button;
    }

    private Div buildMessagesSeparator() {
        var separator = new Div(MESSAGES_SEPARATOR);
        separator.setClassName("messages-separator");
        return separator;
    }

    private void fillMateProps(String mateName, EncryptionMode mateMode, PaddingType matePadding) {
        matePropsHeader.setClassName("props-header");
        matePropsNameLabel.setClassName("props-label-contrast");
        matePropsNameLabel.setText(
                String.format(MATE_NAME_TEMPLATE, mateName));
        matePropsEncryptionModeLabel.setText(
                String.format(MATE_MODE_TEMPLATE, mateMode));
        matePropsPaddingTypeLabel.setText(
                String.format(MATE_PADDING_TEMPLATE, matePadding));
    }

    private void onMateJoiningEvent(MateJoiningEvent mateJoiningEvent) {
        UIUtils.executeWithLockUI(getUI(), mateJoiningEvent, (rawEvent) -> {

            var event = (MateJoiningEvent) rawEvent;
            fillMateProps(event.getMateName(), event.getMateMode(), event.getMatePadding());
            loadingLabel.setText("Обмен публичными ключами...");

        });
    }

    private void onMateExitEvent(MateExitEvent mateExitEvent) {
        UIUtils.executeWithLockUI(getUI(), mateExitEvent, (ignored) -> {

            matePropsHeader.setClassName("absent-header");
            matePropsNameLabel.setText(
                    String.format(MATE_NAME_TEMPLATE, MATE_ABSENT_PLACEHOLDER));
            matePropsEncryptionModeLabel.setText(
                    String.format(MATE_MODE_TEMPLATE, MATE_ABSENT_PLACEHOLDER));
            matePropsPaddingTypeLabel.setText(
                    String.format(MATE_PADDING_TEMPLATE, MATE_ABSENT_PLACEHOLDER));
            lockInput();
            messagesLayout.add(buildMessagesSeparator());
            messagesLayout.getElement().executeJs(SCROLL_JS_SCRIPT);
            loadingLabel.setText("Собеседник отключился, ожидаем нового...");
            loadingLayout.setVisible(true);

        });
    }

    private void onReceiveMatePublicKeyEvent(ReceiveMatePublicKeyEvent receiveMatePublicKeyEvent) {
        UIUtils.executeWithLockUI(getUI(), receiveMatePublicKeyEvent, (ignored) -> {
            loadingLabel.setText("Генерация итогового ключа...");
            chatService.setupEnvironment();
        });
    }

    private void onReadyToChatEvent(ReadyToChatEvent readyToChatEvent) {
        UIUtils.executeWithLockUI(getUI(), readyToChatEvent, (ignored) -> {
            unlockInput();
            loadingLayout.setVisible(false);
        });
    }

    private void onIncomingMessageEvent(IncomingMessageEvent incomingMessageEvent) {
        UIUtils.executeWithLockUI(getUI(), incomingMessageEvent, (rawEvent) -> {

            var chatMessage = ((IncomingMessageEvent) rawEvent).getChatMessage();
            var scrollJsScript = SCROLL_JS_SCRIPT;
            var isSelf = chatMessage.getClientId() == clientDao.getClientId();
            switch (chatMessage.getMessageType()) {

                case TEXT:
                    var messageLabel = new Div(new String(chatMessage.getMessage(), StandardCharsets.UTF_8));
                    if (isSelf) {
                        messageLabel.setClassName("text-message-self");
                        messageLabel.getStyle().set("margin-left", "auto");
                    } else {
                        messageLabel.setClassName("text-message-mate");
                        messageLabel.getStyle().set("margin-right", "auto");
                    }
                    messagesLayout.add(messageLabel);
                    break;

                case IMAGE:
                    var imageResource = new StreamResource(
                            "image", () -> new ByteArrayInputStream(chatMessage.getMessage()));
                    var image = new Image(imageResource, "image");
                    if (isSelf) {
                        image.setClassName("image-message-self");
                        image.getStyle().set("margin-left", "auto");
                    } else {
                        image.setClassName("image-message-mate");
                        image.getStyle().set("margin-right", "auto");
                    }
                    messagesLayout.add(image);
                    scrollJsScript = String.format(SCROLL_UI_CALLBACK_TEMPLATE, image.getSrc());
                    break;

                case FILE:
                    var fileName = this.resolveFilename(chatMessage);
                    var fileDataResource = new StreamResource(
                            fileName, () -> new ByteArrayInputStream(chatMessage.getMessage()));
                    var fileMessageButton = this.buildFileMessageButton(
                            fileName, chatMessage.getMimeType().orElse("<unknown type>"), isSelf);
                    FileDownloadWrapper fileMessageWrapper = new FileDownloadWrapper(fileDataResource);
                    fileMessageWrapper.wrapComponent(fileMessageButton);
                    fileMessageWrapper.getStyle().set(
                            isSelf ? "margin-left" : "margin-right", "auto");
                    messagesLayout.add(fileMessageWrapper);
                    break;

            }
            messagesLayout.getElement().executeJs(scrollJsScript);

        });
    }


    @Autowired
    public ChatView(
            ChatService chatService,
            ChatDao chatDao,
            ClientDao clientDao,
            ApplicationConfig config,
            @Qualifier("configured-upload-buffer")
            UploadBuffer uploadBuffer) {
        this.chatService = chatService;
        this.chatDao = chatDao;
        this.clientDao = clientDao;
        this.config = config;
        this.eventRegistrations = new HashSet<>();
        this.uploadBuffer = uploadBuffer;
        setClassName("host-layout");

        statusHeader = new H2("Chat");
        statusHeader.setClassName("status-header");

        encryptionAlgorithmLabel = new H2(String.valueOf(chatDao.getEncryptionAlgorithm()));
        encryptionAlgorithmLabel.setClassName("encryption-algorithm-label");

        clientPropsHeader = new H3("Вы");
        clientPropsHeader.setClassName("props-header");
        clientPropsNameLabel = new H3("Имя: " + clientDao.getClientName());
        clientPropsNameLabel.setClassName("props-label-contrast");
        clientPropsEncrtyptionModeLabel = new H3("Режим: " + chatDao.getSelfSettings().getEncryptionMode());
        clientPropsEncrtyptionModeLabel.setClassName("props-label");
        clientPropsPaddingTypeLabel = new H3("Набивка: " + chatDao.getSelfSettings().getPaddingType());
        clientPropsPaddingTypeLabel.setClassName("props-label");

        clientPropsLayout = new VerticalLayout();
        clientPropsLayout.setClassName("props-layout");
        clientPropsLayout.add(
                clientPropsHeader,
                clientPropsNameLabel,
                clientPropsEncrtyptionModeLabel,
                clientPropsPaddingTypeLabel
        );

        matePropsHeader = new H3("Собеседник");
        matePropsHeader.setClassName("absent-header");
        matePropsNameLabel = new H3(String.format(MATE_NAME_TEMPLATE, MATE_ABSENT_PLACEHOLDER));
        matePropsNameLabel.setClassName("props-label");
        matePropsEncryptionModeLabel = new H3(String.format(MATE_MODE_TEMPLATE, MATE_ABSENT_PLACEHOLDER));
        matePropsEncryptionModeLabel.setClassName("props-label");
        matePropsPaddingTypeLabel = new H3(String.format(MATE_PADDING_TEMPLATE, MATE_ABSENT_PLACEHOLDER));
        matePropsPaddingTypeLabel.setClassName("props-label");

        matePropsLayout = new VerticalLayout();
        matePropsLayout.setClassName("props-layout");
        matePropsLayout.add(
                matePropsHeader,
                matePropsNameLabel,
                matePropsEncryptionModeLabel,
                matePropsPaddingTypeLabel
        );

        exitButton = new Button("Покинуть чат");
        exitButton.setClassName("exit-button");
        exitButton.addClickListener(this::onExitButtonClick);

        statusLayout = new VerticalLayout();
        statusLayout.setClassName("status-layout");
        statusLayout.add(
                statusHeader,
                encryptionAlgorithmLabel,
                clientPropsLayout,
                matePropsLayout,
                exitButton
        );

        loadingLabel = new H3("Инициализация чата...");
        loadingLabel.setClassName("loading-label");

        loadingSpinner = new Image("spinner.svg", "loading...");
        loadingSpinner.setClassName("loading-spinner");

        loadingPatch = new VerticalLayout();
        loadingPatch.setClassName("loading-patch");
        loadingPatch.add(
                loadingLabel,
                loadingSpinner
        );

        loadingLayout = new VerticalLayout();
        loadingLayout.setClassName("loading-layout");
        loadingLayout.add(
                loadingPatch
        );

        messagesLayout = new VerticalLayout();
        messagesLayout.setClassName("messages-layout");

        inputTextField = new TextField();
        inputTextField.addClassName("input-text-field");
        inputTextField.setPlaceholder("Введите сообщение...");
        inputTextField.getElement().setAttribute("autocomplete", "off");

        sendButton = new Button(VaadinIcon.ENTER_ARROW.create());
        sendButton.setClassName("send-button");
        sendButton.addClickShortcut(Key.ENTER);
        sendButton.addClickListener(this::onSendButtonClick);

        fileButton = new Button(VaadinIcon.UPLOAD_ALT.create());
        fileButton.setClassName("file-button");

        fileUpload = new Upload(uploadBuffer);
        fileUpload.setClassName("file-upload");
        fileUpload.setUploadButton(fileButton);
        fileUpload.setDropAllowed(false);
        fileUpload.setMaxFileSize(config.fileUpload().fileMaxSize());
        fileUpload.addSucceededListener(this::onFileUpload);

        inputPanelLayout = new HorizontalLayout();
        inputPanelLayout.setClassName("input-panel-layout");
        inputPanelLayout.getElement().getThemeList().add(Lumo.DARK);
        inputPanelLayout.add(
                inputTextField,
                sendButton,
                fileUpload
        );
        lockInput();

        chatLayout = new VerticalLayout();
        chatLayout.setClassName("chat-layout");
        chatLayout.add(
                messagesLayout,
                loadingLayout,
                inputPanelLayout
        );

        add(
                statusLayout,
                chatLayout
        );
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, String chatName) {
        chatName = chatName.trim().replace(" ", "-");
        log.info("Someone entered '{}' chat", chatName);
        this.statusHeader.setText(chatName);
    }

    @Override
    public void onAttach(AttachEvent attachEvent) {
        eventRegistrations.add(chatService.attachListener(
                this::onMateJoiningEvent, MateJoiningEvent.class));
        eventRegistrations.add(chatService.attachListener(
                this::onReceiveMatePublicKeyEvent, ReceiveMatePublicKeyEvent.class));
        eventRegistrations.add(chatService.attachListener(
                this::onReadyToChatEvent, ReadyToChatEvent.class));
        eventRegistrations.add(chatService.attachListener(
                this::onIncomingMessageEvent, IncomingMessageEvent.class));
        eventRegistrations.add(chatService.attachListener(
                this::onMateExitEvent, MateExitEvent.class));
        if (chatDao.getSelfSettings().getIsCreator()) {
            loadingLabel.setText("Ожидаем собеседника...");
        } else {
            fillMateProps(
                    chatDao.getMateSettings().getClientName(),
                    chatDao.getMateSettings().getEncryptionMode(),
                    chatDao.getMateSettings().getPaddingType());
            loadingLabel.setText("Генерация итогового ключа...");
            chatService.setupEnvironment();
        }


        //TODO!!!!!!!!!!!!!!!!!!!! STYLING FILE MESSAGE! REMOVE AFTER!

//        UIUtils.executeWithLockUI(getUI(), new IncomingMessageEvent(null), (rawEvent) -> {
//
//            var fileMessageButton = buildFileMessageButton("system_design.txt", "application/txt", true);
//            FileDownloadWrapper fileMessageWrapper = new FileDownloadWrapper(new StreamResource(
//                    "a.txt", () -> new ByteArrayInputStream("So happy text! :)".getBytes())));
//            fileMessageWrapper.wrapComponent(fileMessageButton);
//            fileMessageWrapper.getStyle().set("margin-left", "auto");
//            messagesLayout.add(fileMessageWrapper);
//
//            for (int i = 0; i < 10; i++) {
//                Div div = new Div("hi" + i);
//                div.setClassName("text-message-self");
//                div.getStyle().set("margin-left", "auto");
//                messagesLayout.add(div);
//            }
//            messagesLayout.add(buildMessagesSeparator());
//            Div div = new Div("gay");
//            div.setClassName("text-message-self");
//            div.getStyle().set("margin-left", "auto");
//            messagesLayout.add(div);
//
//
//            loadingLayout.setVisible(false);
//
//        });

    }

    @Override
    public void onDetach(DetachEvent detachEvent) {
        eventRegistrations.forEach(Registration::remove);
    }

}
