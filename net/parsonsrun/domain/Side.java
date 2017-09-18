package net.parsonsrun.domain;

import net.parsonsrun.Utils;
import net.parsonsrun.domain.Match;

public class Side extends DomainObject
{
	private static final long serialVersionUID = 1L;

	Team team;
	boolean forfeit;
	boolean winner;
	int sets[];
	int setsWon;
	int previousElo;
	int resultElo;
	
	public Side init(Team t, int numSets)
	{
		team = t;
		sets = new int[numSets];
		return this;
	}
	
	public boolean isBye()
	{
		return getTeam().isBye();
	}
	
	public int getScore(int i)
	{
		if (i >= sets.length)
			return 0;
		return sets[i];
	}
	
	public int gamesPlayed()
	{
		int g = 0;
		for (int i = 0; i < sets.length; i++)
		{
			g += sets[i];
		}
		return g;
	}
	
	public int gamesWon(Team t)
	{
		return contains(t) ? gamesPlayed() : 0;
	}
	

	
	public int gamesWon(Player p)
	{
		return contains(p) ? gamesPlayed() : 0;
	}
	
	public int setsWon(Team t)
	{
		return contains(t) ? getSetsWon() : 0;
	}

	
	public int setsWon(Player t)
	{
		return contains(t) ? getSetsWon() : 0;
	}
	
	public boolean contains(Team t)
	{
		return team.equals(t);
	}
	
	public boolean contains(Player p)
	{
		return team.contains(p);
	}
	
	public void setScore(int i, int sc)
	{	// Magic numbers to encode forfiets and wins (mainly used by the import process)
		if (sc == 10)
			forfeit = true;
		else
			if (sc == 11)
				winner = true;
			else
				sets[i] = sc;
	}
	
	public boolean isWinner(Team t)
	{
		return winner && getTeam().equals(t);
	}
	
	public boolean isWinner(Player p)
	{
		return winner && getTeam().contains(p);
	}
	
//  http://gobase.org/studying/articles/elo/
public int determineWinner(Side other)
{
	int w1 = 0;
	int w2 = 0;
	int needed = neededToWin();
	if (isForfeit() || other.isForfeit())
	{
		setPreviousElo(getTeam().getElo());
		setResultElo(getTeam().getElo());
		other.setPreviousElo(other.getTeam().getElo());
		other.setResultElo(other.getTeam().getElo());
		setWinner(other.isForfeit() && !isForfeit());
		other.setWinner(!other.isForfeit() && isForfeit());
	}
	else
	{
		int i = 0;
		int r1 = getTeam().getElo();
		setPreviousElo(r1);
		int r2 = other.getTeam().getElo();
		other.setPreviousElo(r2);
		boolean done = false;
		while (! done)
		{
			double score = Utils.expectedScore(Math.abs(r1 - r2));
			double diff1 = score;
			double diff2 = score;
			if (r1 > r2)
				diff1 = 1.0 - score;
			else
				diff2 = 1.0 - score;
			int s1 = getSetScore(i);
			int s2 = other.getSetScore(i);
			//double k = 15.0 + (Math.abs(s1 - s2) / 2.0);   //  6 - 2
			//double k = 12.0 + Math.abs(s1 - s2);
			//double k = 24.0 + Math.abs(s1 - s2);
			double k = 20.0 + (Math.abs(s1 - s2) / 2.0);
			//double k = 20.0 + Math.abs(s1 - s2);
			if (s1 > s2)
			{
				int d = (int)(k * diff1);
				r1 = r1 + d;
				r2 = r2 - d;
				if (++w1 >= needed)
					done = true;
			}
			else
			{
				int d = (int)(k * diff2);
				r1 = r1 - d;
				r2 = r2 + d;
				if (++w2 >= needed)
					done = true;
			}
			i++;
			//System.out.println(getTeam().getName() + " s1:" + s1 + " s2:" + s2 + " diff1:" + diff1 + " diff2:" + diff2 + " r1:" + r1 + " r2:" + r2 + " " + other.getTeam().getName());
		}
		setSetsWon(w1);
		setResultElo(r1);
		setWinner(w1 >= needed);
		other.setResultElo(r2);
		other.setWinner(w2 >= needed);
		other.setSetsWon(w2);
	}
	return w1 + w2;
}
	
	
	int getSetScore(int i)
	{
		return sets[i];
	}
	
	int numberOfSets()
	{
		return sets.length;
	}
	
	int neededToWin()
	{
		return (numberOfSets() / 2) + 1;
	}
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(team.getName());
		if (winner)
			sb.append(" winner");
		if (forfeit)
			sb .append(" forfeit");
		for (int i = 0 ; i < numberOfSets() ; i++)
			sb.append(" " + sets[i]);
		return sb.toString();
	}

	public Team getTeam()
	{
		return team;
	}
	
	public String getName()
	{
		return getTeam().getName();
	}
	
	public String getName(boolean showFullNames, boolean phones)
	{
		if (phones)
			return getTeam().getPhonesHtml();
		return showFullNames ? getFullName() : getName();
	}
	
	public String getFullName()
	{
		return getTeam().getFullName();
	}
	
	
	public String getEloDisplay()
	{
		return getTeam().getEloDisplay();
	}

	public void setTeam(Team team)
	{
		this.team = team;
	}

	public int getPreviousElo()
	{
		return previousElo;
	}
	
	public String getEloChange()
	{
		return getPreviousElo() + "->" + getResultElo();
	}
	
	public String getEloDiff()
	{
		int i = getResultElo() - getPreviousElo();
		return (i < 0) ? "" + i : "+" + i;
	}
	
	public String getScoresString(Side other)
	{
		int needed = neededToWin();
		StringBuilder sb = new StringBuilder();
		if (isForfeit())
		{
			if (other.isForfeit())
				return "Default/Default";
			return "Default/Win";
		}
		if (other.isForfeit())
			return "Win/Default";

		String split = "";
		int i = 0;
		int w1 = 0;
		int w2 = 0;
		boolean done = false;
		while (! done)
		{
			int s1 = getSetScore(i);
			int s2 = other.getSetScore(i);
			if (s1 > s2)
			{
				if (++w1 == needed)
					done = true;
			}
			else
				if (++w2 == needed)
					done = true;

			sb.append(split);
			sb.append(s1);
			sb.append('-');
			sb.append(s2);
			split = "/";
			i++;
		}
		return sb.toString();
	}

	public void setPreviousElo(int previousRank)
	{
		this.previousElo = previousRank;
	}

	public int getResultElo()
	{
		return resultElo;
	}

	public void setResultElo(int resultRank)
	{
		this.resultElo = resultRank;
	}

	public boolean isForfeit()
	{
		return forfeit;
	}
	
	public void setForfeit(boolean b)
	{
		forfeit = b;
	}

	public boolean isWinner()
	{
		return winner;
	}

	public void setWinner(boolean winner)
	{
		this.winner = winner;
	}

	public int getSetsWon()
	{
		return setsWon;
	}

	public void setSetsWon(int setsWon)
	{
		this.setsWon = setsWon;
	}
}
