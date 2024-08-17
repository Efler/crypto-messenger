package org.eflerrr.client.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
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
import com.vaadin.flow.theme.lumo.Lumo;
import lombok.extern.slf4j.Slf4j;
import org.eflerrr.client.configuration.ApplicationConfig;
import org.eflerrr.client.dao.ChatDao;
import org.eflerrr.client.dao.ClientDao;
import org.eflerrr.client.model.uploadbuffer.UploadBuffer;
import org.eflerrr.client.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

@Route("chat")
@PageTitle("Chat")
@CssImport("./styles/chat/chat-styles.css")
@Slf4j
public class ChatView extends HorizontalLayout implements HasUrlParameter<String> {

    private static final String MATE_ABSENT_NAME_PLACEHOLDER = "Имя: ожидаем...";
    private static final String MATE_ABSENT_MODE_PLACEHOLDER = "Режим: ожидаем...";
    private static final String MATE_ABSENT_PADDING_PLACEHOLDER = "Набивка: ожидаем...";

    private final H2 statusHeader;
    private final H2 encryptionAlgorithmLabel;
    private final H3 clientPropsHeader;
    private final H3 clientPropsNameLabel;
    private final H3 clientPropsEncrtyptionModeLabel;
    private final H3 clientPropsPaddingTypeLabel;
    private final VerticalLayout clientPropsLayout;
    private final H3 matePropsHeader;
    private final H3 matePropsNameLabel;
    private final H3 matePropsEncrtyptionModeLabel;
    private final H3 matePropsPaddingTypeLabel;
    private final VerticalLayout matePropsLayout;
    private final VerticalLayout statusLayout;
    private final VerticalLayout chatLayout;
    private final VerticalLayout loadingLayout;
    private final VerticalLayout loadingPatch;
    private final H3 loadingLabel;
    private final Image loaadingSpinner;
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
    private String chatName;


    private void onFileUpload(SucceededEvent event) {
        String fileName = event.getFileName();
        File file = null;

        try (var reader = new BufferedReader(new InputStreamReader(
                uploadBuffer.getInputStream(fileName)))
        ) {
            while (reader.ready()) {
                System.out.println(reader.readLine());  // TODO!
            }
            if (!config.fileUpload().inMemory()) {
                file = uploadBuffer.getFileData(fileName).getFile();
            }
//            String filePath = file.getAbsolutePath();
//            System.out.printf("File saved to: %s%n", filePath);  // TODO!
        } catch (IOException ex) {
            System.out.println(ex.getMessage());     //TODO!
            ex.printStackTrace();
        }

        if (file != null) {
            System.out.println(file.delete());
        }
    }

    private void lockInput() {
        inputPanelLayout.setEnabled(false);
//        fileButton.setEnabled(false);     todo!
    }

    private void unlockInput() {
        inputPanelLayout.setEnabled(true);
        fileButton.setEnabled(true);
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
        this.uploadBuffer = uploadBuffer;
        setClassName("host-layout");

        statusHeader = new H2("Chat");
        statusHeader.setClassName("status-header");

        encryptionAlgorithmLabel = new H2(String.valueOf(chatDao.getEncryptionAlgorithm()));
        encryptionAlgorithmLabel.setClassName("encryption-algorithm-label");

        clientPropsHeader = new H3("Вы");
        clientPropsHeader.setClassName("props-header");
        clientPropsNameLabel = new H3("Имя: " + clientDao.getClientName());
        clientPropsNameLabel.setClassName("props-label");
        clientPropsEncrtyptionModeLabel = new H3("Режим: " + chatDao.getEncryptionMode());
        clientPropsEncrtyptionModeLabel.setClassName("props-label");
        clientPropsPaddingTypeLabel = new H3("Набивка: " + chatDao.getPaddingType());
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
        matePropsHeader.setClassName("props-header");
        matePropsNameLabel = new H3(MATE_ABSENT_NAME_PLACEHOLDER);
        matePropsNameLabel.setClassName("props-label");
        matePropsEncrtyptionModeLabel = new H3(MATE_ABSENT_MODE_PLACEHOLDER);
        matePropsEncrtyptionModeLabel.setClassName("props-label");
        matePropsPaddingTypeLabel = new H3(MATE_ABSENT_PADDING_PLACEHOLDER);
        matePropsPaddingTypeLabel.setClassName("props-label");

        matePropsLayout = new VerticalLayout();
        matePropsLayout.setClassName("props-layout");
        matePropsLayout.add(
                matePropsHeader,
                matePropsNameLabel,
                matePropsEncrtyptionModeLabel,
                matePropsPaddingTypeLabel
        );

        statusLayout = new VerticalLayout();
        statusLayout.setClassName("status-layout");
        statusLayout.add(
                statusHeader,
                encryptionAlgorithmLabel,
                clientPropsLayout,
                matePropsLayout
        );

        loadingLabel = new H3("Инициализация чата...");
        loadingLabel.setClassName("loading-label");

        loaadingSpinner = new Image("spinner.svg", "loading...");
        loaadingSpinner.setClassName("loading-spinner");

        loadingPatch = new VerticalLayout();
        loadingPatch.setClassName("loading-patch");
        loadingPatch.add(
                loadingLabel,
                loaadingSpinner
        );

        loadingLayout = new VerticalLayout();
        loadingLayout.setClassName("loading-layout");
        loadingLayout.add(
                loadingPatch
        );

        messagesLayout = new VerticalLayout();
        messagesLayout.setClassName("messages-layout");
        messagesLayout.add(
                loadingLayout
        );

        inputTextField = new TextField();
        inputTextField.addClassName("input-text-field");
        inputTextField.setPlaceholder("Введите сообщение...");
        inputTextField.getElement().setAttribute("autocomplete", "off");

        sendButton = new Button(VaadinIcon.ENTER_ARROW.create());
        sendButton.setClassName("send-button");

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
        this.chatName = chatName;
        this.statusHeader.setText(chatName);
    }

}
