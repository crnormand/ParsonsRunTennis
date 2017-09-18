package net.parsonsrun.mobile;

import javax.servlet.annotation.WebServlet;

import com.vaadin.addon.touchkit.ui.NavigationManager;
import com.vaadin.addon.touchkit.ui.NavigationManager.NavigationEvent.Direction;
import com.vaadin.addon.touchkit.ui.SwipeView;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.annotations.Widgetset;
import com.vaadin.server.ClassResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;

import net.parsonsrun.ParsonsRunUI;
import net.parsonsrun.domain.*;
import net.parsonsrun.mobile.*;

import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
//@Theme("touchkit")
@Theme("prmobile")
@Widgetset("net.parsonsrun.widgetset.ParsonsruntennisWidgetset")
public class MobileUI extends ParsonsRunUI
{
	public static final String THIN_CHECKBOX = "thin-checkbox";
	
	NavigationManager manager;
	
	
	@Override
	protected void init(VaadinRequest request) 
	{
		init();
		saveRequestParameters(request);
		boolean ok = loginPreviousUser();
		processActions();
		if (ok)
			manager = new NavigationManager(new MainView());
		else
			manager = new NavigationManager(new LoginView());
		setContent(manager);
		
		manager.addNavigationListener(event -> {
			if (manager.getCurrentComponent() instanceof MobileBaseView)
			{
				MobileBaseView p = (MobileBaseView)manager.getCurrentComponent();
				if (event.getDirection() == Direction.FORWARD)
					p.swipedRight();
				else
					p.swipedLeft();
			}
		});
	}
	

	

	
	public String uiType()
	{
		return "Mobile";
	}
	
	public void navigateBack()
	{
		manager.navigateBack();
	}
	
	public NavigationManager getNavigationManager()
	{
		return manager;
	}
	
	public void navigateTo(MobileBaseView v)
	{
		debug("Mobile navigateTo:" + v.getClass().getSimpleName());
		manager.navigateTo(v);
	}
	
	public void gotoMain()
	{
		setCurrentComponent(new MainView());
	}
	
	
	
	public void gotoLogin()
	{
		setCurrentComponent(new LoginView());
	}
	
	public void setCurrentComponent(Component c)
	{
		manager.setCurrentComponent(c);
	}
	
}