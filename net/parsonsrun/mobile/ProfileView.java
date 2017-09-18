package net.parsonsrun.mobile;

import com.vaadin.addon.touchkit.ui.*;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.ValoTheme;

import net.parsonsrun.Utils;
import net.parsonsrun.domain.Player;

public class ProfileView extends MobileBaseView
{
	protected TextField first;
	protected TextField last;
	protected EmailField email;
	protected PasswordField pass1;
	protected PasswordField pass2;
	protected TextField phone;
	protected Switch male = new Switch("Male:");
	protected Switch female = new Switch("Female:");
	protected FormLayout form = new FormLayout();
	
	boolean sexChange = false;
	
	protected String getProfileHeader()
	{
		String s = "User Profile";
		if (getCurrentPlayer().firstLastName().length() > 1)
		{
			s = s + " for: " + getCurrentPlayer().firstLastName();
		}
		return s;
	}
	
	public String toString()
	{
		return super.toString() + ":" + getCurrentPlayer();
	}

	public void buildUI()
	{
		VerticalComponentGroup group = new VerticalComponentGroup(getProfileHeader());
		content.addComponent(group);
		addIntro(group);
		group.addComponent(form);
		first = new TextField("First:");
		addField(first);
		last = new TextField("Last:");
		addField(last);
		email = new EmailField("Email:");
		addField(email);
		phone = new TextField("Phone:");
		addField(phone);
		pass1 = new PasswordField("Password:");
		addField(pass1);
		pass2 = new PasswordField("Confirm:");
		addField(pass2);
		HorizontalLayout h = new HorizontalLayout();
		h.setWidth("100%");
		h.addComponent(male);
		h.addComponent(female);
		group.addComponent(h);
		phone.addBlurListener(e -> phoneChanged());
		male.addValueChangeListener(e -> maleCheck());
		female.addValueChangeListener(e -> femaleCheck());

		Button b = new Button("Save Changes");
		b.setStyleName(ValoTheme.BUTTON_LARGE);
		b.addClickListener(e -> save());
		content.addComponent(b);
		load();
	}
	
	protected void addIntro(VerticalComponentGroup g)
	{
	}

	protected void addField(AbstractTextField f)
	{
		f.setWidth("100%");
		form.addComponent(f);
	}
	
	protected void maleCheck()
	{
		if (sexChange)
			return;
		sexChange = true;
		boolean b = male.getValue();
		female.setValue(!b);
		sexChange = false;
	}
	
	protected void femaleCheck()
	{
		if (sexChange)
			return;
		sexChange = true;
		boolean b = female.getValue();
		male.setValue(!b);
		sexChange = false;
	}

	protected void load()
	{
		first.setValue(getCurrentPlayer().getFirst());
		last.setValue(getCurrentPlayer().getLast());
		email.setValue(getCurrentPlayer().getEmail());
		pass1.setValue("");
		pass2.setValue("");
		male.setValue(getCurrentPlayer().isMale());
		female.setValue(!getCurrentPlayer().isMale());
		phone.setValue(getCurrentPlayer().getPhoneDisplay());
	}
	
	protected void phoneChanged()
	{
		phone.setValue(Utils.expandPhone(phone.getValue()));
	}
	
	protected void save()
	{
		if (email.getValue().isEmpty())
		{
			Notification.show("Unable to save",
        			"You MUST have an email address",
        			Notification.Type.WARNING_MESSAGE);
			return;
		}
		
		if (phone.getValue().isEmpty())
		{
			Notification.show("Empty Phone!",
        			"You must enter a phone number",
        			Notification.Type.WARNING_MESSAGE);
			return;			
		}

		String p1 = pass1.getValue().trim();
		String p2 = pass2.getValue().trim();
		
		if (!(p1.isEmpty() && p2.isEmpty()))
		{
			if (!p1.equals(p2))
			{
				Notification.show("Password mismatch",
	        			"The passwords do not match",
	        			Notification.Type.WARNING_MESSAGE);
				return;
			}
		}
		if (isPasswordRequired())
			return;
		updatePlayer(p1);
		navigateTo(new MainView());
	}
	
	protected void updatePlayer(String pwd)
	{
		Player p = getCurrentPlayer();
		saveWhile(() -> {
			p.setFirst(first.getValue());
			p.setLast(last.getValue());
			p.setEmail(email.getValue());
			p.setPhone(phone.getValue());
			p.setMale(male.getValue());
			if (!pwd.isEmpty())
				p.encyptPassword(pwd);
		});
	}
	protected boolean isPasswordRequired()
	{
		return false;
	}
	
	public String getCaption()
 	{
		return "User Profile";
 	}
	
	protected HorizontalLayout buildTextField(AbstractTextField f, String lbl)
	{
		HorizontalLayout h = new HorizontalLayout();
        h.setWidth(getBrowserWidth(), Unit.PIXELS);
        h.setSpacing(true);
        Label l = new Label(lbl);
        l.setWidth("9em");
        h.addComponent(l);
        //h.setComponentAlignment(l, Alignment.MIDDLE_LEFT);
        //f.setWidth("75%");
        h.addComponent(f);
        //h.setComponentAlignment(f, Alignment.MIDDLE_RIGHT);
        h.setExpandRatio(f, 0.75f);
        return h;
	}
	
}
