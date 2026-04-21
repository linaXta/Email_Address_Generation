package lv.alina.emailgen.views.privatepages;

import java.util.Optional;

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
import lv.alina.emailgen.models.MainEmailHistory;
import lv.alina.emailgen.models.User;
import lv.alina.emailgen.service.ICRUDMainEmailHistoryService;
import lv.alina.emailgen.service.ICRUDMainEmailService;

@Route("main-emails/edit/:id")
@PageTitle("Edit main e-mail")
@CssImport("./styles/main-emails-view.css")
public class EditMainEmailView extends VerticalLayout implements BeforeEnterObserver {
	
	private final ICRUDMainEmailService mainEmailService;
	private final ICRUDMainEmailHistoryService mainEmailHistoryService;

    private TextField emailField;
    private MainEmail currentMainEmail;

    public EditMainEmailView(ICRUDMainEmailService mainEmailService, ICRUDMainEmailHistoryService mainEmailHistoryService) {
        this.mainEmailService = mainEmailService;
        this.mainEmailHistoryService = mainEmailHistoryService;
        buildLayout();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        User loggedInUser = VaadinSession.getCurrent().getAttribute(User.class);
        if (loggedInUser == null) {
            event.forwardTo("login");
            return;
        }

        Optional<Long> id = event.getRouteParameters()
                .get("id")
                .map(Long::valueOf);

        if (id.isEmpty()) {
            Notification.show("Invalid main e-mal id");
            event.forwardTo("main-emails");
            return;
        }

        Optional<MainEmail> mainEmailOptional = mainEmailService.findById(id.get());

        if (mainEmailOptional.isEmpty()) {
            Notification.show("Main e-mail not found");
            event.forwardTo("main-emails");
            return;
        }

        currentMainEmail = mainEmailOptional.get();

        if (!currentMainEmail.getUser().getUserId().equals(loggedInUser.getUserId())) {
            Notification.show("Access denied");
            event.forwardTo("main-emails");
            return;
        }

        if (emailField != null) {
            emailField.setValue(currentMainEmail.getMainEmail());
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
        backButton.addClickListener(event -> getUI().ifPresent(ui -> ui.navigate("main-emails")));

        topBar.add(menuButton, backButton);

        VerticalLayout content = new VerticalLayout();
        content.addClassName("main-emails-add-content");
        content.setWidthFull();
        content.setPadding(false);
        content.setSpacing(false);
        content.setAlignItems(Alignment.CENTER);

        Span title = new Span("Edit Main e-mail address");
        title.addClassName("main-emails-form-title");

        emailField = new TextField();
        emailField.setPlaceholder("Input e-mail");
        emailField.addClassName("main-emails-form-input");

        Span description = new Span("Update your base e-mail address");
        description.addClassName("main-emails-form-description");

        Button saveButton = new Button("SAVE");
        saveButton.addClassName("main-emails-add-button");
        saveButton.addClickListener(event -> updateEmail());

        content.add(title, emailField, description, saveButton);

        shell.add(topBar, content);
        add(shell);
    }

    private void updateEmail() {
        User user = VaadinSession.getCurrent().getAttribute(User.class);

        if (user == null) {
            getUI().ifPresent(ui -> ui.navigate("login"));
            return;
        }

        if (currentMainEmail == null) {
            Notification.show("Main e-mail not loaded");
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

        boolean changed = !currentMainEmail.getMainEmail().equals(email);

        if (changed && mainEmailService.existsExact(user, email)) {
            Notification.show("E-mail already exists");
            return;
        }

        String oldEmail = currentMainEmail.getMainEmail();

        currentMainEmail.setMainEmail(email);
        mainEmailService.add(currentMainEmail);

        MainEmailHistory history = new MainEmailHistory();
        history.setMainEmail(currentMainEmail);
        history.setOldValue(oldEmail);
        history.setNewValue(email);
        history.setActionType(MainEmailHistory.ActionType.UPDATE);

        mainEmailHistoryService.add(history);

        Notification.show("Updated");
        getUI().ifPresent(ui -> ui.navigate("main-emails"));
    }


}
