package net.parsonsrun.desktop;

import java.awt.Point;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.vaadin.data.util.*;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.Grid.SingleSelectionModel;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.ValoTheme;

import de.steinwedel.messagebox.MessageBox;
import net.parsonsrun.ParsonsRunUI;
import net.parsonsrun.PictureFactory;
import net.parsonsrun.Tuple;
import net.parsonsrun.Utils;
import net.parsonsrun.domain.*;
import net.parsonsrun.mobile.ExternalTeamsView;
import net.parsonsrun.mobile.TournamentsView;

public class MainView extends DesktopBaseView implements UpdateListener
{
	protected BeanItemContainer<ExternalTeam> externals;
	protected Grid externalsGrid;
	protected BeanItemContainer<Ladder> ladders;
	protected Grid laddersGrid;
	protected BeanItemContainer<Tournament> tournaments;
	protected Grid tournamentsGrid;
	protected int listWidth;
	protected CheckBox onlyTeam;
	protected CheckBox onlyLadder;
	protected CheckBox onlyTournament;
	protected TextArea recentUsers;
	protected HorizontalLayout pics;
	
	@Override
	public void buildUI()
	{
		if (getLoginUser() == null)
		{
			navigateTo(DesktopUI.LOGIN);
			return;
		}
		listWidth = (getBrowserWidth() * 2) / 7;
		addTitle("Welcome " + getLoginUser().getFirst());
		HorizontalLayout l = new HorizontalLayout();
		//l.setMargin(true);
		l.setSpacing(true);
		l.addComponent(buildExternal());
		l.addComponent(buildLadders());
		l.addComponent(buildLeagues());
		addComponent(l);
		VerticalLayout v2 = new VerticalLayout();
		v2.setSpacing(true);
		HorizontalLayout h2 = new HorizontalLayout(v2);
		h2.setSpacing(true);
		Button profile = new Button("Edit Profile");
		profile.addClickListener(e -> navigateTo(DesktopUI.PROFILE));
		v2.addComponent(profile);
		if (isAdminUser())
		{
			Button b = new Button("Admin");
			b.setSizeUndefined();
			b.setStyleName(ValoTheme.BUTTON_DANGER);
			b.addClickListener(e -> openAdmin());
			v2.addComponent(b);
		}
		Button b = new Button("Logout");
		b.setSizeUndefined();
		b.addStyleName(ValoTheme.BUTTON_LINK);
		b.addStyleName(ValoTheme.BUTTON_TINY);
		b.addClickListener(e -> logout());
		v2.addComponent(b);
		recentUsers = new TextArea("Recent Visitors:");
		recentUsers.setWidth("250px");
		recentUsers.setRows(5);
		h2.addComponent(recentUsers);
		pics = new HorizontalLayout();
		pics.setSpacing(true);
		h2.addComponent(pics);
		b = new Button("ButtonTest");
		b.addStyleName(ValoTheme.BUTTON_TINY);
		b.addClickListener(e -> navigateTo(DesktopUI.TEST));
		v2.addComponent(b);
		addComponent(h2);
		update();
		timer();
		
	}
	
	protected void timer()
	{
		updatePictures();
		if (isAttached())
			ParsonsRunUI.getScheduler().schedule(() -> timer(), 2, TimeUnit.MINUTES);
	}
	
	protected void updatePictures()
	{
		int targetHeight = Utils.PICTURE_THUMBNAIL_HEIGHT;
		pics.removeAllComponents();
		Upload upload = PictureFactory.makeUpload(getHood(), getLoginUser(), getPictureAssociation(), () -> updatePictures());
		VerticalLayout v = new VerticalLayout(upload);
		v.setSizeFull();
		v.setComponentAlignment(upload, Alignment.MIDDLE_CENTER);
		pics.addComponent(v);
		if (getHood().getPictures(getPictureAssociation()).isEmpty())
			return;
		Label lbl = new Label("Click to view ->");
		v.addComponent(lbl);
		v.setComponentAlignment(lbl, Alignment.MIDDLE_RIGHT);
		int w = getBrowserWidth() - 700;
		int size = getHood().getPictures(getPictureAssociation()).size();
		int start = nextInt(size);
		Picture pic = getHood().getPicture(getPictureAssociation(), start);
		while (w > 0 && (size-- > 0))
		{
			int wx = pic.getWidthFor(targetHeight) + 20;
			//println("Sz=" + size + " W=" + w + " w-wx=" + (w - wx) + " pic=" + pic.getWidth() + "," + pic.getHeight());
			w = w - wx;
			final Picture p = pic;
			pics.addComponent(pic.newImage(targetHeight, e -> selectedPicture(p)));
			pic = getHood().getNextPicture(pic, null, 1);
		}
	}
	
	protected void selectedPicture(Picture p)
	{
		currentPic = p;
		Tuple<HorizontalLayout, Point> tuple = getPictureViewer(true);
		getParentUI().openDialog(tuple.getLeft(), (int)tuple.getRight().getX(), (int)tuple.getRight().getY() + 15).addCloseListener(e -> saveWhile(() -> updatePictures()));
	}
	
	public void update()
	{
		if (getLoginUser() != null)
		{
			getLoginUser().setOnlyShowTeam(onlyTeam.getValue());
			getLoginUser().setOnlyShowLadder(onlyLadder.getValue());
			getLoginUser().setOnlyShowTournament(onlyTournament.getValue());
		}
		updateUI(() -> {
			externals.removeAllItems();
			externals.addAll(getHood().getExternals(getLoginUser()));
			ladders.removeAllItems();
			ladders.addAll(getHood().getLadders(getLoginUser()));
			tournaments.removeAllItems();
			tournaments.addAll(getHood().getTournaments(getLoginUser()));
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
	
	public void updateAndSave()
	{
		update();
		saveData();
	}
	
	protected void openAdmin()
	{
		navigateTo(DesktopUI.ADMIN);
	}
	
	protected Component buildExternal()
	{
		VerticalLayout v = new VerticalLayout();
		externals = new BeanItemContainer<ExternalTeam>(ExternalTeam.class);
		externalsGrid = baseGrid("ALTA/USTA Teams", externals, 50);
		externalsGrid.addColumn("fullName");
		externalsGrid.addSelectionListener(e -> selectExternal());	
		v.addComponent(externalsGrid);
		onlyTeam = new CheckBox("Only show MY Teams", getLoginUser().isOnlyShowTeam());
		onlyTeam.addValueChangeListener(e -> updateAndSave());
		v.addComponent(onlyTeam);
		return v;
	}
	
	protected Grid baseGrid(String lbl, BeanItemContainer ctr, int diff)
	{
		Grid g = new Grid(lbl, ctr);
		g.setWidth("" + (listWidth + diff) +"px");
		g.setHeight((getBrowserHeight() / 2) + "px");
		g.removeAllColumns();
		g.setHeaderVisible(false);
		g.setImmediate(true);
		SingleSelectionModel sm = new SingleSelectionModel();
		sm.setDeselectAllowed(false);
		g.setSelectionModel(sm);
		return g;
	}
	
	protected Component buildLadders()
	{
		VerticalLayout v = new VerticalLayout();
		ladders = new BeanItemContainer<Ladder>(Ladder.class);
		laddersGrid = baseGrid("Parsons Run Ladders", ladders, -25);
		laddersGrid.addColumn("name");
		laddersGrid.addSelectionListener(e -> selectLadder());	
		v.addComponent(laddersGrid);
		onlyLadder = new CheckBox("Only show MY Ladders", getLoginUser().isOnlyShowLadder());
		onlyLadder.addValueChangeListener(e -> updateAndSave());
		v.addComponent(onlyLadder);
		return v;
	}
	
	protected Component buildLeagues()
	{
		VerticalLayout v = new VerticalLayout();
		tournaments = new BeanItemContainer<Tournament>(Tournament.class);
		tournamentsGrid = baseGrid("Parsons Run Tournaments", tournaments, -25);
		tournamentsGrid.addColumn("name");
		tournamentsGrid.addSelectionListener(e -> selectTournament());	
		v.addComponent(tournamentsGrid);
		onlyTournament = new CheckBox("Only show MY Tournaments", getLoginUser().isOnlyShowTournament());
		onlyTournament.addValueChangeListener(e -> updateAndSave());
		v.addComponent(onlyTournament);
		return v;
	}
	
	protected void playItForward()
	{
		if (getParentUI().isExternalTeamAction())
			navigateTo(DesktopUI.EXTERNAL);
		if (getParentUI().isTournamentAction())
			navigateTo(DesktopUI.TOURNAMENT);
	}

	protected void selectExternal()
	{
		setCurrentExternalTeam((ExternalTeam)externalsGrid.getSelectedRow());
		navigateTo(DesktopUI.EXTERNAL);
	}
	protected void selectLadder()
	{
		League l = (League)laddersGrid.getSelectedRow();
	}
	protected void selectTournament()
	{
		setCurrentTournament((Tournament)tournamentsGrid.getSelectedRow());
		navigateTo(DesktopUI.TOURNAMENT);
	}
	
	protected void logout()
	{
		getParentUI().logoutUser();
		getParentUI().navigateTo(DesktopUI.LOGIN);
	}
	
	protected void viewResized()
	{
		super.viewResized();
		removeAllComponents();
		buildUI();
	}

}
