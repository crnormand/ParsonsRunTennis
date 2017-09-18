package net.parsonsrun.domain;

public class NotableObject extends DomainObject
{
	private static final long serialVersionUID = 1L;
	protected String note;

	public String getNote()
	{
		return note;
	}

	public void setNote(String note)
	{
		this.note = note;
	}
}
