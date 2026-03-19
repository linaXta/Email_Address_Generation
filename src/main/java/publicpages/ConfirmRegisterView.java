package publicpages;


import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import lv.alina.emailgen.service.ICRUDUserService;

@Route("register/confirm")
@PageTitle("Confirm Register")
@CssImport("./styles/auth-view.css")
public class ConfirmRegisterView extends VerticalLayout implements BeforeEnterObserver{
	
	private final ICRUDUserService userService;
	
	private String email;
	private TextField verificationCodeField;
    private PasswordField passwordField;
    private PasswordField confirmPasswordField;
    private Paragraph message;
    
    public ConfirmRegisterView(ICRUDUserService userService) {
        this.userService = userService;
        buildLayout();
    }
    
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        this.email = event.getLocation().getQueryParameters()   // tas butu ka ?email=test@mail.com
                .getParameters()								// TAS BŪTU KA "email" = ["test@mail.com"]
                .getOrDefault("email", java.util.List.of("") )
                .stream().findFirst().orElse("")
                .trim().toLowerCase();

        if (email.isBlank()) {
            event.forwardTo("register");
        }
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

        HorizontalLayout topLeft = new HorizontalLayout();
        topLeft.setPadding(false);
        topLeft.setSpacing(false);
        topLeft.setWidthFull();
        topLeft.addClassName("auth-top-left");

        Button backButton = new Button("Back");
        backButton.addClassName("auth-back-btn");
        backButton.addClickListener(event -> getUI().ifPresent(ui -> ui.navigate("register")));

        topLeft.add(backButton);

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(false);
        content.setAlignItems(Alignment.CENTER);
        content.addClassName("auth-content");

        H2 title = new H2("Confirm and create your Account!");
        title.addClassName("auth-title");

        VerticalLayout form = new VerticalLayout() ;
        form.setPadding(false);
        form.setSpacing(false);
        form.addClassName("auth-form");

        verificationCodeField = new TextField("Verification code");
        verificationCodeField.setPlaceholder("Enter verification code");
        verificationCodeField.setWidthFull();
        verificationCodeField.addClassName("auth-field");

        Paragraph resendHint = new Paragraph("Send Code again in 0:59");
        resendHint.addClassName("auth-link");

        passwordField = new PasswordField("Password");
        passwordField.setPlaceholder("Create password");
        passwordField.setWidthFull();
        passwordField.addClassName("auth-field");

        confirmPasswordField = new PasswordField("Password confirmation");
        confirmPasswordField.setPlaceholder("Confirm password");
        confirmPasswordField.setWidthFull();
        confirmPasswordField.addClassName("auth-field");

        message = new Paragraph();
        message.addClassName("auth-message");
        message.setVisible(false);

        Button createButton = new Button("Create");
        createButton.addClassName("auth-primary-btn");
        createButton.addClickListener(event -> handleCreate());

        form.add(verificationCodeField,
        		resendHint,
                passwordField,
                confirmPasswordField,
                message,
                createButton
        );

        content.add(title, form);
        shell.add(topLeft, content);
        add(shell);
    }

    private void handleCreate() {
        resetMessageState();

        String code = verificationCodeField.getValue();
        String password = passwordField.getValue();
        String confirmPassword = confirmPasswordField.getValue();

        if (email == null || email.isBlank()) {
            showError("E-mail is missing.Please return to register page.");
            return;
        }

        if (code == null || code.isBlank()){
            showError("Verification code is required.");
            return;
        }

        if (!"123456".equals(code.trim())){
            showError("Invalid verification code.");
            return;
        }

        if (password == null || password.isBlank()){
            showError("Password is required.");
            return;
        }

        if (confirmPassword == null || confirmPassword.isBlank()){
            showError("Password confirmation is required.");
            return;
        }

        if (!password.equals(confirmPassword)){
            showError("Passwords do not match.");
            return;
        }

        try {
            userService.registerUser(email, password);
            showSuccess("Account successfully created.");

            getUI().ifPresent(ui -> ui.navigate("login"));
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

    private void showSuccess(String text) {
        message.setText(text);
        message.removeClassName("auth-message-error");
        message.addClassName("auth-message-success");
        message.setVisible(true);
    }

    private void resetMessageState() {
        message.setVisible(false);
        message.setText("");
        message.removeClassName("auth-message-error");
        message.removeClassName("auth-message-success");
    }

}
