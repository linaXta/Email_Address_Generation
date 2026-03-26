package lv.alina.emailgen.views.privatepages;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import lv.alina.emailgen.models.User;

@Route("main")
@PageTitle("Main")
@CssImport("./styles/main-view.css")
public class MainView extends VerticalLayout implements BeforeEnterObserver {
	
	public MainView() {

        addClassName("main-page");
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        VerticalLayout shell = new VerticalLayout();
        shell.addClassName("main-shell");
        shell.setPadding(false);
        shell.setSpacing(false);
        shell.setAlignItems(Alignment.CENTER);

        H2 title = new H2("Menu");
        title.addClassName("main-title");

        Button mainEmailsButton = createMenuButton("Main e-mails");
        Button companiesButton = createMenuButton("Company's");
        Button generatorButton = createMenuButton("E-mail generator");
        Button profileButton = createMenuButton("Profile");
        Button logoutButton = createMenuButton("Log out");

        mainEmailsButton.addClickListener(event ->
                Notification.show("Main e-mails page will be added later.")
        );

        companiesButton.addClickListener(event ->
                Notification.show("Company's page will be added later.")
        );

        generatorButton.addClickListener(event ->
                Notification.show("E-mail generator page will be added later.")
        );

        profileButton.addClickListener(event ->
                Notification.show("Profile page will be added later.")
        );

        // TODO parlikt uz login
        logoutButton.addClickListener(event -> {
            VaadinSession.getCurrent().setAttribute(User.class, null);
            getUI().ifPresent(ui -> ui.navigate("register"));
        });

        shell.add(
                title,
                mainEmailsButton,
                companiesButton,
                generatorButton,
                profileButton,
                logoutButton
        );

        add(shell);
    }
	
	// TODO nomainīt pārvirzi uz login
	@Override
    public void beforeEnter(BeforeEnterEvent event) {
        User loggedInUser = VaadinSession.getCurrent().getAttribute(User.class);

        if (loggedInUser == null) {
            event.forwardTo("register");
        }
    }


    private Button createMenuButton(String text) {
        Button button = new Button(text);
        button.addClassName("main-menu-button");
        return button;
    }

}
