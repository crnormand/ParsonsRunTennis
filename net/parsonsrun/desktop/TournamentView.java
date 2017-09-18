package net.parsonsrun.desktop;

import java.awt.Point;
import java.util.*;

import com.vaadin.data.util.*;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.Grid.CellReference;
import com.vaadin.ui.Grid.Column;
import com.vaadin.ui.Grid.HeaderRow;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.Table.ColumnHeaderMode;
import com.vaadin.ui.renderers.HtmlRenderer;
import com.vaadin.ui.themes.ValoTheme;

import de.steinwedel.messagebox.ButtonOption;
import de.steinwedel.messagebox.MessageBox;
import net.parsonsrun.*;
import net.parsonsrun.domain.*;

public class TournamentView extends DesktopBaseView implements MatchEditOwner
{
	protected TabSheet sheet;
	protected Tab standingsTab;
	protected Tab roundsTab;
	protected Tab chartEloTab;
	protected Tab setupTab;
	protected Tab comTab;
	protected Tab optionsTab;
	protected Tab rosterTab;
	protected Tab picsTab;
	protected VerticalLayout standingsComp = new VerticalLayout();
	protected VerticalLayout roundsComp = new VerticalLayout();
	protected VerticalLayout setupComp = new VerticalLayout();
	protected VerticalLayout comComp = new VerticalLayout();
	protected VerticalLayout optionsComp = new VerticalLayout();
	protected VerticalLayout rosterComp = new VerticalLayout();
	protected CssLayout picsComp;
	BeanItemContainer<Standing> standings = new BeanItemContainer<Standing>(Standing.class);
	BeanItemContainer<Match> matches = new BeanItemContainer<Match>(Match.class);
	protected Grid standingsGrid;
	protected Panel matchesGrid;
	protected Grid grid;
	protected BeanItemContainer<Row> rows;
	protected Match currentMatch;
	protected long lastClick;
	protected ListSelect teamSelect;
	protected IndexedContainer teams;
	protected ListSelect playersSelect;
	protected IndexedContainer players;
	protected Player tempPlayer;
	protected boolean editorOpened = false;
	protected Table standingsTable;
	protected Label currentTeamName;
	protected Standing currentStanding;

	public class Row
	{
		Tournament league;
		int index;
		public Row(Tournament lg, int rowNumber)
		{
			league = lg;
			index = rowNumber;
		}
		
		public String get(int i)
		{
			return getMatch(i).getDesktopSmallHtml();
		}
		
		public Match getMatch(String prop)
		{
			String col = prop.substring(5);
			int i = Integer.parseInt(col);
			return getMatch(i);
		}
		
		public Match getMatch(int i)
		{
			Round r = league.getRound(i - 1);
			return r.getMatch(index);
		}
		
		public String getRound0()
		{
			return get(0);
		}

		public String getRound1()
		{
			return get(1);
		}
		
		public String getRound2()
		{
			return get(2);
		}

		public String getRound3()
		{
			return get(3);
		}

		public String getRound4()
		{
			return get(4);
		}

		public String getRound5()
		{
			return get(5);
		}

		public String getRound6()
		{
			return get(6);
		}

		public String getRound7()
		{
			return get(7);
		}

		public String getRound8()
		{
			return get(8);
		}

		public String getRound9()
		{
			return get(9);
		}

		public String getRound10()
		{
			return get(10);
		}

		public String getRound11()
		{
			return get(11);
		}

		public String getRound12()
		{
			return get(12);
		}

		public String getRound13()
		{
			return get(13);
		}

		public String getRound14()
		{
			return get(14);
		}

		public String getRound15()
		{
			return get(15);
		}

		public String getRound16()
		{
			return get(16);
		}

		public String getRound17()
		{
			return get(17);
		}

		public String getRound18()
		{
			return get(18);
		}

		public String getRound19()
		{
			return get(19);
		}

		public String getRound20()
		{
			return get(20);
		}
	}
	
	protected ArrayList<Row> getRowData()
	{
		ArrayList<Row> r = new ArrayList<Row>();
		Tournament lg = getTourny();
		if (lg != null && lg.getRounds().size() > 0)
		{
			Round first = lg.getRound(0);
			int sz = first.getMatches().size();
			for (int i = 0; i < sz; i++)
			{
				r.add(new Row(lg, i));
			}
		}
		return r;
	}
	
	protected void viewResized()
	{
		super.viewResized();
		rebuildStandings();
		rebuildRounds();
	}

	@Override
	public void buildUI()
	{
		String lg = getLoginUser() == null ? "" : getLoginUser().getFirst();
		String tn = getTourny() == null ? "" : (" for " + getTourny().getName());
		addBack("Welcome " + lg + ", to the Tournament view :  " + tn + (canSetup() ? "&nbsp;&nbsp;&nbsp; ** NOTE:  Items containing '*' can only be seen by the organizer(s)" : ""));
		if (getTourny() == null)
			return;
		sheet = new TabSheet();
		addComponent(sheet);
		sheet.addStyleName(ValoTheme.TABSHEET_FRAMED);
		sheet.addStyleName(ValoTheme.TABSHEET_COMPACT_TABBAR);
		sheet.addSelectedTabChangeListener(e -> checkSelectedTab());
		sheet.setSizeFull();
		if (!getTourny().isIgnoreStandings())
			buildStandings();
		buildRounds();
		if (getTourny().isIgnoreStandings())
			buildStandings();
		rosterTab = sheet.addTab(rosterComp, "Roster", FontAwesome.USERS);
		buildRoster();

		comComp = new VerticalLayout();
		comTab = sheet.addTab(comComp, "Email", FontAwesome.ENVELOPE_O);
		comTab.setVisible(getTourny().includes(getLoginUser()) || canSetup());
		buildEmail();
		buildPics();
//		buildChartElo();
		if (canSetup())
		{
			buildOptions();
			buildSetup();
		}
		
		update();
		
		if (getParentUI().isTournamentMatchAction())
		{
			int r = getParentUI().getRoundParameter();
			int m = getParentUI().getMatchParameter();
			Round rnd = getTourny().getRound(r);
			if (rnd != null)
			{
				Match mtch = rnd.getMatch(m);
				if (mtch != null && !mtch.hasBeenPlayed())
				{
					currentMatch = mtch;
					openMatchEditor();
				}
			}
		}
		clearAction();
	}
	
	protected void createNewPlayer(TextField f, TextField l, TextField e, TextField c, CheckBox m)
	{
		if (e.getValue().isEmpty())
			return;
		Player p = new Player();
		p.setFirst(f.getValue());
		p.setLast(l.getValue());
		p.setEmail(e.getValue());
		p.setPhone(c.getValue());
		p.setMale(m.getValue());
		if (getHood().addPlayer(p))
		{
			saveWhile(() -> players.addItem(p));
			f.setValue("");
			l.setValue("");
			e.setValue("");
			c.setValue("");
			Notification.show(p.toString() + " successfully added", Type.HUMANIZED_MESSAGE);
		}
		else
			Notification.show(p.toString() + " already exists!", Type.ERROR_MESSAGE);
	}
	
	protected Component buildNewPlayerPanel(CheckBox men, CheckBox women)
	{
		VerticalLayout ex = new VerticalLayout();
		ex.addComponent(new Label("If the player doesn't exist,<br>enter their information below:", ContentMode.HTML));
		TextField first = new TextField("First Name:");
		first.setImmediate(true);
		first.setColumns(20);
		TextField last = new TextField("Last Name:");
		last.setImmediate(true);
		last.setColumns(20);
		TextField email = new TextField("Email:");
		email.setImmediate(true);
		email.setColumns(20);
		TextField phone = new TextField("Cell:");
		phone.setImmediate(true);
		phone.setColumns(10);
		Button a = new Button("Add new player");
		a.setEnabled(false);
		CheckBox male = new CheckBox("Male", men.getValue());
		CheckBox female = new CheckBox("Female", women.getValue() && !men.getValue());
		men.addValueChangeListener(e -> { male.setValue(men.getValue()); });
		women.addValueChangeListener(e -> { female.setValue(women.getValue()); });
		phone.addValueChangeListener(e -> { 
			boolean enabled = !(first.isEmpty() || last.isEmpty() || email.isEmpty() || phone.isEmpty());
			a.setEnabled(enabled);
			if (enabled)
			{
				createNewPlayer(first, last, email, phone, male); 
				updateAvailablePlayers(men,  women);
				first.focus();
			}} );
		first.addValueChangeListener(e -> a.setEnabled(!(first.isEmpty() || last.isEmpty() || email.isEmpty() || phone.isEmpty())));
		last.addValueChangeListener(e -> a.setEnabled(!(first.isEmpty() || last.isEmpty() || email.isEmpty() || phone.isEmpty())));
		email.addValueChangeListener(e -> a.setEnabled(!(first.isEmpty() || last.isEmpty() || email.isEmpty() || phone.isEmpty())));
		ex.addComponent(first);
		ex.addComponent(last);
		ex.addComponent(email);
		ex.addComponent(phone);
		HorizontalLayout sex = new HorizontalLayout();
		male.addValueChangeListener(e -> { if (male.getValue()) female.setValue(false); });
		female.addValueChangeListener(e -> { if (female.getValue()) male.setValue(false); });
		sex.addComponent(male);
		sex.addComponent(female);
		ex.addComponent(sex);
		a.setDescription("If a player is not already in the list, enter their information above and press 'Add new player'");
		a.addClickListener(e -> { 
			createNewPlayer(first, last, email, phone, male); 
			updateAvailablePlayers(men,  women);
			a.setEnabled(false);
			first.focus(); } );
		ex.addComponent(a);
		return ex;
	}
	
	public void updateMatch(Runnable r)
	{
		saveWhile(r);
		getParentUI().closeDialog();
		matchesGrid.markAsDirty();
		rebuildRounds();
		rebuildStandings();
		updateStandingsStats();
	}
	public AbstractField<Date> makeDateField()
	{
		return new DateField();
	}
	
	protected void buildStandings()
	{
		standingsTab = sheet.addTab(standingsComp, getTourny().isIgnoreStandings() ? "Statistics" : "Standings", FontAwesome.TABLE);
		rebuildStandings();
	}
	
	protected void buildRoster()
	{
		rosterComp.setMargin(true);
		rosterComp.setSpacing(false);
		HorizontalLayout h = new HorizontalLayout();
		rosterComp.addComponent(h);
		VerticalLayout vn = new VerticalLayout();
		vn.setSpacing(false);
		vn.setMargin(false);
		h.addComponent(vn);
		VerticalLayout ve = new VerticalLayout();
		ve.setSpacing(false);
		ve.setMargin(false);
		h.addComponent(ve);
		VerticalLayout vc = new VerticalLayout();
		vc.setSpacing(false);
		vc.setMargin(false);
		h.addComponent(vc);
		for (Player p : getTourny().getPlayers())
		{
			if (canSetup() || p.equals(getLoginUser()))
			{
				TextField n = new TextField();
				n.setColumns(15);
				n.setStyleName(ValoTheme.TEXTFIELD_SMALL);
				n.setValue(p.firstLastName());
				n.setReadOnly(true);
				vn.addComponent(n);
				TextField e = new TextField(new MethodProperty<String>(p, "email"));
				e.addValueChangeListener(q -> saveData());
				e.setColumns(20);
				e.setStyleName(ValoTheme.TEXTFIELD_SMALL);
				ve.addComponent(e);
				TextField c = new TextField();
				c.setValue(p.getPhoneDisplay());
				c.addFocusListener(x -> { 		
					c.setValue(Utils.compactPhone(c.getValue()));
				});
				c.addBlurListener(x -> { 		
					c.setValue(Utils.expandPhone(c.getValue()));
				});
				c.setStyleName(ValoTheme.TEXTFIELD_SMALL);
				Label w = new Label("");
				w.setStyleName("redtext");
				if (p.equals(getLoginUser()))
				{
					if (p.getPhone().isEmpty())
					{
						w.setValue(" <- Please enter your cell phone");
						c.addStyleName(DesktopUI.PHONE_EDIT);
					}
					HorizontalLayout h2 = new HorizontalLayout();
					h2.setSpacing(true);
					h2.addComponent(c);
					h2.addComponent(w);
					vc.addComponent(h2);
				}
				else
					vc.addComponent(c);
				c.addValueChangeListener(q -> saveWhile(() -> {
					p.setPhone(c.getValue());
					if (c.getValue().isEmpty())
					{
						w.setValue(" <- Please enter your cell phone");
						c.removeStyleName(DesktopUI.PHONE_EDIT);
						c.addStyleName(DesktopUI.PHONE_EDIT);
					}
					else
					{
						w.setValue("");
						c.removeStyleName(DesktopUI.PHONE_EDIT);
					}
				}));
			}
			else
			{
				vn.addComponent(new Label(p.firstLastName()));
				ve.addComponent(new Label(p.getEmail()));
				vc.addComponent(new Label(p.getPhoneDisplay()));
			}
		}
	
	}
	
	protected void buildEmail()
	{
		comComp.setSpacing(true);
		comComp.setMargin(true);
		HorizontalLayout h0 = new HorizontalLayout();
		h0.setSpacing(true);
		ListSelect players = new ListSelect("Players", getTourny().getPlayers());
		Button direct = new Button("* Send Email with Direct link for '" + getTourny().getName() + "' *");
		direct.setStyleName(ValoTheme.BUTTON_SMALL);
		direct.setDescription("Send Email with a link that will automatically log the player in, and bring them to this tournament '" + getTourny().getName() + "'");
		HorizontalLayout h1 = new HorizontalLayout();
		h1.setSpacing(true);
		players.setMultiSelect(true);
		players.setRows((getBrowserHeight() - 500) / 16);
		Button selectAll = new Button("Select All");
		selectAll.setStyleName(ValoTheme.BUTTON_SMALL);
		selectAll.addClickListener(e -> getTourny().getPlayers().stream().forEach(p -> players.select(p)));
		TextField subject = new TextField("Subject:");
		subject.setColumns(40);
		TextArea mesg = new TextArea("Message:");
		mesg.setWidth((getBrowserWidth() - 300) + "px");
		mesg.setHeight(((getBrowserHeight() - (canSetup() ? 430: 330)) / 16 )+ "em");
		Label l1 = new Label("Hold CTRL key and click<br>to select multiple players", ContentMode.HTML);
		l1.setStyleName(ValoTheme.LABEL_TINY);
		Button send = new Button("Send Email");
		send.setIcon(FontAwesome.ENVELOPE_O);
		String signed = "From: " + getLoginUser().firstLastName() + " (" + getLoginUser().getEmail() + ")";
		send.addClickListener(e -> { 
			ArrayList<Player> ps = new ArrayList<Player>();
			((Set<Player>)players.getValue()).stream().forEach(p -> ps.add(p));
			getParentUI().sendEmail(ps, subject.getValue(), Utils.escapeHTML(mesg.getValue()) + "<br><br>" + signed);
			subject.setValue("");
			mesg.setValue("");
			Notification.show("Email sent"); });
		direct.addClickListener(e -> { 
			((Set<Player>)players.getValue()).stream().forEach(p -> getParentUI().sendEmail(p, 
				subject.getValue(), 
				Utils.escapeHTML(mesg.getValue()) + "<br><br><a href='" + getParentUI().encodeTournament(p, getTourny()) + "'>Click HERE</a> to automatically log you into '" +
						getTourny().getName() + "'.<br><br>" + 
						signed));
			subject.setValue("");
			mesg.setValue("");
			Notification.show("Email sent with Direct Link"); });
		VerticalLayout v1 = new VerticalLayout();
		VerticalLayout v2 = new VerticalLayout();
		comComp.addComponent(h1);
		h1.addComponent(v1);
		h1.addComponent(v2);
		v1.addComponent(players);
		v1.addComponent(selectAll);
		v1.addComponent(l1);
		v2.addComponent(subject);
		v2.addComponent(mesg);
		v2.addComponent(new Label(signed));
		h0.addComponent(send);
		if (canSetup())
		{
			h0.addComponent(new Label("** Organizer functions: "));
			h0.addComponent(direct);
		}
		v2.addComponent(h0);
		send.setEnabled(false);
		direct.setEnabled(false);
		players.addValueChangeListener(e -> { 
			boolean en = ((Set<Player>)players.getValue()).size() > 0;
			send.setEnabled(en);
			direct.setEnabled(en);
		});
	}
	

	
	protected void rebuildStandings()
	{
		standingsComp.removeAllComponents();
		HorizontalLayout h = new HorizontalLayout();
		h.setSpacing(true);
		standingsComp.addComponent(h);
		VerticalLayout v = new VerticalLayout();
		v.addComponent(buildStandingsGrid());
		v.addComponent(getLegend());
		h.addComponent(v);
		h.addComponent(buildStandingsStats());
		h.addComponent(buildMatchesGrid());
		standingSelected();
	}
	
	protected Component buildStandingsStats()
	{
		VerticalLayout v = new VerticalLayout();
		//v.setSpacing(true);
		standingsTable = new Table();
		//standingsTable.setWidth("100%");
		standingsTable.setStyleName("smallpadding");
		standingsTable.setWidth((getStandingsGridWidth() - 200) + "px");
		standingsTable.setColumnHeaderMode(ColumnHeaderMode.HIDDEN);
		standingsTable.addContainerProperty("Description", String.class, null);
		standingsTable.addContainerProperty("Value",  String.class, null);
		//v.addComponent(new Label("Statistics for current team:"));
		currentTeamName = new Label("Statistics", ContentMode.HTML);
		currentTeamName.setStyleName(ValoTheme.LABEL_BOLD);
		v.addComponent(currentTeamName);
		v.addComponent(standingsTable);
		v.addComponent(new Label("Hover over match to view comment ->"));
		return v;
	}
	
	protected void buildPics()
	{
		picsComp = new CssLayout();
		picsTab = sheet.addTab(picsComp, "Pics", FontAwesome.PICTURE_O);
		picsComp.setSizeFull();
		updatePictures();
	}
	
	protected void updatePictures()
	{
		picsComp.removeAllComponents();
		Upload upload = PictureFactory.makeUpload(getHood(), getLoginUser(), getPictureAssociation(), () -> updatePictures());
		VerticalLayout v = new VerticalLayout(upload);
		v.setWidthUndefined();
		v.setComponentAlignment(upload, Alignment.MIDDLE_LEFT);
		picsComp.addComponent(v);
		if (getHood().getPictures(getPictureAssociation()).isEmpty())
			return;
		Label lbl = new Label("Click to view ->");
		v.addComponent(lbl);
		v.setComponentAlignment(lbl, Alignment.BOTTOM_LEFT);

		int size = getHood().getPictures(getPictureAssociation()).size();
		if (size == 0)
			return;
		int start = nextInt(size);
		Picture pic = getHood().getPicture(getPictureAssociation(), start);
		while (pic != null && (size-- > 0))
		{
			final Picture p = pic;
			picsComp.addComponent(pic.newImage(Utils.PICTURE_THUMBNAIL_HEIGHT, e -> selectedPicture(p)));
			pic = getHood().getNextPicture(pic, getPictureAssociation(), 1);
		}
	}
	

	
	protected void selectedPicture(Picture p)
	{
		currentPic = p;
		Tuple<HorizontalLayout, Point> tuple = getPictureViewer(true);
		getParentUI().openDialog(tuple.getLeft(), (int)tuple.getRight().getX(), (int)tuple.getRight().getY() + 15).addCloseListener(e -> saveWhile(() -> updatePictures()));
	}
	
	protected DomainObject getPictureAssociation()
	{
		return getTourny();
	}
	
	protected void checkSelectedTab()
	{
		Component c = sheet.getSelectedTab();
		if (c == null)
		{
			println("Selected Tab: undefined");
			return;
		}
		Tab t = sheet.getTab(c);
		println("Selected Tab: " + (t == null ? "unknown" : t.getCaption()));
	}
	
	protected void updateStandingsStats()
	{
		standingsTable.removeAllItems();
		if (currentStanding == null)
		{
			currentTeamName.setValue("Statistics");
			return;
		}
		int i = 1;
		standingsTable.addItem(row(currentStanding.getCommentBottomA(), currentStanding.getCommentBottomB()), i++);
		standingsTable.addItem(row( currentStanding.getCommentTopA(), currentStanding.getCommentTopB()), i++);
		standingsTable.addItem(row("% Matches won", currentStanding.getMatchWinPerc() + "%"), i++);
		standingsTable.addItem(row("Matches", currentStanding.getMatchesPlayed()), i++);
		standingsTable.addItem(row("Defaulted", currentStanding.getMatchesDefaulted()), i++);
		standingsTable.addItem(row("Matches Won", currentStanding.getMatchesWon()), i++);
		standingsTable.addItem(row("% of Sets won", currentStanding.getSetsWinPerc() + "%"), i++);
		standingsTable.addItem(row("Sets", currentStanding.getSetsPlayed()), i++);
		standingsTable.addItem(row("Sets won", currentStanding.getSetsWon()), i++);
		standingsTable.addItem(row("% of Game won", currentStanding.getGamesWinPerc() + "%"), i++);
		standingsTable.addItem(row("Games", currentStanding.getGamesPlayed()), i++);
		standingsTable.addItem(row("Games won", currentStanding.getGamesWon()), i++);
		standingsTable.addItem(row("LATE PASSes", currentStanding.getNumberOfLatePasses()), i++);
		standingsTable.setPageLength(0);
		Team t = currentStanding.getTeam();
		currentTeamName.setValue("Statistics for " + t.getName());
	}

	
	protected Object[] row(String s1, String s2)
	{
		return new Object[] { s1, s2 };
	}

	protected Object[] row(String s1, int i)
	{
		return row(s1, "" + i);
	}
	
	public CheckBox makeCheckBox()
	{
		return new CheckBox();
	}
	
	public void update()
	{
		updateUI(() -> {
			standings.removeAllItems();
			standings.addAll(getTourny().getStandings());
		});
	}
		
	protected Grid buildStandingsGrid()
	{
		int w = getStandingsGridWidth(); 
		standingsGrid = new Grid("Select to see statistics and matches:", standings);
		standingsGrid.setStyleName("smallpadding");
		standingsGrid.setImmediate(true);
		standingsGrid.removeAllColumns(); 
		standingsGrid.addColumn("rank").setHeaderCaption("").setMinimumWidth(35);
		standingsGrid.addColumn("name").setHeaderCaption("Name").setExpandRatio(1);
		HeaderRow header = standingsGrid.getDefaultHeaderRow();
		header.setStyleName("nopadding");
		header.getCell("name").setStyleName("nopadding");
		header.getCell("rank").setStyleName("nopadding");
		if (getTourny() == null)
			return standingsGrid;
		String[] cols = Standing.getColumnInfo(getTourny().isEloHidden());
		for (int i = 0; i < cols.length; i = i + 3)
		{
			standingsGrid.addColumn(cols[i]).setHeaderCaption(cols[i+1]).setMaximumWidth(Double.parseDouble(cols[i+2])).setMinimumWidth(Double.parseDouble(cols[i+2]));
			header.getCell(cols[i]).setStyleName("nopadding");
		}
		//standingsGrid.getColumns().stream().forEach(c -> c.setSortable(false));
		standingsGrid.addSelectionListener(e -> standingSelected());
		standingsGrid.setWidth(w + "px");
		standingsGrid.setCellDescriptionGenerator(new Grid.CellDescriptionGenerator() {
			public String getDescription(Grid.CellReference cellReference) {
				String tooltip = null;
				Standing s = (Standing)cellReference.getItemId();
				if (s != null)
				{
					tooltip = s.getHoverTextHtml();
				} 
				return tooltip;
			}
			});

		return standingsGrid;
	}
	
	protected int getStandingsGridWidth()
	{
		return (getBrowserWidth() / 3) + 100; 
	}
	

	
	protected Component buildMatchesGrid()
	{
		matchesGrid = new Panel();
		int w = getStandingsGridWidth() - 175;
		int h = getBrowserHeight() - 150;
		matchesGrid.setWidth(w + "px");
		matchesGrid.setHeight(h + "px");
		matchesGrid.setStyleName(ValoTheme.PANEL_BORDERLESS);
		return matchesGrid;
	}
	
	protected void rebuildMatchesGrid(ArrayList<Match> ms)
	{
		VerticalLayout v = new VerticalLayout();
		ms.stream().forEach(m -> {
			v.addComponent(new Label("<hr>" + m.getMobileHtml(), ContentMode.HTML));
		});
		matchesGrid.setContent(v);
	}

	protected Label getLegend()
	{
		return new Label(Standing.getLegendText(getStandingsGridWidth(), getTourny().isAdjustedStandings(), getTourny().isIgnoreStandings()), ContentMode.HTML);
	}
	

	
	protected void standingSelected()
	{
		currentStanding = (Standing)standingsGrid.getSelectedRow();
		if (currentStanding == null)
		{
			matchesGrid.setCaption("Recently played matches:");
			rebuildMatchesGrid(getTourny().getRecentlyPlayedMatches());
		}
		else
		{
			matchesGrid.setCaption("Matches for " + currentStanding.getTeam().getName() + ":");
			rebuildMatchesGrid(currentStanding.getMatches());
		}
		updateStandingsStats();
	}
	
	protected void matchSelected(ItemClickEvent event)
	{
//		Match m = (Match)event.getItemId();
//		if (m != null)
//		{
//			if (m.isBye())
//				Notification.show("Unable to update a BYE match", Type.ERROR_MESSAGE);
//			else
//			{
//				String warning = m.canEdit(getLoginUser());
//				if (warning == null)
//				{
//					MatchComponent mc = new MatchComponent(this, m);
//					
//					matchLayout.removeAllComponents();
//					matchLayout.addComponent(mc.buildUI());
//					matchLayout.setVisible(true);
//				}
//				else
//					Notification.show(warning, Type.ERROR_MESSAGE);
//			}
//		}
	}
	
	public String getEditWidth()
	{
		int w = (getBrowserWidth() / 3) - 200;
		return w + "px";
	}
	
	protected void rebuildRounds()
	{
		roundsComp.removeAllComponents();
		int height = 9;
		if (getTourny().isEloHidden())
			height = 6;
		rows = new BeanItemContainer<Row>(Row.class);
		ArrayList<Row> rs = getRowData();
		rows.addAll(rs);
		String s1 = "";
		if (getTourny().isClosed())
			s1 = "   (This Tournament is closed)";
		grid = new Grid("Coordinator(s): " + getTourny().getOrganizerNames() + s1, rows);
		roundsComp.addComponent(grid);
		grid.setWidth(getBrowserWidth() - 50, Unit.PIXELS);
		int h = height * rs.size();
		grid.setHeight((h + 1) + "em");
		grid.setSelectionMode(SelectionMode.NONE);
		int gridh = height - 1;
		grid.setStyleName("tallgrid" + height);
		grid.removeAllColumns();
		grid.getDefaultHeaderRow().setStyleName("centered");
		int i = 0;
		int rndW = Math.max(((getBrowserWidth() - 50) / getTourny().getRounds().size()) - 5, 160);
		for (Round rnd : getTourny().getRounds())
		{
			String s = "round" + (++i);
			grid.addColumn(s);
			Column col = grid.getColumn(s);
			col.setRenderer(new HtmlRenderer());
			col.setHeaderCaption(rnd.getHeaderName());
			col.setWidth(rndW);  // 160
		}
		grid.getColumns().stream().forEach(c -> c.setSortable(false));
		grid.setImmediate(false);
		grid.addItemClickListener(e -> scheduleMatchSelected(e));
		grid.setCellDescriptionGenerator(new Grid.CellDescriptionGenerator() {
			public String getDescription(Grid.CellReference cellReference) {
				String tooltip = null;
				Row r = (Row)cellReference.getItemId();
				String p = (String)cellReference.getPropertyId();
				Match m = r.getMatch(p);
				if (m != null)
				{
					tooltip = m.getHoverTextHtml();
				} 
				return tooltip;
			}
			});
		roundsComp.addComponent(new Label("Double click on a match to enter the score OR send an invite for a future match"));
	}
	
	protected void buildRounds()
	{
		roundsTab = sheet.addTab(roundsComp, "Schedule", FontAwesome.CALENDAR);
		rebuildRounds();
	}
	
	protected void scheduleMatchSelected(ItemClickEvent event)
	{
		Row r = (Row)event.getItemId();
		String p = (String)event.getPropertyId();
		Match prev = currentMatch;
		currentMatch = r.getMatch(p);
		long t = System.currentTimeMillis();
		if (currentMatch == prev && (t - lastClick < 500))
		{
			matchDoubleClicked();
			lastClick = 0;
		}
		lastClick = t;
	}
	
	protected synchronized void matchDoubleClicked()
	{
		if (editorOpened)
			return;
		editorOpened = true;
		if (currentMatch.isFuture())
			openInviteDialog();
		else
			openMatchEditor();
	}
	
	protected void openInviteDialog()
	{
		MessageBox.createQuestion()
			.withCaption("Future Match")
			.withMessage("Do you want to\n\nSend an Invite to the Match\n\nor\n\nEnter the Score?")
			.withOkButton(() -> sendMatchInvite(), ButtonOption.caption("Send Invite"))
			.withOkButton(() -> openMatchEditor(), ButtonOption.caption("Enter Score"))
			.withCancelButton(() -> editorOpened = false)
			.open();
	}
	
	protected void sendMatchInvite()
	{
		Round current = getTourny().getRoundFor(currentMatch);
		Panel pnl = new Panel();
		VerticalLayout v = new VerticalLayout();
		pnl.setContent(v);
		String subject = "Match Invite for " + current.getName();
		v.addComponent(new Label(subject + "<br><br>" + currentMatch.getEmailHtml(600, true), ContentMode.HTML));
		TextArea input = new TextArea("Optional message:");
		input.setWidth("100%");
		input.setRows(8);
		v.addComponent(input);
		String end = "Thank you,<br><br>" + getLoginUser().firstLastName();
		v.addComponent(new Label(end, ContentMode.HTML));
		MessageBox.createInfo()
			.withCaption("Invite email")
			.withWidth("700px")
			.withHeight("550px")
			.withMessage(pnl)
			.withOkButton(() -> 
			{
				currentMatch.sendMatch(getTourny(), current, subject, subject, Utils.escapeHTML(input.getValue()) + "<br><br>" + end, false, true);
				Notification.show("Invite email sent");
			})
			.withCancelButton()
			.open();
		editorOpened = false;
	}
	
	protected void openMatchEditor()
	{
		if (currentMatch != null)
		{
			if (currentMatch.isBye())
				Notification.show("Unable to update a BYE match", Type.ERROR_MESSAGE);
			else
			{
				String warning = currentMatch.canEdit(getLoginUser());
				if (warning == null)
				{
					Window w = getParentUI().openDialog(new MatchComponent(this, currentMatch).buildUI(), 450, 600);
					w.addCloseListener(e -> editorOpened = false);
				}
				else
					Notification.show(warning, Type.ERROR_MESSAGE);
			}
		}
	}

	protected void buildSetup()
	{
		setupTab = sheet.addTab(setupComp, "* Setup *");
		setupComp.setSpacing(true);
		TextField tf = new TextField("Tournament name:", new MethodProperty<String>(getTourny(), "name"));
		tf.setColumns(20);
		setupComp.addComponent(tf);
		HorizontalLayout h = new HorizontalLayout();
		h.setSpacing(true);
		teams = new IndexedContainer(getTourny().getTeams());
		teamSelect = new ListSelect("Current Teams", teams); 
		teamSelect.setNullSelectionAllowed(false);
		teamSelect.setMultiSelect(false);
		teamSelect.setRows(16);
		h.addComponent(teamSelect);
		Label cur = new Label("Select a player to add to a team");
		Button add = new Button("<- Add");
		Button rem = new Button("Remove ->");
		VerticalLayout v1 = new VerticalLayout(cur, add, rem);
		v1.setSpacing(true);
		CheckBox men = new CheckBox("Men", true);
		CheckBox women = new CheckBox("Women", true);
		men.addValueChangeListener(e -> updateAvailablePlayers(men, women));
		women.addValueChangeListener(e -> updateAvailablePlayers(men, women));
		HorizontalLayout hb = new HorizontalLayout(men, women);
		players = new IndexedContainer();
		playersSelect = new ListSelect();
		playersSelect.setRows(15);
		playersSelect.setContainerDataSource(players);
		playersSelect.setNullSelectionAllowed(false);
		playersSelect.setMultiSelect(false);
		VerticalLayout v2 = new VerticalLayout(new Label("Available Players"), hb, playersSelect);
		h.addComponent(v1);
		h.addComponent(v2);
		setupComp.addComponent(h);
		updateAvailablePlayers(men, women);
		add.setEnabled(false);
		rem.setEnabled(false);
		playersSelect.addValueChangeListener(e -> { add.setEnabled(playersSelect.getValue() != null);  });
		teamSelect.addValueChangeListener(e -> { rem.setEnabled(teamSelect.getValue() != null); });
		int wks = getTourny().getTeams().size() - 1;
		if (getTourny().getTeams().size() % 2 != 0)
			wks  = getTourny().getTeams().size();
		TextField weeks = new TextField("Number of Rounds", "" + wks);
		rem.addClickListener(e -> { 
			rem.setEnabled(false); 
			if (tempPlayer == null)
			{
				teamSelect.removeItem(teamSelect.getValue()); 
				int wk = teams.size() - 1;
				if (teams.size() % 2 != 0)
					wk  = teams.size();
				weeks.setValue(String.valueOf(wk));
			}
			else
			{
				tempPlayer = null;
				cur.setValue("Select a player to add to a team");
			}
			updateAvailablePlayers(men, women);
		});
		add.addClickListener(e -> {
			Player p = (Player)playersSelect.getValue();
			playersSelect.removeItem(p);
			if (tempPlayer == null)
			{
				tempPlayer = p;
				cur.setValue("Make a team with: " + p.firstLastName());
				rem.setEnabled(true);
			}
			else
			{
				Doubles d = new Doubles();
				d.setPlayers(tempPlayer, p);
				teams.addItem(d);
				int wk = teams.size() - 1;
				if (teams.size() % 2 != 0)
					wk  = teams.size();
				weeks.setValue(String.valueOf(wk));
				tempPlayer = null;
				cur.setValue("Select a player to add to a team");
				teamSelect.select(d);
			}
			add.setEnabled(false);
		});
		h.addComponent(buildNewPlayerPanel(men, women));
		DateField dt = new DateField("Start Date", getTourny().getStartDate());
		setupComp.addComponent(dt);
		TextField rndSize = new TextField("Weeks/round", "1");
		TextField late = new TextField("Late passes", "1");

		HorizontalLayout h2 = new HorizontalLayout(weeks, rndSize, late);
		setupComp.addComponent(h2);
		Button create = new Button("Create League");
		create.setEnabled(getTourny().getStartDate() != null && getTourny().canBeDeleted());
		dt.addValueChangeListener(e -> create.setEnabled(getTourny().canBeDeleted()));
		create.addClickListener(e -> { 
			int wk = Integer.parseInt(weeks.getValue());
			int rs = Integer.parseInt(rndSize.getValue());
			int lp = Integer.parseInt(late.getValue());
			if (wk == 0 || rs == 0)
				Notification.show("Invalid data", "Number of Rounds (" + wk + ") and Number of Weeks/Rnd (" + rs + ") must be > 0", Type.ERROR_MESSAGE);
			else
				if (getTourny().getTeams().size() > 0)
				{
					MessageBox.createQuestion()
					.withCaption("Tournament has teams")
					.withMessage("Do you really want to reset the Tournament?")
					.withYesButton(() -> {
						getTourny().delete();
						saveWhile(() -> getTourny().createTournament(teamSelect.getItemIds(), dt.getValue(), wk, rs, lp));
						rebuildRounds();
						rebuildStandings();
						currentStanding = null;
						updateStandingsStats();
					})
					.withNoButton()
					.open();
				}
				else
				{
					saveWhile(() -> getTourny().createTournament(teamSelect.getItemIds(), dt.getValue(), wk, rs, lp));
					rebuildRounds();
					rebuildStandings();
				}
		});
		create.setStyleName(ValoTheme.BUTTON_DANGER);
		setupComp.addComponent(create);
	}
	
	protected void buildOptions()
	{
		optionsTab = sheet.addTab(optionsComp, "* Options *");
		optionsComp.setSpacing(true);
		optionsComp.setMargin(true);
		ListSelect teams = new ListSelect("Teams");
		Label l1 = new Label("Or hold CTRL key and click a team to select multiple teams");
		l1.setStyleName(ValoTheme.LABEL_TINY);
		Runnable updater = () -> {
			teams.removeAllItems();
			getTourny().getLatePassTeams().stream().forEach(t -> teams.addItem(new Tuple("# of LATE PASSes: " + getTourny().getLatePasses(t), t)));
		};
		teams.setNullSelectionAllowed(false);
		teams.setMultiSelect(true);
		VerticalLayout v0 = new VerticalLayout(teams);
		updater.run();
		VerticalLayout v = new VerticalLayout();
		v.setSpacing(true);
		VerticalLayout v2 = new VerticalLayout();
		v2.setSpacing(true);
		HorizontalLayout h = new HorizontalLayout(v0, v, v2);
		h.setSpacing(true);
		optionsComp.addComponent(h);
		Button b = new Button("Select All");
		b.setStyleName(ValoTheme.BUTTON_SMALL);
		b.addClickListener(e -> teams.getItemIds().stream().forEach(t -> teams.select(t)));
		v0.addComponent(b);
		v0.addComponent(l1);
		Button add = new Button("Add a LATE PASS to selected teams");
		add.setEnabled(false);
		add.addClickListener(e -> saveWhile(() -> {
			((Set<Tuple<String, Team>>)teams.getValue()).stream().forEach(t -> getTourny().updateLatePass(t.getRight(), 1));
			updater.run();
		}));
		v.addComponent(new Label("Functions:"));
		v.addComponent(add);
		Button rem = new Button("Remove a LATE PASS from selected teams");
		rem.setEnabled(false);
		rem.addClickListener(e -> saveWhile(() -> {
			((Set<Tuple<String, Team>>)teams.getValue()).stream().forEach(t -> getTourny().updateLatePass(t.getRight(), -1));
			updater.run();
		}));
		v.addComponent(rem);
		teams.addValueChangeListener(e -> {
			int c = ((Set<Tuple<String, Team>>)teams.getValue()).size();
			add.setEnabled(c > 0);
			rem.setEnabled(c > 0);
			add.setCaption("Add a LATE PASS to " + c + " selected team(s)");
			rem.setCaption("Remove a LATE PASS to " + c + " selected team(s)");
		});
		v2.addComponent(new Label("Options:"));
		CheckBox c1 = new CheckBox("Display Winners on the top of the Match", getTourny().isWinnersOnTop());
		c1.setDescription("Display the Winner at the top of the match (which may change the order in which the teams are displayed)");
		c1.addValueChangeListener(e -> saveWhile(() -> getTourny().setWinnersOnTop(c1.getValue())));
		v2.addComponent(c1);
		CheckBox c2 = new CheckBox("Email Organizers when matches are entered", getTourny().isOrganizerNotifiedOnScore());
		c2.setDescription("Send a copy of the match score to each of the organizers");
		c2.addValueChangeListener(e -> saveWhile(() -> getTourny().setOrganizerNotifiedOnScore(c2.getValue())));
		v2.addComponent(c2);
		CheckBox c3 = new CheckBox("Only use non-forfeit Wins to determine Win percentage", getTourny().isAdjustedStandings());
		c3.setDescription("Forfeit wins are ignored (so that the team getting the forfeit does not unduly benefit from a 'free' win).  However, forfeit losses are still counted against the forfeitting team");
		c3.addValueChangeListener(e -> saveWhile(() -> { 
			getTourny().setAdjustedStandings(c3.getValue()); 
			rebuildStandings();
		}));
		v2.addComponent(c3);
		CheckBox c4 = new CheckBox("Hide ELO in Schedule tab", getTourny().isEloHidden());
		c4.setDescription("Do not display the ELO ranking on the schedule tab");
		c4.addValueChangeListener(e -> saveWhile(() -> getTourny().setEloHidden(c4.getValue())));
		v2.addComponent(c4);
		CheckBox c5 = new CheckBox("Do not sort Standings", getTourny().isIgnoreStandings());
		c5.setDescription("Do not compare standings to each other.   Just sort by name.");
		c5.addValueChangeListener(e -> saveWhile(() -> { getTourny().setIgnoreStandings(c5.getValue()); getTourny().recalculate(); }));
		v2.addComponent(c5);

	}

	
	protected void updateAvailablePlayers(CheckBox cm, CheckBox cw)
	{
		players.removeAllItems();
		for (Player p : getHood().getPlayers())
		{
			boolean already = teams.getItemIds().stream().filter(o -> ((Team)o).contains(p)).count() > 0;
			boolean m = p.isMale() && cm.getValue();
			boolean f = !p.isMale() && cw.getValue();
			boolean a = p.equals(tempPlayer);
			if (!already && !a && (m || f))
				players.addItem(p);
		}
		playersSelect.markAsDirty();
	}
	
	protected void buildChartElo()
	{
		ChartViewElo cv = new ChartViewElo(getTourny());
		chartEloTab = sheet.addTab(cv, "ELO Chart");
		cv.buildUI();
	}

	protected Tournament getTourny()
	{
		return getCurrentTournament();
	}
	
	public boolean canSetup()
	{
		Player p = getLoginUser();
		if (p.isAdmin())
			return true;
		return getTourny() == null ? false : getTourny().isOrganizer(p);
	}

}
