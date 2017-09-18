package net.parsonsrun.mobile;

import com.vaadin.addon.touchkit.ui.NavigationButton;
import com.vaadin.addon.touchkit.ui.Popover;
import com.vaadin.addon.touchkit.ui.VerticalComponentGroup;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Grid.CellReference;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.Table.ColumnHeaderMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.UI;
import com.vaadin.ui.Upload;
import com.vaadin.ui.themes.ValoTheme;

import net.parsonsrun.PictureFactory;
import net.parsonsrun.domain.DomainObject;
import net.parsonsrun.domain.League;
import net.parsonsrun.domain.Match;
import net.parsonsrun.domain.Picture;
import net.parsonsrun.domain.Round;
import net.parsonsrun.domain.Standing;
import net.parsonsrun.domain.Tournament;
import net.parsonsrun.domain.UpdateListener;

public class TournamentView extends LeaguesView
{
	protected Table table;
	
	public TournamentView(League t)
	{
		super(t);
		
	}
	
	public String toString()
	{
		return super.toString() + ":" + getTourny();
	}
	
	protected void playItForward()
	{
		if (getParentUI().isTournamentMatchAction())
		{
			int r = getParentUI().getRoundParameter();
			Round rnd = getTourny().getRound(r);
			if (rnd == null) return;
			int m = getParentUI().getMatchParameter();
			Match mtch = rnd.getMatch(m);
			if (mtch == null || mtch.hasBeenPlayed()) return;
			RoundsView rv = null;
			for (int i = 0; i <= r; i++)
			{
				rv = new RoundsView(getTourny(), i);
				navigateTo(rv);
			}
			if (rv != null)
				rv.matchSelected(mtch);
		}
		clearAction();
	}
	
	@Override
	public String getCaption()
	{
		return getTourny().getName() + " (" + getTourny().getTeams().size() + " teams)";
	}
	
	protected HorizontalLayout chartButtons()
	{
		HorizontalLayout toolbar = new HorizontalLayout();
		toolbar.setSpacing(true);
		Button b = new Button();
		b.setIcon(FontAwesome.LINE_CHART);
		b.addClickListener(e -> openHelp());
		toolbar.addComponent(b);
		addChartButton(new ChartViewElo(getTourny()), toolbar);  //.setIcon(FontAwesome.LINE_CHART)
		addChartButton(new ChartViewMWP(getTourny()), toolbar);
		addChartButton(new ChartViewSWP(getTourny()), toolbar);
		addChartButton(new ChartViewGWP(getTourny()), toolbar);
		addChartButton(new ChartViewGW(getTourny()), toolbar);
		return toolbar;
	}
	
	protected void openHelp()
	{
		Popover p = new Popover();
		CssLayout layout = new CssLayout();
		p.setContent(layout);
		StringBuilder s = new StringBuilder();
		s.append("<div><div class='label-centered'><b>Tournament ");
		s.append(getTourny().getName());
		s.append("</b></div><br>");
		s.append("<br>");
		s.append("Click one of the <b><span style='color=darkgreen'>GREEN</span></b>");
		s.append(" buttons to open a chart<br><br>");
		s.append("<table callpadding=5 cellspacing=5>");
		s.append("<tr><td>ELO</td><td>ELO Scores chart</td></tr>");
		s.append("<td></td><td><a href='https://en.wikipedia.org/wiki/Elo_rating_system'>Learn more about the ELO score</a></td></tr>");
		s.append("<tr><td>M%</td><td>Matches Won percentage chart</td></tr>");
		s.append("<tr><td>S%</td><td>Sets Won percentage chart</td></tr>");
		s.append("<tr><td>G%</td><td>Games Won percentage chart</td></tr>");
		s.append("<tr><td>G</td><td>Games Won chart</td></tr>");
		s.append("</table>");
		s.append("</div>");
		Label l = new Label(s.toString(), ContentMode.HTML);
		layout.addComponent(l);
		Button b = new Button("Close");
		b.addClickListener(e -> p.close());
		layout.addComponent(b);
		p.showRelativeTo(this);
	}
	
	protected Button addChartButton(ChartView cv, HorizontalLayout h)
	{
		Button b = new Button(cv.getButtonLabel());
		b.setStyleName("green");
		b.addClickListener(e -> navigateTo(cv));
		h.addComponent(b);
		h.setExpandRatio(b, 1.0f);
		return b;
	}
	
	public void buildUI()
	{
		content.addComponent(getSwipeReminder());
		VerticalComponentGroup g1 = new VerticalComponentGroup(getTourny().getName()); // "View Rounds:"
		HorizontalLayout h = new HorizontalLayout();
		Button b = new Button("Schedule");
		b.setIcon(FontAwesome.CALENDAR);
		b.addClickListener(e -> openRounds());
		g1.addComponent(h);
		h.addComponent(b);
		b = new Button("Recent Matches");
		b.setIcon(FontAwesome.FORWARD);
		b.addClickListener(e -> navigateTo(new MatchesView("Recently played matches", getTourny().getRecentlyPlayedMatches())));
		h.addComponent(b);
		h = new HorizontalLayout();
		g1.addComponent(h);
		//b = new NavigationButton("View Finals for " + getTourny().getName(), new FinalsView(getTourny()));
		//g1.addComponent(b);
		b = new Button("Roster");
		b.setIcon(FontAwesome.USERS);
		b.addClickListener(e -> navigateTo(new LeagueRosterView(getTourny())));
		h.addComponent(b);
		b = new Button("Pics");
		b.setIcon(FontAwesome.PICTURE_O);
		b.addClickListener(e -> navigateTo(new PictureView(getPictureAssociation())));
		b.setEnabled((getHood().getPictures(getPictureAssociation()).size()) > 0);
		h.addComponent(b);
		Upload upload = PictureFactory.makeUpload(getHood(), getLoginUser(), getPictureAssociation(), null);
		h.addComponent(upload);
		g1.addComponent(chartButtons());
		content.addComponent(g1);
		content.addComponent(new Label("Touch name to view statistics and match info"));
		buildTable();
		content.addComponent(table);
		content.addComponent(new Label(Standing.getLegendText1(0.75, getTourny().isIgnoreStandings()) + "<hr>", ContentMode.HTML));
		content.addComponent(new Label(Standing.getLegendText2(0.75, getTourny().isAdjustedStandings()), ContentMode.HTML));
		update();
	}
	
	protected void openRounds()
	{
		int i = 0;
		for (Round rnd : getTourny().getRounds())
		{
			navigateTo(new RoundsView(getTourny(), i++));
			if (!rnd.isEnded())
				return;
		}
	}
	
	protected void buildTable()
	{
		table = new Table();
		table.setStyleName("standings");
		table.setWidth("100%");
		table.setSortEnabled(false);
		table.addItemClickListener(e -> teamSelected((Integer)e.getItemId()));
		table.addContainerProperty("idName", String.class, null);
		table.addContainerProperty("matchesRecorded",  String.class, null);
		table.addContainerProperty("winLoss",  String.class, null);
		table.addContainerProperty("matchWinPerc",  String.class, null);
		table.addContainerProperty("setsWinPerc",  String.class, null);
		table.addContainerProperty("gamesWinPerc",  String.class, null);
		table.setColumnWidth("matchesRecorded", 24);
		table.setColumnWidth("winLoss", 28);
		table.setColumnWidth("matchWinPerc", 30);
		table.setColumnWidth("setsWinPerc", 30);
		table.setColumnWidth("gamesWinPerc", 30);
		if (!getTourny().isEloHidden())
		{
			table.addContainerProperty("elos",  String.class, null);
			table.setColumnWidth("elos", 40);
			table.setColumnHeaders("Name","#-D", "W/L", "M%", "S%", "G%", "ELO");
		}
		else
			table.setColumnHeaders("Name","#-D", "W/L", "M%", "S%", "G%");

		table.setPageLength(0);
	}
	
	protected void teamSelected(Integer i)
	{
		Standing s = getTourny().getStanding(i);
		navigateTo(new StandingsView(s, s.getMatches()));
	}
	
	protected String cellStyle(CellReference cell)
	{
		Standing s = (Standing)cell.getItemId();
		if (s.getTeam().contains(getLoginUser()))
			return "currentuser";
		else
			return "underline";
	}
	
	protected DomainObject getPictureAssociation()
	{
		return getTourny();
	}
	
	public void update()
	{
		if (!built)
			return;
		updateUI(() -> {
			table.removeAllItems();
			int id = 0;
			for (Standing s : getTourny().getStandings())
			{
				if (getTourny().isEloHidden())
					table.addItem(new Object[] { s.getShortName(), s.getMatchesRecorded(),  s.getWinLoss(), s.getMatchWinPerc(), s.getSetsWinPerc(), s.getGamesWinPerc() }, id++);
				else
					table.addItem(new Object[] { s.getShortName(), s.getMatchesRecorded(),  s.getWinLoss(), s.getMatchWinPerc(), s.getSetsWinPerc(), s.getGamesWinPerc(), s.getElos() }, id++);				
			}
		});
	}

	public Tournament getTourny()
	{
		return (Tournament)getLeague();
	}
}
