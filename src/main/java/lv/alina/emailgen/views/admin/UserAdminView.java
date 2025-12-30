package lv.alina.emailgen.views.admin;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

import lv.alina.emailgen.models.User;


@Route("admin/users")
@CssImport("./styles/admin.css")
public class UserAdminView extends VerticalLayout {
	
	public UserAdminView() {
	    addClassName("admin-page");
	    setSizeFull();

	    VerticalLayout card = new VerticalLayout();
	    card.addClassName("admin-card");
	    card.setWidthFull();
	    card.setMaxWidth("1100px");

	    H1 title = new H1("User admin panel");
	    title.addClassName("admin-title");

	    // Augšēja sadaļa
	    TextField searchField = new TextField("Search email");
	    searchField.setPlaceholder("type part of email...");
	    searchField.setClearButtonVisible(true);

	    Button refreshBtn = new Button("Refresh");
	    refreshBtn.addClassName("btn");

	    HorizontalLayout topBar = new HorizontalLayout(searchField, refreshBtn);
	    topBar.setWidthFull();
	    topBar.setAlignItems(Alignment.END);
	    topBar.addClassName("topbar");

	    // tabula
        Grid<User> grid = new Grid<>(User.class, false);
        grid.addColumn(User::getUserId).setHeader("ID").setAutoWidth(true);
        grid.addColumn(User::getEmail).setHeader("Email").setAutoWidth(true);
        grid.addColumn(User::getFullName).setHeader("Full name").setAutoWidth(true);
        grid.addColumn(User::isMfaEnabled).setHeader("MFA").setAutoWidth(true);

        grid.addClassName("admin-grid");
        grid.setWidthFull();
        grid.setHeight("360px");

        // form
        TextField emailField = new TextField("Email");
        TextField fullNameField = new TextField("Full name");
        TextField passwordHashField = new TextField("Password hash");
        Checkbox mfaField = new Checkbox("MFA enabled");

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

        // buttons
        Button createBtn = new Button("Create user");
        Button updateBtn = new Button("Update user");
        Button deleteBtn = new Button("Delete user");
        Button clearBtn = new Button("Clear");

        createBtn.addClassNames("btn", "primary");
        updateBtn.addClassNames("btn");
        deleteBtn.addClassNames("btn", "danger");
        clearBtn.addClassNames("btn");

        HorizontalLayout actions = new HorizontalLayout(
                createBtn, updateBtn, deleteBtn, clearBtn
        );
        actions.addClassName("actions");
        actions.setWidthFull();

        card.add(title, topBar, grid, form, actions);
        add(card);
    }

    
}
