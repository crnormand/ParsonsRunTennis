package net.parsonsrun.desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.Date;

import javax.mail.Address;
import javax.mail.Message;

import com.vaadin.data.util.*;
import com.vaadin.server.BrowserWindowOpener;
import com.vaadin.server.Page;
import com.vaadin.ui.*;
import com.vaadin.ui.Grid.SingleSelectionModel;
import com.vaadin.ui.themes.ValoTheme;

import de.steinwedel.messagebox.ButtonType;
import de.steinwedel.messagebox.MessageBox;
import net.parsonsrun.EmailMessage;
import net.parsonsrun.PrintUI;
import net.parsonsrun.Tester;
import net.parsonsrun.Utils;
import net.parsonsrun.domain.*;

public class AdminView extends DesktopBaseView
{
	protected Grid playersGrid;
	protected BeanItemContainer<Player> players;
	protected Button create;
	protected Button edit;
	protected Button delete;
	protected Button createTeam;
	protected Button createLadder;
	protected Button createTourny;
	protected VerticalLayout left;
	protected VerticalLayout right;
	protected Grid externalsGrid;
	protected BeanItemContainer<ExternalTeam> externals;
	protected Button deleteTeam;
	protected Button upTeam;
	protected Button downTeam;
	protected ExternalTeam currentTeam;
	protected TextArea direct;
	protected Ladder currentLadder;
	protected Grid laddersGrid;
	protected BeanItemContainer<Ladder> ladders;
	protected Button deleteLadder;
	protected Button upLadder;
	protected Button downLadder;
	protected CheckBox strict;
	protected Tournament currentTourny;
	protected Grid tournysGrid;
	protected BeanItemContainer<Tournament> tournys;
	protected Button deleteTourny;
	protected Button upTourny;
	protected Button downTourny;

	@Override
	public void buildUI()
	{
		addBack();
		left = new VerticalLayout();
		left.setSpacing(true);
		HorizontalLayout h = new HorizontalLayout();
		h.setSpacing(true);
		//h.setMargin(true);
		h.addComponent(left);
		buildPlayerList(h);
		right = new VerticalLayout();
		right.setSpacing(true);
		h.addComponent(right);
		addComponent(h);
		buildLeft(left);
		buildRight(right);
		update();
	}
	
	public void backClicked()
	{
		saveWhile(() -> Notification.show("Data saved"));
		super.backClicked();
	}
	
	protected void buildPlayerList(HorizontalLayout h)
	{
		VerticalLayout v = new VerticalLayout();
		h.addComponent(v);
		players = new BeanItemContainer<Player>(Player.class);
		playersGrid = new Grid("Available players:", players);
		playersGrid.setHeight((getBrowserHeight() - 300) + "px");
		playersGrid.removeAllColumns();
		playersGrid.setHeaderVisible(false);
		//playersGrid.addColumn("globalId");
		playersGrid.addColumn("last");
		playersGrid.addColumn("first");
		playersGrid.addColumn("email");
		playersGrid.getColumns().stream().forEach(c -> c.setSortable(false));
		playersGrid.setImmediate(true);
		SingleSelectionModel sm = new SingleSelectionModel();
		sm.setDeselectAllowed(true);
		playersGrid.setSelectionModel(sm);
		playersGrid.addSelectionListener(e -> playerSelected());
		playersGrid.setWidth("100%");
		v.addComponent(playersGrid);
		direct = new TextArea("Direct links");
		direct.setStyleName(ValoTheme.TEXTAREA_TINY);
		direct.setWidth("475px");
		direct.setHeight("7em");
		v.addComponent(direct);
	}
	
	protected void playerSelected()
	{
		Player p = (Player)playersGrid.getSelectedRow();
		boolean sel = (p != null);
		create.setEnabled(!sel);
		edit.setEnabled(sel);
		delete.setEnabled(sel && p.canBeDeleted());
		createTeam.setEnabled(sel);
		createLadder.setEnabled(sel);
		createTourny.setEnabled(sel);
		setCurrentPlayer(p);
		if (sel)
		{
			direct.setValue(getParentUI().encodePlayerId(p) + "\n" + getParentUI().encodeTournament(p, currentTourny) + "\n" + getParentUI().encodeTournamentMatch(currentTourny, 0, 0));
			Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
			StringSelection email = new StringSelection(p.getEmail());
			c.setContents(email, email);
		}
		else
			direct.setValue("");
	}
	
	public void update()
	{
		updateUI(() -> {
			players.removeAllItems();
			players.addAll(getHood().getPlayers());
			playerSelected();
			externals.removeAllItems();
			externals.addAll(getHood().getExternals());
			externalsSelected();
			ladders.removeAllItems();
			ladders.addAll(getHood().getLadders());
			ladderSelected();
			tournys.removeAllItems();
			tournys.addAll(getHood().getTournaments());
			tournySelected();
		});
	}
	
	protected Button newButton(String lbl)
	{
		Button b = new Button(lbl);
		b.setStyleName(ValoTheme.BUTTON_SMALL);
		b.setEnabled(false);
		return b;
	}
	
	protected void buildLeft(VerticalLayout v)
	{
		v.addComponent(new Label("Functions:"));
		create = newButton("Create New Player");
		create.addClickListener(e -> createPlayer());
		v.addComponent(create);
		edit = newButton("Edit Selected Player");
		edit.addClickListener(e -> editPlayer());
		v.addComponent(edit);
		delete = newButton("DELETE Selected Player");
		delete.addClickListener(e -> deletePlayer());
		v.addComponent(delete);
		createTeam = newButton("Create New External Team");
		createTeam.addClickListener(e -> createTeam());
		v.addComponent(createTeam);
		createLadder = newButton("Create New Ladder");
		createLadder.addClickListener(e -> createLadder());
		v.addComponent(createLadder);
		createTourny = newButton("Create New Tournament");
		createTourny.addClickListener(e -> createTourny());
		v.addComponent(createTourny);
		Button b = newButton("Open Log view");
		b.addClickListener(e -> navigateTo(DesktopUI.LOG));
		b.setEnabled(true);
		v.addComponent(b);
		b = new Button("Save");
		b.setStyleName(ValoTheme.BUTTON_DANGER);
		b.addClickListener(e -> {
			Utils.saveData();
			Utils.backupData();
			Notification.show("Data saved");
		});
		v.addComponent(b);
		b = new Button("Fix Pictures");
		b.addClickListener(e -> { 
			getHood().getPictures(null).forEach(p -> {
				if (p.getWidth() == 0)
					p.saveSize(p.readFile());
			});
		});
		v.addComponent(b);
		b = new Button("Get emails");
		b.addClickListener(e -> Utils.processEmails());
		v.addComponent(b);
		b = new Button("Import data");
		b.addClickListener(e -> Utils.importManual());
		v.addComponent(b);
		b = new Button("Tourny Week End");
		b.addClickListener(e -> currentTourny.processSundayMidnight(new Date(new Date().getTime() - (86400*1000))));
		v.addComponent(b);
		b = new Button("Tournys recalc");
		b.addClickListener(e -> getHood().getTournaments().forEach(t -> t.recalculate()));
		v.addComponent(b);

		CheckBox cb = new CheckBox("DEBUG", Utils.getDebugLogging());
		cb.addValueChangeListener(e -> Utils.setDebugLogging(cb.getValue()));
		v.addComponent(cb);	
	}
	
	protected void buildExternalList(VerticalLayout v)
	{
		externals = new BeanItemContainer<ExternalTeam>(ExternalTeam.class);
		externalsGrid = new Grid("Available External teams:", externals);
		externalsGrid.setEditorEnabled(true);
		externalsGrid.removeAllColumns();
		externalsGrid.setHeight(((getBrowserHeight() / 3) - 150) + "px");
		externalsGrid.addColumn("name");
		externalsGrid.addColumn("captainNames").setEditable(false);
		externalsGrid.getColumns().stream().forEach(c -> c.setSortable(false));
		externalsGrid.setImmediate(true);
		SingleSelectionModel sm = new SingleSelectionModel();
		sm.setDeselectAllowed(true);
		externalsGrid.setSelectionModel(sm);
		externalsGrid.addSelectionListener(e -> externalsSelected());
		externalsGrid.setWidth("100%");
		v.addComponent(externalsGrid);
	}
	
	protected void buildLadderList(VerticalLayout v)
	{
		ladders = new BeanItemContainer<Ladder>(Ladder.class);
		laddersGrid = new Grid("Available Ladders:", ladders);
		laddersGrid.setEditorEnabled(true);
		laddersGrid.removeAllColumns();
		laddersGrid.setHeight(((getBrowserHeight() / 3) - 150) + "px");
		laddersGrid.addColumn("name");
		laddersGrid.addColumn("strict").setEditable(false);
		laddersGrid.getColumns().stream().forEach(c -> c.setSortable(false));
		laddersGrid.setImmediate(true);
		SingleSelectionModel sm = new SingleSelectionModel();
		sm.setDeselectAllowed(true);
		laddersGrid.setSelectionModel(sm);
		laddersGrid.addSelectionListener(e -> ladderSelected());
		laddersGrid.setWidth("100%");
		v.addComponent(laddersGrid);	
	}
	
	protected void buildTournyList(VerticalLayout v)
	{
		tournys = new BeanItemContainer<Tournament>(Tournament.class);
		tournysGrid = new Grid("Available Tournaments:", tournys);
		tournysGrid.setEditorEnabled(true);
		tournysGrid.removeAllColumns();
		tournysGrid.setHeight(((getBrowserHeight() / 3) - 150) + "px");
		tournysGrid.addColumn("name");
		tournysGrid.getColumns().stream().forEach(c -> c.setSortable(false));
		tournysGrid.setImmediate(true);
		SingleSelectionModel sm = new SingleSelectionModel();
		sm.setDeselectAllowed(true);
		tournysGrid.setSelectionModel(sm);
		tournysGrid.addSelectionListener(e -> tournySelected());
		tournysGrid.setWidth("100%");
		v.addComponent(tournysGrid);	
	}
	
	protected void buildRight(VerticalLayout v)
	{
		buildExternalList(v);
		HorizontalLayout h  = new HorizontalLayout();
		h.setSpacing(true);
		upTeam = newButton("Move Team Up");
		upTeam.addClickListener(e -> upTeam());
		downTeam = newButton("Move Team Down");
		downTeam.addClickListener(e -> downTeam());
		deleteTeam = newButton("Delete Team");
		deleteTeam.addClickListener(e -> deleteTeam());
		h.addComponent(upTeam);
		h.addComponent(downTeam);
		h.addComponent(deleteTeam);
		v.addComponent(h);
		
		buildLadderList(v);
		h  = new HorizontalLayout();
		h.setSpacing(true);
		upLadder = newButton("Move Ladder Up");
		upLadder.addClickListener(e -> upLadder());
		downLadder = newButton("Move Ladder Down");
		downLadder.addClickListener(e -> downLadder());
		deleteLadder = newButton("Delete Ladder");
		deleteLadder.addClickListener(e -> deleteLadder());
		strict = new CheckBox("Strict?");
		strict.addValueChangeListener(e -> strictLadder());
		h.addComponent(upLadder);
		h.addComponent(downLadder);
		h.addComponent(deleteLadder);
		h.addComponent(strict);
		v.addComponent(h);

		buildTournyList(v);
		h  = new HorizontalLayout();
		h.setSpacing(true);
		upTourny = newButton("Move Tourny Up");
		upTourny.addClickListener(e -> upTourny());
		downTourny = newButton("Move Tourny Down");
		downTourny.addClickListener(e -> downTourny());
		deleteTourny = newButton("Delete Tourny");
		deleteTourny.addClickListener(e -> deleteTourny());
		h.addComponent(upTourny);
		h.addComponent(downTourny);
		h.addComponent(deleteTourny);
		v.addComponent(h);
	}
	
	protected void externalsSelected()
	{
		ExternalTeam t = (ExternalTeam)externalsGrid.getSelectedRow();
		setCurrentTeam(t);
		boolean sel = (t != null);
		deleteTeam.setEnabled(sel && t.canBeDeleted());
		int i = getHood().getExternals().indexOf(t);
		upTeam.setEnabled(sel && (i > 0));
		downTeam.setEnabled(sel && (i < (getHood().getExternals().size() - 1)));
	}
	
	protected void ladderSelected()
	{
		currentLadder = (Ladder)laddersGrid.getSelectedRow();
		boolean sel = (currentLadder != null);
		strict.setEnabled(sel);
		strict.setValue(sel ? currentLadder.isStrict() : false);
		deleteLadder.setEnabled(sel && currentLadder.canBeDeleted());
		int i = getHood().getLadders().indexOf(currentLadder);
		upLadder.setEnabled(sel && (i > 0));
		downLadder.setEnabled(sel && (i < (getHood().getLadders().size() - 1)));
	}
	
	protected void strictLadder()
	{
		if (currentLadder != null)
		{
			currentLadder.setStrict(strict.getValue());
			laddersGrid.refreshAllRows();
		}
	}

	protected void tournySelected()
	{
		currentTourny = (Tournament)tournysGrid.getSelectedRow();
		boolean sel = (currentTourny != null);
		deleteTourny.setEnabled(sel && currentTourny.canBeDeleted());
		int i = getHood().getTournaments().indexOf(currentTourny);
		upTourny.setEnabled(sel && (i > 0));
		downTourny.setEnabled(sel && (i < (getHood().getTournaments().size() - 1)));
	}

	
	protected void deleteTeam()
	{
		if (getCurrentTeam() != null)
		{
			if (getCurrentTeam().canBeDeleted())
			{
				externals.removeItem(getCurrentTeam());
				getHood().removeExternal(getCurrentTeam());
				deleteTeam.setEnabled(false);
				externalsGrid.deselectAll();
				setCurrentTeam(null);
				update();
			}
		}
	}
	
	protected void deleteLadder()
	{
		if (currentLadder != null)
		{
			if (currentLadder.canBeDeleted())
			{
				ladders.removeItem(currentLadder);
				getHood().removeLadder(currentLadder);
				deleteLadder.setEnabled(false);
				laddersGrid.deselectAll();
				currentLadder = null;
				update();
			}
		}
	}
	
	protected void deleteTourny()
	{
		if (currentTourny != null)
		{
			if (currentTourny.canBeDeleted())
			{
				getHood().removeTournament(currentTourny);
				tournys.removeItem(currentTourny);
				tournysGrid.deselectAll();
				deleteTourny.setEnabled(false);
				currentTourny = null;
				update();
			}
		}
	}


	
	protected void upLadder()
	{
		up(currentLadder, getHood().getLadders());
	}
	
	protected void upTourny()
	{
		up(currentTourny, getHood().getTournaments());
	}
	
	protected void upTeam()
	{
		up(getCurrentTeam(), getHood().getExternals());
	}
	
	protected void up(Object obj, ArrayList list)
	{
		if (obj != null)
		{
			int i = list.indexOf(obj);
			if (i > 0)
			{
				list.remove(i);
				list.add(i - 1, obj);
				update();
			}
		}
	}
	
	protected void downTeam()
	{
		down(getCurrentTeam(), getHood().getExternals());
	}
	
	protected void downLadder()
	{
		down(currentLadder, getHood().getLadders());
	}
	
	protected void downTourny()
	{
		down(currentTourny, getHood().getTournaments());
	}
	
	protected void down(Object obj, ArrayList list)
	{
		if (obj != null)
		{
			int i = list.indexOf(obj);
			if (i < (list.size() - 1))
			{
				list.remove(i);
				list.add(i + 1, obj);
				update();
			}
		}
	}

	
	protected void createTeam()
	{
		ExternalTeam t = new ExternalTeam();
		t.addCaptain(getCurrentPlayer());
		getHood().addExternal(t);
		update();
	}
	
	protected void createLadder()
	{
		TextField input = new TextField("Enter Ladder Name:");
		input.setImmediate(true);
		MessageBox mb = MessageBox.createQuestion()
				.withCaption("Create Ladder")
				.withWidth("400px")
				.withMessage(input)
				.withOkButton(() -> { 
					Ladder l = new LadderSingles();
					l.addOrganizer(getCurrentPlayer());
					l.setName(input.getValue());
					getHood().addLeague(l);
					update(); })
				.withCancelButton();
		input.addValueChangeListener(e -> { mb.getButton(ButtonType.OK).click(); });
		mb.open();
	}
	
	protected void createTourny()
	{
		
		TextField input = new TextField("Enter Tournament Name:");
		input.setImmediate(true);
		MessageBox mb = MessageBox.createQuestion()
				.withCaption("Create Tournament")
				.withWidth("400px")
				.withMessage(input)
				.withOkButton(() -> { 
					Tournament t = new Tournament();
					t.addOrganizer(getCurrentPlayer());
					t.setName(input.getValue());
					getHood().addLeague(t);
					update(); })
				.withCancelButton();
		input.addValueChangeListener(e -> { mb.getButton(ButtonType.OK).click(); });
		mb.open();

	}
	

	
	protected void deletePlayer()
	{
		if (getCurrentPlayer() != null && getCurrentPlayer().canBeDeleted())
		{
			getHood().deletePlayer(getCurrentPlayer());
			setCurrentPlayer(null);
			playersGrid.deselectAll();
			update();
		}
	}
	
	protected void createPlayer()
	{
		TextField input = new TextField("Email:");
		input.setImmediate(true);
		MessageBox mb = MessageBox.createQuestion()
				.withCaption("Create Player")
				.withWidth("400px")
				.withMessage(input)
				.withOkButton(() -> { createPlayer(input.getValue()); })
				.withCancelButton();
		input.addValueChangeListener(e -> { mb.getButton(ButtonType.OK).click(); });
		mb.open();
	}
	
	protected void createPlayer(String email)
	{
		if (email != null && !email.isEmpty())
		{
			Player p = new Player();
			p.setEmail(email);
			if (!getHood().addPlayer(p))
				Notification.show("Unable to add player using email:" + p.getEmail());
			update();
		}
	}
	
	protected void editPlayer()
	{
		navigateTo(DesktopUI.PROFILE);
	}

	public ExternalTeam getCurrentTeam()
	{
		return currentTeam;
	}

	public void setCurrentTeam(ExternalTeam currentTeam)
	{
		this.currentTeam = currentTeam;
	}

}
