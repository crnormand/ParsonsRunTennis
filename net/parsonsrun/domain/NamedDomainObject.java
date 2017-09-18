package net.parsonsrun.domain;

public class NamedDomainObject extends DomainObject
{
	private static final long serialVersionUID = 1L;

	String name;
	
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

}
