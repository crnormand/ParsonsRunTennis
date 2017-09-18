package net.parsonsrun.domain;

import java.util.*;

import net.parsonsrun.Utils;

public class Lineup extends NotableObject
{
	private static final long serialVersionUID = 1L;
	protected String name = "";
	protected String location;
	protected int week;
	protected Date date;
	protected boolean away;
	protected boolean sentEmail;
	protected ArrayList<Line> lines = new ArrayList<Line>();
	protected ArrayList<Player> unAvailable;
	protected ArrayList<Player> unCertain;
	protected ExternalTeam team;
	
	private Lineup(ExternalTeam t)
	{
		setTeam(t);
		t.getRoster().stream().forEach(p -> addUnavailable(p));
	}
	
	public static Lineup basicLinup(ExternalTeam t, int numberOfLines, int currentSize)
	{
		Lineup l = new Lineup(t);
		l.setWeek(currentSize + 1);
		for (int i = 1; i <= numberOfLines; i++)
			l.addLine(new Line(i));
		return l;
	}
	
	public Line lineFor(Player p)
	{
		for (Line li : getLines())
		{
			if (li.contains(p))
				return li;
		}
		return null;
	}
	
	public ExternalTeam getTeam()
	{
		return team;
	}

	public void setTeam(ExternalTeam team)
	{
		this.team = team;
	}

	public String subjectLine()
	{
		return getWeekString() + " " + (isAway() ? "@" : "vs") + " " + getName() + " on " + getDateString();
	}
	
	public String subjectLineHtml()
	{
		StringBuilder s = new StringBuilder();
		s.append(getWeekString());
		s.append(" ");
		if (isAway())
		{
			s.append("vs ");
			if (getLocation().isEmpty())
				s.append(getName());
			else
			{
				s.append("<a href='");		
				s.append(getLocationHtml());
				s.append("'>");
				s.append(getName());
				s.append("</a>");
			}
		}
		else
		{
			s.append("@ ");
			s.append(getName());
		}
		s.append(" on ");
		s.append(getDateString());
		return s.toString();
	}

	public ArrayList<Player> getUnCertain()
	{
		if (unCertain == null)
			unCertain = new ArrayList<Player>();
		return unCertain;
	}

	public ArrayList<Player> getPlayers()
	{
		ArrayList<Player> ps = new ArrayList<Player>();
		for (Line li : getLines())
		{
			if (li.getA() != null)
				ps.add(li.getA());
			if (li.getB() != null)
				ps.add(li.getB());
		}
		return ps;		
	}
	
	public boolean isCompleted()
	{
		for (Line l : getLines())
		{
			if (!l.hasBeenPlayed())
				return false;
		}
		return true;
	}
	
	public boolean isAllAssigned()
	{
		for (Line l : getLines())
		{
			if (!l.canBeScored())
				return false;
		}
		return true;
	}
	
	public Line getLineFor(Player p)
	{
		for (Line l : getLines())
		{
			if (p.equals(l.getA()))
				return l;
			if (p.equals(l.getB()))
				return l;
		}
		return null;
	}
	
	public int getLineIntFor(Player p)
	{
		Line l = getLineFor(p);
		return l == null ? 0 : l.getIndex();
	}
	
	public int getIndexFor(Line l)
	{
		for (Line li : getLines())
		{
			if (l.hasSamePlayers(li))
				return li.getIndex();
		}
		return 0;
	}
	
	public boolean isAllConfirmed()
	{
		for (Line l : getLines())
		{
			if (!l.isAllConfirmed())
				return false;
		}
		return true;
	}
	
	public void confirm(Player p)
	{
		for (Line l : getLines())
		{
			l.confirm(p);
		}

	}
	
	public boolean reject(Player p)
	{
		if (notAvailable(p))
			return false;		// Return false is already rejected
		for (Line l : getLines())
		{
			l.reject(p);
		}
		addUnavailable(p);
		return true;		
	}
	
	public void clear()
	{
		for (Line l : getLines())
		{
			l.clear();
		}
	}
	
	public String getWeekString()
	{
		return "Week #" + getWeek() + (isAway() ? " (away)" : " (home)");
	}
	
	public boolean notAvailable(Player p)
	{
		return getUnavailable().contains(p);
	}
	
	public boolean isAvailable(Player p)
	{
		return !notAvailable(p);
	}
	
	public boolean isUncertain(Player p)
	{
		return getUnCertain().contains(p);
	}
	
	public void addUnavailable(Player p)
	{
		if (!getUnavailable().contains(p))
		{
			getUnavailable().add(p);
		}
	}
	
	public void addUnCertain(Player p)
	{
		if (!getUnCertain().contains(p))
		{
			getUnCertain().add(p);
		}
	}
	
	public void removeUnavailable(Player p)
	{
		getUnavailable().remove(p);
	}
	
	public void removeUnCertain(Player p)
	{
		getUnCertain().remove(p);
	}
	
	public boolean contains(Player p)
	{
		for (Line l : getLines())
		{
			if (l.contains(p))
				return true;
		}
		return false;
	}

	public ArrayList<Line> getLines()
	{
		return lines;
	}
	
	public void addLine()
	{
		addLine(new Line(getLines().size() + 1));
	}
	
	public void removeLine()
	{
		getLines().remove(getLines().size() - 1);
	}
	
	protected void addLine(Line l)
	{
		getLines().add(l);
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public int getWeek()
	{
		return week;
	}

	public void setWeek(int week)
	{
		this.week = week;
	}

	public ArrayList<Player> getUnavailable()
	{
		if (unAvailable == null)
			unAvailable = new ArrayList<Player>();
		return unAvailable;
	}

	public Date getDate()
	{
		return date;
	}
	
	public String getDateString()
	{
		return Utils.printDate(getDate());
	}

	public void setDate(Date date)
	{
		this.date = date;
	}

	public boolean isAway()
	{
		return away;
	}

	public void setAway(boolean away)
	{
		this.away = away;
	}

	public String getLocation()
	{
		return location == null ? "" : location;
	}
	
	public String getLocationHtml()
	{
		return Utils.googleMapsUrl(getLocation());
	}

	public void setLocation(String location)
	{
		this.location = location;
	}

	public boolean hasSentEmail()
	{
		return sentEmail;
	}

	public void setSentEmail(boolean sentEmail)
	{
		this.sentEmail = sentEmail;
	}
}
