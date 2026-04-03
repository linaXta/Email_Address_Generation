package lv.alina.emailgen.views.publicpages;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import lv.alina.emailgen.models.User;
import lv.alina.emailgen.service.ICRUDUserService;

@Route("login")
@PageTitle("Login")
@CssImport("./styles/auth-view.css")
public class LoginView extends VerticalLayout{
	
	private final ICRUDUserService userService;
	
	private EmailField emailField;
    private PasswordField passwordField;
    private Paragraph message;
    
    public LoginView(ICRUDUserService userService) {
        this.userService = userService;
        buildLayout();
    }
    
    private void buildLayout() {
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        addClassName("auth-page");

        VerticalLayout shell = new VerticalLayout();
        shell.setPadding(false);
        shell.setSpacing(false);
        shell.addClassName("auth-shell");

        HorizontalLayout topbar = new HorizontalLayout();
        topbar.setPadding(false);
        topbar.setSpacing(true);
        topbar.setWidthFull();
        topbar.addClassName("auth-topbar");
        
        Button loginTab = new Button("Login");
        loginTab.setPrefixComponent(new Icon(VaadinIcon.SIGN_IN));
        loginTab.addClassName("auth-tab");
        loginTab.addClassName("auth-tab-active");

        Button registerTab = new Button("Register");
        registerTab.setPrefixComponent(new Icon(VaadinIcon.USER));
        registerTab.addClassName("auth-tab");
        registerTab.addClickListener(event -> getUI().ifPresent(ui -> ui.navigate("register")));

        topbar.add(loginTab, registerTab);

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(false);
        content.setAlignItems(Alignment.CENTER);
        content.addClassName("auth-content");

        H2 title = new H2("Welcome! Sign in to continue");
        title.addClassName("auth-title");

        VerticalLayout form = new VerticalLayout();
        form.setPadding(false);
        form.setSpacing(false);
        form.addClassName("auth-form");

        emailField = new EmailField("E-mail");
        emailField.setPlaceholder("Enter your e-mail");
        emailField.setClearButtonVisible(true);
        emailField.setWidthFull();
        emailField.addClassName("auth-field");

        passwordField = new PasswordField("Password");
        passwordField.setPlaceholder("Enter your password");
        passwordField.setWidthFull();
        passwordField.addClassName("auth-field");

        HorizontalLayout forgotPasswordRow = new HorizontalLayout();
        forgotPasswordRow.setPadding(false);
        forgotPasswordRow.setSpacing(false);
        forgotPasswordRow.setWidthFull();
        forgotPasswordRow.addClassName("auth-forgot-row");

        Paragraph forgotText = new Paragraph("Forgot password?");
        forgotText.addClassName("auth-forgot-text");

        Button forgotLink = new Button("Click here");
        forgotLink.addClassName("auth-link");
        forgotLink.addClickListener(event -> {
            String enteredEmail = emailField.getValue();

            if (enteredEmail == null || enteredEmail.isBlank()) {
                getUI().ifPresent(ui -> ui.navigate("forgot-password"));
            } else {
                String normalizedEmail = enteredEmail.trim().toLowerCase();
                getUI().ifPresent(ui -> ui.navigate("forgot-password?email=" + normalizedEmail));
            }
        });

        forgotPasswordRow.add(forgotText, forgotLink);

        message = new Paragraph();
        message.addClassName("auth-message");
        message.setVisible(false);

        Button loginButton = new Button("Log in");
        loginButton.addClassName("auth-primary-btn");
        loginButton.addClickListener(event -> handleLogin());

        form.add(emailField,passwordField, forgotPasswordRow, message,loginButton);

        content.add(title, form);
        shell.add(topbar, content);
        add(shell);
    }

    private void handleLogin() {
        resetMessageState();

        String email = emailField.getValue();
        String password = passwordField.getValue() ;

        if (email == null || email.isBlank()) {
            showError("E-mail is needed");
            return;
        }

        if (password == null || password.isBlank()) {
            showError("Password is needed");
            return;
        }

        try {
            User loggedInUser = userService.loginUser(email, password);

            VaadinSession.getCurrent().setAttribute(User.class, loggedInUser);

            getUI().ifPresent(ui -> ui.navigate("main"));

        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private void showError(String text) {
        message.setText(text);
        message.removeClassName("auth-message-success");
        message.addClassName("auth-message-error");
        message.setVisible(true);
    }

    private void resetMessageState() {
        message.setVisible(false);
        message.setText("");
        message.removeClassName("auth-message-error");
        message.removeClassName("auth-message-success");
    }

}
