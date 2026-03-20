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
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import lv.alina.emailgen.service.ICRUDUserService;
import lv.alina.emailgen.service.IEmailService;
import lv.alina.emailgen.service.IRegistrationVerificationService;

@Route("register")
@PageTitle("Register")
@CssImport("./styles/auth-view.css")
public class RegisterView extends VerticalLayout{
	
	private final ICRUDUserService userService;
	private final IRegistrationVerificationService verificationService;
	private final IEmailService emailService;

    private EmailField emailField;
    private Paragraph message;
    private Anchor loginNowLink;
    private Button actionButton;

    private boolean alreadyRegisteredState = false;
    private String alreadyRegisteredEmail;

    public RegisterView(ICRUDUserService userService, IRegistrationVerificationService verificationService, IEmailService emailService) {
        this.userService = userService;
        this.verificationService = verificationService;
        this.emailService = emailService;
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
        topbar.addClassName("auth-topbar") ;

        Button loginTab = new Button("Login");
        loginTab.setPrefixComponent(new Icon(VaadinIcon.SIGN_IN));
        loginTab.addClassName("auth-tab");
        loginTab.addClickListener(event -> getUI().ifPresent(ui -> ui.navigate("login")));

        Button registerTab = new Button("Register");
        registerTab.setPrefixComponent(new Icon(VaadinIcon.USER));
        registerTab.addClassName("auth-tab");
        registerTab.addClassName("auth-tab-active");

        topbar.add(loginTab, registerTab);

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(false);
        content.setAlignItems(Alignment.CENTER);
        content.addClassName("auth-content");

        H2 title = new H2("Welcome!");
        title.addClassName("auth-title");

        Paragraph subtitle = new Paragraph(
            "When you create an account, you'll get a handy tool for testers - generate email addresses quickly, keep a history, and spend less time managing accounts."
        );
        subtitle.addClassName("auth-subtitle");

        VerticalLayout form = new VerticalLayout();
        form.setPadding(false);
        form.setSpacing(false);
        form.addClassName("auth-form");

        emailField = new EmailField("E-mail");
        emailField.setPlaceholder("Enter your e-mail");
        emailField.setClearButtonVisible(true);
        emailField.setWidthFull();
        emailField.addClassName("auth-field");
        emailField.setValueChangeMode(ValueChangeMode.EAGER);
        emailField.addValueChangeListener(event -> handleEmailFieldChange());

        Paragraph hint = new Paragraph("A confirmation code will be sent to this e-mail address.");
        hint.addClassName("auth-hint");

        message = new Paragraph();
        message.addClassName("auth-message");
        message.setVisible(false);

        loginNowLink = new Anchor("/login", "Log in now");
        loginNowLink.addClassName("auth-link");
        loginNowLink.setVisible(false);

        actionButton = new Button("Create");
        actionButton.addClassName("auth-primary-btn");
        actionButton.addClickListener(event -> handleAction());

        form.add(emailField, hint, message, loginNowLink, actionButton);
        content.add(title, subtitle, form);
        shell.add(topbar, content);
        add(shell);
    }

    private void handleAction() {
        if (alreadyRegisteredState) {
            resetForm();
            return;
        }

        resetMessageState();

        String email = emailField.getValue();

        if (email == null || email.isBlank()) {
            showError("E-mail is required.");
            return;
        }

        if (!isEmailFormatValid(email)) {
            showError("Please enter a valid e-mail address.");
            return;
        }

        try {
            boolean alreadyExists = userService.existsByEmail(email);
            if (alreadyExists) {
                showAlreadyRegisteredState();
                return;
            }

            String normalizedEmail = normalize(email);
            String code = verificationService.createAndStoreCode(normalizedEmail);
            emailService.sendVerificationCode(normalizedEmail, code);
                       
            // TODO izdzeest
            System.out.println("Verification code for " + normalizedEmail + ": " + code);

            getUI().ifPresent(ui -> ui.navigate("register/confirm?email=" + normalizedEmail));
            
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private void handleEmailFieldChange() {
        if (!alreadyRegisteredState) {
            return;
        }

        String currentValue = emailField.getValue();
        if (currentValue == null) {
            currentValue = "";
        }

        String normalizedCurrent = normalize(currentValue);
        String normalizedOriginal = normalize(alreadyRegisteredEmail);

        if (!normalizedCurrent.equals(normalizedOriginal)) {
            alreadyRegisteredState = false;
            alreadyRegisteredEmail = null;
            actionButton.setText("Create");
            resetMessageState();
        }
    }

    private boolean isEmailFormatValid(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }

        String normalizedEmail = email.trim();
        return normalizedEmail.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private void showAlreadyRegisteredState() {
        alreadyRegisteredState = true;
        alreadyRegisteredEmail = emailField.getValue();

        message.setText("Oops, looks like you already have account!");
        message.removeClassName("auth-message-success");
        message.addClassName("auth-message-error");
        message.setVisible(true);

        loginNowLink.setVisible(true);
        actionButton.setText("Try another e-mail");
    }

    private void resetForm(){
        alreadyRegisteredState = false;
        alreadyRegisteredEmail = null;
        emailField.clear();
        actionButton.setText("Create");
        resetMessageState();
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
        loginNowLink.setVisible(false);
    }
    
    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

}
