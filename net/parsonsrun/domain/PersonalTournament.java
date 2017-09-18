package net.parsonsrun.domain;

import java.util.ArrayList;

public class PersonalTournament extends Tournament
{
	private static final long serialVersionUID = 1L;
	
	protected ArrayList<Team> latePassTeams;
	
	protected void addStandings()
	{
		for (Player t : getPlayers())
		{
			standings.add(new PersonalStanding(this, t));
		}
	}

	public ArrayList<Team> getLatePassTeams()
	{
		if (latePassTeams == null)
		{
			latePassTeams = new ArrayList<Team>();
			for (Player p : getPlayers())
			{
				latePassTeams.add(getHood().getSingles(p));
			}
		}
		return latePassTeams;
	}

}
