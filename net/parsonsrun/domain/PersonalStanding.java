package net.parsonsrun.domain;

public class PersonalStanding extends Standing
{
	private static final long serialVersionUID = 1L;
	
	protected Player player;

	public PersonalStanding(Tournament l, Player t)
	{
		league = l;
		player = t;
		matches = league.getMatchesFor(player);
		team = l.getHood().getSingles(t);
		gatherInfo();
	}
	
	protected boolean isWinner(Match m)
	{
		return m.isWinner(player);
	}
	
	protected int gamesWon(Match m)
	{
		return m.gamesWon(player);
	}
	
	protected int setsWon(Match m)
	{
		return m.setsWon(player);
	}
	
	public boolean contains(Match m)
	{
		return m.contains(player);
	}
	
	public String getSimpleName()
	{
		return getPlayer().getLast();
	}
	
	public String getName()
	{
		return getPlayer().lastFirstName() + getNameExtention();
	}

	public Player getPlayer()
	{
		return player;
	}
	
	public int getElo()
	{
		int sum = 0;
		int c = 0;
		for (Team t : getLeague().getTeamsFor(player))
		{
			c++;
			sum += t.getElo();
		}
		return sum / c;
	}
	
	public String getElos()
	{
		return String.valueOf(getElo());
	}

	public void setPlayer(Player player)
	{
		this.player = player;
	}
}
