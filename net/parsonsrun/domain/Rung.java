package net.parsonsrun.domain;
import java.io.Serializable;
import java.util.*;

import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;

public class Rung extends DomainObject
{
	private static final long serialVersionUID = 1L;
	public static final String RUNG_SELF = "rung-self";
	public static final String RUNG_CHALLENGE = "rung-challenge";
	public static final String RUNG_PENDING = "rung-pending";
	public static final String RUNG_PENDING_SELF = "rung-pending-self";
	
	protected Ladder ladder;
	protected Team team;
	protected Challenge pending;
	protected RungState current = new RungState();
	protected ArrayList<RungState> previousStates = new ArrayList<RungState>();

	public class RungState implements Serializable
	{
		private static final long serialVersionUID = 1L;
		int position = -1;
		LadderEvent event;
		
		public int getDisplayPosition()
		{
			return position + 1;
		}
		
		public boolean isBump()
		{
			return event.isBump();
		}
		
		public boolean isLadderEntry()
		{
			return event.isLadderEntry();
		}
		
		public String toString()
		{
			return "[" + getDisplayPosition() + "] '" + event + "'";
		}
		
		public String eventInfo(Team t)
		{
			return event.eventInfo(t);
		}
		
		public String detailedEventInfo(Team t)
		{
			return event.detailedEventInfo(t);
		}
	}
	
	public RungState getCurrentRungState()
	{
		return current;
	}
	
	public boolean isLadderEntry()
	{
		return current.isLadderEntry();
	}
	
	public String getButtonLabel()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(getName());
		if (hasPending())
			sb.append(pending.eventInfo(getTeam()));
		else
			sb.append(current.eventInfo(getTeam()));
		return sb.toString();
	}
	
	public String getUpDownHtml()
	{
		RungState next = null;
		if (!previousStates.isEmpty())
			next = previousStates.get(0);

		return getUpDownHtml(current, next);
	}
	
	public boolean canChallenge(Rung r)
	{
		if (r.equals(this))
			return false;		// You can't challenge yourself
		if (r.hasPending() || hasPending())
			return false;		// Can't challenge someone with a pending challenge (or if you already have a pending challenge)
		if (isLadderEntry())
			return true;
		if (getLadder().isStrict())
		{
			int diff = getPosition() - r.getPosition();
			return diff > 0 && diff <= r.getLadder().getChallengeRungs();
		}
		return true;
	}
	
	public String getUpDownHtml(RungState cur, RungState next)
	{
		StringBuilder sb = new StringBuilder();
		String raw = "";
		int p = cur.getDisplayPosition();
		StringBuilder pos = new StringBuilder();
		pos.append(p);
		if (cur.isBump())
		{
			sb.append(FontAwesome.CARET_DOWN.getHtml());
			sb.append(" ");
			pos.append(FontAwesome.LONG_ARROW_LEFT.getHtml());
			pos.append(p-1);
		}
		else if (next != null)
		{
			if (cur.event.isSuccessfulChallenge())
			{
				int last = next.getDisplayPosition();
				if (p < last)
				{
					sb.append(FontAwesome.ARROW_UP.getHtml());
					sb.append(" ");
					raw = " style='color:darkgreen;'";
				}
				else
				{
					sb.append(FontAwesome.ARROW_DOWN.getHtml());
					sb.append(" ");
					raw = " style='color:darkred;'";
				}
				pos.append(FontAwesome.LONG_ARROW_LEFT.getHtml());
				pos.append(last);
			}
		}
		StringBuilder out = new StringBuilder();
		out.append("<div");
		out.append(raw);
		out.append(">");
		out.append(sb.toString());
		out.append(pos.toString());
		out.append("</div>");
		return out.toString();
	}
	
	public String getBackgroundClass(Rung current)
	{
		if (current == null)
			return "";
		Team t = current.getTeam();
		if (contains(t))
			return RUNG_SELF;
		if (hasPending())
			if (getPending().contains(t))
				return RUNG_PENDING_SELF;
			else
				return RUNG_PENDING;
		if (current.hasPending())
			return "";
		if (current.isLadderEntry() && !current.hasPending())
			return RUNG_CHALLENGE;
		if (current.abilityToChallenge(getPosition() + 1))
			return RUNG_CHALLENGE;
		return "";
	}
	
	protected boolean abilityToChallenge(int start)
	{
		ArrayList<Rung> rungs = getLadder().getRungs();
		int stop = Math.min(rungs.size(), start + getLadder().getChallengeRungs());
		for (int index = start ; index < stop ; index++)
		{
			Rung r = rungs.get(index);
			if (r.contains(getTeam()))
				return true;
		}
		return !getLadder().isStrict();
	}
	
	public boolean contains(Team t)
	{
		return t != null && getTeam().equals(t);
	}
	
	public boolean contains(Player p)
	{
		return p != null && getTeam().contains(p);
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		sb.append(getDisplayPosition());
		sb.append("] ");
		sb.append(team.getName());
		sb.append(" '");
		sb.append(getEvent());
		sb.append("'");
		if (pending != null)
		{
			sb.append(" Pending:");
			sb.append(pending);
		}
		for (RungState r : getPreviousStates())
		{
			sb.append("\n -> ");
			sb.append(r);
		}
		return sb.toString();
	}
	
	public String getName()
	{
		return getTeam().getName();
	}
	
	public String getFirstName()
	{
		return getTeam().getFirstName();
	}
	
	public Rung(Ladder l, Team t)
	{
		setLadder(l);
		setTeam(t);
	}
	
	public Rung getNext(LadderEvent reason)
	{
		int p = current.position;
		getPreviousStates().add(0, current);
		current = new RungState();
		current.event = reason;
		current.position = p;
		return this;
	}
	
	public int getPosition()
	{
		return current.position;
	}
	
	public int getDisplayPosition()
	{
		return current.getDisplayPosition();
	}

	
	public void setPosition(int p)
	{
		current.position = p;
	}
	public Team getTeam()
	{
		return team;
	}
	public void setTeam(Team team)
	{
		this.team = team;
	}
	public LadderEvent getEvent()
	{
		return current.event;
	}
	public void setEvent(LadderEvent e)
	{
		current.event = e;
	}
	public Ladder getLadder()
	{
		return ladder;
	}
	public void setLadder(Ladder ladder)
	{
		this.ladder = ladder;
	}
	
	public boolean hasPending()
	{
		return getPending() != null;
	}
	
	public void clearPending()
	{
		pending = null;
	}

	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (o instanceof Rung)
		{
			Rung r = (Rung)o;
			return getTeam().equals(r.getTeam());
		}
		return false;
	}

	public Challenge getPending()
	{
		return pending;
	}

	public void setPending(Challenge pending)
	{
		this.pending = pending;
	}

	public ArrayList<RungState> getPreviousStates()
	{
		return previousStates;
	}

}
