package net.parsonsrun.domain;

public class Line extends NotableObject
{
	private static final long serialVersionUID = 1L;
	public static String[] IDENT = { "AssignA", "AssignB" };
	protected int index;
	protected Player a;
	protected boolean confirmedA;
	protected boolean rejectedA;
	protected Player b;
	protected boolean confirmedB;
	protected boolean rejectedB;
	protected boolean won;
	protected boolean lost;
	protected String score = "";
	
	public Line(int i)
	{
		setIndex(i);
	}
	
	protected void reset()
	{
		score = "";
		won = false;
		lost = false;
	}
	
	public void confirm(Player p)
	{
		if (p.equals(getA()))
			setConfirmedA(true);
		if (p.equals(getB()))
			setConfirmedB(true);
	}
	
	public boolean isConfirmed(Player p)
	{
		if (p.isForfeit())
			return true;
		if (p.equals(getA()))
			return isConfirmedA();
		if (p.equals(getB()))
			return isConfirmedB();
		return false;
	}
	
	public void reject(Player p)
	{
		if (p.equals(getA()))
			setRejectedA(true);
		if (p.equals(getB()))
			setRejectedB(true);
	}

	public Player getPartner(Player p)
	{
		if (p.equals(getA()))
			return getB();
		if (p.equals(getB()))
			return getA();
		return null;
	}
	
	public boolean hasBeenPlayed()
	{
		return isWon() || isLost();
	}
	
	public boolean canBeScored()
	{
		return isAssignedA() && isAssignedB();
	}
	
	public void clear()
	{
		a = null;
		b = null;
		won = false;
		lost = false;
		score = "";
	}
	
	public String toString()
	{
		return getAssignA() + "/" + getAssignB();
	}
	
	public boolean contains(Player p)
	{
		if (p == null)
			return false;
		if (p.equals(getA()))
			return true;
		if (p.equals(getB()))
			return true;
		return false;
	}
	
	public void addTdHtml(StringBuilder s, Player p)
	{
		s.append("<td");
		if (isWon())
			s.append(" style='background-color:#70ff70;'");
		s.append(">");
		boolean a = p != null && p.equals(getA());
		boolean b = p != null && p.equals(getB());
		if (a)
			s.append("<span style='color:red;font-weight:bold;'>");
		s.append(getPreviewA());
		if (a)
			s.append("</span>");
		s.append("/");
		if (b)
			s.append("<span style='color:red;font-weight:bold;'>");
		s.append(getPreviewB());
		if (b)
			s.append("</span>");
//		s.append(" (");
//		s.append(getScore());
//		s.append(")");
		s.append("</td>");
	}


	
	
	public Player get(String access)
	{
		if (IDENT[0].equals(access))
			return getA();
		if (IDENT[1].equals(access))
			return getB();
		return null;
	}
	
	public boolean isConfirmed(String access)
	{
		if (IDENT[0].equals(access))
			return isConfirmedA();
		if (IDENT[1].equals(access))
			return isConfirmedB();
		return false;
	}
	

	
	public void setConfirmed(String access)
	{
		if (IDENT[0].equals(access))
			setConfirmedA(true);
		if (IDENT[1].equals(access))
			setConfirmedB(true);
	}
	
	public boolean isAllConfirmed()
	{
		return isConfirmedA() && isConfirmedB();
	}
	
	public boolean isRejected(String access)
	{
		if (IDENT[0].equals(access))
			return isRejectedA();
		if (IDENT[1].equals(access))
			return isRejectedB();
		return false;
	}
	
	public void set(String access, Player pl)
	{
		if (IDENT[0].equals(access))
			setA(pl);
		if (IDENT[1].equals(access))
			setB(pl);
	}

	
	public int getIndex()
	{
		return index;
	}
	public void setIndex(int line)
	{
		index = line;
	}
	
	public Player getA()
	{
		return a;
	}
	
	public boolean hasSamePlayers(Line l)
	{
		return contains(l.getA()) && contains(l.getB());
	}
	
	public boolean equals(Object l)
	{
		if (l instanceof Line)
			return hasSamePlayers((Line)l);
		else
			return super.equals(l);
	}
	
	public int hashCode()
	{
		return (getA() == null ? 13 : getA().hashCode()) & (getB() == null ? 777 : getB().hashCode());
	}
	
	public String getAssignB()
	{
		if (getB() == null)
			return "Not assigned";
		return getB().firstLastName() + (isConfirmedB() || isRejectedB() ? "" : "*");
	}
	
	public String getAssignA()
	{
		if (!isAssignedA())
			return "Not assigned";
		return getA().firstLastName() + (isConfirmedA() || isRejectedA() ? "" : "*");
	}
	
	public String getPreviewA()
	{
		if (!isAssignedA())
			return "N/A";
		return getA().getIdName();
	}
	public String getPreviewB()
	{
		if (!isAssignedB())
			return "N/A";
		return getB().getIdName();
	}
	
	public String getAssign()
	{
		return getAssignA() + " / " + getAssignB();
	}
	
	public boolean isAssigned(String access)
	{
		if (IDENT[0].equals(access))
			return isAssignedA();
		if (IDENT[1].equals(access))
			return isAssignedB();
		return false;
	}
	
	public boolean isAssignedA()
	{
		return getA() != null;
	}
	
	public boolean isAssignedB()
	{
		return getB() != null;
	}
	public void setA(Player a)
	{
		this.a = a;
		if (a == null)
		{
			reset();	
		}
		setConfirmedA(false);
		setRejectedA(false);
	}
	public Player getB()
	{
		return b;
	}
	public void setB(Player b)
	{
		this.b = b;
		if (a == null)
		{
			reset();
		}
		setConfirmedB(false);
		setRejectedB(false);
}
	
	public void set(Player aa, Player bb)
	{
		if (aa.compareTo(bb) > 0)
		{
			a = bb;
			b = aa;
		}
		else
		{
			a = aa;
			b = bb;
		}
	}

	public boolean isWon()
	{
		return won;
	}
	
	public boolean getWon()
	{
		return isWon();
	}

	public void setWon(boolean won)
	{
		this.won = won;
	}

	public boolean isLost()
	{
		return lost || isForfeitA() || isForfeitB();
	}
	
	public boolean getLost()
	{
		return isLost();
	}

	public void setLost(boolean lost)
	{
		this.lost = lost;
	}

	public String getScore()
	{
		return score;
	}

	public void setScore(String score)
	{
		this.score = score;
	}
	
	public boolean isForfeitA()
	{
		return getA() != null && getA().isForfeit();
	}

	public boolean isForfeitB()
	{
		return getB() != null && getB().isForfeit();
	}

	public boolean isConfirmedA()
	{
		return confirmedA || isForfeitA();
	}
	

	public void setConfirmedA(boolean confirmedA)
	{
		this.confirmedA = confirmedA;
	}

	public boolean isConfirmedB()
	{
		return confirmedB || isForfeitB();
	}

	public void setConfirmedB(boolean confirmedB)
	{
		this.confirmedB = confirmedB;
	}

	public boolean isRejectedA()
	{
		return rejectedA;
	}

	public void setRejectedA(boolean rejectedA)
	{
		this.rejectedA = rejectedA;
	}

	public boolean isRejectedB()
	{
		return rejectedB;
	}

	public void setRejectedB(boolean rejectedB)
	{
		this.rejectedB = rejectedB;
	}
}
