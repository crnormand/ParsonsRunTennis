package net.parsonsrun.domain;

public class ForfeitPlayer extends Player
{
	private static final long serialVersionUID = 1L;
	
	public String getIdName()
	{
		return "FORFEIT";
	}
	
	public String lastFirstName()
	{
		return getIdName();
	}
	
	public boolean isForfeit()
	{
		return true;
	}
	
	public String firstLastName()
	{
		return getIdName();
	}
}
