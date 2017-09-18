package net.parsonsrun.desktop;

import java.util.Stack;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Widgetset;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.*;
import com.vaadin.server.Page.*;
import com.vaadin.ui.*;

import net.parsonsrun.ParsonsRunUI;
import net.parsonsrun.DataAccess.Command;
import net.parsonsrun.desktop.*;
import net.parsonsrun.domain.*;

import com.vaadin.ui.Button.ClickEvent;

@SuppressWarnings("serial")
@Theme("parsonsruntennis")
//@Widgetset("net.parsonsrun.widgetset.ParsonsruntennisWidgetset")
public class DesktopUI extends ParsonsRunUI
{	
	public static final String CONFIRMED = "confirmed";
	public static final String REJECTED = "rejected";
	public static final String CONFIRMED_TEXT = "confirmedText";
	public static final String UNCONFIRMED_TEXT = "unconfirmedText";
	public static final String HOME_BKGD = "homegame-background";
	public static final String PHONE_EDIT = "phone-edit";
	
	public static final String LOGIN = "";					// Empty string "" is default window
	public static final String MAIN = "main";	
	public static final String INITPROFILE = "initprofile";	
	public static final String PROFILE = "profile";	
	public static final String ADMIN = "admin";	
	public static final String EXTERNAL = "external";
	public static final String LOG = "log";
	public static final String TOURNAMENT = "tournament";
	public static final String TEST = "test";

	protected Stack<String> previousPages = new Stack<String>();
	
	public void init()
	{
		super.init();
		setNavigator(new Navigator(this, this));
		getNavigator().addView(LOGIN, LoginView.class);
		getNavigator().addView(MAIN, MainView.class);
		getNavigator().addView(PROFILE, ProfileView.class);
		getNavigator().addView(ADMIN, AdminView.class);
		getNavigator().addView(EXTERNAL, ExternalTeamView.class);
		getNavigator().addView(LOG, LogView.class);
		getNavigator().addView(TOURNAMENT, TournamentView.class);
		getNavigator().addView(TEST, ButtonTestView.class);
		getNavigator().addView(INITPROFILE, InitialProfileView.class);
	}

	@Override
	protected void init(VaadinRequest request) 
	{
		init();
		saveRequestParameters(request);
		getPage().setTitle("Parsons Run Tennis");
		getPage().addBrowserWindowResizeListener(new BrowserWindowResizeListener() {
			public void browserWindowResized(BrowserWindowResizeEvent event)
			{
				browserHeight = event.getHeight();
				browserWidth = event.getWidth();
				viewResized();
			}
		});
		boolean loggedIn = loginPreviousUser();
		processActions();
		if (loggedIn)
			navigateTo(MAIN);
	}
	

	

	
	protected void viewResized()
	{
		DesktopBaseView v = (DesktopBaseView)getNavigator().getCurrentView();
		if (v != null)
			v.viewResized();
	}
	
	public void updateUI(Runnable r)
	{
		UI.getCurrent().access(r);
	}


	public Stack<String> getPreviousPages()
	{
		return previousPages;
	}
	
	public DesktopBaseView navigateTo(String viewName)
	{
		getPreviousPages().push(getNavigator().getState());
		debug("Desktop navigateTo:" + viewName);
		getNavigator().navigateTo(viewName);
		return (DesktopBaseView)getNavigator().getCurrentView();
	}
	
	public String uiType()
	{
		return "Desktop";
	}
	public DesktopBaseView navigateBack()
	{
		if (getPreviousPages().isEmpty())
			getNavigator().navigateTo(MAIN);
		else
			getNavigator().navigateTo(getPreviousPages().pop());
		return (DesktopBaseView)getNavigator().getCurrentView();
	}

}
