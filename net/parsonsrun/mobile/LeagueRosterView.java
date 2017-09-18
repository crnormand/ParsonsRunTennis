package net.parsonsrun.mobile;

import net.parsonsrun.domain.*;

public class LeagueRosterView extends RosterView
{
	protected League league;
	
	public LeagueRosterView(League lg)
	{
		super();
		setLeague(lg);
		setPlayers(lg.getPlayers());
		setName(lg.getName());
	}

	public League getLeague()
	{
		return league;
	}

	public void setLeague(League league)
	{
		this.league = league;
	}
}
