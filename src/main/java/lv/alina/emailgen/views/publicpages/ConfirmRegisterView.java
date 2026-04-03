package lv.alina.emailgen.views.publicpages;


import com.vaadin.flow.component.UI;
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
import com.vaadin.flow.server.VaadinSession;

import lv.alina.emailgen.models.User;
import lv.alina.emailgen.models.enums.VerificationCodeStatus;
import lv.alina.emailgen.service.ICRUDUserService;
import lv.alina.emailgen.service.IEmailService;
import lv.alina.emailgen.service.IRegistrationVerificationService;

@Route("register/confirm")
@PageTitle("Confirm Register")
@CssImport("./styles/auth-view.css")
public class ConfirmRegisterView extends VerticalLayout implements BeforeEnterObserver{
	
	private final ICRUDUserService userService;
	private final IRegistrationVerificationService verificationService;
	private final IEmailService emailService;
	
	private String email;
	private TextField verificationCodeField;
    private PasswordField passwordField;
    private PasswordField confirmPasswordField;
    private Paragraph message;
    private Paragraph emailInfo;
    private Button resendButton;
    
    public ConfirmRegisterView(ICRUDUserService userService, IRegistrationVerificationService verificationService, IEmailService emailService) {
        this.userService = userService;
        this.verificationService = verificationService;
        this.emailService = emailService;
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
            return;
        }
        
        try {
            boolean hasActiveCode = verificationService.hasActiveCode(email);

            if (!hasActiveCode) {
                event.forwardTo("register");
                return;
            }
            
            int secondsLeft = verificationService.getRemainingResendCooldownSeconds(email);
            if (secondsLeft > 0 && resendButton != null) {
                startResendCountdown(secondsLeft);
            }
            
        } catch (Exception e) {
            event.forwardTo("register");
            return;
        }
        
        if (emailInfo != null) {
            emailInfo.setText("A confirmation code was sent to " + email);
        }
        
        if (resendButton != null) {
            startResendCountdown(verificationService.getResendCooldownSeconds());
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
        
        emailInfo = new Paragraph();
        emailInfo.addClassName("auth-subtitle");

        VerticalLayout form = new VerticalLayout() ;
        form.setPadding(false);
        form.setSpacing(false);
        form.addClassName("auth-form");

        verificationCodeField = new TextField("Verification code");
        verificationCodeField.setPlaceholder("Enter verification code");
        verificationCodeField.setWidthFull();
        verificationCodeField.addClassName("auth-field");

        resendButton = new Button("Resend code");
        resendButton.addClassName("auth-link");
        resendButton.addClickListener(event -> handleResendCode());

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
        		resendButton,
                passwordField,
                confirmPasswordField,
                message,
                createButton
        );

        content.add(title, emailInfo, form);
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
        	
        	VerificationCodeStatus codeStatus = verificationService.getCodeStatus(email, code);
        	
        	if (codeStatus == VerificationCodeStatus.INVALID ) {
        		showError("Verification code is incorrect.");
        		return;
        	}
        	
        	if (codeStatus == VerificationCodeStatus.EXPIRED ) {
        		showError("Verification code has expired.");
        		return;
        	}
        	
        	
        	if (codeStatus == VerificationCodeStatus.NOT_FOUND ) {
        		showError("This e-mail has no active verification code.");
        		return;
        	}
        	
        	userService.registerUser(email, password);
        	User loggedInUser = userService.markUserLoggedIn(email);
        	
        	VaadinSession.getCurrent().setAttribute(User.class, loggedInUser);
        	
        	verificationService.removeCode(email);

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
    
    private void handleResendCode() {
        resetMessageState();

        if (email == null || email.isBlank()) {
            showError("E-mail is missing. Please return to register page.");
            return;
        }

        try {
        	boolean canResend = verificationService.canResendCode(email);
        	
        	if (!canResend) {
                showError("Please wait before requesting a new code.");
                return;
            }        	

            String newCode = verificationService.createAndStoreCode(email);
            emailService.sendVerificationCode(email, newCode);
            
            verificationCodeField.clear();

            showSuccess("A new verification code was sent to " + email);
            
            startResendCountdown(verificationService.getResendCooldownSeconds());
            
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }
    
    private void startResendCountdown(int seconds) {
        UI ui = UI.getCurrent();

        resendButton.setEnabled(false);

        final int[] timeLeft = {seconds};

        ui.setPollInterval(1000);

        ui.addPollListener(event -> {
            if (timeLeft[0] > 0) {
                int minutes = timeLeft[0] / 60;
                int secs = timeLeft[0] % 60;

                String formattedTime = String.format("%02d:%02d", minutes, secs);

                resendButton.setText("Resend code in " + formattedTime);

                timeLeft[0]--;
            } else {
                resendButton.setText("Resend code");
                resendButton.setEnabled(true);

                ui.setPollInterval(-1);
            }
        });
    }

}
