package net.parsonsrun.domain;

// Position in ladder moved because they were "bumped" down
public class LadderBump extends LadderEvent
{
	private static final long serialVersionUID = 1L;
	Challenge event;
	
	public LadderBump(Challenge c)
	{
		event = c;
	}
	public String toString()
	{
		return "Bumpped down due to " + event;
	}

	public boolean isBump()
	{
		return true;
	}
	public String eventInfo(Team t)
	{
		return " (bumped by " + event.challenger.getName() + ")";
	}
	public String detailedEventInfo(Team t)
	{
		return "<div class='label-centered'>" + printDate() + ": Bumped by " + event.challenger.getName() + "</div>";
	}

}
