package lv.alina.emailgen.views.admin;

import java.util.ArrayList;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

import lv.alina.emailgen.models.User;
import lv.alina.emailgen.service.ICRUDUserService;


@Route("admin/users")
@CssImport("./styles/admin.css")
public class UserAdminView extends VerticalLayout {
	
	private final ICRUDUserService userService;
	
    private Grid<User> grid;
    
    private TextField searchField;
    private Button refreshBtn;

    private TextField emailField;
    private TextField fullNameField;
    private TextField passwordHashField;
    private Checkbox mfaField;

    private Button createBtn;
    private Button updateBtn;
    private Button deleteBtn;
    private Button clearBtn;
    
    private ArrayList<User> allUsers = new ArrayList<>();
    private Long selectedUserId = null;

	
	public UserAdminView(ICRUDUserService userService) {
		this.userService = userService;
		
	    addClassName("admin-page");
	    setSizeFull();

	    VerticalLayout card = new VerticalLayout();
	    card.addClassName("admin-card");
	    card.setWidthFull();
	    card.setMaxWidth("1100px");

	    H1 title = new H1("User admin panel");
	    title.addClassName("admin-title");
	    card.add(title);

	    // Augšēja sadaļa
	    searchField = new TextField("Search email");
	    searchField.setPlaceholder("type part of email...");
	    searchField.setClearButtonVisible(true);

	    refreshBtn = new Button("Refresh");
	    refreshBtn.addClassName("btn");

	    HorizontalLayout topBar = new HorizontalLayout(searchField, refreshBtn);
	    topBar.addClassName("topbar");
	    topBar.setWidthFull();
	    topBar.setAlignItems(Alignment.END); // pielīdzināt apakšai
	    card.add(topBar);

	    // tabula
        grid = new Grid<>(User.class, false);
        grid.addColumn(User::getUserId).setHeader("ID").setAutoWidth(true);
        grid.addColumn(User::getEmail).setHeader("Email").setAutoWidth(true);
        grid.addColumn(User::getFullName).setHeader("Full name").setAutoWidth(true);
        grid.addColumn(User::isMfaEnabled).setHeader("MFA").setAutoWidth(true);

        grid.addClassName("admin-grid");
        grid.setWidthFull();
        grid.setHeight("360px");
        card.add(grid);

        // form
        emailField = new TextField("Email");
        fullNameField = new TextField("Full name");
        passwordHashField = new TextField("Password hash");
        mfaField = new Checkbox("MFA enabled");

        emailField.setWidthFull();
        fullNameField.setWidthFull();
        passwordHashField.setWidthFull();

        FormLayout form = new FormLayout();
        form.addClassName("admin-form");
        form.setWidthFull();
        form.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("700px", 3)
        );

        form.add(emailField, fullNameField, passwordHashField);
        form.add(mfaField);
        card.add(form);

        // buttons
        createBtn = new Button("Create user");
        updateBtn = new Button("Update user");
        deleteBtn = new Button("Delete user");
        clearBtn = new Button("Clear");

        createBtn.addClassNames("btn", "primary");
        updateBtn.addClassNames("btn");
        deleteBtn.addClassNames("btn", "danger");
        clearBtn.addClassNames("btn");

        HorizontalLayout actions = new HorizontalLayout(createBtn, updateBtn, deleteBtn, clearBtn);
        actions.addClassName("actions");
        actions.setWidthFull();
        card.add(actions);
        
        refreshBtn.addClickListener(e -> {
            loadUsers();
            filterUsers();
        });
        
        searchField.addValueChangeListener(e -> filterUsers());
        
        clearBtn.addClickListener(e -> clearForm());
        
        grid.asSingleSelect().addValueChangeListener(event -> {
            User selectedUser = event.getValue();

            if (selectedUser == null) {
                clearForm();
                return;
            }

            selectedUserId = selectedUser.getUserId();

            if (selectedUser.getEmail() != null) {
                emailField.setValue(selectedUser.getEmail());
            } else {
                emailField.clear();
            }

            if (selectedUser.getFullName() != null) {
                fullNameField.setValue(selectedUser.getFullName());
            } else {
                fullNameField.clear();
            }

            mfaField.setValue(selectedUser.isMfaEnabled());

            // neradit paroli no db
            passwordHashField.clear();

            updateButtonsState();
        });
        
        loadUsers();
        filterUsers();
        updateButtonsState();
        
        add(card);
    }
	
	
	// -----
	private void loadUsers() {
        try {
            allUsers = userService.retrieveAll();
            grid.setItems(allUsers);
        } catch (Exception e) {
            Notification.show("Error appear loading users: " + e.getMessage(), 4000, Notification.Position.MIDDLE);
        }
    }
	
	private void clearForm() {
	    selectedUserId = null;

	    emailField.clear();
	    fullNameField.clear();
	    passwordHashField.clear();
	    mfaField.setValue(false);
	    
	    grid.asSingleSelect().clear();

	    updateButtonsState();
	}

	private void updateButtonsState() {
	    boolean selected = selectedUserId != null;
	    updateBtn.setEnabled(selected);
	    deleteBtn.setEnabled(selected);
	}
	
	private void filterUsers() {
	    String query = searchField.getValue();

	    // ja nekas nav ierakstīts
	    if (query == null) {
	        grid.setItems(allUsers);
	        return;
	    }

	    // atstarpes
	    String trimmedQuery = query.trim();

	    // ja tikkai atstarpes
	    if (trimmedQuery.isEmpty()) {
	        grid.setItems(allUsers);
	        return;
	    }

	    String searchText = trimmedQuery.toLowerCase();
	    ArrayList<User> filteredUsers = new ArrayList<>();

	    for (User user : allUsers) {

	        String email = user.getEmail();
	        if (email == null) {
	            email = "";
	        }

	        String emailLowerCase = email.toLowerCase();

	        if (emailLowerCase.contains(searchText)) {
	            filteredUsers.add(user);
	        }
	    }

	    grid.setItems(filteredUsers);
	}

    
}
