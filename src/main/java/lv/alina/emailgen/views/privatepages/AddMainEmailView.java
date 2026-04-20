package lv.alina.emailgen.views.privatepages;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import lv.alina.emailgen.models.MainEmail;
import lv.alina.emailgen.models.User;
import lv.alina.emailgen.service.ICRUDMainEmailService;


@Route("main-emails/add")
@PageTitle("Add main e-mail")
@CssImport("./styles/main-emails-view.css")
public class AddMainEmailView extends VerticalLayout implements BeforeEnterObserver {
	
	private final ICRUDMainEmailService mainEmailService;
	
	private TextField emailField;
	
	public AddMainEmailView(ICRUDMainEmailService mainEmailService) {
        this.mainEmailService = mainEmailService;

        buildLayout();
	}
	
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        User loggedInUser = VaadinSession.getCurrent().getAttribute(User.class);

        if (loggedInUser == null) {
            event.forwardTo("login");
        }
    }
    
    private void buildLayout() {
    	addClassName("main-emails-page");
    	setSizeFull();
    	setPadding(false);
        setSpacing(false);
        setAlignItems(Alignment.CENTER);
        
        VerticalLayout shell = new VerticalLayout();
        shell.addClassName("main-emails-shell");
        shell.setPadding(false);
        shell.setSpacing(false);
        
        HorizontalLayout topBar = new HorizontalLayout();
        topBar.addClassName("main-emails-topbar");
        topBar.setWidthFull();
        topBar.setAlignItems(Alignment.CENTER);
        topBar.setJustifyContentMode(JustifyContentMode.BETWEEN);

        Button menuButton = new Button("MENU");
        menuButton.addClassName("main-emails-menu-button");
        menuButton.addClickListener(event -> getUI().ifPresent(ui -> ui.navigate("main")));

        Button backButton = new Button("BACK");
        backButton.addClassName("main-emails-menu-button");
        backButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("main-emails")));

        topBar.add(menuButton, backButton);
        
        VerticalLayout addEmailContent = new VerticalLayout();
        addEmailContent.addClassName("main-emails-add-content");
        addEmailContent.setWidthFull();
        addEmailContent.setPadding(false);
        addEmailContent.setSpacing(false);
        addEmailContent.setAlignItems(Alignment.CENTER);

        Span title = new Span("Add new Main e-mail address");
        title.addClassName("main-emails-form-title");

        emailField = new TextField();
        emailField.setPlaceholder("Input e-mail");
        emailField.addClassName("main-emails-form-input");

        Span description = new Span("This will be your base for generating test e-mail addresses");
        description.addClassName("main-emails-form-description");

        Button addBtn = new Button("ADD");
        addBtn.addClassName("main-emails-add-button");
        addBtn.addClickListener(e -> addEmail());

        addEmailContent.add(title, emailField, description, addBtn);

        shell.add(topBar, addEmailContent);
        add(shell);          	
    }
    
    private void addEmail() {
    	User user = VaadinSession.getCurrent().getAttribute(User.class);
        if (user == null) {
            getUI().ifPresent(ui -> ui.navigate("login"));
            return;
        }
        
        String email = emailField.getValue();

        if (email == null || email.isBlank()) {
            Notification.show("Enter e-mail");
            return;
        }
        
        email = email.trim();
        if (!email.contains("@") || email.startsWith("@") || email.endsWith("@")) {
            Notification.show("Enter a valid e-mail");
            return;
        }

        if (mainEmailService.existsExact(user, email)) {
            Notification.show("E-mail already exists");
            return;
        }

        MainEmail newEmail = new MainEmail();
        newEmail.setUser(user);
        newEmail.setMainEmail(email);

        mainEmailService.add(newEmail);

        Notification.show("Added");
        getUI().ifPresent(ui -> ui.navigate("main-emails"));
    }

}
