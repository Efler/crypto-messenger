package org.eflerrr.client.view;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Route("chat")
@PageTitle("Chat")
@CssImport("./styles/chat/chat-styles.css")
@Slf4j
public class ChatView extends VerticalLayout implements HasUrlParameter<String> {

    private MessageList messageList;
    private List<MessageListItem> messages;

    @Autowired
    public ChatView(ResourceLoader resourceLoader) throws IOException {
        setClassName("chat-layout");
        var fileUrl = "myimage.jpg";
        var img = new Image(fileUrl, "myimage");
        add(img);


//        messageList = new MessageList();
//        messages = new ArrayList<>();
//        messageList.setItems(messages);
//        add(messageList);
//
//        var msg = new MessageListItem("Hello, world!", Instant.now(), "eflerrr");
//        messages.add(msg);
//        String fileUrl = resourceLoader.getResource(
//                "classpath:myimage.jpg").getURL().toString();
//        var msg2 = new MessageListItem(
//                "<img src='" + fileUrl + "' style='max-width: 100%;'/>");
//        messages.add(msg2);
//        messageList.setItems(messages);



    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, String s) {
        log.info("Someone entered '{}' chat", s);
    }

}
