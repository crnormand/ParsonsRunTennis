package net.parsonsrun.mobile;import java.util.ArrayList;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnHeaderMode;

import net.parsonsrun.domain.*;

public class StandingsView extends MatchesView
{
	Tournament league;
	Team team;
	Standing standing;
	
	public StandingsView(Standing s, ArrayList<Match> m)
	{
		super(s.getName(), m);
		standing = s;
		league = s.getLeague();
		team = s.getTeam();
	}
	
	public String toString()
	{
		return super.toString() + ":" + league;
	}


	protected void subBuildUI()
	{
		getCurrentStanding();
		Table table = new Table();
		table.setWidth("100%");
		table.setColumnHeaderMode(ColumnHeaderMode.HIDDEN);
		table.addContainerProperty("Description", String.class, null);
		table.addContainerProperty("Value",  String.class, null);
		int i = 1;
		table.addItem(row(standing.getCommentBottomA(), standing.getCommentBottomB()), i++);
		table.addItem(row(standing.getCommentTopA(), standing.getCommentTopB()), i++);
		table.addItem(row("% Matches won", standing.getMatchWinPerc() + "%"), i++);
		table.addItem(row("# of Matches recorded", standing.getMatchesPlayed()), i++);
		table.addItem(row("# of Matches defaulted", standing.getMatchesDefaulted()), i++);
		table.addItem(row("# of Matches Won", standing.getMatchesWon()), i++);
		table.addItem(row("% of Sets won", standing.getSetsWinPerc() + "%"), i++);
		table.addItem(row("# of Sets played", standing.getSetsPlayed()), i++);
		table.addItem(row("# of Sets won", standing.getSetsWon()), i++);
		table.addItem(row("% of Game won", standing.getGamesWinPerc() + "%"), i++);
		table.addItem(row("# of Games played", standing.getGamesPlayed()), i++);
		table.addItem(row("# of Games won", standing.getGamesWon()), i++);
		table.addItem(row("# of LATE PASSes", standing.getNumberOfLatePasses()), i++);
		table.setPageLength(0);
		currentPage.addComponent(table);
	}
	
	protected void getCurrentStanding()
	{
		standing = league.getStandingFor(team);
	}
	
	protected Object[] row(String s1, String s2)
	{
		return new Object[] { s1, s2 };
	}

	protected Object[] row(String s1, int i)
	{
		return row(s1, "" + i);
	}

}
