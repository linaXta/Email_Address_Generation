package lv.alina.emailgen.views.privatepages;

import java.util.ArrayList;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.Autocomplete;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import lv.alina.emailgen.models.MainEmail;
import lv.alina.emailgen.models.Symbol;
import lv.alina.emailgen.models.User;
import lv.alina.emailgen.service.ICRUDCompanyService;
import lv.alina.emailgen.service.ICRUDMainEmailService;
import lv.alina.emailgen.service.ISymbolService;

@Route("companies/add")
@PageTitle("Add company")
@CssImport("./styles/company-view.css")
public class AddCompanyView extends VerticalLayout implements BeforeEnterObserver {

	private final ICRUDCompanyService companyService;
    private final ICRUDMainEmailService mainEmailService;
    private final ISymbolService symbolService;

    private TextField companyNameField;
    private TextField shortCodeField;
    private TextArea notesField;

    private ComboBox<MainEmail> defaultMainEmailBox;
    private ComboBox<Symbol> symbolBeforeShortcodeBox;
    private ComboBox<Symbol> symbolBeforeSequenceBox;

    private Span previewText;

    public AddCompanyView(ICRUDCompanyService companyService, ICRUDMainEmailService mainEmailService, ISymbolService symbolService){
        this.companyService = companyService;
        this.mainEmailService = mainEmailService;
        this.symbolService = symbolService;
        buildLayout();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        User loggedInUser = VaadinSession.getCurrent().getAttribute(User.class);

        if (loggedInUser == null) {
            event.forwardTo("login");
            return;
        }

        loadMainEmails(loggedInUser);
        loadSymbols(loggedInUser);
        updatePreview();
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

        HorizontalLayout topBar = createTopBar();

        VerticalLayout form = new VerticalLayout();
        form.addClassName("company-form-content");
        form.setPadding(false);
        form.setSpacing(false);
        form.setAlignItems(Alignment.CENTER);

        HorizontalLayout titleRow = createTitleRow();

        companyNameField = new TextField("Company name");
        companyNameField.setRequiredIndicatorVisible(true);
        companyNameField.setPlaceholder("Company name");
        companyNameField.setAutocomplete(Autocomplete.OFF);
        companyNameField.addClassName("company-form-input");
        HorizontalLayout companyNameRow = createInfoRow(companyNameField,"Company name is only used to help you identify the system or website you are testing. It does not affect generated e-mail addresses.");

        shortCodeField = new TextField("Short code");
        shortCodeField.setPlaceholder("Short code");
        shortCodeField.setAutocomplete(Autocomplete.OFF);
        shortCodeField.addClassName("company-form-input");
        shortCodeField.setValueChangeMode(ValueChangeMode.EAGER);
        shortCodeField.addValueChangeListener(event -> updatePreview());
        HorizontalLayout shortCodeRow = createInfoRow(shortCodeField,"Short code will be included in generated e-mail addresses. Leave it empty if you want to generate addresses without a short code.");

        notesField = new TextArea("Notes");
        notesField.setPlaceholder("Notes");
        notesField.setAutocomplete(Autocomplete.OFF);
        notesField.addClassName("company-form-input");

        defaultMainEmailBox = new ComboBox<>("Default main e-mail");
        defaultMainEmailBox.setPlaceholder("Select default main e-mail");
        defaultMainEmailBox.setItemLabelGenerator(MainEmail::getMainEmail);
        defaultMainEmailBox.setAllowCustomValue(false);
        defaultMainEmailBox.addClassName("company-form-input");
        defaultMainEmailBox.addValueChangeListener(event -> updatePreview());
        HorizontalLayout defaultMainEmailRow = createInfoRow(defaultMainEmailBox, "This e-mail will be offered first when generating addresses for this company. You can still choose any other main e-mail during generation.");

        symbolBeforeShortcodeBox = new ComboBox<>("Symbol before short code");
        symbolBeforeShortcodeBox.setPlaceholder("None");
        symbolBeforeShortcodeBox.addClassName("company-form-input");
        symbolBeforeShortcodeBox.setItemLabelGenerator(Symbol::getSymbol);
        symbolBeforeShortcodeBox.setClearButtonVisible(true);
        symbolBeforeShortcodeBox.addValueChangeListener(event -> updatePreview());
        HorizontalLayout symbolBeforeShortcodeRow = createSymbolRow(symbolBeforeShortcodeBox,"Symbol before short code is added before the short code in generated addresses.");

        symbolBeforeSequenceBox = new ComboBox<>("Symbol before sequence");
        symbolBeforeSequenceBox.setPlaceholder("None");
        symbolBeforeSequenceBox.addClassName("company-form-input");
        symbolBeforeSequenceBox.setItemLabelGenerator(Symbol::getSymbol);
        symbolBeforeSequenceBox.setClearButtonVisible(true);
        symbolBeforeSequenceBox.addValueChangeListener(event -> updatePreview());
        HorizontalLayout symbolBeforeSequenceRow = createSymbolRow(symbolBeforeSequenceBox,"Symbol before sequence is added before the number, for example before 001.");

        Button manageSymbolsButton = new Button("MANAGE SYMBOLS");
        manageSymbolsButton.addClassName("company-manage-symbols-button");
        manageSymbolsButton.addClickListener(event -> openManageSymbolsDialog());

        previewText = new Span("Example: -");
        previewText.addClassName("company-preview");

        Button saveButton = new Button("SAVE");
        saveButton.addClassName("company-add-button");
        saveButton.addClickListener(event -> saveCompany());

        form.add(titleRow,
                companyNameRow,
                shortCodeRow,
                notesField,
                defaultMainEmailRow,
                symbolBeforeShortcodeRow,
                symbolBeforeSequenceRow,
                manageSymbolsButton,
                previewText,
                saveButton
        );

        shell.add(topBar, form);
        add(shell);
    }

    private HorizontalLayout createTopBar() {
        HorizontalLayout topBar = new HorizontalLayout();
        topBar.addClassName("company-topbar");
        topBar.setWidthFull();
        topBar.setAlignItems(Alignment.CENTER);
        topBar.setJustifyContentMode(JustifyContentMode.BETWEEN);

        Button menuButton = new Button("MENU");
        menuButton.addClassName("company-menu-button");
        menuButton.addClickListener(event -> getUI().ifPresent(ui -> ui.navigate("main")));

        Button backButton = new Button("BACK");
        backButton.addClassName("company-menu-button");
        backButton.addClickListener(event -> getUI().ifPresent(ui -> ui.navigate("companies")));

        topBar.add(menuButton, backButton);
        return topBar;
    }

    private HorizontalLayout createTitleRow() {
        HorizontalLayout titleRow = new HorizontalLayout();
        titleRow.setAlignItems(Alignment.CENTER);
        titleRow.setSpacing(true);

        Span title = new Span("Add new company");
        title.addClassName("company-form-title");

        Span titleInfo = new Span("!");
        titleInfo.addClassName("company-title-info-icon");

        Span titleTooltip = new Span("Company represents a separate system, website or platform that you are testing.");
        titleTooltip.addClassName("company-title-tooltip");

        HorizontalLayout titleInfoWrapper = new HorizontalLayout(titleInfo, titleTooltip);
        titleInfoWrapper.addClassName("company-title-info-wrapper");

        titleRow.add(title, titleInfoWrapper);
        return titleRow;
    }

    private void loadMainEmails(User user) {
        try {
            ArrayList<MainEmail> mainEmails = mainEmailService.retrieveAllByUser(user);

            defaultMainEmailBox.setItems(mainEmails);

            if (!mainEmails.isEmpty()) {
                defaultMainEmailBox.setValue(mainEmails.get(0));
            }

        } catch (Exception e) {
            Notification.show("Failed to load main e-mails: " + e.getMessage());
        }
    }

    private void loadSymbols(User user) {
        ArrayList<Symbol> symbols = symbolService.getSymbolsForUser(user);

        symbolBeforeShortcodeBox.setItems(symbols);
        symbolBeforeSequenceBox.setItems(symbols);
    }

    private void updatePreview() {
        if (previewText == null || defaultMainEmailBox == null) {
            return;
        }

        MainEmail selectedMainEmail = defaultMainEmailBox.getValue();

        if (selectedMainEmail == null || selectedMainEmail.getMainEmail() == null) {
            previewText.setText("Example: select main e-mail first");
            return;
        }

        String mainEmail = selectedMainEmail.getMainEmail();

        if (!mainEmail.contains("@")) {
            previewText.setText("Example: invalid main e-mail");
            return;
        }

        String[] parts = mainEmail.split("@", 2);

        String localPart = parts[0];
        String domainPart = parts[1];

        String symbolBeforeShortcode = getSymbolValue(symbolBeforeShortcodeBox.getValue());
        
        String shortCode = shortCodeField.getValue();
        if (shortCode == null) {
            shortCode = "";
        } else {
            shortCode = shortCode.trim();
        }
        
        String symbolBeforeSequence = getSymbolValue(symbolBeforeSequenceBox.getValue());

        String example = localPart + symbolBeforeShortcode + shortCode + symbolBeforeSequence + "001@" + domainPart;
        previewText.setText("Example: " + example);
    }

    private void saveCompany() {
        User user = VaadinSession.getCurrent().getAttribute(User.class);

        if (user == null) {
            getUI().ifPresent(ui -> ui.navigate("login"));
            return;
        }

        try {
            companyService.add(user, companyNameField.getValue(), notesField.getValue(), defaultMainEmailBox.getValue(), 
            		symbolBeforeShortcodeBox.getValue(), symbolBeforeSequenceBox.getValue(), shortCodeField.getValue());

            Notification.show("Company added");
            getUI().ifPresent(ui -> ui.navigate("companies"));

        } catch (Exception e) {
            Notification.show(e.getMessage());
        }
    }

    private HorizontalLayout createInfoRow(Component field, String text) {
        Span infoIcon = new Span("!");
        infoIcon.addClassName("company-title-info-icon");

        Span tooltip = new Span(text);
        tooltip.addClassName("company-title-tooltip");

        HorizontalLayout infoWrapper = new HorizontalLayout(infoIcon, tooltip);
        infoWrapper.addClassName("company-title-info-wrapper");

        HorizontalLayout row = new HorizontalLayout(field, infoWrapper);
        row.addClassName("company-field-info-row");
        row.setAlignItems(Alignment.END);
        return row;
    }

    private HorizontalLayout createSymbolRow(ComboBox<Symbol> box, String tooltipText) {
        Span infoIcon = new Span("!");
        infoIcon.addClassName("company-title-info-icon");

        Span tooltip = new Span(tooltipText);
        tooltip.addClassName("company-title-tooltip");

        HorizontalLayout infoWrapper = new HorizontalLayout(infoIcon, tooltip);
        infoWrapper.addClassName("company-title-info-wrapper");

        HorizontalLayout row = new HorizontalLayout(box, infoWrapper);
        row.addClassName("company-symbol-row");
        row.setAlignItems(Alignment.END);

        return row;
    }

    private void openManageSymbolsDialog() {
        Dialog dialog = new Dialog();
        dialog.addClassName("company-symbol-dialog");

        User user = VaadinSession.getCurrent().getAttribute(User.class);

        if (user == null) {
            getUI().ifPresent(ui -> ui.navigate("login"));
            return;
        }

        TextField symbolField = new TextField("New symbol");
        symbolField.setPlaceholder("Enter symbol");
        symbolField.setAutocomplete(Autocomplete.OFF);
        symbolField.addClassName("company-form-input");

        VerticalLayout symbolList = new VerticalLayout();
        symbolList.addClassName("company-symbol-list");
        symbolList.setPadding(false);
        symbolList.setSpacing(false);

        refreshSymbolList(symbolList, user);

        Button addButton = new Button("ADD");
        addButton.addClassName("company-add-button");
        addButton.addClickListener(event -> {
            try {
            	Symbol selectedBeforeShortCode = symbolBeforeShortcodeBox.getValue();
                Symbol selectedBeforeSequence = symbolBeforeSequenceBox.getValue();
            	
                symbolService.createSymbol(user, symbolField.getValue());
                symbolField.clear();
                loadSymbols(user);
                
                if (selectedBeforeShortCode != null) {
                    symbolBeforeShortcodeBox.setValue(selectedBeforeShortCode);
                }

                if (selectedBeforeSequence != null) {
                    symbolBeforeSequenceBox.setValue(selectedBeforeSequence);
                }
                
                refreshSymbolList(symbolList, user);
                updatePreview();
                Notification.show("Symbol added");

            } catch (Exception e) {
                Notification.show(e.getMessage());
            }
        });

        Button closeButton = new Button("CLOSE");
        closeButton.addClassName("company-menu-button");
        closeButton.addClickListener(event -> dialog.close());

        HorizontalLayout buttons = new HorizontalLayout(addButton, closeButton);
        buttons.addClassName("company-symbol-dialog-buttons");

        VerticalLayout content = new VerticalLayout(symbolField, symbolList,buttons);

        content.addClassName("company-symbol-dialog-content");
        content.setPadding(false);
        content.setSpacing(false);
        content.setAlignItems(Alignment.CENTER);

        dialog.add(content);
        dialog.open();
    }

    private void refreshSymbolList(VerticalLayout symbolList, User user) {
        symbolList.removeAll();

        ArrayList<Symbol> symbols = symbolService.getSymbolsForUser(user);

        for (Symbol symbol : symbols) {
            HorizontalLayout row = new HorizontalLayout();
            row.addClassName("company-symbol-list-row");
            row.setAlignItems(Alignment.CENTER);

            Span symbolText = new Span(symbol.getSymbol());
            symbolText.addClassName("company-symbol-list-text");

            Button deleteButton = new Button("DELETE");
            deleteButton.addClassName("company-symbol-delete-button");

            if (isDefaultSymbol(symbol)) {
                deleteButton.setEnabled(false);
            }

            deleteButton.addClickListener(event -> {
                try {
                    symbolService.deleteSymbol(symbol);

                    loadSymbols(user);
                    refreshSymbolList(symbolList, user);

                    symbolBeforeShortcodeBox.clear();
                    symbolBeforeSequenceBox.clear();

                    updatePreview();

                    Notification.show("Symbol deleted");

                } catch (Exception e) {
                    Notification.show(e.getMessage());
                }
            });

            row.add(symbolText, deleteButton);
            row.expand(symbolText);

            symbolList.add(row);
        }
    }

    private boolean isDefaultSymbol(Symbol symbol) {
        if (symbol == null || symbol.getSymbol() == null) {
            return true;
        }

        String value = symbol.getSymbol();

        return "+".equals(value) || "-".equals(value);
    }

    private String getSymbolValue(Symbol symbol) {
        if (symbol == null) {
            return "";
        }
        return symbol.getSymbol();
    }
}
