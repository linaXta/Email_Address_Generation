package lv.alina.emailgen.views.publicpages;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import lv.alina.emailgen.service.ICRUDUserService;
import lv.alina.emailgen.service.IEmailService;
import lv.alina.emailgen.service.IRegistrationVerificationService;

@Route("forgot-password")
@PageTitle("Forgot Password")
@CssImport("./styles/auth-view.css")
public class ForgotPasswordView extends VerticalLayout implements BeforeEnterObserver{
	
	private final ICRUDUserService userService;
    private final IRegistrationVerificationService verificationService;
    private final IEmailService emailService;

    private EmailField emailField;
    private Paragraph message;
    
    public ForgotPasswordView(ICRUDUserService userService, IRegistrationVerificationService verificationService, IEmailService emailService) {
		this.userService = userService;
		this.verificationService = verificationService;
		this.emailService = emailService;
		buildLayout();
	}
    
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String emailFromQuery = event.getLocation()
                .getQueryParameters()
                .getParameters()
                .getOrDefault("email", java.util.List.of(""))
                .stream()
                .findFirst()
                .orElse("")
                .trim()
                .toLowerCase();

        if (emailField != null && !emailFromQuery.isBlank()) {
            emailField.setValue(emailFromQuery);
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
        backButton.addClickListener(event -> getUI().ifPresent(ui -> ui.navigate("login")));

        topLeft.add(backButton);

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(false);
        content.setAlignItems(Alignment.CENTER);
        content.addClassName("auth-content");

        H2 title = new H2("Forgot your password?");
        title.addClassName("auth-title");

        Paragraph subtitle = new Paragraph("Enter the email address you used to sign up and we'll send a confirmation code to reset your password.");
        subtitle.addClassName("auth-subtitle");

        VerticalLayout form = new VerticalLayout();
        form.setPadding(false);
        form.setSpacing(false);
        form.addClassName("auth-form");

        emailField = new EmailField("E-mail");
        emailField.setPlaceholder("Enter your e-mail");
        emailField.setWidthFull();
        emailField.addClassName("auth-field");

        message = new Paragraph();
        message.addClassName("auth-message");
        message.setVisible(false);

        Button sendButton = new Button("Send confirmation code");
        sendButton.addClassName("auth-primary-btn");
        sendButton.addClickListener(event -> handleSendCode());

        form.add(emailField, message, sendButton);
        content.add(title, subtitle, form);
        shell.add(topLeft, content);
        add(shell);
    }

    private void handleSendCode() {
        resetMessageState();

        String email = emailField.getValue();

        if (email == null || email.isBlank()) {
            showError("E-mail is required.");
            return;
        }

        try {
            boolean userExists = userService.existsByEmail(email);

            if (!userExists) {
                showError("User with this e-mail was not found.");
                return;
            }

            String normalizedEmail = email.trim().toLowerCase();

            String code = verificationService.createAndStoreCode(normalizedEmail);
            emailService.sendVerificationCode(normalizedEmail, code);

            getUI().ifPresent(ui -> ui.navigate("forgot-password/confirm?email=" + normalizedEmail));

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
