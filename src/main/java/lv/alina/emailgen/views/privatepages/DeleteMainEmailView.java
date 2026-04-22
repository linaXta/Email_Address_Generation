package lv.alina.emailgen.views.privatepages;

import java.util.Optional;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import lv.alina.emailgen.models.MainEmail;
import lv.alina.emailgen.models.User;
import lv.alina.emailgen.service.ICRUDMainEmailService;

@Route("main-emails/delete/:id")
@PageTitle("Delete main e-mail")
@CssImport("./styles/main-emails-view.css")
public class DeleteMainEmailView extends VerticalLayout implements BeforeEnterObserver{
	
	private final ICRUDMainEmailService mainEmailService;
	private MainEmail currentMainEmail;
	private Span emailTitle;
	 
	public DeleteMainEmailView(ICRUDMainEmailService mainEmailService) {
		this.mainEmailService = mainEmailService;
		buildLayout();
	}
	

	
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        User loggedInUser = VaadinSession.getCurrent().getAttribute(User.class);

        if (loggedInUser == null) {
            event.forwardTo("login");
            return;
        }

        Optional<Long> id = event.getRouteParameters()
                .get("id")
                .map(Long::valueOf);

        if (id.isEmpty()) {
            Notification.show("Invalid main e-mail id");
            event.forwardTo("main-emails");
            return;
        }

        Optional<MainEmail> mainEmailOptional = mainEmailService.findById(id.get());

        if (mainEmailOptional.isEmpty()) {
            Notification.show("Main e-mail not found");
            event.forwardTo("main-emails");
            return;
        }

        currentMainEmail = mainEmailOptional.get();

        if (!currentMainEmail.getUser().getUserId().equals(loggedInUser.getUserId())) {
            Notification.show("Access denied");
            event.forwardTo("main-emails");
            return;
        }

        if (emailTitle != null) {
            emailTitle.setText(currentMainEmail.getMainEmail());
        }
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
        topBar.setJustifyContentMode(JustifyContentMode.BETWEEN);

        Button menuButton = new Button("MENU");
        menuButton.addClassName("main-emails-menu-button");
        menuButton.addClickListener(event -> getUI().ifPresent(ui -> ui.navigate("main")));

        Button backButton = new Button("BACK");
        backButton.addClassName("main-emails-menu-button");
        backButton.addClickListener(event -> getUI().ifPresent(ui -> ui.navigate("main-emails")));

        topBar.add(menuButton, backButton);

        VerticalLayout content = new VerticalLayout();
        content.addClassName("main-emails-delete-content");
        content.setWidthFull();
        content.setPadding(false);
        content.setSpacing(false);
        content.setAlignItems(Alignment.CENTER);

        emailTitle = new Span("Main e-mail");
        emailTitle.addClassName("main-emails-delete-title");

        Button deleteEverythingButton = new Button("DELETE MAIN + GENERATED");
        deleteEverythingButton.addClassName("main-emails-delete-danger-button");
        deleteEverythingButton.addClickListener(event -> deleteWithGenerated());

        Span deleteEverythingText = new Span( "This will permanently delete the main e-mail and all generated e-mail addresses connected to it");
        deleteEverythingText.addClassName("main-emails-delete-text");

        HorizontalLayout divider = new HorizontalLayout();
        divider.addClassName("main-emails-delete-divider");

        Span dividerText = new Span("OR");
        dividerText.addClassName("main-emails-delete-divider-text");

        divider.add(dividerText);

        Button deleteMainOnlyButton = new Button("DELETE MAIN EMAIL ONLY");
        deleteMainOnlyButton.addClassName("main-emails-delete-warning-button");
        deleteMainOnlyButton.addClickListener(event -> deleteMainOnly());

        Span deleteMainOnlyText = new Span("After deletion, you will no longer be able to select this main email when generating addresses. Previously generated addresses will remain on the Company page, where you can delete them separately.");
        deleteMainOnlyText.addClassName("main-emails-delete-text");

        content.add(
                emailTitle,
                deleteEverythingButton,
                deleteEverythingText,
                divider,
                deleteMainOnlyButton,
                deleteMainOnlyText
        );

        shell.add(topBar, content);
        add(shell);
    }

    private void deleteWithGenerated() {
        if (currentMainEmail == null) {
            Notification.show("Main e-mail not loaded");
            return;
        }

        mainEmailService.deleteWithGenerated(currentMainEmail);

        Notification.show("Main e-mail and generated e-mails deleted");
        getUI().ifPresent(ui -> ui.navigate("main-emails"));
    }

    private void deleteMainOnly() {
        if (currentMainEmail == null) {
            Notification.show("Main e-mail not loaded");
            return;
        }

        mainEmailService.deleteMainOnly(currentMainEmail);

        Notification.show("Main e-mail deleted");
        getUI().ifPresent(ui -> ui.navigate("main-emails"));
    }
}
