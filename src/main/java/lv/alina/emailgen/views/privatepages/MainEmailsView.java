package lv.alina.emailgen.views.privatepages;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Anchor;
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
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.DownloadResponse;

import lv.alina.emailgen.models.MainEmail;
import lv.alina.emailgen.models.User;
import lv.alina.emailgen.service.ICRUDMainEmailService;

@Route("main-emails")
@PageTitle("Main e-mails")
@CssImport("./styles/main-emails-view.css")
public class MainEmailsView extends VerticalLayout implements BeforeEnterObserver{
	
	private final ICRUDMainEmailService mainEmailService;

    private final int pageSize = 10;
    private int currentPage = 1;

    private ArrayList<MainEmail> allFilteredMainEmails = new ArrayList<>();

    private TextField searchField;
    private ComboBox<String> sortBox;
    private Span pageInfo;
    private Button previousPageButton;
    private Button nextPageButton;
    private VerticalLayout tableContent;
    
    private Anchor exportLink;

    public MainEmailsView(ICRUDMainEmailService mainEmailService) {
        this.mainEmailService = mainEmailService;

        buildLayout();
        refreshTable();
    }

    private void buildLayout() {
        addClassName("main-emails-page");
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        setAlignItems(Alignment.CENTER);

        VerticalLayout shell = new VerticalLayout();
        shell.addClassName("main-emails-shell");
        shell.setPadding(false);
        shell.setSpacing(false);

        HorizontalLayout topBar = new HorizontalLayout();
        topBar.addClassName("main-emails-topbar");
        topBar.setWidthFull();
        topBar.setAlignItems(Alignment.CENTER);

        Button menuButton = new Button("MENU");
        menuButton.addClassName("main-emails-menu-button");
        menuButton.addClickListener(event -> getUI().ifPresent(ui -> ui.navigate("main")));

        searchField = new TextField();
        searchField.setPlaceholder("Search");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.addClassName("main-emails-search");
        searchField.setWidthFull();
        searchField.addValueChangeListener(event -> {
            currentPage = 1;
            refreshTable();
        });

        Button addNewButton = new Button("ADD NEW");
        addNewButton.setIcon(new Icon(VaadinIcon.PLUS));
        addNewButton.addClassName("main-emails-add-button");

        topBar.add(menuButton, searchField, addNewButton);
        topBar.expand(searchField);

        HorizontalLayout tableHeaderBar = new HorizontalLayout();
        tableHeaderBar.addClassName("main-emails-table-header");
        tableHeaderBar.setWidthFull();
        tableHeaderBar.setAlignItems(Alignment.CENTER);

        sortBox = new ComboBox<>();
        sortBox.setItems("OLDEST FIRST", "NEWEST FIRST", "A-Z", "Z-A");
        sortBox.setValue("OLDEST FIRST");
        sortBox.setPlaceholder("Sort");
        sortBox.addClassName("main-emails-sort-box");
        sortBox.addValueChangeListener(event -> {
            currentPage = 1;
            refreshTable();
        });
        
        Button exportButton = new Button("EXPORT");
        exportButton.addClassName("main-emails-export-button");

        exportLink = new Anchor(createDownloadHandler(), "");
        exportLink.addClassName("main-emails-export-link");
        exportLink.add(exportButton);

        pageInfo = new Span("0 - 0 FROM 0");
        pageInfo.addClassName("main-emails-page-info");

        previousPageButton = new Button("<");
        previousPageButton.addClassName("main-emails-page-button");
        previousPageButton.addClickListener(event -> {
            if (currentPage > 1) {
                currentPage--;
                refreshTable();
            }
        });

        nextPageButton = new Button(">");
        nextPageButton.addClassName("main-emails-page-button");
        nextPageButton.addClickListener(event -> {
            int totalPages = calculateTotalPages();
            if (currentPage < totalPages) {
                currentPage++;
                refreshTable();
            }
        });

        HorizontalLayout leftSide = new HorizontalLayout(sortBox, exportLink);
        leftSide.addClassName("main-emails-left-side");
        leftSide.setAlignItems(Alignment.CENTER);

        HorizontalLayout rightSide = new HorizontalLayout(pageInfo, previousPageButton, nextPageButton);
        rightSide.addClassName("main-emails-right-side");
        rightSide.setAlignItems(Alignment.CENTER);

        tableHeaderBar.add(leftSide, rightSide);

        tableContent = new VerticalLayout();
        tableContent.addClassName("main-emails-table-content");
        tableContent.setPadding(false);
        tableContent.setSpacing(false);
        tableContent.setWidthFull();

        shell.add(topBar, tableHeaderBar, tableContent);
        add(shell);
    }

    private void refreshTable() {
        User loggedInUser = VaadinSession.getCurrent().getAttribute(User.class);
        if (loggedInUser == null) {
            return;
        }

        try {
            allFilteredMainEmails = mainEmailService.searchByUserAndText(loggedInUser, searchField.getValue());
            sortMainEmails(allFilteredMainEmails);
            updateTableRows();
            updatePaginationInfo();
            updatePaginationButtons();

        } catch (Exception e) {
            tableContent.removeAll();
            tableContent.add(new Span("Error: " + e.getMessage()));
        }
    }
    

    private void sortMainEmails(ArrayList<MainEmail> mainEmails) {
        String selectedSort = sortBox.getValue();

        if ("A-Z".equals(selectedSort)) {
            mainEmails.sort(Comparator.comparing(MainEmail::getMainEmail, String.CASE_INSENSITIVE_ORDER));
        } else if ("Z-A".equals(selectedSort)) {
            mainEmails.sort(Comparator.comparing(MainEmail::getMainEmail, String.CASE_INSENSITIVE_ORDER).reversed());
        } else if ("NEWEST FIRST".equals(selectedSort)) {
            mainEmails.sort(Comparator.comparing(MainEmail::getCreatedAt).reversed());
        } else {
            mainEmails.sort(Comparator.comparing(MainEmail::getCreatedAt));
        }
    }

    private void updateTableRows() {
        tableContent.removeAll();

        if (allFilteredMainEmails.isEmpty()) {
            VerticalLayout emptyState = new VerticalLayout();
            emptyState.addClassName("main-emails-empty-state");
            emptyState.setAlignItems(Alignment.CENTER);

            Span emptyText = new Span("No base e-mails yet.");
            emptyText.addClassName("main-emails-empty-text");

            Button addNewButton = new Button("Add new base e-mail");
            addNewButton.addClassName("main-emails-add-empty-button");

            emptyState.add(emptyText, addNewButton);
            tableContent.add(emptyState);
            return;
        }

        int startIndex = (currentPage - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, allFilteredMainEmails.size());

        for (int i = startIndex; i < endIndex; i++) {
            MainEmail mainEmail = allFilteredMainEmails.get(i);

            HorizontalLayout row = new HorizontalLayout();
            row.addClassName("main-emails-row");
            row.setWidthFull();
            row.setAlignItems(Alignment.CENTER);

            Span emailText = new Span(mainEmail.getMainEmail());
            emailText.addClassName("main-emails-row-text");

            HorizontalLayout actionButtons = new HorizontalLayout();
            actionButtons.addClassName("main-emails-actions");

            Button historyButton = createActionButton("HISTORY", VaadinIcon.FILE_TEXT_O);
            Button editButton = createActionButton("EDIT", VaadinIcon.EDIT);
            Button copyButton = createActionButton("COPY", VaadinIcon.COPY_O);
            Button deleteButton = createActionButton("DELETE", VaadinIcon.TRASH);

            copyButton.addClickListener(event -> { getUI().ifPresent(ui -> ui.getPage().executeJs("navigator.clipboard.writeText($0)", mainEmail.getMainEmail() ));
                Notification.show("E-mail copied");
            });

            actionButtons.add(historyButton, editButton, copyButton, deleteButton);

            row.add(emailText, actionButtons);
            row.expand(emailText);

            tableContent.add(row);
        }
    }

    private void updatePaginationInfo() {
        int total = allFilteredMainEmails.size();

        if (total == 0) {
            pageInfo.setText("0 - 0 FROM 0");
            return;
        }

        int start = (currentPage - 1) * pageSize + 1;
        int end = Math.min(currentPage * pageSize, total);

        pageInfo.setText(start + " - " + end + " FROM " + total);
    }

    private void updatePaginationButtons() {
        int totalPages = calculateTotalPages();

        previousPageButton.setEnabled(currentPage > 1);
        nextPageButton.setEnabled(currentPage < totalPages);
    }

    private int calculateTotalPages() {
        if (allFilteredMainEmails.isEmpty()) {
            return 1;
        }

        return (int) Math.ceil((double) allFilteredMainEmails.size() / pageSize);
    }
    
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        User loggedInUser = VaadinSession.getCurrent().getAttribute(User.class);

        if (loggedInUser == null) {
            event.forwardTo("login");
        }
    }
    
    private DownloadHandler createDownloadHandler() {
    	return DownloadHandler.fromInputStream(event ->
    		new DownloadResponse(createExcelFile(),"main-emails.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", -1)
		);
    }
    
    private ByteArrayInputStream createExcelFile() {
    	
        try (XSSFWorkbook workbook = new XSSFWorkbook();
        	ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Main Emails");

            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Nr.");
            headerRow.createCell(1).setCellValue("E-mail");

            for (int i = 0; i < allFilteredMainEmails.size(); i++) {
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(i + 1);
                row.createCell(1).setCellValue(allFilteredMainEmails.get(i).getMainEmail());
            }

           sheet.autoSizeColumn(0);
           sheet.autoSizeColumn(1);

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Excel file", e);
        }
    }
    
    private Button createActionButton(String text, VaadinIcon iconType) {
        Icon icon = iconType.create();
        icon.addClassName("main-emails-action-icon");

        Span label = new Span(text);
        label.addClassName("main-emails-action-label");

        VerticalLayout content = new VerticalLayout(icon, label);
        content.addClassName("main-emails-action-content");
        content.setPadding(false);
        content.setSpacing(false);
        content.setAlignItems(Alignment.CENTER);

        Button button = new Button(content);
        button.addClassName("main-emails-action-button");
        return button;
    }

}
