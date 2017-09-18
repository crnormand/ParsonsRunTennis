package net.parsonsrun.domain;

import java.util.ArrayList;

import net.parsonsrun.ParsonsRunUI;

public class Doubles extends Team
{
	private static final long serialVersionUID = 1L;

	Player a;
	Player b;
	
	public void setPlayers(Player p1, Player p2)
	{
		if (p1.isBefore(p2))
		{
			a = p1;
			b = p2;
		}
		else
		{
			b = p1;
			a = p2;
		}
	}
	
	public String getFirstName()
	{
		return a.getFirst() + "/" + b.getFirst();
	}
	
	public String getPhonesHtml()
	{
		String pa = "Unknown";
		if (!a.getPhone().isEmpty())
			pa = a.getPhoneDisplay();
		String pb = "Unknown";
		if (!b.getPhone().isEmpty())
			pb = b.getPhoneDisplay();
		return a.firstLastName() + " <span style='font-size: 0.75em;'>(" + pa + ")</span> / " + 
					b.firstLastName() + " <span style='font-size: 0.75em;'>(" + pb + ")</span>";
	}
	
	public void sendEmail(ParsonsRunUI ui, String subject, String htmlMessage)
	{
		if (a != null)
			ui.sendEmail(a, subject, htmlMessage);
		if (b != null)
			ui.sendEmail(b, subject, htmlMessage);
	}

	
	public boolean isOrganizer(League lg)
	{
		return lg.isOrganizer(a) || lg.isOrganizer(b);
	}

	public ArrayList<Player> getPlayers()
	{
		ArrayList<Player> p = super.getPlayers();
		p.add(getPlayerA());
		p.add(getPlayerB());
		return p;
	}

	
	public boolean isAdmin()
	{
		return a.isAdmin() || b.isAdmin();
	}

	public void init()
	{
		a.addDoublesTeam(this);
		b.addDoublesTeam(this);
	}

	public void delete()
	{
		super.delete();
		if (a != null) a.removeTeam(this);
		if (b != null) b.removeTeam(this);
	}
	
	public Player getPlayerA()
	{
		return a;
	}
	
	public Player getPlayerB()
	{
		return b;
	}
	
	public boolean isDoubles()
	{
		return true;
	}
	
	public boolean contains(Player p)
	{
		return getPlayerA().equals(p) || getPlayerB().equals(p);
	}
	

	
	public String getName()
	{
		return getPlayerA().getIdName() + "/" + getPlayerB().getIdName();
	}
	
	public String getNameA()
	{
		return getPlayerA().getIdName();
	}
	
	public String getNameB()
	{
		return getPlayerB().getIdName();
	}
	
	public String getShortName()
	{
		return getPlayerA().getShortLast() + "/" + getPlayerB().getShortLast();
	}
	
	public String getFullName()
	{
		return getPlayerA().firstLastName() + " / " + getPlayerB().firstLastName();
	}


	
	public boolean equals(Team t)
	{
		return t != null && t.equalsDoubles(this);
	}
	
	public boolean equalsDoubles(Doubles d)
	{
		if (this == d)
			return true;
		return getPlayerA().equals(d.getPlayerA()) && getPlayerB().equals(d.getPlayerB());
	}

}
