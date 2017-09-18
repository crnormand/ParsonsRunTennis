package net.parsonsrun.domain;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Function;

import com.vaadin.addon.charts.model.Configuration;
import com.vaadin.addon.charts.model.ListSeries;
import com.vaadin.addon.charts.model.PlotOptionsLine;

import net.parsonsrun.ParsonsRunUI;
import net.parsonsrun.Utils;

public class Tournament extends League
{
	private static final long serialVersionUID = 1L;

	protected ArrayList<Round> rounds = new ArrayList<Round>();
	protected ArrayList<Standing> standings = new ArrayList<Standing>();
	protected HashMap<Team, Integer> latePasses;
	protected int weeksInRounds = 1;
	protected HashSet<Match> reported;
	protected boolean adjustedStandings = false;
	protected boolean ignoreStandings = false;
	
	public boolean isAdjustedStandings()
	{
		return adjustedStandings;
	}

	public void setAdjustedStandings(boolean adjustedStandings)
	{
		this.adjustedStandings = adjustedStandings;
		recalculate();
	}

	public HashSet<Match> getReported()
	{
		if (reported == null)
			reported = new HashSet<Match>();
		return reported;
	}
	
	public ArrayList<Team> getLatePassTeams()
	{
		return getTeams();
	}
	
	protected boolean hasBeenReported(Match m)
	{
		return getReported().contains(m);
	}
	
	public boolean isLocked(Player p, Match m)
	{
		return m.isLocked() && !hasLatePass(p);
	}
	
	public boolean okToSave(Match m, Player p1)
	{
		boolean ok = super.okToSave(m, p1);
		if (ok && m.isLocked() && !p1.isAdmin())
			useLatePass(p1);
		return ok;
	}
	
	public Team getLastPassTeamFor(Player p)
	{
		for (Team t : getLatePassTeams())
			if (t.contains(p))
				return t;
		return null;
	}
	
	public ArrayList<Team> getTeamsFor(Player p)
	{
		ArrayList<Team> ts = new ArrayList<Team>();
		for (Team t : getTeams())
			if (t.contains(p))
				ts.add(t);
		return ts;
	}
	
	public int indexOf(Round r)
	{
		return getRounds().indexOf(r);
	}

	public void recalculate()
	{
		super.recalculate();
		standings.clear();
		addStandings();
		Collections.sort(standings);
		for (int i = 1; i < standings.size(); i++)
		{
			Standing top = standings.get(i - 1);
			Standing bottom = standings.get(i);
			top.makeCommentVs(bottom);
		}
		updateListeners();
		Utils.debug("Recalc:" + this);
	}
	
	protected void addStandings()
	{
		for (Team t : getTeams())
		{
			standings.add(new Standing(this, t));
		}
	}
	
	protected HashMap<Team, Integer> getLatePasses()
	{
		if (latePasses == null)
			latePasses = new HashMap<Team, Integer>();
		return latePasses;
	}
	
	protected synchronized boolean hasLatePass(Player p)
	{
		return hasLatePass(getLastPassTeamFor(p));
	}
	
	public int getLatePasses(Player p)
	{
		return getLatePasses(getLastPassTeamFor(p));
	}
	
	public synchronized int getLatePasses(Team t)
	{
		if (t == null) return 0;
		Integer i = getLatePasses().get(t);
		if (i == null)
		{
			i = getNumberOfLatePasses();
			getLatePasses().put(t, i);
		}
		return i;
	}
	
	public synchronized void updateLatePass(Team t, int delta)
	{
		if (t == null) return;
		Integer i = getLatePasses().get(t);
		if (i == null)
		{
			i = getNumberOfLatePasses();
		}
		int n = i + delta;
		if (n < 0) 
			n = 0;
		getLatePasses().put(t, n);
	}
	
	protected boolean hasLatePass(Team t)
	{
		return getLatePasses(t) > 0;
	}
	
	protected void useLatePass(Team t)
	{
		int i = getLatePasses(t);
		getLatePasses().put(t, i - 1);
	}
	
	public synchronized void useLatePass(Player p)
	{
		useLatePass(getLastPassTeamFor(p));
	}

	public int getWeeksInRounds()
	{
		return weeksInRounds;
	}

	public void setWeeksInRounds(int weeksInRounds)
	{
		this.weeksInRounds = weeksInRounds;
	}

	public boolean contains(Player p)
	{
		for (Round r : getRounds())
		{
			if (r.contains(p))
				return true;
		}
		return false;
	}
	
	public void delete()
	{
		super.delete();
		getRounds().forEach(r -> r.delete());
		getRounds().clear();
		getStandings().clear();
		getLatePasses().clear();
		getReported().clear();
	}
	
	public void processSundayMidnight(Date d)
	{
		for (Round r : getRounds())
		{
			if (!r.isEnded() && r.isRoundFor(d))
			{
				r.endWeek();
				if (r.isEnded())
				{
					r.closeOut(d);
					sendRoundSummary(r);
				}
				return;
			}
		}
	}
	
	public void processFriday1am(Date d)
	{
		for (Round r : getRounds())
		{
			if (!r.isEnded() && r.isStarted() && r.isRoundFor(d))
			{
				if (r.isLastWeek())
				{
					r.sendReminders();
				}
				return;
			}
		}
	}
	
	public void processMonday1am(Date d)
	{
		for (Round r : getRounds())
		{
			if (r.isRoundFor(d) && !r.isStarted())
			{
				sendStartOfRound(r);
				return;
			}
		}
	}
	
	public void sendStartOfRound(Round current)
	{
		current.startRound();
		for (Match m : current.getMatches())
		{
			if (!m.isBye() && !m.hasBeenPlayed())
				m.sendInvite(this, current);
		}
	}
	
	public void sendRoundSummary(Round current)
	{
		if (current == null)
		{
			Utils.println("trying to send summary to NULL tourney");
			return;
		}
		int width = 500;
		boolean first = true;		
		StringBuilder sb = new StringBuilder();
		if (!isIgnoreStandings())
		{
			sb.append("<b>Current statistics:</b>");
			sb.append("<br>");
			sb.append("<table style='width: ");
			sb.append(width);
			sb.append("px; border-collapse: collapse; border: 1px solid grey'>");
			sb.append(Standing.getHeaderHtml(isEloHidden()));
			int position = 0;
			for (Standing s : getStandings())
			{
				sb.append(s.getStandingHtml(++position));
			}
			sb.append("</table>");
			sb.append(Standing.getLegendText(width, isAdjustedStandings(), isIgnoreStandings()));
			sb.append("<br><br>");
		}
		sb.append("<b>Recently scored matches:</b><hr>");
		for (Match m : getMatches())
		{
			if (!hasBeenReported(m) && !m.isTotalForfeit())
			{
				if (!first)
				{
					sb.append("<hr width='");
					sb.append(width);
					sb.append("px'>");
				}
				first = false;
				sb.append(m.getEmailHtml(width, false));
				getReported().add(m);
			}
		}
		Utils.saveData();		// Because of "reported" matches
		Utils.sendTeamsEmail(getTeams(), null, "Summary for Round " + current.getName(), sb.toString());
	}
	
	
	
	public ArrayList<Round> getRounds()
	{
		return rounds;
	}
	
	public Round getRoundFor(Match m)
	{
		for (Round r : getRounds())
			if (r.contains(m))
				return r;
		return null;
	}
	
	public Round getRound(int i)
	{
		if (i < 0)
			return null;
		return getRounds().isEmpty() ? null : getRounds().get(i);
	}
	
	public void addRound(Round r)
	{
		getRounds().add(r);
		r.setLeague(this);
	}
	
	protected void buildRounds(Date startDate, int roundLengthInWeeks, int numRounds)
	{
		ArrayList<Team> tms = (ArrayList<Team>)getTeams().clone();
		if (tms.size() % 2 != 0)
		{
			tms.add(new ByeTeam());
		}
		int round = 1;
		while (round <= numRounds)
		{
			createRound(round++, tms, startDate, roundLengthInWeeks);
			shuffleTeams(tms);
		};
	}
	
	public void createTournament(Collection tms, Date dt, int weeks, int rndSize, int late)
	{
		setNumberOfLatePasses(late);
		setWeeksInRounds(rndSize);
		tms.forEach(o -> {
			Team t = getHood().addTeam((Team)o);
			addTeam(t);
		});
		setStartDate(dt);
		buildRounds(dt, rndSize, weeks);
		if (getStandings().size() == 0)
			recalculate();
	}
	
	public void createManualTournament(ArrayList<Team> tms)
	{
		setNumberOfLatePasses(0);
		setWeeksInRounds(1);
		tms.forEach(t -> addTeam(t));
	}
	
	protected void createRound(int r, ArrayList<Team> tms, Date startDate, int roundLengthInWeeks)
	{
		if (tms.size() % 2 != 0)
			throw new RuntimeException("Must have an even number of teams... Include a ByeTeam if necessary");
		long time = startDate.getTime();
		long dayInMs = 86400 * 1000;
		long week = dayInMs * roundLengthInWeeks * 7;
		int offset =  r - 1;
		SimpleDateFormat format = new SimpleDateFormat("M/dd");
		Date start = new Date(time + (week * offset));
		Date end = new Date(time + (week * r) - dayInMs);
		Round round = new Round();
		round.setWeeksLeft(roundLengthInWeeks);
		round.setName(format.format(start) + "-" + format.format(end));
		addRound(round);
		int count = tms.size() / 2;
		int t1 = 0;
		int t2 = tms.size() - 1;
		while (count-- > 0)
		{
			Team teamA = tms.get(t1++);
			Team teamB = tms.get(t2--);
			Match m = new Match(this);
			m.setTeamA(teamA);
			m.setTeamB(teamB);
			round.addMatch(m);
		}
	}
	
	/*
	 * Following algorithm from:  https://en.wikipedia.org/wiki/Round-robin_tournament
	 * Leave team @ 0 in place, and move all other teams around.
	 */
	protected void shuffleTeams(ArrayList<Team> tms)
	{
		int lastIndex = tms.size() - 1;
		Team last = tms.get(lastIndex);
		for (int i = lastIndex; i > 1; i--)
		{
			tms.set(i, tms.get(i - 1));
		}
		tms.set(1, last);
	}
	
	public Standing getStanding(int i)
	{
		return getStandings().get(i);
	}
	
	public Standing getStandingFor(Team t)
	{
		for (Standing s : getStandings())
		{
			if (s.contains(t))
				return s;
		}
		return null;
	}

	public boolean isTournament()
	{
		return true;
	}
	
	public void addGWData(Configuration conf) throws Exception
	{
		addStandingsData(s -> s.getGamesWinPercentage(), conf);
	}
	
	public void addStandingsData(Function<Standing, Number> c, Configuration conf) throws Exception
	{
		HashMap<Team, ArrayList<Match>> matches = new HashMap<Team, ArrayList<Match>>();
		HashMap<Team, ListSeries> map = new HashMap<Team, ListSeries>();
		for (Team t : getTeams())
		{
			ListSeries ls = new ListSeries();
	        ls.setName(t.getName());
	        map.put(t, ls);
	        matches.put(t, new ArrayList<Match>());
	        conf.addSeries(ls);
		}
		ArrayList<Match> rev = new ArrayList<Match>();
		rev.addAll(getMatches());
		Collections.reverse(rev);
		for (Match m : rev)
		{
			addStandingsData(c, m.getA().getTeam(), m, map, matches);
			addStandingsData(c, m.getB().getTeam(), m, map, matches);
		}
	}
	
	protected void addStandingsData(Function<Standing, Number> c, Team t, Match m, HashMap<Team, ListSeries> map, HashMap<Team, ArrayList<Match>> matches) throws Exception
	{
		ArrayList<Match> ms = matches.get(t);
		ms.add(m);
		Standing s = new Standing(this, t, ms);
		ListSeries ls = map.get(t);
		ls.addData(c.apply(s), false, false);
	}
	
	public void addEloData(Configuration conf)
	{
		HashMap<Team, ListSeries> map = new HashMap<Team, ListSeries>();
		for (Team t : getTeams())
		{
			ListSeries ls = new ListSeries();
	        ls.setName(t.getName());
	        ls.addData(getStartingElo(t));
	        map.put(t, ls);
	        conf.addSeries(ls);
		}
		ArrayList<Match> rev = new ArrayList<Match>();
		rev.addAll(getMatches());
		Collections.reverse(rev);
		for (Match m : rev)
		{
			Team a = m.getA().getTeam();
			ListSeries ls = map.get(a);
			ls.addData(m.getA().getResultElo(), false, false);
			Team b = m.getB().getTeam();
			ls = map.get(b);
			ls.addData(m.getB().getResultElo(), false, false);
		}
	}

	public ArrayList<Standing> getStandings()
	{
		return standings;
	}

	public boolean isIgnoreStandings()
	{
		return ignoreStandings;
	}

	public void setIgnoreStandings(boolean ignoreStandings)
	{
		this.ignoreStandings = ignoreStandings;
	}

}
