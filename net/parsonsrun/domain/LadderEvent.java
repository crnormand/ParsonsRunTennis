package net.parsonsrun.domain;

import java.time.LocalDate;
import java.util.Date;

import net.parsonsrun.Utils;

public abstract class LadderEvent extends Event
{
	private static final long serialVersionUID = 1L;
	protected LocalDate date;

	public LadderEvent()
	{
		date = LocalDate.now();
	}
	
	protected String printDate()
	{
		return Utils.printDate(date);
	}
	
	public boolean isChallenge()
	{
		return false;
	}
	
	public boolean isBump()
	{
		return false;
	}
	
	public boolean isSuccessfulChallenge()
	{
		return false;
	}
	
	public boolean isLadderEntry()
	{
		return false;
	}

	public abstract String detailedEventInfo(Team t);
	public abstract String eventInfo(Team t);
}
