package net.parsonsrun.domain;

import java.text.SimpleDateFormat;
import java.util.Date;

import net.parsonsrun.ParsonsRunUI;
import net.parsonsrun.Utils;

public class Match extends Event implements Comparable<Match>
{
	private static final long serialVersionUID = 1L;
	
	public static final String[] CSS = { 
			"class='match-self'", "style='text-align: center;'",
			"class='match-other'", "style='text-align: center;'",
			"class='match-score'", "style='font-weight: bold;color: black;'",
			"class='match-win'", "style='color:#006600;'",
			"class='match-forfeit'", "style='color:#660000;'",
			"class='match-lose'", "style='color:#660000;'"
	};

	League league;
	Side a;
	Side b;
	Date played;
	String comment = "";
	Player recordedBy;
	int setsPlayed;
	boolean locked = false;
	boolean recorded = false;	// Has this match been recorded in the league?
	
	public Match(League l)
	{
		league = l;
	}
	
	public void delete()
	{
		super.delete();
		if (a != null)
			a.getTeam().removeMatch(this);
		if (b != null)
			b.getTeam().removeMatch(this);
		a = null;
		b = null;
	}
	
	public int compareTo(Match m)
	{
		if (played == null && m.getPlayed() == null)
			return 0;
		if (played == null)
			return 1;
		if (m.getPlayed() == null)
			return -1;
		return m.getPlayed().compareTo(played);
	}
	
	public String getPlayedDisplay()
	{
		return (getPlayed() == null) ? "" : new SimpleDateFormat("M/dd/yy").format(getPlayed());
	}
	
	public boolean hasBeenPlayed()
	{
		return played != null;
	}
	
	public Side getA()
	{
		return a;
	}
	
	public Side getB()
	{
		return b;
	}
	
	public void determineWinner()
	{
		setsPlayed = a.determineWinner(b);
		a.getTeam().setElo(a.getResultElo());
		b.getTeam().setElo(b.getResultElo());
	}
	
	public void setTeamA(Team t)
	{
		a = new Side().init(t, getLeague().requiredNumberOfSets());
		t.addMatch(this);
	}
	
	public void setTeamB(Team t)
	{
		b = new Side().init(t, getLeague().requiredNumberOfSets());
		t.addMatch(this);
	}
	
	public League getLeague()
	{
		return league;
	}
	
	public void setLeague(League league)
	{
		this.league = league;
	}
	
	public String getLabel()
	{
		return "Match: " + a.getName() + " vs " + b.getName();
	}
	
	public String toString()
	{
		return "Match " + a + " vs " + b;
	}
	
	public String displayString()
	{
		StringBuilder s = new StringBuilder();
		Team ta = (a == null) ? null : a.getTeam();
		Team tb = (b == null) ? null : b.getTeam();
		if (ta == null)
			s.append("n/a");
		else
			s.append(ta.getName());
		s.append(" vs ");
		if (tb == null)
			s.append("n/a");
		else
			s.append(tb.getName());
		return s.toString();
	}
	
	public Date getPlayed()
	{
		return played;
	}

	// Zero based index.   Set #1 = Index 0
	public void setScore(int i, int sca, int scb)
	{
		a.setScore(i, sca);
		b.setScore(i, scb);
	}

	public int setsPlayed()
	{
		return setsPlayed;
	}

	public void setPlayed(Date played)
	{
		this.played = played;
	}
	
	// Call this method when match is initially played
	public void setMatchPlayed(Date p)
	{
		setPlayed(p);
		if (!isBye())
		{
			determineWinner();
			getLeague().addOrUpdateMatch(this, recorded);
			recorded = true;
			getLeague().recalculate();
			updateListeners();
		}
	}
	
	public boolean contains(Team t)
	{
		return a.contains(t) || b.contains(t);
	}
	
	public void sendScore()
	{
		Tournament t = (Tournament)getLeague();
		Round r = t.getRoundFor(this);
		sendMatch(t, r, "Match score for " + displayString(), "The following score has been entered for the match:", null, true, false);
	}
	
	public boolean isFuture()
	{
		Tournament t = (Tournament)getLeague();
		Round r = t.getRoundFor(this);
		return !r.isStarted();
	}
	
	public void sendInvite(Tournament t, Round current)
	{
		sendMatch(t, current, "Match Invite for " + current.getName(), "Your match for this round:", null, false, true);
	}
	
	public void sendReminderInvite(Tournament t, Round current)
	{
		sendMatch(t, current, "Reminder: Match Invite for " + current.getName(), "This is an automated reminder.<br><br>Your match for this round has not yet been played:", null, false, true);
	}
	
	public void sendMatch(Tournament t, Round current, String subject, String first, String last, boolean score, boolean includePhones)
	{
		StringBuilder s1 = new StringBuilder();
		s1.append(first);
		s1.append("<br><br>");
		s1.append(getEmailHtml(400, includePhones));
		s1.append("<br>");
		if (last != null && !last.isEmpty())
		{
			s1.append("<br>");
			s1.append(last);
			s1.append("<br><br>");
		}
		s1.append("<a href='");
		s1.append(ParsonsRunUI.encodeTournamentMatch(t, current, this));
		s1.append("'>Click here to ");
		s1.append(score ? "update" : "enter");
		s1.append(" the match score</a>");
		Utils.sendTeamsEmail(getA().getTeam(), getB().getTeam(), null, subject, s1.toString());
		for (Player p : t.getOrganizers())
			if (score && t.isOrganizerNotifiedOnScore() && !contains(p))
				Utils.sendEmail(t.getOrganizers(), null, subject, s1.toString());
	}
	
	public void closeOut(Date d)
	{
		if (!hasBeenPlayed())
		{
			if (getA() != null) getA().setForfeit(true);
			if (getB() != null) getB().setForfeit(true);
			setComment("Automatic default." + ((getLeague().getNumberOfLatePasses() > 0) ? "  Match can be made up using a LATE PASS" : ""));
			setMatchPlayed(d);
		}
		setLocked(true);
	}

	public int requiredNumberOfSets()
	{
		return getLeague().requiredNumberOfSets();
	}
	
	public int numberOfSetsNeededForWin()
	{
		return getLeague().numberOfSetsNeededForWin();
	}
	
	public boolean contains(Player p)
	{
		return a.contains(p) || b.contains(p);
	}
	
	public boolean contains(Standing s)
	{
		return s.contains(this);
	}
	
	public boolean isWinner(Team t)
	{
		return a.isWinner(t) || b.isWinner(t);
	}
	
	public boolean isWinner(Player t)
	{
		return a.isWinner(t) || b.isWinner(t);
	}
	
	public int gamesPlayed()
	{
		return a.gamesPlayed() + b.gamesPlayed();
	}
	
	public int gamesWon(Team t)
	{
		return a.gamesWon(t) + b.gamesWon(t);
	}
	
	public int gamesWon(Player t)
	{
		return a.gamesWon(t) + b.gamesWon(t);
	}
	
	public int setsWon(Team t)
	{
		return a.setsWon(t) + b.setsWon(t);
	}
	
	public int setsWon(Player t)
	{
		return a.setsWon(t) + b.setsWon(t);
	}
	
	public String getMobileHtml(boolean phones)
	{
		return getGridHtml(true, getHoverTextHtml(), phones);
	}
	
	public String getMobileHtml()
	{
		return getMobileHtml(false);
	}
	
	public String getDesktopFullHtml()
	{
		return getGridHtml(true, null, false);
	}
	
	public String getDesktopSmallHtml()
	{
		return getGridHtml(false, null, false);
	}
	
	public String getEmailHtml(int width, boolean includePhones)
	{
		String html = getMobileHtml(includePhones);
		for (int i = 0; i < CSS.length; i = i + 2)
		{
			html = html.replaceAll(CSS[i], CSS[i+1]);
		}
		StringBuilder s = new StringBuilder();
		s.append("<table style='width: ");
		s.append(width);
		s.append("px'><tr><td>");
		s.append(html);
		s.append("</td></tr>");
		s.append("</table>");
		return s.toString();
	}
	
	public String getHoverTextHtml()
	{
		String c = "";
		String r = "";
		String h = "";
		if (!getComment().isEmpty())
			c = '"' + getEscapedComment() + '"';
		if (getRecordedBy() != null)
			r = "<small>Recorded by: " + getRecordedBy().firstLastName() + "</small>";
		if (c.isEmpty() && r.isEmpty())
			return null;
		if (!c.isEmpty() && !r.isEmpty())
			h = "<hr width='150px'>";
		return c + h + r;
	}
	
	public String getEscapedComment()
	{
		return Utils.escapeHTML(getComment());
	}
	
	public boolean isBye()
	{
		return a.isBye() || b.isBye();
	}
	
	public boolean isTotalForfeit()
	{
		return a.isForfeit() && b.isForfeit();
	}
	
	public boolean isForfeit()
	{
		return a.isForfeit() || b.isForfeit();
	}
	
	public boolean isForfeitWin(Team tm)
	{
		return isForfeit() && isWinner(tm);
	}

	
	public String getGridHtml(boolean showFullNames, String optional, boolean phones)
	{
		String sm1 = "<small>";
		String sm2 = "</small>";
		Side top = a;
		Side bot = b;
		if (hasBeenPlayed() && getLeague().isWinnersOnTop() && bot.isWinner())
		{
			top = b;
			bot = a;
		}
		String w1s = "";
		String w2s = "";
		String we = "";
		String br = "<br>";
		String vs = "vs";
		String r1 = sm1 + top.getEloDisplay() + sm2;
		String r2 = sm1 + bot.getEloDisplay() + sm2;
		if (isBye())
		{
			r1 = "-";
			r2 = "-";
		}
		StringBuilder sb = new StringBuilder();
		if (contains(getLoginUser()))
			sb.append("<div class='match-self'>"); 
		else
			sb.append("<div class='match-other'>");
		if (hasBeenPlayed())
		{
			we = "</div></b>";
			vs = "<span class='match-score'>" + top.getScoresString(bot) + "</span>";
			r1 = sm1 + "(" + top.getEloChange() + ") [" +top.getEloDiff() + "]" + sm2;
			r2 = sm1 + "(" + bot.getEloChange() + ") [" +bot.getEloDiff() + "]" + sm2;
			if (top.isWinner())
			{
				w1s = "<b><div class='match-win'>W: ";
			}
			else
			{
				if (top.isForfeit())
					w1s = "<b><div class='match-forfeit'>D: ";
				else
					w1s = "<b><div class='match-lose'>L: ";
			}
			if (bot.isWinner())
			{
				w2s = "<b><div class='match-win'>W: ";
			}
			else
			{
				if (bot.isForfeit())
					w2s = "<b><div class='match-forfeit'>D: ";
				else
					w2s = "<b><div class='match-lose'>L: ";
			}
		}
		sb.append(w1s);
		sb.append(top.getName(showFullNames, phones));
		if (!getLeague().isEloHidden())
		{
			sb.append(br);
			sb.append(r1);
		}
		sb.append(we);
		sb.append("<div>");
		sb.append(vs);
		sb.append("</div>");
		sb.append(w2s);
		sb.append(bot.getName(showFullNames, phones));
		if (!getLeague().isEloHidden())
		{
			sb.append(br);
			sb.append(r2);
		}
		sb.append(we);
		if (hasBeenPlayed())
		{
			sb.append("<div><small>Played on: ");
			sb.append(getPlayedDisplay());
			sb.append("</small></div>");
		}
		if (optional != null)
		{
			sb.append("<br>");
			sb.append(optional);
		}
		sb.append("</div>");
		return sb.toString();
	}

	public String getComment()
	{
		return comment;
	}

	public void setComment(String comment)
	{
		this.comment = comment.trim();
	}

	public boolean isLocked()
	{
		return locked;
	}
	
	// Return null is OK to edit.
	public String canEdit(Player p)
	{
		return getHood().canEdit(this, p, getLeague().isOrganizer(p), contains(p), getLeague().isLocked(p, this));
	}
	
	public boolean isOrganizer(Player p)
	{
		return getLeague().isOrganizer(p);
	}
	
	public boolean okToSave(Player p)
	{
		boolean ok = getLeague().okToSave(this, p);
		if (ok)
		{
			if (getLeague().includes(p))
				setRecordedBy(p);
		}
		return ok;
	}
	
	public Neighborhood getHood()
	{
		return getLeague().getHood();
	}

	public void setLocked(boolean locked)
	{
		this.locked = locked;
	}

	public Player getRecordedBy()
	{
		return recordedBy;
	}

	public void setRecordedBy(Player recordedBy)
	{
		this.recordedBy = recordedBy;
	}

	public boolean isRecorded()
	{
		return recorded;
	}
}
