package lv.alina.emailgen.views.admin;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("admin/users")
public class UserAdminView extends VerticalLayout{
	
	public UserAdminView() {
        add(new H1("User admin panel"));
    }

}
