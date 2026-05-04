package lv.alina.emailgen.views.privatepages;

import java.util.Optional;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import lv.alina.emailgen.models.MainEmail;
import lv.alina.emailgen.models.User;
import lv.alina.emailgen.service.ICRUDMainEmailService;
import lv.alina.emailgen.service.ICRUDUserService;

@Route("main-emails/delete/:id")
@PageTitle("Delete main e-mail")
@CssImport("./styles/main-emails-view.css")
public class DeleteMainEmailView extends VerticalLayout implements BeforeEnterObserver{
	
	// TODO pievienot history un pie delete genrations skatu 
	
	private final ICRUDMainEmailService mainEmailService;
	private final ICRUDUserService userService;

	
	private MainEmail currentMainEmail;
    private Span emailTitle;
    private Checkbox deleteGeneratedCheckbox;
    private PasswordField passwordField;
	 
    public DeleteMainEmailView(ICRUDMainEmailService mainEmailService, ICRUDUserService userService) {
        this.mainEmailService = mainEmailService;
        this.userService = userService;
        buildLayout();
    }
	

	
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        User loggedInUser = VaadinSession.getCurrent().getAttribute(User.class);

        if (loggedInUser == null) {
            event.forwardTo("login");
            return;
        }

        Optional<Long> id = event.getRouteParameters().get("id").map(Long::valueOf);

        if (id.isEmpty()) {
            Notification.show("Invalid main e-mail id");
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

        if (emailTitle != null) {
            emailTitle.setText(currentMainEmail.getMainEmail());
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
        content.addClassName("main-emails-delete-content");
        content.setWidthFull();
        content.setPadding(false);
        content.setSpacing(false);
        content.setAlignItems(Alignment.CENTER);

        Span title = new Span("Are you sure you want to delete this main e-mail?");
        title.addClassName("main-emails-delete-question");

        emailTitle = new Span("Main e-mail");
        emailTitle.addClassName("main-emails-delete-title");

        Span description = new Span( "This will remove the main e-mail from your account and prevent it from being used for future address generation." );
        description.addClassName("main-emails-delete-description");

        VerticalLayout infoList = new VerticalLayout();
        infoList.addClassName("main-emails-delete-info-list");
        infoList.setPadding(false);
        infoList.setSpacing(false);

        Span info1 = new Span("• Previously generated addresses that reference this main e-mail will remain unless you remove them separately.");
        Span info2 = new Span("• To delete those generated addresses now, check the box below.");
        Span info3 = new Span("• This action cannot be undone.");

        info1.addClassName("main-emails-delete-info-item");
        info2.addClassName("main-emails-delete-info-item");
        info3.addClassName("main-emails-delete-info-item-bold");

        infoList.add(info1, info2, info3);

        deleteGeneratedCheckbox = new Checkbox();
        deleteGeneratedCheckbox.addClassName("main-emails-delete-checkbox");

        Span checkboxText = new Span("Also delete all generated addresses that reference this main e-mail");
        checkboxText.addClassName("main-emails-delete-checkbox-text");

        HorizontalLayout checkboxRow = new HorizontalLayout(deleteGeneratedCheckbox, checkboxText);
        checkboxRow.addClassName("main-emails-delete-checkbox-row");
        checkboxRow.setAlignItems(Alignment.CENTER);
        checkboxRow.setJustifyContentMode(JustifyContentMode.CENTER);

        Span generatedLink = new Span("Show addresses generated under this main e-mail");
        generatedLink.addClassName("main-emails-delete-link");

        HorizontalLayout actions = new HorizontalLayout();
        actions.addClassName("main-emails-delete-actions");
        actions.setAlignItems(Alignment.CENTER);
        actions.setJustifyContentMode(JustifyContentMode.CENTER);
        

        Button cancelButton = new Button("CANCEL");
        cancelButton.addClassName("main-emails-cancel-button");
        cancelButton.addClickListener(event -> getUI().ifPresent(ui -> ui.navigate("main-emails")));

        Button deleteButton = new Button("DELETE");
        deleteButton.addClassName("main-emails-delete-danger-button");
        deleteButton.addClickListener(event -> openPasswordDialog());

        actions.add(cancelButton, deleteButton);

        content.add( title,emailTitle, description, infoList, checkboxRow, generatedLink, actions);

        shell.add(topBar, content);
        add(shell);
    }

    private void openPasswordDialog() {
        Dialog dialog = new Dialog();
        dialog.addClassName("main-emails-password-dialog");
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);

        Span title = new Span("To confirm deletion, enter your password!");
        title.addClassName("main-emails-password-dialog-title");

        passwordField = new PasswordField("Password");
        passwordField.setPlaceholder("Enter your password");
        passwordField.addClassName("main-emails-password-field");

        Button deleteButton = new Button("DELETE");
        deleteButton.addClassName("main-emails-delete-danger-button");
        deleteButton.addClickListener(event -> confirmDelete(dialog));

        Button cancelButton = new Button("CANCEL");
        cancelButton.addClassName("main-emails-cancel-button");
        cancelButton.addClickListener(event -> dialog.close());

        HorizontalLayout buttons = new HorizontalLayout(deleteButton, cancelButton);
        buttons.addClassName("main-emails-password-dialog-buttons");
        buttons.setAlignItems(Alignment.CENTER);
        buttons.setJustifyContentMode(JustifyContentMode.CENTER);

        VerticalLayout content = new VerticalLayout(title, passwordField, buttons);
        content.addClassName("main-emails-password-dialog-content");
        content.setPadding(false);
        content.setSpacing(false);
        content.setAlignItems(Alignment.CENTER);

        dialog.add(content);
        dialog.open();
    }

    private void confirmDelete(Dialog dialog) {
        User user = VaadinSession.getCurrent().getAttribute(User.class);

        if (user == null) {
            getUI().ifPresent(ui -> ui.navigate("login"));
            return;
        }

        if (currentMainEmail == null) {
            Notification.show("Main e-mail not loaded");
            return;
        }

        String password = passwordField.getValue();

        try {
            if (!userService.isPasswordCorrect(user, password)) {
                Notification.show("Incorrect password");
                return;
            }

            if (deleteGeneratedCheckbox.getValue()) {
                mainEmailService.deleteWithGenerated(currentMainEmail);
                Notification.show("Main e-mail and generated e-mails deleted");
            } else {
                mainEmailService.deleteMainOnly(currentMainEmail);
                Notification.show("Main e-mail deleted");
            }

            dialog.close();
            getUI().ifPresent(ui -> ui.navigate("main-emails"));

        } catch (Exception e) {
            Notification.show("Delete failed: " + e.getMessage());
        }
    }
}
