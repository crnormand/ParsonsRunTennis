package net.parsonsrun.domain;import java.util.*;

public abstract class League extends NamedDomainObject
{
	private static final long serialVersionUID = 1L;

	protected Neighborhood hood;
	protected ArrayList<Team> teams = new ArrayList();
	protected ArrayList<Player> organizers = new ArrayList<Player>();
	protected ArrayList<Match> matches = new ArrayList<Match>();
	protected HashMap<Team, Integer> startingElo = new HashMap<Team, Integer>();

	protected int numberOfSets = 3;
	protected int numberInFinals = 4;   // Works best when power of 2 (2, 4, 8, 16, etc.)
	protected int numberOfLatePasses = 1;
	protected boolean winnersOnTop = false;
	protected boolean closed = false;
	protected Date startDate;
	protected boolean organizerNotifiedOnScore = false;
	protected boolean eloHidden = false;

	public boolean isOrganizerNotifiedOnScore()
	{
		return organizerNotifiedOnScore;
	}

	public void setOrganizerNotifiedOnScore(boolean organizerNotifiedOnScore)
	{
		this.organizerNotifiedOnScore = organizerNotifiedOnScore;
	}

	public boolean isWinnersOnTop()
	{
		return winnersOnTop;
	}

	public void setWinnersOnTop(boolean winnersOnTop)
	{
		this.winnersOnTop = winnersOnTop;
	}

	public int getNumberOfLatePasses()
	{
		return numberOfLatePasses;
	}

	public void setNumberOfLatePasses(int numberOfLatePasses)
	{
		this.numberOfLatePasses = numberOfLatePasses;
	}

	public Neighborhood getHood()
	{
		return hood;
	}
	
	public ArrayList<Match> getRecentlyPlayedMatches()
	{
		ArrayList<Match> ma = new ArrayList<Match>();
		getMatches().stream().filter(m -> !m.isTotalForfeit()).forEach(m -> ma.add(m));
		return ma;
	}
	
	public boolean okToSave(Match m, Player p1)
	{
		return getHood().okToSave(m, p1);
	}
	
	public ArrayList<Player> getPlayers()
	{
		HashSet<Player> ps = new HashSet<Player>();
		for (Team t : getTeams())
		{
			ps.addAll(t.getPlayers());
		}
		ArrayList<Player> pls = new ArrayList<Player>();
		ps.stream().forEach(p -> pls.add(p));
		Collections.sort(pls);
		return pls;
	}
	
	public void delete()
	{
		for (Team t : getTeams())
		{
			if (!t.isBye())
			{
				t.setElo(getStartingElo(t));
			}
		}
		getTeams().clear();
		for (Match m : getMatches())
		{
			m.delete();
		}
		getMatches().clear();
	}
	
	public boolean canBeDeleted()
	{
		// TODO
		return true;
	}
	
	public void updateListeners()
	{
		super.updateListeners();
		getHood().updateListeners();
	}
	
	public boolean isOrganizer(Player p1)
	{
		for (Player p : getOrganizers())
			if (p.equals(p1))
				return true;
		return false;
	}
	
	public boolean includes(Player p)
	{
		for (Team t : getTeams())
		{
			if (t.contains(p))
				return true;
		}
		return false;
	}
	
	public boolean isLocked(Player p, Match m)
	{
		return m.isLocked();
	}
	
	public String toString()
	{
		return getClass().getSimpleName() + ":" + getName() + ", " + teams.size() + " teams";
	}
	
	public boolean isLadder()
	{
		return false;
	}
	public boolean isTournament()
	{
		return false;
	}
	
	public ArrayList<Match> getMatchesFor(Team t)
	{
		ArrayList<Match> ms = new ArrayList<Match>();
		for (Match m : getMatches())
		{
			if (m.contains(t))
			{
				ms.add(m);
			}
		}
		return ms;
	}
	
	public ArrayList<Match> getMatchesFor(Player p)
	{
		ArrayList<Match> ms = new ArrayList<Match>();
		for (Match m : getMatches())
		{
			if (m.contains(p))
			{
				ms.add(m);
			}
		}
		return ms;
	}
	
	public void setHood(Neighborhood parent)
	{
		this.hood = parent;
	}
	public ArrayList<Team> getTeams()
	{
		return teams;
	}
	
	public void addMatch(Match m)
	{
		getMatches().add(0, m);
		updateListeners();
	}
	
	public synchronized void addOrUpdateMatch(Match m, boolean recorded)
	{
		if (!recorded)
			addMatch(m);
		Collections.sort(getMatches());
	}
	
	public Team getTeam(String s)
	{
		for (Team t : getTeams())
		{
			if (t.getName().equalsIgnoreCase(s))
				return t;
		}
		int i = 0;
		try
		{
			i = Integer.parseInt(s);
		} catch (NumberFormatException e) {}
		
		if (i > 0)
		{
			return getTeams().get(i - 1);
		}
		ByeTeam b = new ByeTeam();
		if (b.getName().equalsIgnoreCase(s))
			return b;
		return null;
	}
	public synchronized void addTeam(Team tm)
	{
		if (!tm.isBye())
		{
			getTeams().add(tm);
			getStartingElo().put(tm, tm.getElo());
		}
	}
	
	public synchronized void removePlayer(Player p)
	{
		ArrayList<Team> removed = new ArrayList<Team>();
		for (Team t : getTeams())
		{
			if (t.contains(p))
			{
				removed.add(t);
				getStartingElo().remove(t);
			}
		}
		getTeams().removeAll(removed);
	}
	
	public int getStartingElo(Team t)
	{
		Integer i = startingElo().get(t);
		return (i == null) ? Team.STARTING_RANK : i.intValue();			
	}

	public void addOrganizer(Player p)
	{
		getOrganizers().add(p);
	}
	
	public int numberOfSetsNeededForWin()
	{
		return (requiredNumberOfSets() / 2) + 1;
	}
	
	public String getName()
	{
		return name;
	}
	
	public synchronized void recalculate()
	{
		Iterator<Team> i = getStartingElo().keySet().iterator();
		while (i.hasNext())
		{
			Team t = i.next();
			t.setElo(getStartingElo(t));
		}	
		ArrayList<Match> rev = new ArrayList<Match>();
		rev.addAll(getMatches());
		Collections.reverse(rev);
		for (Match m : rev)
		{
			m.determineWinner();
		}
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public ArrayList<Player> getOrganizers()
	{
		return organizers;
	}

	public int requiredNumberOfSets()
	{
		return numberOfSets;
	}

	public void setNumberOfSets(int s)
	{
		numberOfSets = s;
	}

	public ArrayList<Match> getMatches()
	{
		return matches;
	}
	
	public boolean contains(Team t)
	{
		for (Team x : getTeams())
		{
			if (x.equals(t))
				return true;
		}
		return false;
	}

	public HashMap<Team, Integer> startingElo()
	{
		return startingElo;
	}
	
	public String getOrganizerNames()
	{
		StringBuilder sb = new StringBuilder();
		boolean q = false;
		for (Player p : getOrganizers())
		{
			q = true;
			sb.append(p.firstLastName());
			sb.append(", ");
		}
		if (q)
			sb.setLength(sb.length() - 2);
		else
			sb.append("n/a");
		return sb.toString();
	}

	public HashMap<Team, Integer> getStartingElo()
	{
		return startingElo;
	}

	public int getNumberInFinals()
	{
		return numberInFinals;
	}

	public boolean isClosed()
	{
		return closed;
	}

	public void setClosed(boolean closed)
	{
		this.closed = closed;
	}

	public Date getStartDate()
	{
		return startDate;
	}

	public void setStartDate(Date startDate)
	{
		this.startDate = startDate;
	}

	public boolean isEloHidden()
	{
		return eloHidden;
	}

	public void setEloHidden(boolean eloHidden)
	{
		this.eloHidden = eloHidden;
	}
}
