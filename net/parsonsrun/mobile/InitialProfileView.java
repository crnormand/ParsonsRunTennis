package net.parsonsrun.mobile;

import com.vaadin.addon.touchkit.ui.VerticalComponentGroup;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;

public class InitialProfileView extends ProfileView
{
	protected boolean isPasswordRequired()
	{
		String p1 = pass1.getValue().trim();
		String p2 = pass2.getValue().trim();
		
		if (p1.isEmpty() && p2.isEmpty())
		{
			Notification.show("Empty Password!",
	    			"Since this is your first time in the system, you must enter a password",
	    			Notification.Type.WARNING_MESSAGE);
			return true;
		}
		return false;
	}
	
	protected void addIntro(VerticalComponentGroup g)
	{
		g.addComponent(new Label("Since this is your first time on this website,<br>" +
			"you must create a password.", ContentMode.HTML));
	}

}
