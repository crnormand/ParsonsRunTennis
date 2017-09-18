package net.parsonsrun.domain;import java.util.*;

import net.parsonsrun.mobile.LadderView;
import net.parsonsrun.mobile.MobileBaseView;

public abstract class Ladder extends League
{
	private static final long serialVersionUID = 1L;
	protected ArrayList<Rung> rungs = new ArrayList<Rung>();
	protected ArrayList<Challenge> challenges = new ArrayList<Challenge>();
	protected ArrayList<Challenge> pending = new ArrayList<Challenge>();
	protected int challengeRungs = 3;
	protected boolean strict;
	public ArrayList<Rung> getRungs()
	{
		return rungs;
	}
	public ArrayList<Challenge> getChallenges()
	{
		return challenges;
	}
		
	public Rung addNewTeam(Team t)
	{
		Rung r = new Rung(this, t);
		r.setEvent(new EnterLadder());
		addRung(r);
		updateListeners();
		return r;
	}
	
	public boolean contains(Player p)
	{
		for (Rung r : getRungs())
		{
			if (r.contains(p))
				return true;
		}
		return false;
	}
	public boolean isLadder()
	{
		return true;
	}
	
	protected void addRung(Rung r)
	{
		addTeam(r.getTeam());
		r.setPosition(getRungs().size());
		getRungs().add(r);
	}
	
	public Rung getRungFor(Team t)
	{
		if (t == null)
			return null;
		for (Rung r : getRungs())
		{
			if (r.contains(t))
				return r;
		}
		return null;
	}
	
	public void addPendingChallenge(Challenge c)
	{
		getPending().add(c);
	}
	
	public Challenge createChallenge(Rung challenger, Rung challengee)
	{
		Challenge c = Challenge.makeChallenge(this, challenger, challengee);
		addPendingChallenge(c);
		updateListeners();
		return c;
	}
	
	public Challenge createChallenge(Team challenger, Rung challengee)
	{
		for (Rung r : getRungs())
		{
			if (r.contains(challenger))
			{
				return createChallenge(r, challengee);
			}
		}
		throw new RuntimeException("Tried to create a challenge for a team that is not on the ladder: " + challenger);
	}
	
	public boolean hasPending(Team t)
	{
		for (Rung r : getRungs())
		{
			if (r.contains(t))
				return r.hasPending();
		}
		return false;	
	}
	
	// Search pending challenges to find and apply.
	public void addMatch(Match m)
	{
		Challenge saved = null;
		for (Challenge c : getPending())
		{
			if (c.contains(m))
			{
				saved = c;
			}
		}
		if (saved == null)
		{
			throw new RuntimeException("Tring to add a Match that was not part of a challenge");
		}
		else
		{
			getPending().remove(saved);
			getChallenges().add(saved);
			saved.applyMatchResults();
		}
		super.addMatch(m);		// Must do last so that updateListeners() is called after all changes are made.
	}
	
	// The challenge was successful, modify the ladder
	public void applySuccessfulChallenge(Challenge c)
	{
		Rung lower = c.getLower();
		int bottom = lower.getPosition();	
		Rung upper = c.getUpper();
		int top = upper.getPosition();
		LadderBump bump = new LadderBump(c);
		ArrayList<Rung> rungs = getRungs();
		if (bottom - top > 1)
		{
			for (int i = bottom - 1; i >= top + 1; i--)
			{
				Rung r = rungs.get(i);
				setRung(i+1, r.getNext(bump));
			}
		}
		setRung(top, lower.getNext(c));
		setRung(top + 1, upper.getNext(c));
	}
	
	protected void setRung(int i, Rung r)
	{
		getRungs().set(i, r);
		r.setPosition(i);
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Ladder: " + getName());
//		for (Rung r : getRungs())
//		{
//			sb.append("\n");
//			sb.append(r);
//		}
		ArrayList<Rung> prev = new ArrayList<Rung>();
		for (Rung r : getRungs())
		{
			if (prev.contains(r))
			{
				System.out.println("ERROR in Ladder, appears more than once: " + prev);
			}
			prev.add(r);
		}
		return sb.toString();
	}
	public ArrayList<Challenge> getPending()
	{
		return pending;
	}
	public abstract boolean isSingles();
	
	public boolean isDoubles()
	{
		return !isSingles();
	}
	public int getChallengeRungs()
	{
		return challengeRungs;
	}
	public void setChallengeRungs(int challengeRungs)
	{
		this.challengeRungs = challengeRungs;
	}
	public boolean isStrict()
	{
		return strict;
	}
	public void setStrict(boolean strict)
	{
		this.strict = strict;
	}
	
}
