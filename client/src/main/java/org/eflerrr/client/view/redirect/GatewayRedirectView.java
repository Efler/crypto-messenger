package org.eflerrr.client.view.redirect;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.eflerrr.client.configuration.ApplicationConfig;
import org.springframework.beans.factory.annotation.Autowired;

@Route("")
@PageTitle("Redirecting...")
public class GatewayRedirectView extends Div implements BeforeEnterObserver {

    private final String gatewayUrl;

    @Autowired
    public GatewayRedirectView(ApplicationConfig config) {
        gatewayUrl = config.gatewayEndpoint();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        event.forwardTo(gatewayUrl);
    }

}
