package net.parsonsrun.domain;import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

import net.parsonsrun.ParsonsRunUI;

public class Challenge extends LadderEvent
{
	private static final long serialVersionUID = 1L;
	Ladder ladder;
	Rung challenger;
	Rung challengee;
	Match match;
	Date challenged;
	boolean causedChange = false;		// Order changed in ladder
	boolean successful = false;			// challenger won
	boolean inverted = false;			// if challenger was above challengee
	
	public static Challenge makeChallenge(Ladder l, Rung me, Rung other)
	{
		Challenge c = new Challenge();
		c.setLadder(l);
		c.setChallenger(me);
		c.setChallengee(other);
		c.setChallenged(new Date());
		me.setPending(c);
		other.setPending(c);
		c.makeMatch();
		return c;
	}
	
	public String eventInfo(Team t)
	{
		if (isComplete())
		{
			String challengerVerb = isSuccessfulChallenge() ? " (beat " : " (lost to ";
			String challengeeVerb = (!isSuccessfulChallenge()) ? " (def " : " (lost to ";
			if (challenger.getTeam().equals(t))
			{
				return challengerVerb + challengee.getTeam().getName() + ")";
			}
			else
				return challengeeVerb + challenger.getTeam().getName() + ")";
		}
		else
			if (challenger.getTeam().equals(t))
			{
				return " (challenging " + challengee.getTeam().getName() + ")";
			}
			else
				return " (challenged by " + challenger.getTeam().getName() + ")";
	}
	
	public String detailedEventInfo(Team t)
	{
		StringBuilder s = new StringBuilder();
		s.append("<div>");
		if (isComplete())
		{
			s.append("<div class='label-centered'>");
			if (isSuccessfulChallenge())
			{
				if (challengee.contains(t))
				{
					s.append(challengee.getFirstName());
					s.append(" was challenged by ");
					s.append(challenger.getFirstName());
					s.append(" and lost.<br>");
					if (hasCausedChange())
					{
						s.append(challengee.getFirstName());
						s.append(" moved down.<br>");
					}
					else
						s.append("No change in positions.<br>");
				}
				if (challenger.contains(t))
				{
					s.append(challenger.getFirstName());
					s.append(" challenged ");
					s.append(challengee.getFirstName());
					s.append(" and won!<br>");
					if (hasCausedChange())
					{
						s.append(challenger.getFirstName());
						s.append(" moved up.<br>");
					}
					else
						s.append("No change in positions.<br>");
				}
			}
			else
			{
				if (challengee.contains(t))
				{
					s.append(challengee.getFirstName());
					s.append(" was challenged by ");
					s.append(challenger.getFirstName());
					s.append(" and won!<br>");
					if (hasCausedChange())
					{
						s.append(challengee.getFirstName());
						s.append(" moved up.<br>");
					}
					else
						s.append("No change in positions.<br>");
				}
				if (challenger.contains(t))
				{
					s.append(challenger.getFirstName());
					s.append(" challenged ");
					s.append(challengee.getFirstName());
					s.append(" and lost.<br>");
					if (hasCausedChange())
					{
						s.append(challenger.getFirstName());
						s.append(" moved down.<br>");
					}
					else
						s.append("No change in positions.<br>");
				}
			}
			s.append("</div>");
			s.append(getMatch().getMobileHtml());
		}
		else
		{
			s.append(printDate());
			s.append(": ");
			if (challenger.getTeam().equals(t))
			{
				s.append("challenging ");
				s.append(challengee.getTeam().getName());
				s.append("<br>");
			}
			else
			{
				s.append("challenged by ");
				s.append(challenger.getTeam().getName());
			}
		}
		s.append("</div>");
		return s.toString();
	}

	public String pendingTeamInfo(Team t)
	{
		if (challenger.getTeam().equals(t))
		{
			return challengee.getTeam().getName();
		}
		else
			return challenger.getTeam().getName();
	}

	public void applyMatchResults()
	{   
		challenger.clearPending();
		challengee.clearPending();
		successful = match.isWinner(challenger.getTeam());
		if (successful && isNormal())	
		{
			causedChange = true;
			ladder.applySuccessfulChallenge(this);
		}
		else if (!successful && isInverted())	
		{
			causedChange = true;
			ladder.applySuccessfulChallenge(this);
		}
		else
		{
			challenger.getNext(this);
			challengee.getNext(this);
		}
	}
	
	public boolean isNormal()
	{
		return !isInverted();
	}
	
	public boolean contains(Match m)
	{
		return match == m;
	}
	
	public boolean contains(Team t)
	{
		return getChallenger().contains(t)  || getChallengee().contains(t);
	}
	
	public Match makeMatch()
	{
		setInverted(challenger.getPosition() < challengee.getPosition());
		match = new Match(ladder);
		match.setTeamA(challengee.getTeam());
		match.setTeamB(challenger.getTeam());
		return match;
	}
	
	public String toString()
	{
		if (match == null)
			return challenger.getName() + " challenged " + challengee.getName();
		else
			return "" + match + (causedChange ? " Caused swap!" : "");
	}

	
	public boolean isChallenge()
	{
		return true;
	}
	
	public boolean isSuccessfulChallenge()
	{
		return successful;
	}

	public Rung getChallenger()
	{
		return challenger;
	}
	public void setChallenger(Rung challenger)
	{
		this.challenger = challenger;
	}
	public Rung getChallengee()
	{
		return challengee;
	}
	
	public Rung getLower()
	{
		return isInverted() ? getChallengee() : getChallenger();
	}
	
	public Rung getUpper()
	{
		return isInverted() ? getChallenger() : getChallengee();
	}
	public void setChallengee(Rung challengee)
	{
		this.challengee = challengee;
	}
	public Match getMatch()
	{
		return match;
	}
	public void setMatch(Match match)
	{
		this.match = match;
	}
	public Date getChallenged()
	{
		return challenged;
	}
	public void setChallenged(Date challenged)
	{
		this.challenged = challenged;
	}
	
	public boolean isComplete()
	{
		return getMatch().hasBeenPlayed();
	}

	public League getLadder()
	{
		return ladder;
	}

	public void setLadder(Ladder ladder)
	{
		this.ladder = ladder;
	}

	public boolean isInverted()
	{
		return inverted;
	}

	public void setInverted(boolean inverted)
	{
		this.inverted = inverted;
	}

	public boolean hasCausedChange()
	{
		return causedChange;
	}
}
