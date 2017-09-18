package net.parsonsrun.domain;import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.mail.Address;

import com.vaadin.ui.Notification;

import net.parsonsrun.ParsonsRunUI;
import net.parsonsrun.Tester;
import net.parsonsrun.Tuple;
import net.parsonsrun.Utils;

public class Neighborhood extends NamedDomainObject
{
	private static final long serialVersionUID = 1L;
	
	public static Neighborhood singleton;
	
	protected ArrayList<Player> players = new ArrayList<Player>();
	protected ArrayList<Singles> singles = new ArrayList<Singles>();
	protected ArrayList<Doubles> doubles = new ArrayList<Doubles>();
	protected ArrayList<Tournament> tournaments = new ArrayList<Tournament>();
	protected ArrayList<Ladder> ladders = new ArrayList<Ladder>();
	protected ArrayList<ExternalTeam> externals = new ArrayList<ExternalTeam>();
	protected ArrayList<Picture> pictures;
	
	protected ArrayList<Tuple<Player, Date>> recentUsers;
	
	protected long globalId = 0;
	protected long pictureId = 0;
	
	protected transient HashMap<Match, Player> editing;

	public static Neighborhood getSingleton()
	{
		if (singleton == null)
		{
			singleton = Utils.loadData();
		}
		return singleton;
	}
	
	public synchronized ArrayList<Tuple<Player, Date>> getRecentUsers()
	{
		if (recentUsers == null)
		{
			recentUsers = new ArrayList<Tuple<Player, Date>>();
		}
		return recentUsers;
	}
	
	public synchronized void addLoginUser(Player p)
	{
		if (!getRecentUsers().isEmpty())
		{
			Tuple<Player, Date> t = getRecentUsers().get(getRecentUsers().size() - 1);
			if (t.getLeft().equals(p))
				return;
		}
		getRecentUsers().add(new Tuple(p, new Date()));
		if (getRecentUsers().size() > 100)
		{
			getRecentUsers().remove(0);
		}
		updateListeners();
	}
	
	public synchronized Picture getNextPicture(Picture current, DomainObject assoc, int delta)
	{
		ArrayList<Picture> list = getPictures(assoc);
		int i = list.indexOf(current) + delta;
		if (i < 0)
			i = list.size() - 1;
		if (i >= list.size())
			i = 0;
		return list.get(i);
	}
	
	public ArrayList<Picture> getPictures(DomainObject assoc)
	{
		ArrayList<Picture> list = new ArrayList<Picture>();
		getPictures().forEach(p -> {
			if (assoc == null || assoc.equals(p.getAssociation()))
				list.add(p);
		});
		return list;
	}
	
	public synchronized Picture deletePicture(Picture current)
	{
		current.delete();
		int i = getPictures().indexOf(current);
		getPictures().remove(current);
		if (i >= getPictures().size())
			i = 0;
		return getPictures().isEmpty() ? null : getPictures().get(i);
}
	
	public static void resetDB()
	{
		singleton = null;
	}
	
	public synchronized String canEdit(Match m, Player p1, boolean org, boolean member, boolean locked)
	{
		if (!p1.isAdmin())
		{
			if (!member && !org)
				return "You do not have the privileges to edit this match";
			if (member && locked)
				return "The match is locked";
		}	
		if (editing == null)
		{
			editing = new HashMap<Match, Player>();
		}
		Player p2 = editing.get(m);
		//Utils.println(p1 + " wants to edit " + m + " Prev:" + p2);
		if (p2 == null || p2.equals(p1))
		{
			editing.put(m, p1);
			ParsonsRunUI.getScheduler().schedule(() -> finishedEditing(m), 5, TimeUnit.MINUTES);
			return null;
		}
		return p2.firstLastName() + " is currently editing this match.";
	}
	
	public synchronized boolean okToSave(Match m, Player p1)
	{
		if (editing != null)
		{
			Player prev = editing.get(m);
			boolean ok =  p1.equals(prev);
			//Utils.println(p1 + " want to save: " + m + " OK:" + ok + " Prev:" + prev);
			if (ok)
				finishedEditing(m);
			return ok;
		}
		return false;
	}
	
	public synchronized void finishedEditing(Match m)
	{
		Object o = editing.remove(m);
		//Utils.println("Finished editing:" + m + " Prev:" + o);
	}
	
	public void processHourly(Date d)
	{
		//Utils.println("processHourly: " + d);
	}
	
	public void processNightly(Date d)
	{
		Utils.println("processNightly: " + d);
		Utils.backupData();
	}
	
	
	public void processFriday1am(Date d)
	{
		Utils.println("processFriday1am: " + d);
		getTournaments().forEach(t -> t.processFriday1am(d));
	}
	
	public void processSunday1am(Date d)
	{
		Utils.println("processSunday1am: " + d);
	}
	
	public void processSundayMidnight(Date d)
	{
		Utils.println("processSundayMidnight: " + d);
		getTournaments().forEach(t -> t.processSundayMidnight(d));
	}
	
	public void processMonday1am(Date d)
	{
		Utils.println("processMonday1am: " + d);
		getTournaments().forEach(t -> t.processMonday1am(d));
	}
	
	public synchronized long getNextGlobalId()
	{
		return ++globalId;
	}
	
	public synchronized long getNextPictureId()
	{
		return ++pictureId;
	}
	
	public ArrayList<Doubles> getDoubles()
	{
		return doubles;
	}
	
	public String emailReferenceFor(Player p)
	{
		if (p == null)
			return "";
		String id = p.getGlobalIdString();
		return "<br><br><span style='font-size: 40%;'>" + Utils.EMAIL_REF + Utils.encodeBase64(id) + " </span>";
	}
	
	public Player playerForReference(String ref)
	{
		int start = ref.indexOf(Utils.EMAIL_REF);
		if (start >= 0)
		{
			int end = ref.indexOf(" ", start + Utils.EMAIL_REF.length());
			if (end < start)
				end = ref.length() - 1;
			Utils.debug("Player for ref, start:" + start + " end:" + end + "\nRAW:" + ref);
			String id = ref.substring(start + Utils.EMAIL_REF.length(), end);
			Utils.debug("Player for ref id:" + id);
			return getPlayer(Long.parseLong(Utils.decodeBase64(id)));
		}
		return null;
	}
	
	public Player playerForAddresses(Address[] addrs)
	{
		if (addrs.length == 0)
			return null;
		return getPlayer(addrs[0].toString());
	}
	
	public List<Ladder> getDoublesLadders()
	{
		return getLadders().stream().filter(l -> l.isDoubles()).collect(Collectors.toList());
	}
	
	public List<Ladder> getDoublesLadders(Player p)
	{
		if (p != null && p.isOnlyShowTournament())
			return getDoublesLadders().stream().filter(t -> t.contains(p)).collect(Collectors.toCollection(ArrayList::new));
		else
			return getDoublesLadders();
	}
	
	public List<Ladder> getSinglesLadders()
	{
		return getLadders().stream().filter(l -> l.isSingles()).collect(Collectors.toList());
	}
	
	public List<Ladder> getSinglesLadders(Player p)
	{
		if (p != null && p.isOnlyShowTournament())
			return getSinglesLadders().stream().filter(t -> t.contains(p)).collect(Collectors.toCollection(ArrayList::new));
		else
			return getSinglesLadders();
	}

	public ArrayList<Singles> getSingles()
	{
		return singles;
	}

	
	public Player getLoginPlayer(String email, String password)
	{
		String e = (email == null ? "" : email.trim());
		String pw = (password == null ? null : password.trim());
		for (Player p : getPlayers())
		{
			if (p.matches(e, pw))
				return p;
		}
		return null;
	}


	// Do we need to reset any data after it has been loaded from the disk?
	public void postLoad()
	{
		
	}
	
	public Player getPlayer(Player orig, String s)
	{
		for (Player p : getPlayers())
		{
			if (p == orig)
				return p;
			if (p.getEmail().equalsIgnoreCase(s))
				return p;
			if (p.firstLastName().equalsIgnoreCase(s))
				return p;
			if (p.getLast().equalsIgnoreCase(s))
				return p;
		}
		return null;
	}
	
	public boolean containsPlayer(String email)
	{
		return getPlayer(email) != null;
	}
	
	
	public synchronized void deletePlayer(Player player)
	{
		getPlayers().remove(player);
		singles.removeIf(p -> p.contains(player));
		doubles.removeIf(p -> p.contains(player));
		tournaments.stream().forEach(t -> t.removePlayer(player));
		ladders.stream().forEach(t -> t.removePlayer(player));
		externals.stream().forEach(t -> t.removePlayer(player));
	}
	
	public Player getPlayer(String n)
	{
		return getPlayer(null, n);
	}
	
	public Player getPlayer(long i)
	{
		for (Player p : getPlayers())
			if (p.getGlobalId() == i)
				return p;
		return null;
	}
	
	public ExternalTeam getExternalTeam(long i)
	{
		for (ExternalTeam p : getExternals())
			if (p.getGlobalId() == i)
				return p;
		return null;
		
	}
	
	public Tournament getTournament(long i)
	{
		for (Tournament t : getTournaments())
			if (t.getGlobalId() == i)
				return t;
		return null;
	}
	
	public Tournament getTournament(String name)
	{
		for (Tournament t : getTournaments())
			if (t.getName().equalsIgnoreCase(name))
				return t;
		return null;
	}

	public synchronized boolean addPlayer(Player p)
	{
		if (getPlayer(p, p.getEmail()) == null)
		{
			p.setGlobalId(getNextGlobalId());
			Utils.println("New Player created: " + p);
			markDuplicateLastName(p);
			getPlayers().add(p);
			sortPlayers();
			// Always create "Singles" team for a Player
			Singles tm = new Singles();
			tm.setPlayer(p);
			addTeam(tm);
			return true;
		}
		return false;
	}
	
	public void sortPlayers()
	{
		Collections.sort(getPlayers());
	}
	
	protected void markDuplicateLastName(Player newp)
	{
		for (Player p : getPlayers())
		{
			if (p.getLast().equals(newp.getLast()))
			{
				p.setShowInitial(true);
				newp.setShowInitial(true);
			}
		}
	}
	
	public synchronized Team addTeam(Team tm)
	{
		if (tm.isDoubles())
		{
			Doubles d = (Doubles)tm;
			Doubles existing = getDoubles(d);
			if (existing == null)
			{
				tm.setGlobalId(getNextGlobalId());
				tm.init();
				getDoubles().add(d);
				return d;
			}
			return existing;
		}
		if (tm.isSingles())
		{
			Singles s = (Singles)tm;
			Singles existing = getSingles(s);
			if (existing == null)
			{
				tm.setGlobalId(getNextGlobalId());
				tm.init();
				getSingles().add(s);
				return s;
			}
			return existing;		
		}
		throw new RuntimeException("Unknown team type");
	}
	

	
	public Doubles getDoubles(Team tm)
	{
		for (Doubles t : getDoubles())
		{
			if (t.equals(tm))
				return t;
		}
		return null;
	}
	
	public Singles getSingles(Team tm)
	{
		for (Singles t : getSingles())
		{
			if (t.equals(tm))
				return t;
		}
		return null;
	}
	
	public Team getSingles(Player p)
	{
		for (Team t : getSingles())
		{
			if (t.contains(p))
				return t;
		}
		return null;
	}
	
	public Team getTeam(String s)
	{
		for (Team t : getDoubles())
		{
			if (t.getName().equalsIgnoreCase(s))
				return t;
		}
		int i = 0;
		try
		{
			i = Integer.parseInt(s);
		} catch (NumberFormatException e) {}
		
		if (i > 0)
		{
			return getDoubles().get(i - 1);
		}
		ByeTeam b = new ByeTeam();
		if (b.getName().equalsIgnoreCase(s))
			return b;
		return null;
	}
	
	public synchronized void addLeague(League l)
	{
		l.setGlobalId(getNextGlobalId());
		l.setHood(this);
		if (l.isLadder())
			getLadders().add((Ladder)l);
		if (l.isTournament())
			getTournaments().add((Tournament)l);
		updateListeners();
	}
	
	public synchronized void removeLadder(League l)
	{
		getLadders().remove(l);
		l.delete();
		updateListeners();
	}
	
	public synchronized void removeTournament(League t)
	{
		getTournaments().remove(t);
		t.delete();
		updateListeners();
	}
	
	public synchronized void addExternal(ExternalTeam t)
	{
		t.setGlobalId(getNextGlobalId());
		getExternals().add(t);
	}
	
	public void removeExternal(ExternalTeam t)
	{
		getExternals().remove(t);
	}

	public ArrayList<Player> getPlayers()
	{
		return players;
	}


	public ArrayList<Tournament> getTournaments()
	{
		return tournaments;
	}
	
	public ArrayList<Tournament> getTournaments(Player p)
	{
		if (p != null && p.isOnlyShowTournament())
			return getTournaments().stream().filter(t -> t.contains(p)).collect(Collectors.toCollection(ArrayList::new));
		else
			return getTournaments();
	}
	
	public ArrayList<Ladder> getLadders()
	{
		return ladders;
	}
	
	public ArrayList<Ladder> getLadders(Player p)
	{
		if (p != null && p.isOnlyShowLadder())
			return getLadders().stream().filter(t -> t.contains(p)).collect(Collectors.toCollection(ArrayList::new));
		else
			return getLadders();
	}
	

	public ArrayList<ExternalTeam> getExternals()
	{
		return externals;
	}
	
	public ArrayList<ExternalTeam> getExternals(Player p)
	{
		if (p != null && p.isOnlyShowTeam())
			return getExternals().stream().filter(t -> t.includes(p)).collect(Collectors.toCollection(ArrayList::new));
		else
			return getExternals();
	}

	protected synchronized ArrayList<Picture> getPictures()
	{
		if (pictures == null)
		{
			pictures =  new ArrayList<Picture>();
		}
		return pictures;
	}
	
	public Picture getPicture(DomainObject assoc, int i)
	{
		return getPictures(assoc).get(i);
	}
	
	public synchronized void addPicture(Picture p)
	{
		getPictures().add(p);
		Utils.println("Picture: " + p.getFilename() + (p.getNote().isEmpty() ? "" : " '" + p.getNote() + "'"));
	}
}
