package lv.alina.emailgen.views.admin;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import lv.alina.emailgen.service.ICRUDUserService;

@Route("register")
@PageTitle("Register")
public class RegisterView extends VerticalLayout{
	
	private final ICRUDUserService userService;
	
	private EmailField emailField;
    private PasswordField passwordField;
    private PasswordField confirmPasswordField;
    private Button createButton;
    private Paragraph message;

    public RegisterView(ICRUDUserService userService) {
        this.userService = userService;

        buildLayout();
    }

    private void buildLayout() {
    	setSizeFull();
    	setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        
        H2 title = new H2("Create account");
        
        Paragraph description = new Paragraph("Create your account to use the email generation system.");
        
        emailField = new EmailField("E-mail");
        emailField.setPlaceholder("Enter your e-mail");
        emailField.setClearButtonVisible(true);
        emailField.setWidth("320px");
        
        passwordField = new PasswordField("Password");
        passwordField.setPlaceholder("Enter password");
        passwordField.setWidth("320px");
        
        confirmPasswordField = new PasswordField("Confirm password");
        confirmPasswordField.setPlaceholder("Repeat password");
        confirmPasswordField.setWidth("320px");
        
        createButton = new Button("Create");
        createButton.addClickListener(event -> registerUser());
        
        message = new Paragraph();
        message.setVisible(false);
        
        VerticalLayout formLayout = new VerticalLayout(
                title,
                description,
                emailField,
                passwordField,
                confirmPasswordField,
                createButton,
                message
            );

        formLayout.setSpacing(true);
        formLayout.setPadding(true);
        formLayout.setWidth("420px");
        formLayout.getStyle()
            .set("border", "1px solid #d6d6d6")
            .set("border-radius", "12px")
            .set("background-color", "#faf6ef")
            .set("box-shadow", "0 2px 8px rgba(0,0,0,0.08)");
        formLayout.setAlignItems(Alignment.CENTER);
        
        add(formLayout);
    }
    
    private void registerUser() {
    	message.setVisible(false);
    	
    	String email = emailField.getValue();
        String password = passwordField.getValue();
        String confirmPassword = confirmPasswordField.getValue();
        
        if (email == null || email.isBlank()) {
            showError("Email is required.");
            return;
        }
        
        if (password == null || password.isBlank()) {
            showError("Password is required.");
            return;
        }

        if (confirmPassword == null || confirmPassword.isBlank()) {
            showError("Password confirmation is required.");
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match.");
            return;
        }
        
        try {
            userService.registerUser(email, password);

            showSuccess("Account successfully created.");

            emailField.clear();
            passwordField.clear();
            confirmPasswordField.clear();

        } catch (Exception e) {
            showError(e.getMessage());
        }
    }
    
    private void showError(String text) {
        message.setText(text);
        message.getStyle().set("color", "red");
        message.setVisible(true);

        Notification.show(text, 3000, Position.TOP_CENTER);
    }

    private void showSuccess(String text) {
        message.setText(text);
        message.getStyle().set("color", "green");
        message.setVisible(true);

        Notification.show(text, 3000, Position.TOP_CENTER);
    }
    
    

}
