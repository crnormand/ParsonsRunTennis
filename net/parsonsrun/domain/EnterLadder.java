package net.parsonsrun.domain;

//First entry into the bottom of the ladder.
public class EnterLadder extends LadderEvent
{
	private static final long serialVersionUID = 1L;
	public String toString()
	{
		return "Entered Ladder";
	}
	public String eventInfo(Team t)
	{
		return " (start)";
	}
	
	public boolean isLadderEntry()
	{
		return true;
	}
	public String detailedEventInfo(Team t)
	{
		return "<div class='label-centered'>" + printDate() + ": Entered the ladder</div>";
	}
}
