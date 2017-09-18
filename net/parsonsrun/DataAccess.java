package net.parsonsrun;

import java.io.*;
import java.util.*;
import java.util.concurrent.Callable;

import net.parsonsrun.domain.*;

public class DataAccess
{
	public static final String USERS = "Users";
	public static final String TEAMS = "Teams";
	public static final String TOURNAMENT = "Tournament";
	public static final String PERSONAL_TOURNAMENT = "PersonalTournament";
	public static final String PICTURES = "Pictures";
	
	protected Neighborhood hood;
	protected String file;
	protected BufferedReader reader;
	protected String line;
	protected String [] splits;
	protected ArrayList<Player> players = new ArrayList<Player>();
	protected ArrayList<Team> teams = new ArrayList<Team>();
	
	public enum Command { READ_USERS, IMPORT_MANUAL };
	
	public DataAccess(Neighborhood h, String f)
	{
		hood = h;
		file = f;
	}
	
	public void exec(Command cmd)
	{
		if (new File(file).exists())
			try
			{
				try
				{
					open();
					switch (cmd) 
					{
						case READ_USERS : 
							readUsers();
							readTeams();
							readLeagues();
							readExternalTeams();
							break;
						case IMPORT_MANUAL :
							importManual();
							break;
					}
				}
				finally
				{
					close();
				}
			}
			catch (Exception e)
			{
				System.out.println("Unable to import " + e);
				e.printStackTrace();
			}
	}
	
protected boolean next() throws IOException
		{
			line = reader.readLine();
			if (line == null)
				return false;
			line = line.trim();
			Utils.debug("Import: " + line);
			return ! line.isEmpty();
		}

protected void importManual() throws IOException
{
	while (next())
	{
		if (USERS.equalsIgnoreCase(line))
		{
			readUsers();
		}
		if (TOURNAMENT.equalsIgnoreCase(line))
		{
			readTournament(new Tournament());
		}
		if (PERSONAL_TOURNAMENT.equalsIgnoreCase(line))
		{
			readTournament(new PersonalTournament());
		}
		if (PICTURES.equalsIgnoreCase(line))
		{
			readPictures();
		}
	}
}

protected void readTournament(Tournament t) throws IOException
{
	Player p1;
	Player p2;
	Team t1;
	Team t2;
	if (!next()) return;
	t.setName(line);
	if (!next()) return;
	Player org = hood.getPlayer(line);
	Utils.debug("Organizer: " + org);
	if (org != null)
		t.addOrganizer(org);
	while (next())
	{
		Round r = new Round();
		t.addRound(r);
		r.setName(line);
		r.setWeeksLeft(1);
		while (next())
		{
			Match m = new Match(t);
			if ("bye".equalsIgnoreCase(line))
			{
				t1 = new ByeTeam();
				t2 = new ByeTeam();
			}
			else
			{
				splitt();
				String save[] = splits;
				if ("bye".equalsIgnoreCase(save[0]))
				{
					t1 = new ByeTeam();
				}
				else
				{
					splitd(save[0]);
					Player t1p1 = getPlayer(splits[0]);
					Player t1p2 = getPlayer(splits[1]);
					t1 = getTeam(t1p1, t1p2);
				}
				if ("bye".equalsIgnoreCase(save[1]))
				{
					t2 = new ByeTeam();
				}
				else
				{
					splitd(save[1]);
					Player t2p1 = getPlayer(splits[0]);
					Player t2p2 = getPlayer(splits[1]);
					t2 = getTeam(t2p1, t2p2);
				}
			}
			m.setTeamA(t1);
			m.setTeamB(t2);
			r.addMatch(m);
		}
	}
	t.createManualTournament(teams);
	hood.addLeague(t);
}

protected Team getTeam(Player p1, Player p2)
{
	Doubles team = new Doubles();
	team.setPlayers(p1, p2);
	Team t = hood.addTeam(team);
	teams.add(t);
	return t;
}
		
		protected void split()
		{
			splits = line.split(" ");
		}
		
		protected void splitx()
		{
			if (line.indexOf('-') >= 0)
			{
				splitd();
				return;
			}
			if (line.indexOf('/') >= 0)
			{
				splitt();
				return;
			}
			split();
		}
		
		protected void splitt(String s)
		{
			splits = s.split("/");
		}
		
		protected void splitd(String s)
		{
			splits = s.split("-");
		}
		
		protected void splitt()
		{
			splitt(line);
		}
		
		protected void splitd()
		{
			splitd(line);
		}

		protected void open() throws IOException
		{
			reader = new BufferedReader(new FileReader(file));
		}
		
		protected void close() throws IOException
		{
			reader.close();
		}
		
		protected void readUsers() throws IOException
		{
			while (next())
			{
				split();
				Player p = new Player();
				p.setFirst(splits[0]);
				p.setLast(splits[1]);
				p.setEmail(splits[2]);
				if ("crnormand@bellsouth.net".equals(splits[2]))
					p.setAdmin(true);
				if (splits.length > 3)
					p.encyptPassword(splits[3]);
				if (splits.length > 4)
					p.setMale(false);
				if (!hood.addPlayer(p))
					p = hood.getPlayer(null, p.getEmail());
				players.add(p);
			}
		}
		
		protected void readPictures() throws IOException
		{
			while (next())
			{
				DomainObject t = hood.getTournament(line);
				if (t == null)
				{
					Utils.println("Unknown Tournament " + line);
				}
				while (next())
				{
					splitd();
					String last = splits[1];
					Player p = hood.getPlayer(last);
					if (p == null)
						p = hood.getPlayer("crnormand@bellsouth.net");
					Picture pic = Picture.rawPicture(hood, p, t);
					pic.setFilename(Utils.pictureDirectory() + line);
					hood.addPicture(pic);
				}
			}
		}
		
		protected void readTeams() throws IOException
		{
			for (Team t : readTeamList())
			{
				teams.add(hood.addTeam(t));
			}
		}
		protected ArrayList<Doubles> readTeamList() throws IOException
		{
			ArrayList<Doubles> tms = new ArrayList<Doubles>();
			while (next())
			{
				splitx();
				Player p1 = hood.getPlayer(splits[0]);
				if (p1 == null)
				{
					p1 = getPlayer(splits[0]);
				}
				Player p2 = hood.getPlayer(splits[1]);
				if (p2 == null)
				{
					p2 = getPlayer(splits[1]);
				}
				Doubles t = new Doubles();
				t.setPlayers(p1, p2);
				tms.add(t);
			}
			return tms;
		}
		
		protected Player getPlayer(String id)
		{
			int i = Integer.parseInt(id) - 1;  // zero based index
			return players.get(i);
		}
		
		protected void readExternalTeams() throws IOException
		{
			while (next())
			{
				ExternalTeam t = new ExternalTeam();
				hood.addExternal(t);
				Player p = hood.getPlayer(line);
				t.addCaptain(p);
				while (next())
				{
					p = hood.getPlayer(line);
					t.addCaptain(p);
				}
				while (next())
				{
					split();
					p = hood.getPlayer(splits[2]);
					if (p == null)
					{
						p = new Player();
						p.setFirst(splits[0]);
						p.setLast(splits[1]);
						p.setEmail(splits[2]);
						hood.addPlayer(p);
					}
					t.addPlayer(p);
				}
			}
		}
		
		protected void readLeagues() throws IOException
		{
			Player c = hood.getPlayer("Chris Normand");
			while (next())
			{
				Tournament lg = new Tournament();
				hood.addLeague(lg);
				lg.setName(line);
				lg.addOrganizer(c);
				System.out.println("League:" + line);
				for (Team t : readTeamList())
				{
					Doubles lgt = hood.getDoubles(t);
					lg.addTeam(lgt);
				}
				while (next())
				{
					Round rnd = new Round();
					rnd.setName(line);
					//System.out.println("Rnd:" +line);
					lg.addRound(rnd);
					while (next())
					{
						split();
						boolean hasScores = splits.length > 1;
						splitt(splits[0]);
						Team t1 = lg.getTeam(splits[0]);
						Team t2 = lg.getTeam(splits[1]);
						Match m = new Match(lg);
						m.setTeamA(t1);
						m.setTeamB(t2);
						if (hasScores)
							readMatch(m);
						rnd.addMatch(m);
						m.setMatchPlayed(m.getPlayed());  // This will trigger the calculation of scores, elo, etc.
						//System.out.println(m);
					}
				}
			}
		}
		
	protected void readMatch(Match m)
	{
		split();
		String[] save = splits;
		splitt(save[1]);
		Date d = new Date(116, Integer.parseInt(splits[0]), Integer.parseInt(splits[1]));
		m.setPlayed(d);
		for (int i = 2; i < save.length ; i++)
		{
			splitd(save[i]);
			m.setScore(i-2, Integer.parseInt(splits[0]), Integer.parseInt(splits[1]));
		}
	}

	}
