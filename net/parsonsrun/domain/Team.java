package net.parsonsrun.domain;import java.util.*;

import net.parsonsrun.ParsonsRunUI;

public abstract class Team extends DomainObject
{
	private static final long serialVersionUID = 1L;
	public static final int STARTING_RANK = 2000;
	
	protected int elo = STARTING_RANK;
	
	ArrayList<Event> events = new ArrayList<Event>();
	
	public boolean isSingles()
	{
		return false;
	}
	
	public abstract boolean contains(Player p);
	
	public abstract String getName();
	
	public abstract String getNameA();
	public abstract String getNameB();
	public abstract String getShortName();
	public abstract String getFirstName();
	public abstract void init();
	public abstract String getFullName();
	
	public ArrayList<Player> getPlayers()
	{
		return new ArrayList<Player>();
	}

	public boolean isDoubles()
	{
		return false;
	}
	
	public void sendEmail(ParsonsRunUI ui, String subject, String htmlMessage)
	{
	}
	
	public boolean isBye()
	{
		return false;
	}
	
	public void addMatch(Match m)
	{
		getEvents().add(m);
	}
	
	public void removeMatch(Match m)
	{
		getEvents().remove(m);
	}

	public abstract boolean equals(Team t);
	
	public abstract String getPhonesHtml();
	
	public boolean equalsDoubles(Doubles t)
	{
		return false;
	}
	public boolean equalsSingles(Singles t)
	{
		return false;
	}

	public int getElo()
	{
		return elo;
	}
	
	public void delete()
	{
		ArrayList<Event> copy = (ArrayList<Event>)getEvents().clone();
		copy.forEach(e -> e.delete());
	}
	
	public String toString()
	{
		return getName();
	}
	

	
	public boolean canUpdateChallenge(Rung r)
	{
		if (isAdmin())
			return true;
		if (isOrganizer(r.getLadder()))
			return true;
		return r.getPending().contains(this);	// This team is part of the challenge
	}
	
	public abstract boolean isOrganizer(League lg);
	
	public abstract boolean isAdmin();
	
	public String getEloDisplay()
	{
		return "(" + getElo() + ")";
	}

	public void setElo(int elo)
	{
		this.elo = elo;
	}

	public ArrayList<Event> getEvents()
	{
		return events;
	}

}
