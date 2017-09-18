package net.parsonsrun;

import java.io.*;
import java.util.*;
import java.util.concurrent.Callable;

import net.parsonsrun.domain.*;

public class DataAccess
{
	protected Neighborhood hood;
	protected String file;
	protected BufferedReader reader;
	String line;
	String [] splits;
	
	public enum Command { READ_USERS };
	
	public DataAccess(Neighborhood h, String f)
	{
		hood = h;
		file = f;
	}
	
	public void exec(Command cmd)
	{
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
			return ! line.isEmpty();
		}
		
		protected void split()
		{
			splits = line.split(" ");
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

				hood.addPlayer(p);
				//System.out.println("Loading " + p);
			}
		}
		
		protected void readTeams() throws IOException
		{
			for (Team t : readTeamList())
			{
				hood.addTeam(t);
				//System.out.println("Loading " + t);
			}
		}
		protected ArrayList<Team> readTeamList() throws IOException
		{
			ArrayList<Team> tms = new ArrayList<Team>();
			while (next())
			{
				splitt();
				Player p1 = hood.getPlayer(splits[0]);
				Player p2 = hood.getPlayer(splits[1]);
				Doubles t = new Doubles();
				t.setPlayers(p1, p2);
				tms.add(t);
			}
			return tms;
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
					Team lgt = hood.getDoubles(t);
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
