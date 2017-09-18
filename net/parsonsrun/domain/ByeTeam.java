package net.parsonsrun.domain;

import java.util.ArrayList;

public class ByeTeam extends Team
{
	private static final long serialVersionUID = 1L;
	public boolean equals(Team t)
	{
		return false;
	}
	
	public String getName()
	{
		return "bye";
	}
	
	public String getFirstName()
	{
		return "bye";
	}
	
	public String getPhonesHtml()
	{
		return "bye";
	}
	
	public void init()
	{
	}
	
	public boolean isOrganizer(League lg)
	{
		return false;
	}

	public boolean isAdmin()
	{
		return false;
	}

	public String getNameA()
	{
		return getName();
	}
	
	public String getNameB()
	{
		return getName();
	}
	
	public String getFullName()
	{
		return "bye";
	}
	
	public String getShortName()
	{
		return "bye";
	}

	public boolean contains(Player p)
	{
		return false;
	}
	
	public boolean isBye()
	{
		return true;
	}
}
