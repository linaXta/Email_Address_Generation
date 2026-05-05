package lv.alina.emailgen.views.privatepages;

import java.util.ArrayList;
import java.util.Optional;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import lv.alina.emailgen.models.Company;
import lv.alina.emailgen.models.MainEmail;
import lv.alina.emailgen.models.ShortCodes;
import lv.alina.emailgen.models.Symbol;
import lv.alina.emailgen.models.User;
import lv.alina.emailgen.repos.ISymbolRepo;
import lv.alina.emailgen.service.ICRUDCompanyService;
import lv.alina.emailgen.service.ICRUDMainEmailService;
import lv.alina.emailgen.service.ISymbolService;

@Route("companies/edit/:id")
@PageTitle("Edit company")
@CssImport("./styles/company-view.css")
public class EditCompanyView extends VerticalLayout implements BeforeEnterObserver {

	// TODO
	private final ICRUDCompanyService companyService;
    private final ICRUDMainEmailService mainEmailService;
    private final ISymbolService symbolService;

    private Company currentCompany;

    private TextField companyNameField;
    private TextArea notesField;
    private ComboBox<MainEmail> defaultMainEmailBox;
    private ComboBox<Symbol> symbolBeforeShortcodeBox;
    private ComboBox<Symbol> symbolBeforeSequenceBox;
    private TextField shortCodeField;
    private Span previewText;

    public EditCompanyView(ICRUDCompanyService companyService, ICRUDMainEmailService mainEmailService, ISymbolService symbolService) {
        this.companyService = companyService;
        this.mainEmailService = mainEmailService;
        this.symbolService = symbolService;

        buildLayout();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        User user = VaadinSession.getCurrent().getAttribute(User.class);

        if (user == null) {
            event.forwardTo("login");
            return;
        }

        Optional<Long> id = event.getRouteParameters().get("id").map(Long::valueOf);

        if (id.isEmpty()) {
            Notification.show("Invalid company id");
            event.forwardTo("companies");
            return;
        }

        Optional<Company> companyOptional = companyService.findById(id.get());

        if (companyOptional.isEmpty()) {
            Notification.show("Company not found");
            event.forwardTo("companies");
            return;
        }

        currentCompany = companyOptional.get();

        if (!currentCompany.getUser().getUserId().equals(user.getUserId())) {
            Notification.show("Access denied");
            event.forwardTo("companies");
            return;
        }
        loadMainEmails(user);
        loadSymbols(user);
        fillForm();
        updatePreview();
    }

    private void buildLayout() {
        addClassName("company-page");
        setSizeFull();
        setAlignItems(Alignment.CENTER);

        VerticalLayout shell = new VerticalLayout();
        shell.addClassName("company-shell");

        HorizontalLayout topBar = new HorizontalLayout();
        topBar.addClassName("company-topbar");
        topBar.setWidthFull();
        topBar.setJustifyContentMode(JustifyContentMode.BETWEEN);

        Button backButton = new Button("BACK");
        backButton.addClassName("company-menu-button");
        backButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("companies")));

        topBar.add(backButton);

        VerticalLayout form = new VerticalLayout();
        form.setAlignItems(Alignment.CENTER);

        Span title = new Span("Edit company");
        title.addClassName("company-form-title");

        companyNameField = new TextField("Company name");
        companyNameField.setRequiredIndicatorVisible(true);
        companyNameField.addClassName("company-form-input");

        notesField = new TextArea("Notes");
        notesField.addClassName("company-form-input");

        defaultMainEmailBox = new ComboBox<>("Default main e-mail");
        defaultMainEmailBox.setItemLabelGenerator(MainEmail::getMainEmail);
        defaultMainEmailBox.addClassName("company-form-input");
        defaultMainEmailBox.setAllowCustomValue(false);
        defaultMainEmailBox.addValueChangeListener(e -> updatePreview());

        symbolBeforeShortcodeBox = new ComboBox<>("Symbol before short code");
        symbolBeforeShortcodeBox.setItemLabelGenerator(Symbol::getSymbol);
        symbolBeforeShortcodeBox.setClearButtonVisible(true);
        symbolBeforeShortcodeBox.addValueChangeListener(e -> updatePreview());

        HorizontalLayout symbolRow1 = createSymbolRow(symbolBeforeShortcodeBox);

        shortCodeField = new TextField("Short code");
        shortCodeField.addClassName("company-form-input");
        shortCodeField.addValueChangeListener(e -> updatePreview());

        symbolBeforeSequenceBox = new ComboBox<>("Symbol before sequence");
        symbolBeforeSequenceBox.setItemLabelGenerator(Symbol::getSymbol);
        symbolBeforeSequenceBox.setClearButtonVisible(true);
        symbolBeforeSequenceBox.addValueChangeListener(e -> updatePreview());

        HorizontalLayout symbolRow2 = createSymbolRow(symbolBeforeSequenceBox);

        previewText = new Span();
        previewText.addClassName("company-preview");

        Button saveButton = new Button("SAVE");
        saveButton.addClassName("company-add-button");
        saveButton.addClickListener(e -> updateCompany());

        form.add(title,
        		companyNameField,
                notesField,
                defaultMainEmailBox,
                symbolRow1,
                shortCodeField,
                symbolRow2,
                previewText,
                saveButton
        );

        shell.add(topBar, form);
        add(shell);
    }

    private HorizontalLayout createSymbolRow(ComboBox<Symbol> symbolBox) {
        Button addButton = new Button("+");
        addButton.addClassName("company-symbol-small-button");
        addButton.addClickListener(e -> openAddSymbolDialog());

        Button deleteButton = new Button("DEL");
        deleteButton.addClassName("company-symbol-delete-button");
        deleteButton.addClickListener(e -> deleteSymbol(symbolBox));

        HorizontalLayout row = new HorizontalLayout(symbolBox, addButton, deleteButton);
        row.addClassName("company-symbol-row");
        row.setAlignItems(Alignment.END);

        return row;
    }

    private void openAddSymbolDialog() {
        Dialog dialog = new Dialog();
        dialog.addClassName("company-symbol-dialog");

        TextField symbolField = new TextField("Symbol");
        symbolField.addClassName("company-form-input");

        Button saveButton = new Button("SAVE");
        saveButton.addClassName("company-add-button");
        saveButton.addClickListener(e -> {
            User user = VaadinSession.getCurrent().getAttribute(User.class);

            if (user == null) {
                getUI().ifPresent(ui -> ui.navigate("login"));
                return;
            }

            try {
                symbolService.createSymbol(user, symbolField.getValue());
                loadSymbols(user);
                updatePreview();

                dialog.close();
                Notification.show("Symbol added");
            } catch (Exception ex) {
                Notification.show(ex.getMessage());
            }
        });

        Button cancelButton = new Button("CANCEL");
        cancelButton.addClassName("company-menu-button");
        cancelButton.addClickListener(e -> dialog.close());

        HorizontalLayout buttons = new HorizontalLayout(cancelButton, saveButton);
        buttons.addClassName("company-symbol-dialog-buttons");

        VerticalLayout content = new VerticalLayout(symbolField, buttons);
        content.addClassName("company-symbol-dialog-content");
        content.setPadding(false);
        content.setSpacing(false);
        content.setAlignItems(Alignment.CENTER);

        dialog.add(content);
        dialog.open();
    }

    private void deleteSymbol(ComboBox<Symbol> symbolBox) {
        Symbol selectedSymbol = symbolBox.getValue();

        if (selectedSymbol == null) {
            Notification.show("Select symbol first");
            return;
        }

        User user = VaadinSession.getCurrent().getAttribute(User.class);

        if (user == null) {
            getUI().ifPresent(ui -> ui.navigate("login"));
            return;
        }

        try {
            symbolService.deleteSymbol(selectedSymbol);

            loadSymbols(user);
            symbolBox.clear();
            updatePreview();

            Notification.show("Symbol deleted");
        } catch (Exception e) {
            Notification.show(e.getMessage());
        }
    }

    private void loadMainEmails(User user) {
        try {
            ArrayList<MainEmail> mainEmails = mainEmailService.retrieveAllByUser(user);
            defaultMainEmailBox.setItems(mainEmails);
        } catch (Exception e) {
            Notification.show("Failed to load main e-mails: " + e.getMessage());
        }
    }

    private void loadSymbols(User user) {
        ArrayList<Symbol> symbols = symbolService.getSymbolsForUser(user);

        symbolBeforeShortcodeBox.setItems(symbols);
        symbolBeforeSequenceBox.setItems(symbols);
    }

    private void fillForm() {
        if (currentCompany == null) {
            return;
        }

        companyNameField.setValue(currentCompany.getCompanyName());

        if (currentCompany.getNotes() != null) {
            notesField.setValue(currentCompany.getNotes());
        }

        if (currentCompany.getDefaultMainEmail() != null) {
            defaultMainEmailBox.setValue(currentCompany.getDefaultMainEmail());
        }

        if (currentCompany.getSymbolBeforeShortcode() != null) {
            symbolBeforeShortcodeBox.setValue(currentCompany.getSymbolBeforeShortcode());
        }

        if (currentCompany.getSymbolBeforeSequence() != null) {
            symbolBeforeSequenceBox.setValue(currentCompany.getSymbolBeforeSequence());
        }

        ShortCodes currentShortCode = currentCompany.getCurrentShortCode();

        if (currentShortCode != null && currentShortCode.getShortCode() != null) {
            shortCodeField.setValue(currentShortCode.getShortCode());
        }
    }

    private void updatePreview() {
        if (previewText == null || defaultMainEmailBox == null) {
            return;
        }

        MainEmail mainEmail = defaultMainEmailBox.getValue();

        if (mainEmail == null || mainEmail.getMainEmail() == null) {
            previewText.setText("Example: -");
            return;
        }

        if (!mainEmail.getMainEmail().contains("@")) {
            previewText.setText("Example: invalid main e-mail");
            return;
        }

        String[] parts = mainEmail.getMainEmail().split("@", 2);

        Symbol symbol1 = symbolBeforeShortcodeBox.getValue();
        Symbol symbol2 = symbolBeforeSequenceBox.getValue();

        String symbolBeforeShortcode = getSymbolValue(symbol1);
        String symbolBeforeSequence = getSymbolValue(symbol2);
        String shortCode = getTextValue(shortCodeField);

        String example = parts[0] + symbolBeforeShortcode + shortCode + symbolBeforeSequence + "001@" + parts[1];

        previewText.setText("Example: " + example);
    }

    private void updateCompany() {
        if (currentCompany == null) {
            Notification.show("Company not loaded");
            return;
        }

        try {
            companyService.update(
                    currentCompany,
                    companyNameField.getValue(),
                    notesField.getValue(),
                    defaultMainEmailBox.getValue(),
                    symbolBeforeShortcodeBox.getValue(),
                    symbolBeforeSequenceBox.getValue(),
                    shortCodeField.getValue()
            );

            Notification.show("Company updated");
            getUI().ifPresent(ui -> ui.navigate("companies"));
        } catch (Exception e) {
            Notification.show(e.getMessage());
        }
    }

    private String getSymbolValue(Symbol symbol) {
        if (symbol == null) {
            return "";
        }

        return symbol.getSymbol();
    }

    private String getTextValue(TextField field) {
        String value = field.getValue();

        if (value == null) {
            return "";
        }

        return value.trim();
    }
}
