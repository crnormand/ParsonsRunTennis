package net.parsonsrun.desktop;

import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import net.parsonsrun.ParsonsRunUI;
import net.parsonsrun.Utils;
import net.parsonsrun.domain.Player;
import net.parsonsrun.domain.Team;

public class ProfileView extends DesktopBaseView
{
	public static final String Male = "Male";
	public static final String Female = "Female";
	protected TextField first;
	protected TextField last;
	protected TextField email;
	protected PasswordField pass1;
	protected PasswordField pass2;
	protected TextField phone;
	protected OptionGroup sex;
	protected FormLayout form = new FormLayout();	
	protected boolean sexChange = false;
	protected ListSelect teams;
	
	protected String getProfileHeader()
	{
		String s = "User Profile";
		if (getCurrentPlayer() != null && getCurrentPlayer().firstLastName().length() > 1)
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
		addTitle(getProfileHeader());
		addIntro();
		HorizontalLayout h = new HorizontalLayout();
		h.setSpacing(true);
		addComponent(h);
		h.addComponent(form);
		first = new TextField("First:");
		addField(first);
		last = new TextField("Last:");
		addField(last);
		email = new TextField("Email:");
		addField(email);
		phone = new TextField("Phone:");
		addField(phone);
		pass1 = new PasswordField("Password:");
		addField(pass1);
		pass2 = new PasswordField("Confirm:");
		addField(pass2);
		phone.addBlurListener(e -> phoneChanged());
		sex = new OptionGroup();
		sex.addItems(Male, Female);
		form.addComponent(sex);
		Button b = new Button("Save Changes");
		b.setStyleName(ValoTheme.BUTTON_LARGE);
		b.addClickListener(e -> save());
		form.addComponent(b);
		addRight(h);
		load();
	}
	
	protected void addIntro()
	{
	}
	
	protected void addRight(HorizontalLayout h)
	{
		teams = new ListSelect("Teams", getCurrentPlayer().getTeams());
		teams.setNullSelectionAllowed(false);
		teams.setMultiSelect(false);
		ListSelect matches = new ListSelect("Matches");
		matches.setNullSelectionAllowed(false);
		matches.setMultiSelect(false);
		Button del = new Button("Delete Team");
		teams.addValueChangeListener(e -> { 
			matches.removeAllItems();
			del.setEnabled(false);
			if (teams.getValue() != null)
			{
				del.setEnabled(true);
				matches.addItems(((Team)(teams.getValue())).getEvents());
			}
		});
		del.addClickListener(e -> {
			if (teams.getValue() != null)
			{
				Team t = (Team)teams.getValue();
				getCurrentPlayer().removeTeam(t);
				t.delete();
				teams.removeAllItems();
				teams.addItems(getCurrentPlayer().getTeams());
			}
		});
		VerticalLayout v = new VerticalLayout(teams);
		if (getLoginUser().isAdmin())
			v.addComponent(del);
		h.addComponent(v);
		h.addComponent(matches);
	}

	protected void addField(AbstractTextField f)
	{
		f.setWidth("350px");
		form.addComponent(f);
	}
	
	protected void load()
	{
		first.setValue(getCurrentPlayer().getFirst());
		last.setValue(getCurrentPlayer().getLast());
		email.setValue(getCurrentPlayer().getEmail());
		pass1.setValue("");
		pass2.setValue("");
		sex.setValue(getCurrentPlayer().isMale() ? Male : Female);
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
		if (phone.getValue().isEmpty() & !getLoginUser().isAdmin())
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
				Notification.show("Password mismatch!",
	        			"The passwords do not match",
	        			Notification.Type.WARNING_MESSAGE);
				return;
			}
		}
		if (isPasswordRequired())
			return;
		updatePlayer(p1);
		next();
	}
	
	protected void next()
	{
		navigateBack();
	}
	
	protected boolean isPasswordRequired()
	{
		return false;
	}
	
	protected void updatePlayer(String pwd)
	{
		Player p = getCurrentPlayer();
		saveWhile(() -> {
			p.setFirst(first.getValue());
			p.setLast(last.getValue());
			p.setEmail(email.getValue());
			p.setPhone(phone.getValue());
			p.setMale(Male.equals(sex.getValue()));
			if (!pwd.isEmpty())
				p.encyptPassword(pwd);
			getHood().sortPlayers();
		});
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
