package lv.alina.emailgen.views.privatepages;

import java.util.ArrayList;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
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
    private TextArea notesField;
    private ComboBox<MainEmail> defaultMainEmailBox;
    private ComboBox<Symbol> symbolBeforeShortcodeBox;
    private ComboBox<Symbol> symbolBeforeSequenceBox;
    private TextField shortCodeField;
    private Span previewText;

    public AddCompanyView(ICRUDCompanyService companyService, ICRUDMainEmailService mainEmailService, ISymbolService symbolService) {
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

        loadMainEmails(user);
        loadSymbols(user);
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
        backButton.addClickListener(e ->
                getUI().ifPresent(ui -> ui.navigate("companies"))
        );

        topBar.add(backButton);

        VerticalLayout form = new VerticalLayout();
        form.setAlignItems(Alignment.CENTER);

        Span title = new Span("Add new company");
        title.addClassName("company-form-title");

        companyNameField = new TextField("Company name");
        companyNameField.setRequiredIndicatorVisible(true);
        companyNameField.addClassName("company-form-input");

        notesField = new TextArea("Notes");
        notesField.addClassName("company-form-input");

        defaultMainEmailBox = new ComboBox<>("Default main e-mail");
        defaultMainEmailBox.setItemLabelGenerator(MainEmail::getMainEmail);
        defaultMainEmailBox.addClassName("company-form-input");
        defaultMainEmailBox.addValueChangeListener(e -> updatePreview());

        symbolBeforeShortcodeBox = new ComboBox<>("Symbol before short code");
        symbolBeforeShortcodeBox.setPlaceholder("NONE"); // <-- ŠEIT
        symbolBeforeShortcodeBox.setItemLabelGenerator(Symbol::getSymbol);
        symbolBeforeShortcodeBox.setClearButtonVisible(true);
        symbolBeforeShortcodeBox.addValueChangeListener(e -> updatePreview());

        HorizontalLayout symbolRow1 = createSymbolRow(symbolBeforeShortcodeBox);

        shortCodeField = new TextField("Short code");
        shortCodeField.addClassName("company-form-input");
        shortCodeField.addValueChangeListener(e -> updatePreview());


        symbolBeforeSequenceBox = new ComboBox<>("Symbol before sequence");
        symbolBeforeSequenceBox.setPlaceholder("NONE");
        symbolBeforeSequenceBox.setItemLabelGenerator(Symbol::getSymbol);
        symbolBeforeSequenceBox.setClearButtonVisible(true);
        symbolBeforeSequenceBox.addValueChangeListener(e -> updatePreview());

        HorizontalLayout symbolRow2 = createSymbolRow(symbolBeforeSequenceBox);

        previewText = new Span();
        previewText.addClassName("company-preview");

        Button save = new Button("SAVE");
        save.addClassName("company-add-button");
        save.addClickListener(e -> saveCompany());

        form.add(title, companyNameField, notesField, defaultMainEmailBox, symbolRow1, shortCodeField, symbolRow2, previewText, save);

        shell.add(topBar, form);
        add(shell);
    }

    private HorizontalLayout createSymbolRow(ComboBox<Symbol> box) {

        Button add = new Button("ADD SYBOL");
        add.addClickListener(e -> openAddSymbolDialog());

        Button delete = new Button("DEL");
        delete.addClickListener(e -> deleteSymbol(box));

        HorizontalLayout row = new HorizontalLayout(box, add, delete);
        row.setAlignItems(Alignment.END);

        return row;
    }


    private void openAddSymbolDialog() {

        Dialog dialog = new Dialog();

        TextField field = new TextField("Symbol");

        Button save = new Button("SAVE");
        save.addClickListener(e -> {
            try {
                User user = VaadinSession.getCurrent().getAttribute(User.class);
                symbolService.createSymbol(user, field.getValue());

                loadSymbols(user);
                updatePreview();

                dialog.close();
            } catch (Exception ex) {
                Notification.show(ex.getMessage());
            }
        });

        dialog.add(new VerticalLayout(field, save));
        dialog.open();
    }

    private void deleteSymbol(ComboBox<Symbol> box) {

        Symbol s = box.getValue();

        if (s == null) {
            Notification.show("Select symbol first");
            return;
        }

        try {
            symbolService.deleteSymbol(s);

            User user = VaadinSession.getCurrent().getAttribute(User.class);
            loadSymbols(user);

            box.clear();
            updatePreview();

        } catch (Exception e) {
            Notification.show(e.getMessage());
        }
    }

    private void loadMainEmails(User user) {
        ArrayList<MainEmail> list = mainEmailService.retrieveAllByUser(user);

        defaultMainEmailBox.setItems(list);

        if (!list.isEmpty()) {
            defaultMainEmailBox.setValue(list.get(0));
        }
    }

    private void loadSymbols(User user) {
        ArrayList<Symbol> list = symbolService.getSymbolsForUser(user);

        symbolBeforeShortcodeBox.setItems(list);
        symbolBeforeSequenceBox.setItems(list);
    }

    private void updatePreview() {

        MainEmail email = defaultMainEmailBox.getValue();

        if (email == null) {
            previewText.setText("Example: -");
            return;
        }

        String[] parts = email.getMainEmail().split("@");

        String result = parts[0] + getSymbol(symbolBeforeShortcodeBox.getValue()) + getText(shortCodeField) + getSymbol(symbolBeforeSequenceBox.getValue()) + "001@" + parts[1];

        previewText.setText("Example: " + result);
    }

    private String getSymbol(Symbol s) {
        return s == null ? "" : s.getSymbol();
    }

    private String getText(TextField f) {
        return f.getValue() == null ? "" : f.getValue().trim();
    }

    private void saveCompany() {

        try {
            User user = VaadinSession.getCurrent().getAttribute(User.class);

            companyService.add(
                    user,
                    companyNameField.getValue(),
                    notesField.getValue(),
                    defaultMainEmailBox.getValue(),
                    symbolBeforeShortcodeBox.getValue(),
                    symbolBeforeSequenceBox.getValue(),
                    shortCodeField.getValue()
            );

            Notification.show("Saved");
            getUI().ifPresent(ui -> ui.navigate("companies"));

        } catch (Exception e) {
            Notification.show(e.getMessage());
        }
    }
}
