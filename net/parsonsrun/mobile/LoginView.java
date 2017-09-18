package net.parsonsrun.mobile;

import com.vaadin.addon.touchkit.ui.DatePicker;
import com.vaadin.addon.touchkit.ui.EmailField;
import com.vaadin.addon.touchkit.ui.NavigationView;
import com.vaadin.addon.touchkit.ui.VerticalComponentGroup;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

import net.parsonsrun.desktop.DesktopUI;
import net.parsonsrun.domain.Neighborhood;

public class LoginView extends MobileBaseView
{
	EmailField email;
	PasswordField password;
	protected int count;

    public void buildUI()
    {
    	Label l = new Label("<p>Welcome to Parsons Run Tennis</p>", ContentMode.HTML);
        l.setStyleName(TEXT_DARK);
        content.addComponent(l);
        VerticalComponentGroup group = new VerticalComponentGroup();
        HorizontalLayout h = new HorizontalLayout();
        //h.setWidth("100%");
        h.setSpacing(true);
        l = new Label("Email:");
        h.addComponent(l);
        h.setComponentAlignment(l, Alignment.MIDDLE_LEFT);
        email = new EmailField();
        email.setWidth("30em");
        email.setInputPrompt("Enter your email address...");
        h.addComponent(email);
        h.setComponentAlignment(email, Alignment.MIDDLE_RIGHT);
        h.setExpandRatio(email, 1.0f);
        group.addComponent(h);

        h = new HorizontalLayout();
        h.setSpacing(true);
        l = new Label("Password:");
        h.addComponent(l);
        h.setComponentAlignment(l, Alignment.MIDDLE_LEFT);
        password = new PasswordField();
        password.setWidth("30em");
        h.addComponent(password);
        h.setComponentAlignment(password, Alignment.MIDDLE_RIGHT);
        h.setExpandRatio(password, 1.0f);
        group.addComponent(h);

        Button b = new Button("Login");
        b.addClickListener(e -> login());

        content.addComponent(group);
        content.addComponent(b);
        l = new Label("<div style='color:#333;'><p>If you do not have an account, please " +
        		"<a href='mailto:parsonsruntennis@bellsouth.net?subject=Account Creation" +
        		"&body=Please create an account for <enter_your_email_address>'>send an email</a>" +
        		" requesting an account.</p>" +
        		"<p>If this is your first time logging in, your password will be blank and you will be " +
        		"requested to update your profile.</p>" +
        		"<p>The website can also be viewed from your desktop "
        		+ "(although it will look different), and some of the features may only be available on the desktop.</p><p>" +
				"Once you have logged in, you will not need to log in again from this device.</p></div>", ContentMode.HTML);
        content.addComponent(l);
        setContent(content);
    }
    
 	public String getCaption()
 	{
 		return "Login";
 	}
    
    void login()
    {
        String em = (String) email.getValue();
        String ps = (String) password.getValue();
        if (em.isEmpty())
        {
        	println("Unsuccessful login attempt, empty email address");
        	Notification.show("Unable to login:",
        			"Serious?!? How can you log in without an email address?",
        			Notification.Type.WARNING_MESSAGE);
        }
        else
        {
	        if (getParentUI().loginUser(em, ps))
	        {
	        	if (ps.isEmpty())
	        		getParentUI().navigateTo(new InitialProfileView());
	        	else
	        		getParentUI().navigateTo(new MainView());
	        }
	        else
	        {
	        	println("Unsuccessful login attempt: " + em);
	        	password.setValue("");
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
