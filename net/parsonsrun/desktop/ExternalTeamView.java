package net.parsonsrun.desktop;

import java.io.PrintStream;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.vaadin.addon.touchkit.ui.VerticalComponentGroup;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.DefaultItemSorter;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.MethodProperty;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.Grid.SingleSelectionModel;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.themes.ValoTheme;

import de.steinwedel.messagebox.MessageBox;
import net.parsonsrun.PrintUI;
import net.parsonsrun.Tuple;
import net.parsonsrun.Utils;
import net.parsonsrun.domain.*;

public class ExternalTeamView extends DesktopBaseView
{
	protected TabSheet sheet;
	protected Tab main;
	protected Tab avail;
	protected Tab setup;
	protected Tab lineup;
	protected Tab matchSetup;
	protected Tab com;
	protected Tab roster;
	protected VerticalLayout mainComp;
	protected VerticalLayout availComp;
	protected VerticalLayout setupComp;
	protected VerticalLayout lineupComp;
	protected VerticalLayout matchSetupComp;
	protected VerticalLayout comComp;
	protected VerticalLayout rosterComp;
	protected TabSheet lineupSheet;
	protected ArrayList<Tab> weeks;
	protected HorizontalLayout schedule;
	protected HorizontalLayout availSchedule;
	protected NativeSelect goodbye;
	protected HashMap<Lineup, IndexedContainer> lineups = new HashMap<Lineup, IndexedContainer>();
	protected HashMap<Lineup, ListSelect> lineupSelects = new HashMap<Lineup, ListSelect>();
	protected HashMap<Lineup, CheckBox> lineupShowAllCheckboxes = new HashMap<Lineup, CheckBox>();
	protected HashMap<Lineup, CheckBox> lineupShowUncertainCheckboxes = new HashMap<Lineup, CheckBox>();

	public class HtmlLineups
	{
		int currentWeek;
		Player selected;
		public void setPlayer(Player p) { selected = p; }
		
		public HtmlLineups(int w)
		{
			currentWeek = w;
		}
		
		public Label getLabel()
		{
			Label pl = new Label(getHtml(), ContentMode.HTML);
			//pl.setStyleName(ValoTheme.LABEL_SMALL);
			return pl;
		}
		
		public Label getLabel(Player p)
		{
			setPlayer(p);
			return getLabel();
		}
		
		public String getHtml()
		{
			StringBuilder s = new StringBuilder();
			s.append("<div style='font-size: 0.7em;'><table cellpadding=2 style='border: 1px solid grey;border-collapse: collapse;'>");
			s.append("<tr><td style='border: 1px solid grey;'>Previous Results:</td>");
			for (int week = 1; week < currentWeek; week++)
			{
				s.append("<td style='border: 1px solid grey;text-align:center;'>Week #");
				s.append(week);
				s.append("</td>");
			}
			s.append("</tr>");
			for (int line = 1; line <= getE().getNumberOfLines(); line++)
			{
				s.append("<tr>");
				s.append("<td style='border: 1px solid grey;'>Line #");
				s.append(line);
				s.append("</td>");
				for (int week = 1; week < currentWeek; week++)
				{
					Line li = getE().getLine(week, line);
					li.addTdHtml(s, selected);
				}
				s.append("</tr>");
			}
			s.append("</table></div>");
			return s.toString();
		}
	}


	
	@Override
	public void buildUI()
	{
		addBack("Welcome " + getLoginUser().getFirst() + ", to the team management view." + (canSetup() ? "&nbsp;&nbsp; NOTE:  Tabs containing '*' can only be seen by the captain(s)" : ""));
		sheet = new TabSheet();
		sheet.addStyleName(ValoTheme.TABSHEET_FRAMED);
		sheet.addStyleName(ValoTheme.TABSHEET_COMPACT_TABBAR);
		sheet.addSelectedTabChangeListener(e -> checkSelectedTab());
		sheet.setSizeFull();
		mainComp = new VerticalLayout();
		main = sheet.addTab(mainComp, (getE() == null ? "" : getE().getName()));
		buildMain();
		rosterComp = new VerticalLayout();
		roster = sheet.addTab(rosterComp, "Roster");
		buildRoster();
		comComp = new VerticalLayout();
		com = sheet.addTab(comComp, "Email");
		com.setVisible(getE().includes(getLoginUser()) || canSetup());
		availComp = new VerticalLayout();
		avail = sheet.addTab(availComp, "Availability");
		avail.setVisible(getE().includes(getLoginUser()) || canSetup());
		buildAvail();
		buildEmail();
		if (canSetup())
		{
			setupComp = new VerticalLayout();
			lineupComp = new VerticalLayout();
			lineupComp.setMargin(true);
			matchSetupComp = new VerticalLayout(); 
			lineup = sheet.addTab(lineupComp, "* Lineups *");
			matchSetup = sheet.addTab(matchSetupComp, "* Season Setup *");
			setup = sheet.addTab(setupComp, "* Team Setup *");
			buildLineups();
			buildSeasonSetup();
			buildTeamSetup();
			if (getE().needsSetup())
				sheet.setSelectedTab(setup);
			else
				if (getE().getFirstDate() == null)
					sheet.setSelectedTab(matchSetup);
		}
		if (getE().includes(getLoginUser()) && !getE().hasCheckedAvailability(getLoginUser()))
			sheet.setSelectedTab(avail);
		addComponent(sheet);
		clearAction();
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
		if (c == availComp)
		{
			if (!getE().hasCheckedAvailability(getLoginUser()))
			{
				println("Viewed Availability tab");
				saveWhile(() -> getE().checkedAvailability(getLoginUser()));
			}
		}
		if (c == lineupComp)
		{
			lineupSheet.setSelectedTab(getE().getNextWeekIndex());
		}
	}
	
	public String toString()
	{
		return super.toString() + " " + getE();
	}
	public void showAvailTab()
	{
		sheet.setSelectedTab(avail);
	}
	
	protected ExternalTeam getE()
	{
		return getCurrentExternalTeam();
	}
	
	protected void buildMain()
	{
		schedule = new HorizontalLayout();
		mainComp.addComponent(schedule);
		buildSchedule();
		Label l = new Label("* Player has not confirmed lineup");
		l.addStyleName(ValoTheme.LABEL_SMALL);
		mainComp.addComponent(l);
	}
	
	protected void buildEmail()
	{
		comComp.setSpacing(true);
		comComp.setMargin(true);
		HorizontalLayout h0 = new HorizontalLayout();
		h0.setSpacing(true);
		ListSelect players = new ListSelect("Players", getE().getRoster());
		Button direct = new Button("* Send Email with Direct link for '" + getE().getName() + "' *");
		direct.setEnabled(false);
		direct.setStyleName(ValoTheme.BUTTON_SMALL);
		direct.setDescription("Email a link that will automatically log the player in, and bring them to this team '" + getE().getName() + "'");
		HorizontalLayout h1 = new HorizontalLayout();
		h1.setSpacing(true);
		players.setMultiSelect(true);
		players.setRows((getBrowserHeight() - 500) / 16);
		Button selectAll = new Button("Select All");
		selectAll.setStyleName(ValoTheme.BUTTON_SMALL);
		selectAll.addClickListener(e -> getE().getRoster().stream().forEach(p -> players.select(p)));
		TextField subject = new TextField("Subject:");
		subject.setColumns(40);
		TextArea mesg = new TextArea("Message:");
		mesg.setWidth((getBrowserWidth() - 300) + "px");
		mesg.setHeight(((getBrowserHeight() - (canSetup() ? 430: 330)) / 16 )+ "em");
		Label l1 = new Label("Hold CTRL key<br>to select multiple players", ContentMode.HTML);
		l1.setStyleName(ValoTheme.LABEL_TINY);
		Button send = new Button("Send Email");
		send.setEnabled(false);
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
				Utils.escapeHTML(mesg.getValue()) + "<br><br><a href='" + getParentUI().encodeViewAction(p, getE()) + "'>Click HERE</a> to automatically log you into '" +
						getE().getName() + "'.<br><br>" + signed));
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
		v2.addComponent(h0);
		if (canSetup())
		{
			h0.addComponent(new Label("** Captain functions: "));
			h0.addComponent(direct);
		}
		players.addValueChangeListener(e -> { 
			boolean en = ((Set<Player>)players.getValue()).size() > 0;
			send.setEnabled(en);
			direct.setEnabled(en);
		});

	}

	
	protected void buildAvail()
	{
		availComp.setSpacing(true);
		availSchedule = new HorizontalLayout();
		availComp.addComponent(availSchedule);
		buildAvailSchedule();
		Label l1 = new Label("Player HAS viewed their availability &nbsp;", ContentMode.HTML);
		l1.setStyleName(DesktopUI.CONFIRMED_TEXT);
		l1.addStyleName(ValoTheme.LABEL_SMALL);
		Label l2 = new Label("/ * Player HAS NOT viewed their availability / ");
		l2.addStyleName(ValoTheme.LABEL_SMALL);
		Label l3 = new Label("&nbsp; Player is unsure of their availability", ContentMode.HTML);
		l3.setStyleName(DesktopUI.UNCONFIRMED_TEXT);
		l3.addStyleName(ValoTheme.LABEL_SMALL);
		HorizontalLayout h = new HorizontalLayout();
		h.addComponent(new Label("("));
		h.addComponent(l1);
		h.addComponent(l2);
		h.addComponent(l3);
		h.addComponent(new Label(")"));
		if (canSetup() || !getE().shouldHideAvailabilities())
			availComp.addComponent(h);
		if (canSetup())
		{
			Button b = new Button("Send availability invite email (to " + getE().getAvailToDo().size() + " unconfirmed players)");
			b.setIcon(FontAwesome.ENVELOPE_O);
			b.addClickListener(e -> emailInvite(false));
			availComp.addComponent(b);
			b.setEnabled(getE().getAvailToDo().size() > 0);
		}
	}
	
	public void saveData(String s)
	{
		println(s);
		saveData();
	}
	
	protected void buildLineupSheet()
	{
		if (lineupSheet == null)
			return;						// Not a captain, nothing to do.
		lineups.clear();
		lineupSelects.clear();
		lineupSheet.removeAllComponents();
		lineupSheet.setSizeFull();
		for (Lineup l : getE().getLineups())
		{
			VerticalLayout v = new VerticalLayout();
			v.setMargin(false);
			v.setSpacing(true);
			lineupSheet.addTab(v, l.getWeekString());
			buildLineupWeek(v, l);
		}
	}
	
	
	protected void buildAvailSchedule()
	{
		availSchedule.removeAllComponents();
		availSchedule.setSizeFull();
		for (Lineup l : getE().getLineups())
		{
			availSchedule.addComponent(buildAvailWeek(l));
		}
	}
	
	protected VerticalLayout buildLineupWeek(VerticalLayout v, Lineup lu)
	{
		HorizontalLayout h0 = new HorizontalLayout();
		h0.setSpacing(true);
		v.addComponent(h0);
		Label lt = new Label("Opponent " + lu.getWeekString() + ":");
		lt.addStyleName(ValoTheme.LABEL_SMALL);
		if (!lu.isAway())
			lt.addStyleName(DesktopUI.HOME_BKGD);
		h0.addComponent(lt);
		h0.setComponentAlignment(lt, Alignment.MIDDLE_CENTER);
		TextField tf = new TextField(null, new MethodProperty<String>(lu, "name"));
		tf.setInputPrompt("Enter opponent");
		tf.setWidth("17em");
		tf.addStyleName(ValoTheme.TEXTFIELD_SMALL);
		tf.addValueChangeListener(e -> saveData(lu.getWeekString() + " opponenet: " + tf.getValue()));
		h0.addComponent(tf);
		Label ld = new Label("Date of match:");
		ld.addStyleName(ValoTheme.LABEL_SMALL);
		h0.addComponent(ld);
		h0.setComponentAlignment(ld, Alignment.MIDDLE_CENTER);
		DateField d = new DateField(new MethodProperty<String>(lu, "date"));
		d.setStyleName(ValoTheme.DATEFIELD_SMALL);
		d.setImmediate(true);
		d.setDateFormat("MM-dd-yyyy");
		d.addValueChangeListener(e -> saveData());
		h0.addComponent(d);
		VerticalLayout prevLineups = new VerticalLayout();
		v.addComponent(prevLineups);
		HtmlLineups html = new HtmlLineups(lu.getWeek());
		Label pl = new Label(html.getHtml(), ContentMode.HTML);
		//pl.setStyleName(ValoTheme.LABEL_SMALL);
		prevLineups.addComponent(pl);
		HorizontalLayout h1 = new HorizontalLayout();
		h1.setSpacing(true);
		v.addComponent(h1);
		TextArea stats = new TextArea("Stats for:");
		stats.setStyleName(ValoTheme.TEXTAREA_TINY);
		stats.setWidth("40em");
		stats.setHeight("11em");
		stats.setReadOnly(true);
		TextArea notes = new TextArea("Notes for:");
		notes.setStyleName(ValoTheme.TEXTAREA_TINY);
		notes.setWidth("40em");
		notes.setHeight("11em");
		notes.setImmediate(true);
		notes.addValueChangeListener(e -> {
			ListSelect sel = lineupSelects.get(lu);
			if (sel != null && sel.getValue() != null)
			{
				Player p = (Player)sel.getValue();
				saveWhile(() -> getE().setNotesFor(p, notes.getValue()));
			}
		});
		h1.addComponent(buildLineupSelection(lu, notes, stats, prevLineups));
		VerticalLayout v2 = new VerticalLayout();
		v2.setSpacing(true);
		h1.addComponent(v2);
		Button confirm = new Button("Lineup Confirmation request to unconfirmed players");
		Button pregame = new Button("Pregame Invite");
		for (Line li : lu.getLines())
		{
			v2.addComponent(buildLineupLine(lu, li, confirm, pregame, prevLineups, notes, stats));
		}
		VerticalLayout v3 = new VerticalLayout();
		v3.addComponent(stats);
		v3.addComponent(notes);
		h1.addComponent(v3);
		HorizontalLayout h2 = new HorizontalLayout();
		h2.setSpacing(true);
		pregame.setIcon(FontAwesome.ENVELOPE_O);
		pregame.setDescription("Send an email to the whole team with the lineup and directions (if away)");
		pregame.setEnabled(!lu.isCompleted() && lu.isAllAssigned());
		pregame.addClickListener(e -> emailPregame(lu));
		confirm.setIcon(FontAwesome.ENVELOPE_O);
		confirm.setDescription("Send an email to unconfirmed players in the lineup");
		confirm.addClickListener(e -> emailConfirmation(lu));
		confirm.setEnabled(!lu.isAllConfirmed());
		h2.addComponent(confirm);
		h2.addComponent(pregame);
		Button print = PrintUI.printButton("Print Scorecard");
		print.addClickListener(e -> PrintUI.setHtmlContent(getScorecardHtml(lu, 0)));
		h2.addComponent(print);
//		print = PrintUI.printButton("Print Scorecard w/border");
//		print.addClickListener(e -> PrintUI.setHtmlContent(getScorecardHtml(lu, 1)));
//		h2.addComponent(print);
//		Label help = new Label("<small>(Select player and then press " + FontAwesome.CHECK.getHtml() + " to add to lineup.   Press <b>X</b> to remove)</small>", ContentMode.HTML);
//		v.addComponent(help);
		v.addComponent(h2);
		return v;
	}
	
	protected String getScorecardHtml(Lineup lu, int border)
	{
		StringBuilder s = new StringBuilder();
		s.append("<div style='background-image:url(https://www.altatennis.org/images/alta-printbk.png); background-repeat:repeat-x; height:6em; margin-top:1em; width:100%;'>");
		s.append("<img style='padding-left:30px; border-width:0px;' src='https://www.altatennis.org/images/alta-printlogo.gif'></div>");
		s.append("<h1><span style='text-align:center;'>");
		s.append(getE().getName());
		s.append("</span></h1>");
		
		//s.append("<div><table border='0'><tr><td></td><td>home</td><td></td><td></td><td></td><td>Away</td><td></td></tr>");
		s.append("<div><table border=");
		s.append(border);
		s.append(" cellpadding=5>");
		//s.append("<tr><td>0</td><td>1</td><td>2</td><td>3</td><td>4</td><td>5</td><td>6</td><td>7</td><td>8</td><td>9</td><td>10</td><td>11</td><td>12</td><td>13</td></tr>");
		rs(s).td(s).td(s).tdc(s, "Home").td(s, "", 7).tdc(s, "Away").td(s).td(s).re(s);
		rs(s).td(s, "", 3).td(s, 1).td(s, 2).td(s, 3).td(s, "sets").td(s, 1).td(s, 2).td(s, 3).td(s, "", 3).re(s);
		String blank = "________________________________";
		int width = 220;
		for (Line li : lu.getLines())
		{
			//rs(s).td(s, li.getIndex()).td(s,"Result").tdr(s,"Win / Loss").td(s,"__").td(s,"__").td(s,"__").td(s,"").td(s,"__").td(s,"__").td(s,"__").td(s, "Win / Loss").td(s, "Result").td(s, li.getIndex()).re(s);
			rs(s).td(s, li.getIndex()).td(s).tdr(s,"Win / Loss").td(s,"__").td(s,"__").td(s,"__").td(s,"").td(s,"__").td(s,"__").td(s,"__").td(s, "Win / Loss").td(s).td(s, li.getIndex()).re(s);
			if (lu.isAway())
			{
				rs(s).td(s).td(s, "Player 1").td(s, blank, 1, width).td(s).td(s).td(s).td(s).td(s).td(s).td(s).td(s, li.getAssignA(), 1, width).td(s, "Player 1").td(s).re(s);
				rs(s).td(s).td(s, "Player 2").td(s, blank, 1, width).td(s).td(s).td(s).td(s).td(s).td(s).td(s).td(s, li.getAssignB(), 1, width).td(s, "Player 2").td(s).re(s);
			}
			else
			{
				rs(s).td(s).td(s, "Player 1").td(s, li.getAssignA(), 1, width).td(s).td(s).td(s).td(s).td(s).td(s).td(s).tdr(s, blank).td(s, "Player 1").td(s).re(s);
				rs(s).td(s).td(s, "Player 2").td(s, li.getAssignB(), 1, width).td(s).td(s).td(s).td(s).td(s).td(s).td(s).tdr(s, blank).td(s, "Player 2").td(s).re(s);
//				rs(s).td(s).td(s, "Player 1").td(s, li.getAssignA(), 4, width).td(s).td(s, blank, 4, width).td(s, "Player 1").td(s).re(s);
//				rs(s).td(s).td(s, "Player 2").td(s, li.getAssignB(), 4, width).td(s).td(s, blank, 4, width).td(s, "Player 1").td(s).re(s);
			}
		}
		rs(s).td(s, "", 13).re(s);
		rs(s).td(s, "", 13).re(s);
		rs(s).td(s).td(s, "Signature").td(s, blank).td(s, "", 7).tdr(s,  blank).td(s, "Signature").td(s).td(s).re(s);
		s.append("</table></div>");

		return s.toString();
	}
	
	protected ExternalTeamView td(StringBuilder b, String s, int c, int w)
	{
		b.append("<td colspan='");
		b.append(c);
		b.append("' style='width:");
		b.append(w);
		b.append("px; border-bottom: 1px'>");
		b.append(s);
		b.append("</td>");
		return this;
	}
	

	
	protected ExternalTeamView rs(StringBuilder b)
	{
		b.append("<tr><td style='width:30px'></td>");
		return this;
	}
	protected ExternalTeamView re(StringBuilder b)
	{
		b.append("</tr>");
		return this;
	}

	protected ExternalTeamView td(StringBuilder b, String s)
	{
		b.append("<td>");
		b.append(s);
		b.append("</td>");
		return this;
	}
	
	protected ExternalTeamView tdr(StringBuilder b, String s)
	{
		b.append("<td style='text-align: right;'>");
		b.append(s);
		b.append("</td>");
		return this;
	}
	
	protected ExternalTeamView tdc(StringBuilder b, String s)
	{
		b.append("<td style='text-align:center;font-size:24px;font-weight:bold'>");
		b.append(s);
		b.append("</td>");
		return this;
	}
	
	protected ExternalTeamView tdl(StringBuilder b, String s)
	{
		b.append("<td style='text-align: left;'>");
		b.append(s);
		b.append("</td>");
		return this;
	}
	protected ExternalTeamView td(StringBuilder b, int s)
	{
		b.append("<td>");
		b.append(s);
		b.append("</td>");
		return this;
	}
	protected ExternalTeamView td(StringBuilder b, String s, int c)
	{
		b.append("<td colspan='");
		b.append(c);
		b.append("'>");
		b.append(s);
		b.append("</td>");
		return this;
	}

	protected ExternalTeamView td(StringBuilder b)
	{
		b.append("<td></td>");
		return this;
	}
	

	
	protected VerticalLayout buildWeek(Lineup lu)
	{
		VerticalLayout v = new VerticalLayout();
		Label lbl = new Label(lu.getWeekString() + " vs");
		lbl.addStyleName(ValoTheme.LABEL_SMALL);
		v.addComponent(lbl);
		if (lu.isAway() && !lu.getLocation().isEmpty())
		{
			Link lnk = new Link(lu.getName(), new ExternalResource(lu.getLocationHtml()));
			lnk.addStyleName(ValoTheme.LINK_SMALL);
			lnk.setTargetName("_blank");
			v.addComponent(lnk);
		}
		else
		{
			Label opp = new Label(lu.getName());
			opp.addStyleName(ValoTheme.LABEL_SMALL);
			opp.addStyleName(DesktopUI.HOME_BKGD);
			v.addComponent(opp);
		}
		DateField d = new DateField(new MethodProperty<String>(lu, "date"));
		d.addStyleName(ValoTheme.DATEFIELD_TINY);
		d.setDateFormat("MM-dd-yyyy");
		d.setReadOnly(true);
		v.addComponent(d);
		Button results = new Button("Email results");
		for (Line li : lu.getLines())
		{
			v.addComponent(buildLine(lu, li, results));
		}
		if (canSetup())
		{
			results.setIcon(FontAwesome.ENVELOPE_O);
			results.setEnabled(lu.isCompleted() && !lu.hasSentEmail());
			results.addClickListener(e -> emailResults(lu, results));
			v.addComponent(results);
		}
		return v;	
	}
	
	protected void emailResults(Lineup lu, Button results)
	{
		String subject = "Results for " + lu.subjectLine();
		StringBuilder b2 = new StringBuilder();
		b2.append(subject);
		b2.append(" are:<br><br>");
		b2.append("<table border='1' cellpadding='5'>");
		for (Line li : lu.getLines())
		{
			b2.append("<tr><th>Line: ");
			b2.append(li.getIndex());
			b2.append("</th><th>");
			if (li.isWon())
				b2.append("WIN");
			else
				b2.append("Loss");
			b2.append("</th><th>");
			b2.append(li.getA().firstLastName());
			b2.append(" / ");
			b2.append(li.getB().firstLastName());
			b2.append("</th><th>");
			b2.append(li.getScore());
			b2.append("</th></tr>");
		}
		b2.append("</table>");
		b2.append("<br>");
		Panel pnl = new Panel("Subject: " + subject);
		VerticalLayout v = new VerticalLayout();
		pnl.setContent(v);
		Label l1 = new Label(b2.toString(), ContentMode.HTML);
		TextArea input = new TextArea("Optional message:");
		input.setWidth("100%");
		input.setRows(7);
		v.addComponent(l1);
		v.addComponent(input);
		StringBuilder b = new StringBuilder();
		b.append(getE().getGoodbye());
		Label l2 = new Label(b.toString(), ContentMode.HTML);
		v.addComponent(l2);
		MessageBox.createInfo()
			.withCaption("Weekly results email")
			.withWidth("700px")
			.withHeight("650px")
			.withMessage(pnl)
			.withOkButton(() -> {
				getParentUI().sendEmail(getE().getRoster(), subject, b2.toString() + Utils.escapeHTML(input.getValue()) + "<br><br>" + b.toString());
				Notification.show("Weekly results email sent!");
				saveWhile(() -> lu.setSentEmail(true));
				results.setEnabled(false);
			})
			.withCancelButton()
			.open();
	}
	
	protected void emailInvite(boolean all)
	{	
		String subject = "et your availability for " + getE().getName();
		Panel pnl = new Panel("Subject: S" + subject);
		VerticalLayout v = new VerticalLayout();
		pnl.setContent(v);
		String b1 = "<br><br>Click here: ";
		String b2 = "<br><small>(you can do this from your phone or computer)</small>" +
				"<br><br>Select the dates that you can play.&nbsp; Your changes will be saved automatically.<br><br>";
		v.addComponent(new Label("Please s" + subject + b1 + "<u>Set Availability for NAME</u>" + b2, ContentMode.HTML));
		TextArea input = new TextArea("Optional message:");
		input.setWidth("100%");
		input.setRows(8);
		v.addComponent(input);
		String end = "Thank you,<br><br>" + getE().getGoodbye();
		v.addComponent(new Label(end, ContentMode.HTML));

		MessageBox.createInfo()
			.withCaption("Availability email")
			.withWidth("700px")
			.withHeight("550px")
			.withMessage(pnl)
			.withOkButton(() -> 
			{
				int c = 0;
				String b1a = "<a href='";
				for (Player p : getE().getRoster())
				{
					String b1b = "'>Set Availability for " + p.firstLastName() + "</a>";
					if (all || !getE().hasCheckedAvailability(p))
					{
						c++;
						String msg = p.getFirst() + ", please s" + subject + "." + b1 + b1a + getParentUI().encodeAvailAction(p, getE()) + b1b + b2 + Utils.escapeHTML(input.getValue()) + "<br><br>" + end;
						getParentUI().sendEmail(p, "S" + subject, msg);
					}
				}
				Notification.show("Availability email sent to " + c + " players");
			})
			.withCancelButton()
			.open();
	}
	
	protected void emailPregame(Lineup lu)
	{
		String subject = lu.subjectLine();
		StringBuilder b2 = new StringBuilder();
		b2.append("The lineup for ");
		b2.append(lu.subjectLineHtml());
		b2.append(" is:<br><br>");
		for (Line li : lu.getLines())
		{
			b2.append(li.getIndex());
			b2.append(": ");
			b2.append(li.getA().firstLastName());
			b2.append(" / ");
			b2.append(li.getB().firstLastName());
			b2.append("<br>");
		}
		b2.append("<br>");
		Panel pnl = new Panel("Subject: " + subject);
		VerticalLayout v = new VerticalLayout();
		pnl.setContent(v);
		Label l1 = new Label(b2.toString(), ContentMode.HTML);
		TextArea input = new TextArea("Optional message:");
		input.setWidth("100%");
		input.setRows(9);
		v.addComponent(l1);
		v.addComponent(input);
		StringBuilder b = new StringBuilder();
		b.append("Thank you,<br><br>");
		b.append(getE().getGoodbye());
		Label l2 = new Label(b.toString(), ContentMode.HTML);
		v.addComponent(l2);
		MessageBox.createInfo()
			.withCaption("Pregame email")
			.withWidth("700px")
			.withHeight("610px")
			.withMessage(pnl)
			.withOkButton(() -> {
				getParentUI().sendEmail(getE().getRoster(), subject, b2.toString() + Utils.escapeHTML(input.getValue()) + "<br><br>" + b.toString());
				Notification.show("Pregame email sent!");
			})
			.withCancelButton()
			.open();
	}
	
	protected void emailConfirmation(Lineup lu)
	{
		String subject = "You are in the lineup for " + lu.subjectLine();
		String subjectHtml = "You are in the lineup for " + lu.subjectLineHtml();
		StringBuilder b1 = new StringBuilder();
		b1.append("Congratulations!<br><br>");
		b1.append(subjectHtml);
		b1.append("<br><br>");
		Panel pnl = new Panel("Subject: " + subject);
		VerticalLayout v = new VerticalLayout();
		pnl.setContent(v);
		TextArea input = new TextArea("Optional message:");
		input.setWidth("100%");
		input.setRows(9);
		v.addComponent(new Label(b1.toString(), ContentMode.HTML));
		v.addComponent(input);
		v.addComponent(new Label("You have been assigned to line #[Line] with [Player]<br>Click CONFIRM if you are able to play.<br>Click REJECT if you are NOT able to play. The captain will be notified.", ContentMode.HTML));
		StringBuilder b2 = new StringBuilder();
		b2.append("Thank you,<br><br>");
		b2.append(getE().getGoodbye());
		Label l2 = new Label(b2.toString(), ContentMode.HTML);
		v.addComponent(l2);
		MessageBox.createInfo()
		.withCaption("Lineup confirmation email")
		.withWidth("700px")
		.withHeight("610px")
		.withMessage(pnl)
		.withOkButton(() -> {
			for (Player p : lu.getPlayers())
			{
				Line li = lu.lineFor(p);
				if (!li.isConfirmed(p))
				{
					StringBuilder b = new StringBuilder();
					b.append(b1.toString());
					if (!input.getValue().isEmpty())
					{
						b.append(Utils.escapeHTML(input.getValue()));
						b.append("<br>");
					}
					b.append("You have been assigned to line #");
					b.append(li.getIndex());
					Player p2 = li.getPartner(p);
					if (p2 != null)
					{
						b.append(" with ");
						b.append(p2.firstLastName());
					}
					b.append(".<br><br>Click <a href='");
					b.append(getParentUI().encodeConfirmAction(p, getE(), lu));
					b.append("'>CONFIRM</a> if you are able to play.<br><br>Click <a href='");
					b.append(getParentUI().encodeRejectAction(p, getE(), lu));
					b.append("'>REJECT</a> if you are NOT able to play.  The captain");
					if (getE().getCaptains().size() != 1)
						b.append("s");
					b.append(" will be notified.<br><br>");
					b.append(b2.toString());
					getParentUI().sendEmail(p, subject, b.toString());
				}
			}
			Notification.show("Lineup confirmation email sent!");
		})
		.withCancelButton()
		.open();
	}
	
	protected VerticalLayout buildAvailWeek(Lineup lu)
	{
		VerticalLayout v = new VerticalLayout();
		Label lbl = new Label(lu.getWeekString() + " vs");
		lbl.addStyleName(ValoTheme.LABEL_SMALL);
		v.addComponent(lbl);
		if (lu.isAway() && !lu.getLocation().isEmpty())
		{
			Link lnk = new Link(lu.getName(), new ExternalResource(lu.getLocationHtml()));
			lnk.setTargetName("_blank");
			v.addComponent(lnk);
		}
		else
		{
			Label opp = new Label(lu.getName());
			opp.addStyleName(ValoTheme.LABEL_SMALL);
			opp.addStyleName(DesktopUI.HOME_BKGD);
			v.addComponent(opp);
		}

		DateField d = new DateField(new MethodProperty<String>(lu, "date"));
		d.addStyleName(ValoTheme.DATEFIELD_TINY);
		d.setDateFormat("MM-dd-yyyy");
		d.setReadOnly(true);
		v.addComponent(d);
		TextField total = new TextField(new MethodProperty<String>(String.class, getE(), "getAvailableStringFor", null, new Object[] { lu }, new Object[] {}, 0));
		for (Player p : getE().getRoster())
		{
			if (p.equals(getLoginUser()) || canSetup() || !getE().shouldHideAvailabilities())
				v.addComponent(buildAvail(lu, p, total));
		}
		total.addStyleName(ValoTheme.TEXTFIELD_TINY);
		total.addStyleName(ValoTheme.TEXTFIELD_BORDERLESS);
		total.setReadOnly(true);
		if (!getE().shouldHideAvailabilities() || canSetup())
			v.addComponent(total);
		v.setEnabled(getE().getNextWeekIndex() < lu.getWeek());
		return v;	
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
		for (Player p : getE().getRoster())
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
	
	protected Component buildAvail(Lineup lu, Player p, TextField total)
	{
		boolean checked = (getE().hasCheckedAvailability(p));
		String n = p.firstLastName() + (checked? "" : "*");
		CheckBox c = new CheckBox(n, lu.isAvailable(p));
		CheckBox u = new CheckBox(" Don't know", lu.isUncertain(p));
		c.addValueChangeListener(e -> saveWhile(() ->
		{
			boolean b = c.getValue();
			if (b)
			{
				lu.removeUnavailable(p);
				lu.removeUnCertain(p);
				u.setValue(false);
				c.setStyleName(DesktopUI.CONFIRMED_TEXT);
			}
			else
				lu.addUnavailable(p);
			println("Change avail for " + p + " " + lu.getWeekString() + " to " + b);
			buildLineupSheet();
			total.markAsDirty();
		}));
		u.addValueChangeListener(e -> saveWhile(() ->
		{
			boolean b = u.getValue();
			if (b)
			{
				lu.addUnCertain(p);
				lu.addUnavailable(p);
				buildLineupSheet();
				c.setValue(false);
				c.setStyleName(DesktopUI.UNCONFIRMED_TEXT);
				total.markAsDirty();
			}
			else
			{
				lu.removeUnCertain(p);
				if (checked)
					c.setStyleName(DesktopUI.CONFIRMED_TEXT);
			}
			println("Change uncertain for " + p + " " + lu.getWeekString() + " to " + b);
		}));
		c.setEnabled(canSetup() || getLoginUser().equals(p));
		c.setDescription("Check this if you are available to play on this date.   Uncheck if you are NOT available.");
		u.setDescription("Check this if you not sure if you available on this date.");
		if (checked)
			c.setStyleName(DesktopUI.CONFIRMED_TEXT);
		if (lu.isUncertain(p))
			c.setStyleName(DesktopUI.UNCONFIRMED_TEXT);
		if (!p.equals(getLoginUser()))
			return c;
		VerticalLayout v = new VerticalLayout();
		v.addComponent(c);
		v.addComponent(u);
		v.setComponentAlignment(u, Alignment.TOP_CENTER);
		return v;
	}
	
	protected boolean canEdit(Line li)
	{
		return (canSetup() || li.contains(getLoginUser())) && li.canBeScored();
	}
	
	protected HorizontalLayout buildLineupLine(Lineup lu, Line li, Button confirm, Button pregame, VerticalLayout prevLineups, TextArea notes, TextArea stats)
	{
		HorizontalLayout h = new HorizontalLayout();
		h.setSpacing(true);
		Label lbl = new Label("#" + li.getIndex());
		h.addComponent(lbl);
		h.setComponentAlignment(lbl, Alignment.MIDDLE_LEFT);
		VerticalLayout v = new VerticalLayout();
		h.addComponent(v);
		for (int i = 0; i < Line.IDENT.length; i++)
		{
			String access = Line.IDENT[i];
			HorizontalLayout h1 = new HorizontalLayout();
			v.addComponent(h1);
			TextField c = new TextField(null, new MethodProperty<Boolean>(li, access));
			c.setDescription("Select a player from the list, and then press the Check mark to add to the lineup");
			c.setWidth("13em");
			c.setReadOnly(true);
			c.addFocusListener(e -> {
				HtmlLineups html = new HtmlLineups(lu.getWeek());
				prevLineups.removeAllComponents();
				Player p = li.get(access);
				prevLineups.addComponent(html.getLabel(p));
				notes.setCaption("Notes for: " + p);
				notes.setValue(getE().getNotesFor(p));	
				stats.setReadOnly(false);
				stats.setCaption("Stats for: " + p);
				stats.setValue(getE().getStatsFor(p));
				stats.setReadOnly(true);
			});
			c.setStyleName(ValoTheme.TEXTFIELD_SMALL);
			if (li.isConfirmed(access))
				c.addStyleName(DesktopUI.CONFIRMED);
			if (li.isRejected(access))
				c.addStyleName(DesktopUI.REJECTED);
			h1.addComponent(c);
			Button force = new Button("confirm");
			force.setEnabled(li.isAssigned(access) && !li.isConfirmed(access));
			Button b = new Button();
			b.setDescription("Select a player, and then press here to add to the lineup");
			b.setIcon(FontAwesome.CHECK);
			b.addStyleName(ValoTheme.BUTTON_SMALL);
			h1.addComponent(b);
			b.addClickListener(e -> saveWhile(() -> { 
				assignPlayer(lu, li, access); 
				if (li.isConfirmed(access))
				{
					force.setEnabled(false);
					c.addStyleName(DesktopUI.CONFIRMED);
				}
				c.markAsDirty(); 
				buildSchedule(); 
				force.setEnabled(true);
				pregame.setEnabled(lu.isAllConfirmed());
				confirm.setEnabled(!lu.isAllConfirmed()); 
				isLegal(lu); }));
			b = new Button("X");
			b.setDescription("Press here to remove this player from this lineup");
			b.addStyleName(ValoTheme.BUTTON_SMALL);
			b.addClickListener(e -> saveWhile(() -> { 
				removePlayer(lu, li, access, true); 
				force.setEnabled(false);
				c.removeStyleName(DesktopUI.CONFIRMED); 
				c.removeStyleName(DesktopUI.REJECTED); 
				c.markAsDirty(); 
				buildSchedule(); 
				pregame.setEnabled(lu.isAllConfirmed());
				confirm.setEnabled(!lu.isAllConfirmed()); }));
			h1.addComponent(b);
			force.addStyleName(ValoTheme.BUTTON_SMALL);
			force.setDescription("Click here to CONFIRM the player");
			force.addClickListener(e -> saveWhile(() -> { 
				li.setConfirmed(access);
				c.addStyleName(DesktopUI.CONFIRMED); 
				c.removeStyleName(DesktopUI.REJECTED); 
				c.markAsDirty(); 
				buildSchedule(); 
				force.setEnabled(false);
				pregame.setEnabled(lu.isAllConfirmed());
				confirm.setEnabled(!lu.isAllConfirmed()); }));
			h1.addComponent(force);
		}
		return h;
	}
	
	protected void isLegal(Lineup lu)
	{
		String s = getE().isLegal(lu);
		if (s != null)
			Notification.show(s, Notification.Type.ERROR_MESSAGE);
	}
		
	protected void assignPlayer(Lineup lu, Line li, String access)
	{
		ListSelect sel = lineupSelects.get(lu);
		Player p = (Player)sel.getValue();
		if (p == null)
		{
			Notification.show("You must select a player before assigning to a line");
			return;
		}
		sel.select(null);
		Player old = li.get(access);
		if (old != null)
			removePlayer(lu,  li, access, true);
		saveWhile(() -> li.set(access, p));
		if (!p.isForfeit())
		{
			IndexedContainer col = lineups.get(lu);
			col.removeItem(p);
		}
		buildSchedule();
	}
	
	protected void removePlayer(Lineup lu, Line li, String access, boolean update)
	{
		Player p = li.get(access);
		if (p == null)
		{
			Notification.show("No player has been assigned to this slot");
			return;
		}
		saveWhile(() -> li.set(access, null));
		updatePlayerList(lu);
		if (update)
			buildSchedule();
	}
	

	
	protected Panel buildLine(Lineup lu, Line li, Button results)
	{
		int textCols = 10;
		Panel pnl = new Panel();
		HorizontalLayout h = new HorizontalLayout();
		if (li.isWon())
			h.setStyleName(DesktopUI.CONFIRMED);
		h.setSpacing(true);
		pnl.setContent(h);
		Label lbl = new Label("#" + li.getIndex());
		h.addComponent(lbl);
		h.setComponentAlignment(lbl, Alignment.MIDDLE_LEFT);
		VerticalLayout v = new VerticalLayout(); 
		h.addComponent(v);
		for (int i = 0; i < Line.IDENT.length; i++)
		{
			String access = Line.IDENT[i];
			TextField c = new TextField(null, new MethodProperty<Boolean>(li, access));
			c.setReadOnly(true);
			c.setColumns(textCols);
			c.addStyleName(ValoTheme.TEXTFIELD_BORDERLESS);
			c.addStyleName(ValoTheme.TEXTFIELD_TINY);
			v.addComponent(c);
		}
		HorizontalLayout h2 = new HorizontalLayout();
		v.addComponent(h2);
		CheckBox won = new CheckBox("Won", new MethodProperty<Boolean>(li, "won"));
		CheckBox lost = new CheckBox("Lost", new MethodProperty<Boolean>(li, "lost"));
		won.addStyleName(ValoTheme.COMBOBOX_SMALL);
		lost.addStyleName(ValoTheme.COMBOBOX_SMALL);
		won.addValueChangeListener(e -> saveWhile(() -> {
			if (won.getValue()) 
			{
				lost.setValue(false); 
				h.setStyleName(DesktopUI.CONFIRMED);
				h.markAsDirty();
			}
			else
				h.removeStyleName(DesktopUI.CONFIRMED);
			results.setEnabled(lu.isCompleted()); }));
		lost.addValueChangeListener(e -> saveWhile(() -> { 
			if (lost.getValue()) 
			{
				won.setValue(false); 
				h.removeStyleName(DesktopUI.CONFIRMED);
				h.markAsDirty();
			}
			results.setEnabled(lu.isCompleted()); }));
		h2.setSpacing(true);
		h2.addComponent(won);
		h2.addComponent(lost);
		TextField sc = new TextField(new MethodProperty<String>(li, "score"));
		sc.setImmediate(true);
		sc.addValueChangeListener(e -> saveData("Score for " + li + ":" + sc.getValue()));
		sc.addStyleName(ValoTheme.TEXTFIELD_TINY);
		sc.setColumns(textCols);
		v.addComponent(sc);
		won.setReadOnly(!canEdit(li) || lu.hasSentEmail());
		lost.setReadOnly(!canEdit(li) || lu.hasSentEmail());
		sc.setReadOnly(!canEdit(li) || lu.hasSentEmail());
		return pnl;
	}
	

	
	protected Component buildLineupSelection(Lineup lu, TextArea notes, TextArea stats, VerticalLayout prev)
	{
		VerticalLayout v = new VerticalLayout();
		ListSelect sel = new ListSelect(null, updatePlayerList(lu));
		sel.setDescription("Select a player, and then press the Check mark to add to the lineup");
		sel.setNullSelectionAllowed(false);
		sel.addValueChangeListener(e -> {
			prev.removeAllComponents();
			HtmlLineups html =  new HtmlLineups(lu.getWeek());
			if (sel.getValue() == null) 
			{
				notes.setCaption("Notes for:");
				notes.setValue("");
				stats.setReadOnly(false);
				stats.setCaption("Stats for:");
				stats.setValue("");
				stats.setReadOnly(true);
				html.setPlayer(null);
			}
			else
			{
				Player p = (Player)sel.getValue();
				notes.setCaption("Notes for: " + p);
				notes.setValue(getE().getNotesFor(p));	
				stats.setReadOnly(false);
				stats.setCaption("Stats for: " + p);
				stats.setValue(getE().getStatsFor(p));
				stats.setReadOnly(true);
				html.setPlayer(p);
			}
			prev.addComponent(html.getLabel());
		});
		lineupSelects.put(lu,  sel);
		sel.setRows(14);
		v.addComponent(sel);
		HorizontalLayout h = new HorizontalLayout();
		h.setSpacing(true);
		CheckBox u = new CheckBox("Unsure");
		u.addValueChangeListener(e -> updatePlayerList(lu));
		lineupShowUncertainCheckboxes.put(lu, u);
		h.addComponent(u);
		CheckBox c = new CheckBox("ALL");
		lineupShowAllCheckboxes.put(lu, c);
		c.addValueChangeListener(e -> { updatePlayerList(lu); u.setValue(false); });
		h.addComponent(c);
		v.addComponent(h);
		return v;
	}
	
	protected IndexedContainer updatePlayerList(Lineup lu)
	{
		CheckBox cb = lineupShowAllCheckboxes.get(lu);
		boolean all = (cb == null ? false : cb.getValue());
		cb = lineupShowUncertainCheckboxes.get(lu);
		boolean un = (cb == null ? false : cb.getValue());
		IndexedContainer c = lineups.get(lu);
		if (c == null)
		{
			c = new IndexedContainer();
			lineups.put(lu, c);
		}
		else
			c.removeAllItems();
		c.addItem(new ForfeitPlayer());
		for (Player p : getE().getRoster())
		{
			if (!lu.contains(p))
			{
				if (all || lu.isAvailable(p) || (un && lu.isUncertain(p)))
					c.addItem(p);
			}
		}
		return c;
	}
	
	protected void updatePlayerLists()
	{
		for (Lineup lu : getE().getLineups())
			updatePlayerList(lu);
	}
	
	protected void buildSchedule()
	{
		schedule.removeAllComponents();
		schedule.setSizeFull();
		for (Lineup l : getE().getLineups())
		{
			schedule.addComponent(buildWeek(l));
		}
	}
	
	protected void buildTeamSetup()
	{
		setupComp.setSpacing(true);
		setupComp.setMargin(true);
		if (getE().needsName())
			setupComp.addComponent(new Label("Please edit the team name so that it follows the form:<br><br>" +
				"<b>&lt;Year&gt; &lt;Season&gt; &lt;ALTA|USTA|league&gt; [Senior|SuperSenior] &lt;Mens|Womens|Mixed&gt &lt;level&gt; [Division #]</b><br><br>" +
				"Examples: '2017 Spring ALTA Mens B3 Division 6', '2017 Summer USTA Womens 3.0', '2017 Winter ALTA Senior Mixed C7', etc.<br>", ContentMode.HTML));
		MethodProperty<String> p1 = new MethodProperty(getE(), "name");
		TextField n = new TextField("Team name", p1);
		n.setWidth("400px");
		n.addValueChangeListener(e -> updateMainTabName());
		HorizontalLayout h1 = new HorizontalLayout();
		h1.setSpacing(true);
		h1.addComponent(n);
		setupComp.addComponent(h1);
		Button b = new Button();
		setupComp.addComponent(buildTeamSetupRosterSelect(b));
		HorizontalLayout bh = new HorizontalLayout();
		bh.setSpacing(true);
		b.setCaption("Send availability invite email to the whole team");
		b.setIcon(FontAwesome.ENVELOPE_O);
		b.addClickListener(e -> emailInvite(true));
		bh.addComponent(b);
		setupComp.addComponent(bh);
	}
	
	protected void fixAvailButton(Button b)
	{
		b.setCaption("Send availability invite email (to " + getE().getAvailToDo().size() + " unconfirmed players)");
	}
	
	public void setupGoodbyeSelect(AbstractSelect sel)
	{
		String tst = getE().getGoodbye();
		String selected = null;
		String x = "";
		if (getE().getCaptains().size() != 1)
			x = "s";
		String n = getE().getCaptainFullNames();
		String n2 = getE().getCaptainFirstNames();
		String[] g = new String[] { "Your Captain", "Your Fearless Leader", "Your King", "Your Queen" };
		sel.removeAllItems();
		for (int i = 0; i < g.length; i++)
		{
			sel.addItem(g[i]);
			if (g[i].equals(tst))
				selected = g[i];
			String s = g[i] + x + ", " + n2;
			sel.addItem(s);
			if (s.equals(tst))
				selected = s;
			s = g[i] + x + ", " + n;
			sel.addItem(s);
			if (s.equals(tst))
				selected = s;
		}
		if (selected != null)
			sel.select(selected);
	}
		

	
	protected void buildSeasonSetup()
	{
		matchSetupComp.removeAllComponents();
		matchSetupComp.setSpacing(true);
		matchSetupComp.setMargin(true);
		HorizontalLayout h = new HorizontalLayout();
		h.setSpacing(true);
		MethodProperty<Integer> p3 = new MethodProperty(getE(), "numberOfWeeks");
		TextField numWeeks = new TextField("Number of weeks", p3);
		numWeeks.addValueChangeListener(e -> updateSchedule());
		h.addComponent(numWeeks);	
		MethodProperty<Integer> p2 = new MethodProperty(getE(), "numberOfLines"); 
		TextField numLines = new TextField("Number of lines per week", p2);
		numLines.addValueChangeListener(e -> updateSchedule());
		h.addComponent(numLines);
		matchSetupComp.addComponent(h);
		DateField d = new DateField("Select the date of the first match (shortcut to assign weeks):", getE().getFirstDate());
		d.setDateFormat("MM-dd-yyyy");
		d.addValueChangeListener(e -> { updateDates(d.getValue()); buildSeasonSetup(); });
		h.addComponent(d);
		matchSetupComp.addComponent(new Label("Enter the opponent team name and their street address, if away."));
		buildSeasonSetupWeeks(matchSetupComp);
	}
	
	protected void buildSeasonSetupWeeks(VerticalLayout v)
	{
		for (Lineup lu : getE().getLineups())
		{
			HorizontalLayout h = new HorizontalLayout();  
			h.setSpacing(true);
			TextField tf = new TextField("Opponent Week #" + lu.getWeek() + ":", new MethodProperty<String>(lu, "name"));
			tf.setInputPrompt("Enter opponent");
			tf.setWidth("17em");
			tf.addStyleName(ValoTheme.TEXTFIELD_SMALL);
			tf.addValueChangeListener(e -> saveData(lu.getWeekString() + " opponent: " + tf.getValue()));
			h.addComponent(tf);
			TextField addr = new TextField("Address:", new MethodProperty<String>(lu, "location"));
			CheckBox c = new CheckBox("Away?", lu.isAway());
			addr.setWidth("28em");
			addr.addStyleName(ValoTheme.TEXTFIELD_SMALL);
			addr.addValueChangeListener(e -> {
				saveData(lu.getWeekString() + " location: " + tf.getValue());
				c.setValue(!addr.getValue().isEmpty());
			});
			h.addComponent(addr);
			DateField d = new DateField("Date of match:", new MethodProperty<String>(lu, "date"));
			d.addStyleName(ValoTheme.DATEFIELD_SMALL);
			d.setImmediate(true);
			d.setDateFormat("MM-dd-yyyy");
			d.addValueChangeListener(e -> saveData());
			h.addComponent(d);
			c.addValueChangeListener(e -> saveWhile(() -> lu.setAway(c.getValue())));
			h.addComponent(c);
			h.setComponentAlignment(c, Alignment.BOTTOM_RIGHT);
			v.addComponent(h);
		}
	}
	
	protected void updateDates(Date d)
	{
		if (d == null)
		{
			for (Lineup lu : getE().getLineups())
			{
				lu.setDate(null);
			}
		}
		else
		{
			long weekInMs = 604800 * 1000;
			long next = d.getTime();
			for (Lineup lu : getE().getLineups())
			{
				lu.setDate(new Date(next));
				next = next + weekInMs;
			}
		}
		saveData();
	}
	
	protected HorizontalLayout buildTeamSetupRosterSelect(Button avail)
	{
		HorizontalLayout h = new HorizontalLayout();
		h.setSpacing(true);
		VerticalLayout team = new VerticalLayout();
		VerticalLayout middle = new VerticalLayout();
		VerticalLayout all = new VerticalLayout();
		VerticalLayout ex = new VerticalLayout();
		h.addComponent(team);
		h.addComponent(middle);
		h.setComponentAlignment(middle, Alignment.MIDDLE_CENTER);
		h.addComponent(all);
		h.addComponent(ex);
		ListSelect tmlst = new ListSelect(); 
		tmlst.setRows(16);
		ListSelect alllst = new ListSelect(); 
		alllst.setDescription("Select a player from the neighborhood and press 'Add to team'");
		alllst.setRows(15);
		CheckBox men = new CheckBox("Men", getE().isMen());
		CheckBox women = new CheckBox("Women", getE().isWomen());
		CheckBox male = new CheckBox("Male", getE().isMen());
		CheckBox female = new CheckBox("Female", getE().isWomen());
		Button add = new Button("<- Add to team");
		Button remove = new Button("Remove from team ->");
		Label total = new Label();
		Button ac = new Button("Add Captain");
		ac.setEnabled(false);
		Button rc = new Button("Remove Captain");
		rc.setEnabled(false);
		add.setEnabled(false);
		remove.setEnabled(false);
		tmlst.setNullSelectionAllowed(false);
		tmlst.addValueChangeListener(e -> 
			{ 
				remove.setEnabled(tmlst.getValue() != null && !getE().includesCaptain(tmlst.getValue())); 
				ac.setEnabled(tmlst.getValue() != null && !getE().includesCaptain(tmlst.getValue()));
				rc.setEnabled(tmlst.getValue() != null && getE().includesCaptain(tmlst.getValue()) && getE().getCaptains().size() > 1);
			});
		alllst.setNullSelectionAllowed(false);
		alllst.addValueChangeListener(e -> { add.setEnabled(alllst.getValue() != null); } );
		team.addComponent(new Label("Team members"));
		team.addComponent(tmlst);
		men.addValueChangeListener(e -> saveWhile(() -> { fix(alllst, men, women); male.setValue(men.getValue()); }));
		women.addValueChangeListener(e -> saveWhile(() -> { fix(alllst, men, women); female.setValue(women.getValue());}));
		add.addClickListener(e -> { getE().addPlayer(alllst.getValue()); saveWhile(() -> fix(alllst, men, women, total, tmlst)); fixAvailButton(avail); });
		remove.addClickListener(e -> { getE().removePlayer(tmlst.getValue()); saveWhile(() -> fix(alllst, men, women, total, tmlst)); fixAvailButton(avail); });
		middle.addComponent(add);
		middle.addComponent(remove);
		middle.addComponent(total);
		middle.setComponentAlignment(add, Alignment.MIDDLE_CENTER);
		middle.setComponentAlignment(remove, Alignment.MIDDLE_CENTER);
		middle.setComponentAlignment(total, Alignment.MIDDLE_CENTER);
		ac.addStyleName(ValoTheme.BUTTON_LINK);
		ac.addStyleName(ValoTheme.BUTTON_TINY);
		ac.addClickListener(e -> saveWhile(() -> { getE().addCaptain(tmlst.getValue()); setupGoodbyeSelect(goodbye); ac.setEnabled(false); rc.setEnabled(true); }));
		rc.addStyleName(ValoTheme.BUTTON_LINK);
		rc.addStyleName(ValoTheme.BUTTON_TINY);
		rc.addClickListener(e -> saveWhile(() -> { getE().removeCaptain(tmlst.getValue()); setupGoodbyeSelect(goodbye); rc.setEnabled(false); ac.setEnabled(true); }));
		middle.addComponent(ac);
		middle.addComponent(rc);
		middle.setComponentAlignment(ac, Alignment.BOTTOM_CENTER);
		middle.setComponentAlignment(rc, Alignment.BOTTOM_CENTER);
		HorizontalLayout h2 = new HorizontalLayout();
		h2.setSpacing(true);
		h2.addComponent(men);
		h2.addComponent(women);
		all.addComponent(new Label("Neighborhood players"));
		all.addComponent(h2);
		all.addComponent(alllst);
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
		phone.addValueChangeListener(e -> { 
			boolean enabled = !(first.isEmpty() || last.isEmpty() || email.isEmpty() || phone.isEmpty());
			a.setEnabled(enabled);
			if (enabled)
			{
				createNewPlayer(first, last, email, phone, male); fix(alllst, men, women, total, tmlst);
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
		male.setValue(men.getValue());
		female.setValue(women.getValue());
		male.addValueChangeListener(e -> female.setValue(!male.getValue()));
		female.addValueChangeListener(e -> male.setValue(!female.getValue()));
		sex.addComponent(male);
		sex.addComponent(female);
		ex.addComponent(sex);
		a.setDescription("If a player is not already in the list, enter their information above and press 'Add new player'");
		a.addClickListener(e -> { 
			createNewPlayer(first, last, email, phone, male); fix(alllst, men, women, total, tmlst); 
			a.setEnabled(false);
			first.focus(); } );
		ex.addComponent(a);
		fix(alllst, men, women, total, tmlst);
		h.addComponent(addOptionsPanel());
		return h;
	}
	
	protected Component addOptionsPanel()
	{
		Panel panel = new Panel("Preferences:");
		panel.setSizeFull();
		VerticalLayout v = new VerticalLayout();
		v.setMargin(true);
		v.setSpacing(true);
		panel.setContent(v);
		goodbye = new NativeSelect("Select your email 'goodbye':");
		goodbye.setNullSelectionAllowed(false);
		goodbye.addValueChangeListener(e -> saveWhile(() -> getE().setGoodbye((String)goodbye.getValue())));
		setupGoodbyeSelect(goodbye);
		v.addComponent(goodbye);
		CheckBox showa = new CheckBox("Keep players availability private.", getE().shouldHideAvailabilities());
		showa.setDescription("Do not allow players to see other player's availability");
		showa.addValueChangeListener(e -> saveWhile(() -> getE().setHideAvailabilities(showa.getValue())));
		v.addComponent(showa);
		return panel;
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
			saveWhile(() -> getE().addPlayer(p));
			f.setValue("");
			l.setValue("");
			e.setValue("");
			c.setValue("");
			Notification.show(p.toString() + " successfully added", Type.HUMANIZED_MESSAGE);
		}
		else
			Notification.show(p.toString() + " already exists!", Type.ERROR_MESSAGE);
	}
	
	protected void fix(ListSelect a, CheckBox men, CheckBox women, Label lbl, ListSelect t)
	{
		fix(lbl, t);
		fix(a, men, women);
	}
	
	protected void fix(ListSelect a, CheckBox men, CheckBox women)
	{
		getE().setMen(men.getValue());
		getE().setWomen(women.getValue());
		Predicate<Player> pred = p -> !getE().getRoster().contains(p) && ((p.isMale() && men.getValue()) || (!p.isMale() && women.getValue()));
		ArrayList<Player> all = getHood().getPlayers().stream().filter(pred).collect(Collectors.toCollection(ArrayList::new));
		all.removeAll(getE().getRoster());
		a.setContainerDataSource(new IndexedContainer(all));
	}
	
	protected void fix(Label lbl, ListSelect t)
	{
		t.setContainerDataSource(new IndexedContainer(getE().getRoster()));
		lbl.setValue(getE().getRoster().size() + " member" + (getE().getRoster().size() == 1 ? "" : "s"));
		updatePlayerLists();
	}
	
	protected void buildLineups()
	{
		lineupSheet = new TabSheet();
		lineupSheet.addStyleName(ValoTheme.TABSHEET_FRAMED);
		lineupSheet.addStyleName(ValoTheme.TABSHEET_COMPACT_TABBAR);
		lineupComp.addComponent(lineupSheet);
		buildLineupSheet();
	}
	
	protected void updateSchedule()
	{
		println("Changed schedule, num weeks: " + getE().getNumberOfWeeks() + " num lines:" + getE().getNumberOfLines());
		saveData();
		buildLineupSheet();
		buildAvailSchedule();
		buildSchedule();
		buildSeasonSetup();
	}
	

	
	protected void updateMainTabName()
	{
		println("Changed name to: " + getE().getName());
		main.setCaption(getE().getName());
		saveData();
	}
	
	protected boolean canSetup()
	{
		Player p = getLoginUser();
		if (p.isAdmin())
			return true;
		return getCurrentExternalTeam() != null && getCurrentExternalTeam().includesCaptain(p);
	}

}
