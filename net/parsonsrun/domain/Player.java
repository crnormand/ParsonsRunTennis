package net.parsonsrun.domain;

import java.util.*;

import net.parsonsrun.Utils;

public class Player extends DomainObject implements Comparable<Player>
{
	private static final long serialVersionUID = 1L;
	public static final String SYSTEM_EMAIL = "parsonsruntennis@gmail.com";
	public static final String SYSTEM_EMAIL_PASSWORD = "11055lbl415ncd";
	public static Player SYSTEM = new Player().init("ParsonsRun", "Tennis", SYSTEM_EMAIL, SYSTEM_EMAIL_PASSWORD);

	protected String first = "";
	protected String last = "";
	protected String email = "";
	protected String passwordHash = Utils.cryptoHash("");   // Default password is blank
	protected String phone = "";
	protected boolean isMale = true;
	protected boolean isAdmin = false;
	protected boolean showInitial = false;
	protected boolean onlyShowTeam = false;
	protected boolean onlyShowLadder = false;
	protected boolean onlyShowTournament = false;
	
	protected ArrayList<Doubles> teams = new ArrayList<Doubles>();
	protected Singles singles;
	
	public Player init(String f, String l, String e, String plainTextPassword)
	{
		setFirst(f);
		setLast(l);
		setEmail(e);
		encyptPassword(plainTextPassword);
		return this;
	}
	
	public int compareTo(Player p)
	{
		return lastFirstName().compareToIgnoreCase(p.lastFirstName());
	}
	
	public boolean matches(String e, String pw)
	{
		if (getEmail().equalsIgnoreCase(e))
		{	// If password is null, then we can ignore it (for re-logins)
			return pw == null || Utils.cryptoHash(pw).equals(passwordHash);
		}
		return false;
	}
	
	public boolean isForfeit()
	{
		return false;
	}
	
	public String getFirst()
	{
		return first;
	}
	
	public char getFirstChar()
	{
		return getFirst().isEmpty() ? '-' : getFirst().charAt(0);
	}
	
	public char getLastChar()
	{
		return getLast().isEmpty() ? '-' : getLast().charAt(0);
	}
	public String getIdName()
	{
		if (getShowInitial())
		{
			return getFirstChar() + " " + getLast();
		}
		else
			return getLast();
	}
	
	public boolean hasPassword()
	{
		return passwordHash != null;
	}
	public void setFirst(String first)
	{
		this.first = first;
	}
	public String getLast()
	{
		return last;
	}
	
	public String toString()
	{
		return lastFirstName();
	}
	
	public boolean equals(Player p)
	{
		if (p == null)
			return false;
		return getEmail().equalsIgnoreCase(p.getEmail());
	}
	
	public boolean isBefore(Player other)
	{
		return lastFirstName().compareToIgnoreCase(other.lastFirstName()) < 0;
	}
	
	public String firstLastName()
	{
		if (getLast().isEmpty())
			return getEmail();
		return getFirst() + " " + getLast();
	}
	
	public String getShortLast()
	{
		return getShortLast(6);
	}
	
	public String getShortLast(int i)
	{
		int l = Math.min(getLast().length(), i);
		return getLast().substring(0, l);
	}
	
	public String lastFirstName()
	{
		if (getLast().isEmpty())
			return getEmail();
		return getLast() + ", " + getFirst();
	}
	
	public String getLastFirstName()
	{
		return lastFirstName();
	}
	public void setLast(String last)
	{
		this.last = last;
	}
	public String getEmail()
	{
		return email;
	}
	

	public void setEmail(String email)
	{
		this.email = email.trim().toLowerCase();
	}
	public void encyptPassword(String p)
	{
		passwordHash = Utils.cryptoHash(p);
	}
	public String getPhone()
	{
		return phone;
	}
	public void setPhone(String p)
	{
		phone = Utils.compactPhone(p);
	}
	
	public String getPhoneDisplay()
	{
		return Utils.expandPhone(getPhone());
	}

	public boolean isMale()
	{
		return isMale;
	}
	public void setMale(boolean isMale)
	{
		this.isMale = isMale;
	}
	public boolean isAdmin()
	{
		return isAdmin;
	}
	public void setAdmin(boolean isAdmin)
	{
		this.isAdmin = isAdmin;
	}
	public ArrayList<Doubles> getTeams()
	{
		return teams;
	}
	
	public void addDoublesTeam(Doubles t)
	{
		getTeams().add(t);
	}
	
	public void removeTeam(Team t)
	{
		getTeams().remove(t);
	}

	public boolean getShowInitial()
	{
		return showInitial;
	}

	public void setShowInitial(boolean showInitial)
	{
		this.showInitial = showInitial;
	}

	public Singles getSingles()
	{
		return singles;
	}

	public void setSingles(Singles singles)
	{
		this.singles = singles;
	}
	
	// Able to delete if not a member of any doubles teams
	public boolean canBeDeleted()
	{
		return getTeams().isEmpty();
	}

	public boolean isOnlyShowTeam()
	{
		return onlyShowTeam;
	}

	public void setOnlyShowTeam(boolean onlyShowTeam)
	{
		this.onlyShowTeam = onlyShowTeam;
	}

	public boolean isOnlyShowLadder()
	{
		return onlyShowLadder;
	}

	public void setOnlyShowLadder(boolean onlyShowLadder)
	{
		this.onlyShowLadder = onlyShowLadder;
	}

	public boolean isOnlyShowTournament()
	{
		return onlyShowTournament;
	}

	public void setOnlyShowTournament(boolean onlyShowTournament)
	{
		this.onlyShowTournament = onlyShowTournament;
	}
}
