package net.parsonsrun.domain;

/**
 * This class represents events that can happen to a team.   Most of the time, these will be 'matches',
 * but there could be others.
 */
public abstract class Event extends DomainObject
{
	private static final long serialVersionUID = 1L;

	public void delete()
	{
		
	}
}
