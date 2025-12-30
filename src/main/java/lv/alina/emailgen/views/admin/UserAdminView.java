package lv.alina.emailgen.views.admin;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;


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

        card.add(title);
        add(card);
    }

    
}
