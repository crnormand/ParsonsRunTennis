package net.parsonsrun.mobile;

import net.parsonsrun.domain.League;
import net.parsonsrun.domain.UpdateListener;

public abstract class LeaguesView extends MobileBaseView implements UpdateListener
{
	protected League league;
	
	public LeaguesView(League l)
	{
		league = l;
		league.addListener(this);
	}
	
	protected void finalize() throws Throwable 
	{
        try {
        	league.removeListener(this);
        }
        catch(Throwable t) {
            throw t;
        }
        finally {
            super.finalize();
        }
    }

	public League getLeague()
	{
		return league;
	}

	public void setLeague(League league)
	{
		this.league = league;
	}
}
