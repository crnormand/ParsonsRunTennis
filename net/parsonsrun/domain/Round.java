package net.parsonsrun.domain;

import java.text.SimpleDateFormat;
import java.util.*;

public class Round extends NamedDomainObject
{
	private static final long serialVersionUID = 1L;

	protected ArrayList<Match> matches = new ArrayList();
	protected League league;
	protected int weeksLeft = 0;
	protected boolean started;
	
	public boolean isStarted()
	{
		return started;
	}

	public void startRound()
	{
		started = true;
	}

	public int indexOf(Match m)
	{
		return getMatches().indexOf(m);
	}

	public void setWeeksLeft(int weeksLeft)
	{
		this.weeksLeft = weeksLeft;
	}

	public boolean isEnded()
	{
		return weeksLeft < 1;
	}
	
	public boolean isLastWeek()
	{
		return weeksLeft == 1;
	}
	
	public void endWeek()
	{
		weeksLeft--;
	}
	
	public Tournament getTourny()
	{
		return (Tournament)getLeague();
	}
	
	public String getHeaderName()
	{
		if (isEnded())
			return getName() + " (closed)";
		if (isStarted())
			return getName() + " (current)";
		return getName();
	}
	
	public boolean contains(Player p)
	{
		for (Match m : getMatches())
		{
			if (m.contains(p))
				return true;
		}
		return false;
	}
	
	public boolean contains(Match test)
	{
		for (Match m : getMatches())
		{
			if (m.equals(test))
				return true;
		}
		return false;
	}
	
	public void delete()
	{
		getMatches().forEach(m -> m.delete());
		getMatches().clear();
	}
	
	public Match getMatch(int i)
	{
		if (i < 0 || getMatches().isEmpty())
			return null;
		return getMatches().get(i);
	}
	
	public League getLeague()
	{
		return league;
	}
	
	public boolean isRoundFor(Date d)
	{
		SimpleDateFormat fm = new SimpleDateFormat("MM");
		SimpleDateFormat fd = new SimpleDateFormat("dd");
		int mon = Integer.parseInt(fm.format(d));
		int day = Integer.parseInt(fd.format(d));
		String[] rn = getName().split("-");
		String[] start = rn[0].split("/");
		String[] end = rn[1].split("/");
		int startm = Integer.parseInt(start[0]);
		int startd = Integer.parseInt(start[1]);
		int endm = Integer.parseInt(end[0]);
		int endd = Integer.parseInt(end[1]);
		boolean match = false;
		if (startm == mon && endm == mon)
			return (startd <= day && endd >= day);
		else
			return (startm == mon && startd <= day) || (endm == mon && endd >= day);
	}

	public void setLeague(League league)
	{
		this.league = league;
	}
	public ArrayList<Match> getMatches()
	{
		return matches;
	}
	public void addMatch(Match m)
	{
		getMatches().add(m);
	}
	public Neighborhood getHood()
	{
		return getLeague().getHood();
	}
	
	public void closeOut(Date d)
	{
		for (Match m : getMatches())
		{
			if (!m.isBye())
			{
				m.closeOut(d);
			}
		}
	}
	
	public void sendReminders()
	{
		for (Match m : getMatches())
		{
			if (!m.isBye() && !m.hasBeenPlayed())
			{
				m.sendReminderInvite(getTourny(), this);
			}
		}

	}
}
