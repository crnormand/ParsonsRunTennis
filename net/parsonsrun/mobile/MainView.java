package net.parsonsrun.mobile;

import java.util.Date;
import com.vaadin.addon.touchkit.ui.NavigationButton;
import com.vaadin.addon.touchkit.ui.VerticalComponentGroup;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.Upload;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.ValoTheme;

import net.parsonsrun.PictureFactory;
import net.parsonsrun.Tuple;
import net.parsonsrun.Utils;
import net.parsonsrun.domain.Neighborhood;
import net.parsonsrun.domain.Player;

public class MainView extends MobileBaseView
{
	VerticalComponentGroup group;
	TextArea recentUsers;
	
 	public void buildUI()
	{
		content.addComponent(new Label("<div style='font-size: 150%'>Welcome " + getLoginUser().getFirst() + "</div>", ContentMode.HTML));
		content.addComponent(getSwipeReminder());
		group = new VerticalComponentGroup("Leagues");
		group.setWidth("100%");
		content.addComponent(group);
		VerticalComponentGroup group2 = new VerticalComponentGroup();
		group2.setWidth("100%");
//		group2.addComponent(new NavigationButton(new ToDoView("My Teams")));
		
		Button b1 = new Button("Profile");
		b1.setIcon(FontAwesome.USER);
		b1.addClickListener(e -> navigateTo(new ProfileView()));
		Upload upload = PictureFactory.makeUpload(getHood(), getLoginUser(), null, null);
		Button b2 = new Button("Pics");
		b2.setIcon(FontAwesome.PICTURE_O);
		b2.addClickListener(e -> navigateTo(new PictureView(getPictureAssociation())));
		b2.setEnabled(getHood().getPictures(getPictureAssociation()).size() > 0);
		HorizontalLayout h = new HorizontalLayout(b1, b2, upload);
		h.setComponentAlignment(b1, Alignment.TOP_LEFT);
		h.setComponentAlignment(upload, Alignment.MIDDLE_RIGHT);
		h.setComponentAlignment(b2, Alignment.TOP_CENTER);

		group2.addComponent(h);
		content.addComponent(group2);
		Button b = new Button("Logout");
		b.setStyleName(ValoTheme.BUTTON_LINK);
		b.setStyleName(ValoTheme.BUTTON_TINY);
		b.addClickListener(e -> logout());
		recentUsers = new TextArea("Recent Visitors:");
		recentUsers.setWidth("100%");
		recentUsers.setRows(8);
		content.addComponent(recentUsers);
		if (getLoginUser().isAdmin())
			content.addComponent(b);
		update();
	}
 	
	public void update()
	{
		super.update();
		updateUI(() -> {
			group.removeAllComponents();
			group.addComponent(new NavigationButton(new ExternalTeamsView()));
			group.addComponent(new NavigationButton(new LaddersView()));
			group.addComponent(new NavigationButton(new TournamentsView()));
			updateRecentUsers();
		});
	}
	protected void updateRecentUsers()
	{
		recentUsers.setReadOnly(false);
		StringBuilder s = new StringBuilder();
		for (Tuple<Player, Date> t : getHood().getRecentUsers())
		{
			s.append(Utils.format2(t.getRight()));
			s.append(" - ");
			s.append(t.getLeft().firstLastName());
			s.append("\n");
		}
		recentUsers.setValue(s.toString());
		recentUsers.setCursorPosition(recentUsers.getValue().length());
		recentUsers.setReadOnly(true);
	}
 	
 	protected void resetDB()
 	{
 		System.out.println("DB Reset");
 		Neighborhood.resetDB();
 	}
 	
	protected void playItForward()
	{
		if (getParentUI().isExternalTeamAction())
			navigateTo(new ExternalTeamsView());
		if (getParentUI().isTournamentAction())
			navigateTo(new TournamentsView());
	}
 	
 	public String getCaption()
 	{
 		return "Main";
 	}
	
	void logout()
	{
		getParentUI().logoutUser();
		getParentUI().gotoLogin();
	}
}
