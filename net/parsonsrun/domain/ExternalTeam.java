package net.parsonsrun.domain;

import java.util.*;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Label;

import net.parsonsrun.desktop.ExternalTeamView;

public class ExternalTeam extends NotableObject
{
	private static final long serialVersionUID = 1L;
	protected String name;
	protected int numberOfLines = 5;
	protected int numberOfWeeks = 7;
	protected ArrayList<Player> roster = new ArrayList<Player>();
	protected ArrayList<Lineup> lineups = new ArrayList<Lineup>();
	protected ArrayList<Lineup> playoffs = new ArrayList<Lineup>();
	protected ArrayList<Player> captains = new ArrayList<Player>();
	protected HashMap<Player, String> playerNotes = new HashMap<Player, String>();
	protected HashSet<Player> availToDo = new HashSet<Player>();
	protected boolean men = true;
	protected boolean women = true;
	protected String goodbye;
	protected boolean hideAvailabilities = false;
	protected boolean defaultAvailability = false;
	protected boolean closed;

	public boolean getDefaultAvailability()
	{
		return defaultAvailability;
	}
	
	public int getNumberAvailable(Player p)
	{
		int i = 0;
		for (Lineup lu : getLineups())
		{
			if (lu.isAvailable(p))
				i++;
		}
		return i;
	}
	
	public int getNumberUnAvailable(Player p)
	{
		int i = 0;
		for (Lineup lu : getLineups())
		{
			if (!lu.isAvailable(p) && !lu.isUncertain(p))
				i++;
		}
		return i;
	}
	
	public int getNumberUnCertain(Player p)
	{
		int i = 0;
		for (Lineup lu : getLineups())
		{
			if (!lu.isAvailable(p) && lu.isUncertain(p))
				i++;
		}
		return i;
	}
	


	public void setDefaultAvailability(boolean defaultAvailability)
	{
		this.defaultAvailability = defaultAvailability;
	}

	public ExternalTeam()
	{
		fixSize();
	}
	
	public ArrayList<Player> getRoster()
	{
		return roster;
	}
	
	public String toString()
	{
		return super.toString() + " " + getName();
	}
	public String getNotesFor(Player p)
	{
		String n = playerNotes.get(p);
		return (n == null) ? "" : n;
	}
	
	public String getStatsFor(Player p)
	{
		if (p == null || p.isForfeit())
			return "";
		StringBuilder s = new StringBuilder();
		HashMap<Line, Integer> map = getWinLoseMap();
		for (Lineup lu : getLineups())
		{
			Line li = lu.getLineFor(p);
			if (li != null)
			{
				Integer wl = map.get(li);
				if (wl != null)
				{
					map.remove(li);
					s.append(li);
					if (wl > 0)
						s.append(" (Winning team : ");
					if (wl < 0)
						s.append(" (Losing team : ");
					if (wl == 0)
						s.append(" (Neutral : ");
					s.append(wl);
					s.append(")\n");
					for (Lineup lu2 : getLineups())
					{
						for (Line li2 : lu2.getLines())
						{
							if (li2.hasSamePlayers(li))
							{
								if (!li2.getScore().isEmpty())
								{
									s.append("   ");
									s.append(lu2.getWeekString());
									s.append(" : ");
									s.append(li2.getScore());
									s.append("\n");
								}
							}
						}
					}
				}
			}
		}
		return s.toString();
	}
	
	public void setNotesFor(Player p, String n)
	{
		playerNotes.put(p, n);
	}

	public String getFullName()
	{
		if (needsName())
			return "New team for " + getCaptainNames();
		return getName() + " (" + getCaptainNames() + ")";
	}
	
	public void confirm(Player p, int week)
	{
		if (week <= getLineups().size())
		{
			Lineup lu = getLineups().get(week - 1);
			lu.confirm(p);
		}
	}
	
	public boolean reject(Player p, int week)
	{
		if (week < getLineups().size())
		{
			Lineup lu = getLineups().get(week - 1);
			if (lu.reject(p))
				return true;		// Return true if someone actually rejects
		}	
		return false;
	}
	
	// Return zero-based index
	public int getNextWeekIndex()
	{
		int i = 0;
		for (Lineup lu : getLineups())
		{
			if (!lu.isCompleted())
				return i;
			i++;
		}
		return i;
	}
	
	protected void fixSize()
	{
		if (getLineups().size() > 0)
		{
			int l = getNumberOfLines() - getLineups().get(0).getLines().size();
			for (int i = l; i < 0; i++)
				for (Lineup lu : getLineups())
					lu.removeLine();
			for (int i = l; i > 0; i--)
				for (Lineup lu : getLineups())
					lu.addLine();
		}
		int w = getNumberOfWeeks() - getLineups().size();  // neg means remove from array, pos means add to array
		for (int i = w; i < 0; i++)
			getLineups().remove(getLineups().size() - 1);
		for (int i = w; i > 0; i--)
			getLineups().add(Lineup.basicLinup(this, getNumberOfLines(), getLineups().size()));
	}
	
	// Return null if legal lineup.   Otherwise return string indicating issue
	// check individual player moves, then team moves.
	public String isLegal(Lineup lu)
	{
		int end = getLineups().indexOf(lu);
		if (end == 0)
			return null;	// first week, always legal
		ArrayList<Player> currentPlayers = lu.getPlayers();
		for (int i = end - 1; i >= 0; i--)
		{
			Lineup prev = getLineups().get(i);
			Iterator<Player> players = currentPlayers.iterator();
			while (players.hasNext())
			{
				Player p = players.next();
				int currentLine = lu.getLineIntFor(p);
				int prevLine = prev.getLineIntFor(p);
				if (prevLine > 0)
				{
					if (Math.abs(currentLine - prevLine) > 2)
						return "Illegal player placement: " + p.firstLastName() + " last played at line #" + prevLine + " on " + prev.getWeekString();
					players.remove();
				}
			}
		}
		ArrayList<Line> currentLines = new ArrayList<Line>();
		for (Line li : lu.getLines())
			currentLines.add(li);
		for (int i = end - 1; i >= 0; i--)
		{
			Lineup prev = getLineups().get(i);
			Iterator<Line> ls = currentLines.iterator();
			while (ls.hasNext())
			{
				Line l = ls.next();
				int prevLine = prev.getIndexFor(l);
				if (prevLine > 0)
				{
					if (Math.abs(l.getIndex() - prevLine) > 1)
						return "Illegal team placement: " + l + " last played at line #" + prevLine + " on " + prev.getWeekString();
					ls.remove();
				}
			}
		}
		HashMap<Line, Integer> winLose = getWinLoseMap();
		for (int i = 1 ; i < lu.getLines().size(); i++)
		{
			Line cur = lu.getLines().get(i);
			Integer wl = winLose.get(cur);
			int p = previousPosition(cur, end);
			if (wl != null && wl > 0 && p > 0 && p < cur.getIndex())	// winner that moved down
			{		// Check to see if previous line is loser that moved up
				Line above = lu.getLines().get(i - 1);
				Integer aboveWl = winLose.get(above);
				int aboveP = previousPosition(above, end);
				if (aboveWl != null && aboveWl < 0 && aboveP > 0 && aboveP > above.getIndex())
					return "Sandbag Alert, Winning team:" + cur + " (" + wl + ") moved below " + above + " (" + aboveWl + ")";
			}
		}
		return null;
	}
		
	protected int previousPosition(Line li, int end)
	{
		for (int i = end - 1; i >= 0; i--)
		{
			Lineup prev = getLineups().get(i);
			int p = prev.getIndexFor(li);
			if (p > 0)
				return p;
		}
		return 0;
	}
	
	protected HashMap<Line, Integer> getWinLoseMap()
	{
		HashMap<Line, Integer> map = new HashMap<Line, Integer>();
		for (Lineup lu : getLineups())
		{
			for (Line li: lu.getLines())
			{
				int winLoss = li.isWon() ? 1 : (li.isLost() ? -1 : 0);
				Integer prev = map.get(li);
				if (prev == null)
				{
					prev = winLoss;
				}
				else
					prev = prev + winLoss;
				map.put(li, prev);
			}
		}
		return map;
	}
	

	public void addCaptain(Object p)
	{
		if (getCaptains().contains(p))
			return;
		getCaptains().add((Player)p);
		addPlayer(p);
	}
	
	public boolean needsSetup()
	{
		return needsName() || getRoster().size() <= 1;
	}
	
	public boolean needsName()
	{
		return name == null || name.isEmpty();
	}
	
	public boolean includesCaptain(Object p)
	{
		return getCaptains().contains(p);
	}
	
	public boolean includes(Object p)
	{
		return getRoster().contains(p);
	}

	public String getCaptainNames()
	{
		if (!getCaptains().isEmpty())
		{
			StringBuilder s = new StringBuilder();
			for (Player p : getCaptains())
			{
				s.append(p.getIdName());
				s.append('/');
			}
			s.setLength(s.length() - 1);
			return s.toString();
		}
		return "None assigned";
	}
	
	public String getCaptainFullNames()
	{
		if (!getCaptains().isEmpty())
		{
			StringBuilder s = new StringBuilder();
			for (Player p : getCaptains())
			{
				s.append(p.firstLastName());
				s.append('/');
			}
			s.setLength(s.length() - 1);
			return s.toString();
		}
		return "None assigned";
	}
	
	public String getCaptainFirstNames()
	{
		if (!getCaptains().isEmpty())
		{
			StringBuilder s = new StringBuilder();
			for (Player p : getCaptains())
			{
				s.append(p.getFirst());
				s.append('/');
			}
			s.setLength(s.length() - 1);
			return s.toString();
		}
		return "None assigned";
	}
	
	public void addPlayer(Object o)
	{
		if (getRoster().contains(o))
			return;
		Player p = (Player)o;
		getRoster().add(p);
		Collections.sort(getRoster());
		getAvailToDo().add(p);
		getLineups().stream().forEach(lu -> lu.addUnavailable(p));
	}
	
	public void removePlayer(Object p)
	{
		Player t = (Player)p;
		if (t.isForfeit())
			return;
		getRoster().remove(p);
		getPlayerNotes().remove(p);
		getAvailToDo().remove(p);
		getLineups().stream().forEach(lu -> lu.removeUnavailable(t));
	}
	
	public Line getLine(int week, int line)
	{
		Lineup lu = getLineups().get(week - 1);
		return lu.getLines().get(line -1);
	}
	
	public int getAvailableFor(Lineup lu)
	{
		return getRoster().size() - lu.getUnavailable().size();
	}
	
	public String getAvailableStringFor(Lineup lu)
	{
		return getAvailableFor(lu) + " available";
	}
	
	
	public void removeCaptain(Object p)
	{
		getCaptains().remove(p);
	}

	public String getName()
	{
		if (needsName())
			return "New team for " + getCaptainNames();
		return name;
	}
	
	protected void addLineup(Lineup l)
	{
		getLineups().add(l);
	}
	


	public void setName(String name)
	{
		this.name = name;
	}

	public ArrayList<Lineup> getLineups()
	{
		return lineups;
	}
	
	public Date getFirstDate()
	{
		for (Lineup l : getLineups())
		{
			return l.getDate();
		}
		return null;
	}

	public ArrayList<Player> getCaptains()
	{
		return captains;
	}
	
	public boolean canBeDeleted()
	{
		// TODO
		return true;
	}

	public int getNumberOfLines()
	{
		return numberOfLines;
	}

	public void setNumberOfLines(int numberOfLines)
	{
		this.numberOfLines = numberOfLines;
		fixSize();
	}

	public int getNumberOfWeeks()
	{
		return numberOfWeeks;
	}

	public void setNumberOfWeeks(int numberOfWeeks)
	{
		this.numberOfWeeks = numberOfWeeks;
		fixSize();
	}

	public boolean isMen()
	{
		return men;
	}

	public void setMen(boolean men)
	{
		this.men = men;
	}

	public boolean isWomen()
	{
		return women;
	}

	public void setWomen(boolean women)
	{
		this.women = women;
	}

	public HashSet<Player> getAvailToDo()
	{
		return availToDo;
	}
	
	public void checkedAvailability(Player e)
	{
		getAvailToDo().remove(e);
	}
	
	public String getRosterHtml()
	{
		StringBuilder s = new StringBuilder();
		s.append("<div><table cellpadding=2 style='border: 0px solid grey;'>");
		for (Player p : getRoster())
		{
			s.append("<tr>");
			td(s, p.firstLastName());
			td(s, "<a href='mailto:" + p.getEmail() + "'>" + p.getEmail() + "</a>");
			td(s, "<a href='tel:" + p.getPhone() + "'>" + p.getPhoneDisplay() + "</a>");
			s.append("</tr>");
		}
		s.append("</table></div>");
		return s.toString();
	}
	
	protected void td(StringBuilder b, String s)
	{
		b.append("<td>");
		b.append(s);
		b.append("</td>");
	}
	
	public boolean hasCheckedAvailability(Player e)
	{
		return e.isForfeit() || (getRoster().contains(e) && !getAvailToDo().contains(e));
	}

	public HashMap<Player, String> getPlayerNotes()
	{
		return playerNotes;
	}

	public String getGoodbye()
	{
		if (goodbye == null)
		{
			String s = "";
			if (getCaptains().size() > 1)
				s = "s";
			return "Your Captain" + s + ", " + getCaptainFullNames();
		}
		return goodbye;
	}

	public void setGoodbye(String goodbye)
	{
		this.goodbye = goodbye;
	}

	public boolean shouldHideAvailabilities()
	{
		return hideAvailabilities;
	}

	public void setHideAvailabilities(boolean hideAvailabilities)
	{
		this.hideAvailabilities = hideAvailabilities;
	}

	public boolean isClosed()
	{
		return closed;
	}

	public void setClosed(boolean closed)
	{
		this.closed = closed;
	}
}
