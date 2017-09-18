package net.parsonsrun.domain;

import java.util.ArrayList;

import net.parsonsrun.ParsonsRunUI;

public class Singles extends Team
{
	private static final long serialVersionUID = 1L;

	Player a;
	
	public Player getPlayer()
	{
		return a;
	}
	
	public String getFirstName()
	{
		return a.getFirst();
	}
	
	public boolean isAdmin()
	{
		return a.isAdmin();
	}
	
	public boolean isOrganizer(League lg)
	{
		return lg.isOrganizer(a);
	}
	
	public ArrayList<Player> getPlayers()
	{
		ArrayList<Player> p = super.getPlayers();
		p.add(getPlayer());
		return p;
	}

	
	public boolean contains(Player p)
	{
		return getPlayer().equals(p);
	}
	
	public void setPlayer(Player p)
	{
		a = p;
	}
	
	public void sendEmail(ParsonsRunUI ui, String subject, String htmlMessage)
	{
		if (a != null)
			ui.sendEmail(a, subject, htmlMessage);
	}

	public void init()
	{
		a.setSingles(this);
	}
	
	public boolean isSingles()
	{
		return true;
	}
	
	public String getName()
	{
		return getPlayer().getIdName();
	}
	
	public String getNameA()
	{
		return getName();
	}
	
	public String getNameB()
	{
		return getName();
	}

	
	public String getShortName()
	{
		return getName();
	}
	
	public String getFullName()
	{
		return getPlayer().firstLastName();
	}
		
	public String getPhonesHtml()
	{
		String p = "(Unknown)";
		if (!getPlayer().getPhone().isEmpty())
			p = getPlayer().getPhoneDisplay();
		return getPlayer().firstLastName() + " <span style='font-size: 0.75em;'>(" + p + ")</span>";
	}
	
	public String toString()
	{
		return getFullName();
	}
	public boolean equals(Team t)
	{
		return t != null && t.equalsSingles(this);
	}
	public boolean equalsSingles(Singles t)
	{
		if (this == t)
			return true;
		return getPlayer().equals(t.getPlayer());
	}

}
