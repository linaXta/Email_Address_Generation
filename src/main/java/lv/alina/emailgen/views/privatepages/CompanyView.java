package lv.alina.emailgen.views.privatepages;

import java.util.ArrayList;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import lv.alina.emailgen.models.Company;
import lv.alina.emailgen.models.User;
import lv.alina.emailgen.service.ICRUDCompanyService;

@Route("companies")
@PageTitle("Companies")
@CssImport("./styles/company-view.css")
public class CompanyView extends VerticalLayout implements BeforeEnterObserver{
	
	private final ICRUDCompanyService companyService;

    private ArrayList<Company> companies = new ArrayList<>();

    private VerticalLayout listContainer;
    private TextField searchField;

    public CompanyView(ICRUDCompanyService companyService) {
        this.companyService = companyService;
        buildLayout();
    }
    
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        User loggedInUser = VaadinSession.getCurrent().getAttribute(User.class);

        if (loggedInUser == null) {
            event.forwardTo("login");
            return;
        }

        loadCompanies(loggedInUser);
    }


    private void buildLayout() {
        addClassName("company-page");
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        setAlignItems(Alignment.CENTER);

        VerticalLayout shell = new VerticalLayout();
        shell.addClassName("company-shell");
        shell.setPadding(false);
        shell.setSpacing(false);

        HorizontalLayout topBar = new HorizontalLayout();
        topBar.addClassName("company-topbar");
        topBar.setWidthFull();
        topBar.setAlignItems(Alignment.CENTER);

        Button menuButton = new Button("MENU");
        menuButton.addClassName("company-menu-button");
        menuButton.addClickListener(event -> getUI().ifPresent(ui -> ui.navigate("main")));

        searchField = new TextField();
        searchField.setPlaceholder("Search...");
        searchField.addClassName("company-search");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.addClassName("company-search");
        searchField.setWidthFull();

        searchField.addValueChangeListener(e -> filter());

        Button addNewButton = new Button("ADD NEW");
        addNewButton.setIcon(new Icon(VaadinIcon.PLUS));
        addNewButton.addClassName("company-add-button");
        addNewButton.addClickListener(event -> getUI().ifPresent(ui -> ui.navigate("companies/add")));

        topBar.add(menuButton, searchField, addNewButton);
        topBar.expand(searchField);

        listContainer = new VerticalLayout();
        listContainer.addClassName("company-list");
        listContainer.setWidthFull();

        shell.add(topBar, listContainer);
        add(shell);
    }

    private void loadCompanies(User user) {
        try {
            companies = companyService.retrieveAllByUser(user);
            renderList(companies);
        } catch (Exception e) {
            Notification.show("Error: " + e.getMessage());
        }
    }

    private void filter() {
        String value = searchField.getValue();

        if (value == null || value.trim().isEmpty()) {
            renderList(companies);
            return;
        }

        String search = value.trim().toLowerCase();

        ArrayList<Company> filtered = new ArrayList<>();

        for (Company c : companies) {
            String name = c.getCompanyName() == null ? "" : c.getCompanyName();

            if (name.toLowerCase().contains(search)) {
                filtered.add(c);
            }
        }

        renderList(filtered);
    }

    private void renderList(ArrayList<Company> list) {
        listContainer.removeAll();

        if (list.isEmpty()) {
            showEmptyState();
            return;
        }

        for (Company c : list) {
            HorizontalLayout row = new HorizontalLayout();
            row.addClassName("company-row");

            Span name = new Span(c.getCompanyName());
            name.addClassName("company-row-text");

            row.add(name);
            listContainer.add(row);
        }
    }

    private void showEmptyState() {
        VerticalLayout empty = new VerticalLayout();
        empty.addClassName("company-empty");

        Span text = new Span("No companies yet");
        text.addClassName("company-empty-text");

        Button addBtn = new Button("ADD NEW");
        addBtn.addClassName("company-add-empty-button");
        addBtn.addClickListener(event -> getUI().ifPresent(ui -> ui.navigate("companies/add")));

        empty.add(text, addBtn);
        listContainer.add(empty);
    }

}
