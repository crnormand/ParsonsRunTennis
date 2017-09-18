package net.parsonsrun.domain;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

import net.parsonsrun.Utils;

public class Standing extends DomainObject implements Comparable<Standing>
{
	private static final long serialVersionUID = 1L;
	
	public Tournament league;
	public Team team;
	public ArrayList<Match> matches;
	public int matchesPlayed;
	public int matchesDefaulted;
	public int matchesWon;
	public int setsWon;
	public int setsPlayed;
	public int gamesPlayed;
	public int gamesWon;
	public int adjustedMatchesPlayed;
	public int adjustedMatchesWon;
	public int adjustedSetsWon;
	public int adjustedSetsPlayed;
	public int adjustedGamesPlayed;
	public int adjustedGamesWon;

	public double matchWinPercentage;
	public double setsWinPercentage;
	public double gamesWinPercentage;
	public boolean tied;
	public boolean hthWin;
	public boolean hthLoss;
	public String commentTop;
	public String commentBottom;
	
	public static String[] getColumnInfo(boolean hideElo)
	{
		if (hideElo)
			return new String[] { 
					"matchesRecorded", "#-D", "28",
					"winLoss", "W/L", "54",
					"matchWinPerc", "M%", "54",
					"setsWinPerc", "S%", "54",
					"gamesWinPerc", "G%", "54"
			};

		return new String[] { 
				"matchesRecorded", "#-D", "28",
				"winLoss", "W/L", "54",
				"matchWinPerc", "M%", "54",
				"setsWinPerc", "S%", "54",
				"gamesWinPerc", "G%", "54",
				"elos", "ELO", "64" 
		};
	}
	
	public static String getLegendText(int width, boolean include, boolean ignore)
	{
		StringBuilder s = new StringBuilder();
		s.append("<table style='width: ");
		s.append(width);
		s.append("px; border-collapse: collapse; border: 1px solid grey'><tr><td>");
		s.append(getLegendText1(0.8, ignore));
		s.append("</td><td>");
		s.append(getLegendText2(0.8, include));
		s.append("</td></tr></table>");
		return s.toString();
	}
	
	public static String getLegendText1(double sz, boolean ignore)
	{
		String normal = String.format("%04.1f", sz);
		String large = String.format("%04.1f", sz + 0.25);

		StringBuilder s = new StringBuilder();
		s.append("<div style='font-size: " + normal + "em;'>");
		if (ignore)
		{
			s.append("Due to the nature of the tournament, neither team nor<br>individual statistics can be collected in a meaningful way.<br>These statistics are presented for entertainment purposes only.");
		}
		else
		{
			s.append("&nbsp;<span style='font-size: " + large + "em;'>=</span> : Tied<br>");
			s.append("&nbsp;<span style='font-size: " + large + "em;'>+</span> : Won in Head to Head match<br>");
			s.append("&nbsp;<span style='font-size: " + large + "em;'>~</span> : Lost in Head to Head match</div>");
		}
		return s.toString();
	}

	public static String getLegendText2(double sz, boolean include)
	{
		String t = "<td style='text-align:center'>";
		String normal = String.format("%04.1f", sz);
		StringBuilder s = new StringBuilder();
		s.append("<div style='font-size: " + normal + "em;'>");
		s.append("<table>");
		s.append("<tr><td style='text-align:center'>#-D</td><td>:</td><td>Matches Recorded-Defaulted</td></tr>");
		s.append("<tr><td style='text-align:center'>W/L</td><td>:</td><td>Matches Won/Lost</td></tr>");
		if (include)
			s.append("<tr><td></td><td>:</td><td>* forfeit wins ignored</td></tr>");
		s.append("<tr><td style='text-align:center'>M%</td><td>:</td><td>Matches Won percentage</td></tr>");
		s.append("<tr><td style='text-align:center'>S%</td><td>:</td><td>Sets Won percentage</td></tr>");
		s.append("<tr><td style='text-align:center'>G%</td><td>:</td><td>Games Won percentage</td></tr></table>");
		s.append("<a href='https://en.wikipedia.org/wiki/Elo_rating_system'>Learn more about the ELO score</a></div>");
		return s.toString();
	}

	public Standing(Tournament l, Team t)
	{
		league = l;
		team = t;
		matches = league.getMatchesFor(team);
		gatherInfo();
	}
	
	protected Standing()
	{
		super();
	}
	
	public Standing(Tournament l, Team t, ArrayList<Match> ms)
	{
		league = l;
		team = t;
		matches = ms;
		gatherInfo();
	}
	
	public String getMatchesRecorded()
	{
		return "" + getMatchesPlayed() + "-" + getMatchesDefaulted();
	}
	
	public int getNumberOfLatePasses()
	{
		return getLeague().getLatePasses(getTeam());
	}
	
	public boolean contains(Team t)
	{
		return getTeam().equals(t);
	}
	
	public int getElo()
	{
		return getTeam().getElo();
	}
	
	public static String getHeaderHtml(boolean hideElo)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("<tr><td align='center'>Rank</td><td>Team</td>");
		String cols[] = getColumnInfo(hideElo);
		for (int i = 0; i < cols.length; i = i + 3)
		{
			sb.append("<td align='center'>");
			sb.append(cols[i+1]);
			sb.append("</td>");
		}
		sb.append("</tr>");
		return sb.toString();
	}
	
	public String getStandingHtml(int position)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("<tr>");
		sb.append("<td align='center'>");
		if (!getLeague().isIgnoreStandings())
			sb.append(position);
		sb.append("</td>");		
		sb.append("<td>");
		sb.append(getTeam().getFullName());
		sb.append("</td>");

		String cols[] = getColumnInfo(getLeague().isEloHidden());
		for (int i = 0; i < cols.length; i = i + 3)
		{
			Object value = "";
			try
			{
				Method m = Utils.findGetter(getClass(), cols[i]);
				value = m.invoke(this);
			} catch (Exception e) {
				System.out.println(e);
			}

			sb.append("<td align='center'>");
			sb.append(value);
			sb.append("</td>");
		}
		sb.append("</tr>");
		return sb.toString();
	}

	
	public String getElos()
	{
		return String.valueOf(getTeam().getElo());
	}
	
	public String getWinLoss()
	{
		StringBuilder s = new StringBuilder();
		if (getLeague().isAdjustedStandings())
		{
			s.append(adjustedMatchesWon);
			s.append("/");
			s.append(adjustedMatchesPlayed - adjustedMatchesWon);
			if (matchesWon != adjustedMatchesWon)
				s.append("*");
		}
		else
		{
			s.append(matchesWon);
			s.append("/");
			s.append(matchesPlayed - matchesWon);
		}
		return s.toString();
	}
	
	protected void gatherInfo()
	{
		for (Match m : matches)
		{
			boolean adjust = false;
			matchesPlayed++;
			adjustedMatchesPlayed++;
			if (m.isTotalForfeit())
				matchesDefaulted++;
			if (isWinner(m))
			{
				matchesWon++;
				if (m.isForfeit())
					adjust = true;
				else
					adjustedMatchesWon++;
			}
			gamesPlayed += m.gamesPlayed();
			gamesWon += gamesWon(m);
			setsPlayed += m.setsPlayed();
			setsWon += setsWon(m);
			if (adjust)
			{
				adjustedMatchesPlayed--;
			}
			else
			{
				adjustedGamesPlayed += m.gamesPlayed();
				adjustedGamesWon += gamesWon(m);
				adjustedSetsPlayed += m.setsPlayed();
				adjustedSetsWon += setsWon(m);
			}
		}
		if (getLeague().isAdjustedStandings())
		{
			if (adjustedMatchesPlayed > 0)
				matchWinPercentage = 100.0 * ((double)adjustedMatchesWon / (double)adjustedMatchesPlayed);
			if (gamesPlayed > 0)
				gamesWinPercentage = 100.0 * ((double)adjustedGamesWon / (double)adjustedGamesPlayed);
			if (setsPlayed > 0)
				setsWinPercentage = 100.0 * ((double)adjustedSetsWon / (double)adjustedSetsPlayed);
		}
		else
		{
			if (matchesPlayed > 0)
				matchWinPercentage = 100.0 * ((double)matchesWon / (double)matchesPlayed);
			if (gamesPlayed > 0)
				gamesWinPercentage = 100.0 * ((double)gamesWon / (double)gamesPlayed);
			if (setsPlayed > 0)
				setsWinPercentage = 100.0 * ((double)setsWon / (double)setsPlayed);
		}
	}
	
	protected boolean isWinner(Match m)
	{
		return m.isWinner(team);
	}
	
	protected int gamesWon(Match m)
	{
		return m.gamesWon(team);
	}
	
	protected int setsWon(Match m)
	{
		return m.setsWon(team);
	}
	
	public String getMatchWinPerc()
	{
		if (getMatchesPlayed() == 0)
			return "n/a";
		return String.format("%04.1f", getMatchWinPercentage());
	}
	
	public String getGamesWinPerc()
	{
		if (getMatchesPlayed() == 0)
			return "n/a";
		return String.format("%04.1f", getGamesWinPercentage());
	}
	
	public String getSetsWinPerc()
	{
		if (getSetsPlayed() == 0)
			return "n/a";
		return String.format("%04.1f", getSetsWinPercentage());
	}
	
	public int compareTo(Standing other)
	{
		if (getLeague().isIgnoreStandings())
			return getName().compareTo(other.getName());
		if (getMatchWinPercentage() == other.getMatchWinPercentage())
		{
			Boolean hth = playedAgainst(other);
			if (hth != null)
			{
				if (hth.booleanValue())
				{
					hthWin = true;
					other.hthLoss = true;
					return -1;
				}
				else
				{
					other.hthWin = true;
					hthLoss = true;		
					return 1;
				}
			}
			if (getSetsWinPercentage() == other.getSetsWinPercentage())
			{
				if (getGamesWinPercentage() == other.getGamesWinPercentage())
				{
					if (getGamesPlayed() == other.getGamesPlayed())
					{
						tied = true;
						other.tied = true;
						return getName().compareTo(other.getName());
					}
					else
						return getGamesPlayed() > other.getGamesPlayed() ? -1 : 1;
				}
				else
					return getGamesWinPercentage() > other.getGamesWinPercentage() ? -1 : 1;
			}
			else
				return getSetsWinPercentage() > other.getSetsWinPercentage() ? -1 : 1;
		}
		else
			return getMatchWinPercentage() > other.getMatchWinPercentage() ? -1 : 1;
	}

	// Return null if never played each other or tied in # of wins/losses
	// Otherwise return true if this team is ahead in the Head To Head matchups.
	protected Boolean playedAgainst(Standing other)
	{
		int wins = 0;
		int losses = 0;
		for (Match m : matches)
		{
			if (m.contains(other.getTeam()))
			{
				if (!getLeague().isAdjustedStandings() && !m.isForfeit())
				{
					// Only consider an actual winner... double defaults don't count as a win
					if (m.isWinner(getTeam()))
						wins++;
					if (m.isWinner(other.getTeam()))
						losses++;
				}
			}
		}
		if (wins == losses)
			return null;
		return new Boolean(wins > losses);
	}
	
	public boolean contains(Match m)
	{
		return m.contains(team);
	}
	
	public void makeCommentVs(Standing other)
	{
		if (getLeague().isIgnoreStandings())
		{
			setCommentTop("");
			other.setCommentBottom("");
			return;
		}
		if (getMatchWinPercentage() == other.getMatchWinPercentage())
		{
			Boolean hth = playedAgainst(other);
			if (hth != null)
			{
				if (hth.booleanValue())
				{
					setCommentTop("Won Head-to-Head vs " + other.getSimpleName());
					other.setCommentBottom("Lost Head-to-Head vs " + getSimpleName());
				}
			}
			if (getSetsWinPercentage() == other.getSetsWinPercentage())
			{
				if (getGamesWinPercentage() == other.getGamesWinPercentage())
				{
					if (getGamesPlayed() == other.getGamesPlayed())
					{
						setCommentTop("Tied with " + other.getSimpleName());
						other.setCommentBottom("Tied with " + getSimpleName());
					}
					else
						if (getGamesPlayed() > other.getGamesPlayed())
						{
							setCommentTop("Tied, but played more games than " + other.getSimpleName());
							other.setCommentBottom("Tied, but played less games than " + getSimpleName());
						}
						else
							setCommentTop("Unknown against " + other.getSimpleName());
				}
				else
					if (getGamesWinPercentage() > other.getGamesWinPercentage())
					{
						setCommentTop("Better Games Won% than " + other.getSimpleName());
						other.setCommentBottom("Worse Games Won% than " + getSimpleName());
					}
			}
			else
				if (getSetsWinPercentage() > other.getSetsWinPercentage())
				{
					setCommentTop("Better Sets Won% than " + other.getSimpleName());
					other.setCommentBottom("Worse Sets Won% than " + getSimpleName());
				}
		}
		else
			if (getMatchWinPercentage() > other.getMatchWinPercentage())
			{
				setCommentTop("Better Matches Won% than " + other.getSimpleName());
				other.setCommentBottom("Worse Matches Won% than " + getSimpleName());
			}
	}
	
	public Tournament getLeague()
	{
		return league;
	}
	
	public String getName()
	{
		return getTeam().getFullName() + getNameExtention();
	}
	

	
	public String getShortName()
	{
		return getSimpleName() + getNameExtention();
	}
	
	public String getSimpleName()
	{
		return getTeam().getShortName();
	}
	
	public String getCommentTopA()
	{
		if (getCommentTop() == null)
			return "";
		int i = getCommentTop().lastIndexOf(' ');
		if (i < 0)
			return "";
		return getCommentTop().substring(0, i).trim();
	}
	
	public String getCommentTopB()
	{
		if (getCommentTop() == null)
			return "";
		int i = getCommentTop().lastIndexOf(' ');
		if (i < 0)
			return getCommentTop();
		return getCommentTop().substring(i).trim();
	}
	
	public String getCommentBottomA()
	{
		if (getCommentBottom() == null)
			return "";
		int i = getCommentBottom().lastIndexOf(' ');
		if (i < 0)
			return "";
		return getCommentBottom().substring(0, i).trim();
	}
	
	
	public String getCommentBottomB()
	{
		if (getCommentBottom() == null)
			return "";
		int i = getCommentBottom().lastIndexOf(' ');
		if (i < 0)
			return getCommentBottom();
		return getCommentBottom().substring(i).trim();
	}
	
	protected String getNameExtention()
	{
		return (tied ? "=" : "") + (hthWin ? "+" : "") + (hthLoss ? "~" : "");
	}
	public void setLeague(Tournament league)
	{
		this.league = league;
	}
	
	public String getHoverTextHtml()
	{
		if (getLeague().isIgnoreStandings())
			return null;
		String t = "";
		String m = "";
		String b = "";
		if (getCommentTop() != null && !getCommentTop().isEmpty())
			b = getCommentTop();
		if (getCommentBottom() != null && !getCommentBottom().isEmpty())
			t = getCommentBottom();
		if (!t.isEmpty() && !b.isEmpty())
			m = "<hr>";
		return t + m + b;
	}
	public Team getTeam()
	{
		return team;
	}
	
	public String getRank()
	{
		if (getLeague().isIgnoreStandings())
			return "";
		int i = getLeague().getStandings().indexOf(this) + 1;
		if (i == 1)
			return "1st";
		else if (i == 2)
			return "2nd";
		else if (i == 3)
			return "3rd";
		else
			return i + "th";
	}
	public void setTeam(Team team)
	{
		this.team = team;
	}
	public int getGamesWon()
	{
		return gamesWon;
	}

	public int getMatchesPlayed()
	{
		return matchesPlayed;
	}

	public int getMatchesWon()
	{
		return matchesWon;
	}

	public int getGamesPlayed()
	{
		return gamesPlayed;
	}

	public double getMatchWinPercentage()
	{
		return matchWinPercentage;
	}

	public double getGamesWinPercentage()
	{
		return gamesWinPercentage;
	}

	public int getSetsWon()
	{
		return setsWon;
	}

	public int getSetsPlayed()
	{
		return setsPlayed;
	}

	public ArrayList<Match> getMatches()
	{
		return matches;
	}

	public double getSetsWinPercentage()
	{
		return setsWinPercentage;
	}

	public String getCommentTop()
	{
		return commentTop;
	}

	public void setCommentTop(String commentTop)
	{
		this.commentTop = commentTop;
	}

	public String getCommentBottom()
	{
		return commentBottom;
	}

	public void setCommentBottom(String commentBottom)
	{
		this.commentBottom = commentBottom;
	}

	public int getAdjustedMatchesPlayed()
	{
		return adjustedMatchesPlayed;
	}

	public int getAdjustedMatchesWon()
	{
		return adjustedMatchesWon;
	}

	public int getAdjustedSetsWon()
	{
		return adjustedSetsWon;
	}

	public int getAdjustedSetsPlayed()
	{
		return adjustedSetsPlayed;
	}

	public int getAdjustedGamesPlayed()
	{
		return adjustedGamesPlayed;
	}

	public int getAdjustedGamesWon()
	{
		return adjustedGamesWon;
	}

	public int getMatchesDefaulted()
	{
		return matchesDefaulted;
	}

	public void setMatchesDefaulted(int matchesDefaulted)
	{
		this.matchesDefaulted = matchesDefaulted;
	}
}
