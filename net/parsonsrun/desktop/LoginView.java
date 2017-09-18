package net.parsonsrun.desktop;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.ValoTheme;

import net.parsonsrun.DataAccess;
import net.parsonsrun.DataAccess.Command;
import net.parsonsrun.Utils;
import net.parsonsrun.domain.Neighborhood;

public class LoginView extends DesktopBaseView
{
	TextField email;
	PasswordField password;
	protected int count = 0;
	
	public void buildUI()
	{
		addTitle("Welcome to the Parsons Run Tennis website!");
		email = new TextField("Email:");
		email.setImmediate(true);
		email.setWidth("32em");
		addComponent(email);
		password = new PasswordField("Password:");
		password.setImmediate(true);
		password.setWidth("16em");
		addComponent(password);
		email.addValueChangeListener(e -> password.focus());
		password.addValueChangeListener(e -> login());
		addComponent(createSpacer());
		
		Button b = new Button("Login");
		b.setWidth("10em");
		b.addClickListener(e -> login());
		b.addStyleName(ValoTheme.BUTTON_PRIMARY);
		addComponent(b);
		addComponent(createSpacer());
		
        Label l = new Label("<div style='color:#333;'><p>If you do not have an account, please " +
        		"<a href='mailto:parsonsruntennis@bellsouth.net?subject=Account Creation" +
        		"&body=Please create an account for <enter_your_email_address>'>send an email</a>" +
        		" requesting an account.</p>" +
        		"<p>If this is your first time logging in, your password will be blank and you will be " +
        		"requested to update your profile.</p>" +
        		"<p>The website can also be viewed from your desktop "
        		+ "(although it will look different), and some of the features may only be available on the desktop.</p><p>" +
				"Once you have logged in, you will not need to log in again from this device.</p></div>", ContentMode.HTML);
        addComponent(l);
	}
	
	protected void login()
	{
        String em = (String) email.getValue();
        String ps = (String) password.getValue();
        if (em.isEmpty())
        {
        	Notification.show("Unable to login:",
        			"Serious?!? How can you log in without an email address?",
        			Notification.Type.WARNING_MESSAGE);
        }
        else
        {
	        if (getParentUI().loginUser(em, ps))
	        {
  	        	if (ps.isEmpty())
	        		getParentUI().navigateTo(DesktopUI.INITPROFILE);
  	        	else
  	        		getParentUI().navigateTo(DesktopUI.MAIN);
	        }
	        else
	        {
	        	password.setValue("");
	        	println("Unsuccessful login attempt: " + em);
	        	if (count++ > 1 && Neighborhood.getSingleton().containsPlayer(em))
		        	Notification.show("Unable to login:",
		        			"Your passsword is incorrect.  If this is your first time logging in, you MUST use a blank password.",
		        			Notification.Type.WARNING_MESSAGE);
	        	else
	        		Notification.show("Unable to login:",
	        			"Email or passsword incorrect.",
	        			Notification.Type.WARNING_MESSAGE);
	        }
        }
	}
	

}
