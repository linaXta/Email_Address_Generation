package lv.alina.emailgen.views.privatepages;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.Autocomplete;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import lv.alina.emailgen.models.User;
import lv.alina.emailgen.service.ICRUDUserService;

@Route("profile")
@PageTitle("Profile")
@CssImport("./styles/profile-view.css")
public class ProfileView extends VerticalLayout implements BeforeEnterObserver {

    private final ICRUDUserService userService;

    private User loggedInUser;

    private Span emailValue;
    private TextField fullNameField;

    public ProfileView(ICRUDUserService userService) {
        this.userService = userService;
        buildLayout();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        loggedInUser = VaadinSession.getCurrent().getAttribute(User.class);

        if (loggedInUser == null) {
            event.forwardTo("login");
            return;
        }

        refreshUserData();
    }

    private void buildLayout() {
        addClassName("profile-page");
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        setAlignItems(Alignment.CENTER);

        VerticalLayout shell = new VerticalLayout();
        shell.addClassName("profile-shell");
        shell.setPadding(false);
        shell.setSpacing(false);

        HorizontalLayout topBar = createTopBar();

        Span title = new Span("Profile");
        title.addClassName("profile-title");

        HorizontalLayout cards = new HorizontalLayout();
        cards.addClassName("profile-cards");
        cards.setWidthFull();
        cards.setAlignItems(Alignment.START);
        cards.setJustifyContentMode(JustifyContentMode.CENTER);

        VerticalLayout accountCard = createAccountCard();
        VerticalLayout securityCard = createSecurityCard();

        cards.add(accountCard, securityCard);

        shell.add(topBar, title, cards);
        add(shell);
    }

    private HorizontalLayout createTopBar() {
        HorizontalLayout topBar = new HorizontalLayout();
        topBar.addClassName("profile-topbar");
        topBar.setWidthFull();
        topBar.setAlignItems(Alignment.CENTER);
        topBar.setJustifyContentMode(JustifyContentMode.BETWEEN);

        Button menuButton = new Button("MENU");
        menuButton.addClassName("profile-menu-button");
        menuButton.addClickListener(event -> getUI().ifPresent(ui -> ui.navigate("main")));

        topBar.add(menuButton);

        return topBar;
    }

    private VerticalLayout createAccountCard() {
        VerticalLayout card = new VerticalLayout();
        card.addClassName("profile-card");
        card.setPadding(false);
        card.setSpacing(false);

        Span cardTitle = new Span("Account information");
        cardTitle.addClassName("profile-card-title");

        Span emailLabel = new Span("E-mail");
        emailLabel.addClassName("profile-label");

        emailValue = new Span("-");
        emailValue.addClassName("profile-value-box");

        Button changeEmailButton = new Button("CHANGE E-MAIL");
        changeEmailButton.addClassName("profile-secondary-button");
        changeEmailButton.addClickListener(event -> openChangeEmailDialog());

        Span fullNameLabel = new Span("Full name");
        fullNameLabel.addClassName("profile-label");

        fullNameField = new TextField();
        fullNameField.setPlaceholder("Full name");
        fullNameField.setAutocomplete(Autocomplete.OFF);
        fullNameField.addClassName("profile-input");

        Button saveNameButton = new Button("SAVE NAME");
        saveNameButton.addClassName("profile-main-button");
        saveNameButton.addClickListener(event -> saveFullName());

        card.add(cardTitle, emailLabel, emailValue, changeEmailButton, fullNameLabel, fullNameField, saveNameButton);
        return card;
    }

    private VerticalLayout createSecurityCard() {
        VerticalLayout card = new VerticalLayout();
        card.addClassName("profile-card");
        card.setPadding(false);
        card.setSpacing(false);

        Span cardTitle = new Span("Security");
        cardTitle.addClassName("profile-card-title");

        Button changePasswordButton = new Button("CHANGE PASSWORD");
        changePasswordButton.addClassName("profile-main-button");
        changePasswordButton.addClickListener(event -> openChangePasswordDialog());

        Button enable2faButton = new Button("ENABLE 2FA");
        enable2faButton.addClassName("profile-main-button");
        enable2faButton.addClickListener(event -> Notification.show("2FA will be added later"));

        Button deleteAccountButton = new Button("DELETE ACCOUNT");
        deleteAccountButton.addClassName("profile-danger-button");
        deleteAccountButton.addClickListener(event -> openDeleteAccountDialog());

        card.add(cardTitle, changePasswordButton, enable2faButton, deleteAccountButton);
        return card;
    }

    private void refreshUserData() {
        if (loggedInUser == null) {
            return;
        }

        emailValue.setText(loggedInUser.getEmail());

        if (loggedInUser.getFullName() == null) {
            fullNameField.clear();
        } else {
            fullNameField.setValue(loggedInUser.getFullName());
        }
    }

    private void saveFullName() {
        if (loggedInUser == null) {
            getUI().ifPresent(ui -> ui.navigate("login"));
            return;
        }

        try {
            User updatedUser = userService.updateFullName(loggedInUser, fullNameField.getValue());

            VaadinSession.getCurrent().setAttribute(User.class, updatedUser);
            loggedInUser = updatedUser;

            Notification.show("Full name updated");

        } catch (Exception e) {
            Notification.show(e.getMessage());
        }
    }

    private void openChangePasswordDialog() {
        Dialog dialog = new Dialog();
        dialog.addClassName("profile-dialog");

        Span title = new Span("Change password");
        title.addClassName("profile-dialog-title");

        PasswordField currentPasswordField = new PasswordField("Current password");
        currentPasswordField.setPlaceholder("Current password");
        currentPasswordField.addClassName("profile-input");
        currentPasswordField.getElement().setAttribute("autocomplete", "new-password");
        currentPasswordField.getElement().setAttribute("name", "profile-current-password");
        currentPasswordField.getElement().setAttribute("id", "profile-current-password");

        PasswordField newPasswordField = new PasswordField("New password");
        newPasswordField.setPlaceholder("New password");
        newPasswordField.addClassName("profile-input");
        newPasswordField.getElement().setAttribute("autocomplete", "new-password");
        newPasswordField.getElement().setAttribute("name", "profile-new-password");
        newPasswordField.getElement().setAttribute("id", "profile-new-password");

        PasswordField repeatPasswordField = new PasswordField("Repeat new password");
        repeatPasswordField.setPlaceholder("Repeat new password");
        repeatPasswordField.addClassName("profile-input");
        repeatPasswordField.getElement().setAttribute("autocomplete", "new-password");
        repeatPasswordField.getElement().setAttribute("name", "profile-repeat-password");
        repeatPasswordField.getElement().setAttribute("id", "profile-repeat-password");

        Button saveButton = new Button("SAVE");
        saveButton.addClassName("profile-main-button");
        saveButton.addClickListener(event -> {
            try {
                userService.changePassword(
                        loggedInUser,
                        currentPasswordField.getValue(),
                        newPasswordField.getValue(),
                        repeatPasswordField.getValue()
                );

                dialog.close();
                Notification.show("Password changed");

            } catch (Exception e) {
                Notification.show(e.getMessage());
            }
        });

        Button cancelButton = new Button("CANCEL");
        cancelButton.addClassName("profile-secondary-button");
        cancelButton.addClickListener(event -> dialog.close());

        HorizontalLayout buttons = new HorizontalLayout(saveButton, cancelButton);
        buttons.addClassName("profile-dialog-buttons");

        VerticalLayout content = new VerticalLayout(title, currentPasswordField, newPasswordField, repeatPasswordField, buttons);
        content.addClassName("profile-dialog-content");
        content.setPadding(false);
        content.setSpacing(false);
        content.setAlignItems(Alignment.CENTER);

        dialog.add(content);
        dialog.open();
    }
    
    private void openChangeEmailDialog() {
        Dialog dialog = new Dialog();
        dialog.addClassName("profile-dialog");

        Span title = new Span("Change e-mail");
        title.addClassName("profile-dialog-title");

        PasswordField currentPasswordField = new PasswordField("Current password");
        currentPasswordField.setPlaceholder("Current password");
        currentPasswordField.addClassName("profile-input");
        currentPasswordField.getElement().setAttribute("autocomplete", "new-password");
        currentPasswordField.getElement().setAttribute("name", "profile-email-change-password");
        currentPasswordField.getElement().setAttribute("id", "profile-email-change-password");

        TextField newEmailField = new TextField("New e-mail");
        newEmailField.setPlaceholder("New e-mail");
        newEmailField.setAutocomplete(Autocomplete.OFF);
        newEmailField.addClassName("profile-input");

        TextField codeField = new TextField("Verification code");
        codeField.setPlaceholder("Code from new e-mail");
        codeField.setAutocomplete(Autocomplete.OFF);
        codeField.addClassName("profile-input");
        codeField.setVisible(false);

        Button sendCodeButton = new Button("SEND CODE");
        sendCodeButton.addClassName("profile-main-button");

        Button saveButton = new Button("SAVE");
        saveButton.addClassName("profile-main-button");
        saveButton.setVisible(false);

        sendCodeButton.addClickListener(event -> {
            try {
                userService.requestEmailChange(loggedInUser, currentPasswordField.getValue(), newEmailField.getValue());

                codeField.setVisible(true);
                saveButton.setVisible(true);
                sendCodeButton.setVisible(false);

                currentPasswordField.setEnabled(false);
                newEmailField.setEnabled(false);

                Notification.show("Verification code sent to new e-mail");

            } catch (Exception e) {
                Notification.show(e.getMessage());
            }
        });

        saveButton.addClickListener(event -> {
            try {
                User updatedUser = userService.confirmEmailChange(loggedInUser, newEmailField.getValue(), codeField.getValue() );

                VaadinSession.getCurrent().setAttribute(User.class, updatedUser);
                loggedInUser = updatedUser;

                emailValue.setText(updatedUser.getEmail());

                dialog.close();
                Notification.show("E-mail updated");

            } catch (Exception e) {
                Notification.show(e.getMessage());
            }
        });

        Button cancelButton = new Button("CANCEL");
        cancelButton.addClassName("profile-secondary-button");
        cancelButton.addClickListener(event -> dialog.close());

        HorizontalLayout buttons = new HorizontalLayout(sendCodeButton, saveButton, cancelButton);
        buttons.addClassName("profile-dialog-buttons");
        buttons.setWidthFull();
        buttons.setAlignItems(Alignment.CENTER);
        buttons.setJustifyContentMode(JustifyContentMode.CENTER);

        VerticalLayout content = new VerticalLayout(title, currentPasswordField, newEmailField, codeField, buttons);

        content.addClassName("profile-dialog-content");
        content.setPadding(false);
        content.setSpacing(false);
        content.setAlignItems(Alignment.CENTER);

        dialog.add(content);
        dialog.open();
    }
    
    private void openDeleteAccountDialog() {
        Dialog dialog = new Dialog();
        dialog.addClassName("profile-dialog");

        Span title = new Span("Delete account");
        title.addClassName("profile-dialog-title");

        Span warning = new Span("This will permanently delete your account and all related data. This action cannot be undone.");
        warning.addClassName("profile-warning-text");
        
        Checkbox confirmCheckbox = new Checkbox("I understand that this action cannot be undone");
        confirmCheckbox.addClassName("profile-delete-checkbox");

        PasswordField passwordField = new PasswordField("Current password");
        passwordField.setPlaceholder("Current password");
        passwordField.addClassName("profile-input");
        passwordField.getElement().setAttribute("autocomplete", "new-password");
        passwordField.getElement().setAttribute("name", "profile-delete-account-password");
        passwordField.getElement().setAttribute("id", "profile-delete-account-password");

        Button deleteButton = new Button("DELETE ACCOUNT");
        deleteButton.addClassName("profile-danger-button");
        deleteButton.addClickListener(event -> {
            try {
            	if (!confirmCheckbox.getValue()) {
                    Notification.show("Please confirm that you understand this action cannot be undone");
                    return;
                }
            	
                if (!userService.isPasswordCorrect(loggedInUser, passwordField.getValue())) {
                    Notification.show("Current password is incorrect");
                    return;
                }         

                Long userId = loggedInUser.getUserId();

                userService.deleteById(userId);

                VaadinSession.getCurrent().setAttribute(User.class, null);

                dialog.close();

                Notification.show("Account deleted");
                getUI().ifPresent(ui -> ui.navigate("login"));

            } catch (Exception e) {
                Notification.show(e.getMessage());
            }
        });

        Button cancelButton = new Button("CANCEL");
        cancelButton.addClassName("profile-secondary-button");
        cancelButton.addClickListener(event -> dialog.close());

        HorizontalLayout buttons = new HorizontalLayout(deleteButton, cancelButton);
        buttons.addClassName("profile-delete-buttons");
        buttons.setWidthFull();
        buttons.setAlignItems(Alignment.CENTER);
        buttons.setJustifyContentMode(JustifyContentMode.CENTER);

        VerticalLayout content = new VerticalLayout(title, warning, confirmCheckbox, passwordField, buttons);

        content.addClassName("profile-dialog-content");
        content.setPadding(false);
        content.setSpacing(false);
        content.setAlignItems(Alignment.CENTER);

        dialog.add(content);
        dialog.open();
    }
}
