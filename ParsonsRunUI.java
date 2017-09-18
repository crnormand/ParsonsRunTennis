package net.parsonsrun;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;

import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;

import net.parsonsrun.domain.ExternalTeam;
import net.parsonsrun.domain.Lineup;
import net.parsonsrun.domain.Neighborhood;
import net.parsonsrun.domain.Player;
import net.parsonsrun.domain.Team;
import net.parsonsrun.domain.Tournament;

public abstract class ParsonsRunUI extends UI
{
	private static String PARAMETERDELIM = ";";
	private static String PROPERTYDELIM = "=";
	private static String SALT[] = { "Paon", "Ru", "Tens", "Noe", "Chrs", "Lius", "Jasne", "Jane", "Rael", "Couey", "Liook", "Geoa", "USA" };
	public static String PARAMETERKEY = "rand";
	public static String PLAYERID = "playerid";
	public static String TEAMID = "teamid";
	public static String WEEK = "week";
	public static String ACTION = "action";
	
	public static String ACTION_VIEW = "view";
	public static String ACTION_AVAIL = "avail";
	public static String ACTION_CONFIRM= "confirm";
	public static String ACTION_REJECT= "reject";

	public static ScheduledExecutorService scheduler;

	public static int SessionId = 0;
	protected int browserHeight;
	protected int browserWidth;
	protected int id;
	protected Properties requestParameters = new Properties();
	
	protected Player loginUser;		// Login user
	protected Player currentPlayer;	// Current Player (for editing/displaying)
	protected ExternalTeam currentExternalTeam;
	protected Tournament currentTournament;
	
	protected synchronized int getNextSessionId()
	{
		return ++SessionId;
	}
	
	protected void saveRequestParameters(VaadinRequest request)
	{
		String v = request.getParameter(PARAMETERKEY);
		if (v == null)
			return;
		decodeRequest(v);
	}
	
	protected boolean isSalt(String s)
	{
		for (int i = 0; i < SALT.length; i++)
			if (SALT[i].equals(s))
				return true;
		return false;
	}
	
	protected void decodeRequest(String req)
	{
		String decode = Utils.decodeBase64(req);
		String[] splits = decode.split(PARAMETERDELIM);	// Should always be odd number
		if (splits.length % 2 != 1)
			return;
		int c = 0;
		if (!isSalt(splits[c++]))
			return;
		while (c < splits.length)
		{
			String[] p = splits[c++].split(PROPERTYDELIM);
			if (p.length != 2)
				return;
			saveRequestParameters(p[0], p[1]);
			if (!isSalt(splits[c++]))
				return;
		}
	}
	
	public String encodeRequest(String... s)
	{
		if (s.length % 2 != 0)
			throw new RuntimeException("Must encode pairs of strings Key & value)");
		StringBuilder b = new StringBuilder();
		Random rnd = new Random(System.currentTimeMillis());
		b.append(SALT[rnd.nextInt(SALT.length)]);
		for (int i = 0; i < s.length; i = i + 2)
		{
			String k = s[i];
			String v = s[i+1];
			b.append(PARAMETERDELIM);
			b.append(k);
			b.append(PROPERTYDELIM);
			b.append(v);
			b.append(PARAMETERDELIM);
			b.append(SALT[rnd.nextInt(SALT.length)]);
		}
		String uri = Page.getCurrent().getLocation().getSchemeSpecificPart();
		int last = uri.lastIndexOf('/');
		uri = uri.substring(0, last);
		return Page.getCurrent().getLocation().getScheme() + ":" + uri + "?" + 
        	PARAMETERKEY + "=" + Utils.encodeBase64(b.toString());
	}

	
	public String encodePlayerId(Player p)
	{
		return encodeRequest(PLAYERID, p.getGlobalIdString());
	}
	
	public String encodeAvailAction(Player p, ExternalTeam t)
	{
		return encodeRequest(PLAYERID, p.getGlobalIdString(), TEAMID, t.getGlobalIdString(), ACTION, ACTION_AVAIL);
	}
	
	public String encodeViewAction(Player p, ExternalTeam t)
	{
		return encodeRequest(PLAYERID, p.getGlobalIdString(), TEAMID, t.getGlobalIdString(), ACTION, ACTION_VIEW);
	}
	
	protected abstract void navToExternal();
	
	public void updateUI(Runnable r)
	{
		UI.getCurrent().access(r);
	}
	
	protected void processActions()
	{
		String action = getRequestParameter(ACTION);
		if (action.equals(ACTION_VIEW))
		{
			ExternalTeam t = getHood().getExternalTeam(Long.parseLong(getRequestParameter(TEAMID)));
			if (t != null)
			{
				setCurrentExternalTeam(t);
				navToExternal();
			}
		}
		if (action.equals(ACTION_AVAIL))
		{
			ExternalTeam t = getHood().getExternalTeam(Long.parseLong(getRequestParameter(TEAMID)));
			if (t != null)
			{
				println("Registration Link: " + t.getFullName());
				setCurrentExternalTeam(t);
				navToExternal();
			}
		}
		if (action.equals(ACTION_CONFIRM))
		{
			ExternalTeam t = getHood().getExternalTeam(Long.parseLong(getRequestParameter(TEAMID)));
			if (t != null)
			{
				int w = Integer.parseInt(getRequestParameter(WEEK));
				Utils.saveWhile(() -> t.confirm(getLoginUser(), w));
				println("Confirmation Link: " + t.getFullName() + "Week:" + w);
				setCurrentExternalTeam(t);
				navToExternal();
				updateUI(() -> Notification.show("Thank for you for the confirmation!", Notification.Type.ERROR_MESSAGE));
			}
		}
		if (action.equals(ACTION_REJECT))
		{
			ExternalTeam t = getHood().getExternalTeam(Long.parseLong(getRequestParameter(TEAMID)));
			if (t != null)
			{
				int w = Integer.parseInt(getRequestParameter(WEEK));
				if (t.reject(getLoginUser(), w))		// Only send email if someone new rejects
				{
					println("REJECTION Link: " + t.getFullName() + " for Week #" + w);
					Utils.saveData();
					setCurrentExternalTeam(t);
					navToExternal();
					updateUI(() -> Notification.show("Thank for you for the information.  The captains have been notified.", Notification.Type.ERROR_MESSAGE));
					String s = "Rejected: " + getLoginUser().firstLastName() + " has rejected the assignment";
					for (Player p : t.getCaptains())
					{
						sendEmail(p, s, s + " for Week #" + w + ".<br><br>Click <a href='" + encodeViewAction(p, t) + "'>HERE</a> to open the Team view.<br>NOTE: The Captain's features are ONLY available from the DESKTOP.");
					}
				}
			}
		}
	}
	
	public int getWeekParameter()
	{
		String w = getRequestParameter(WEEK);
		if (w == null || w.isEmpty())
			return 0;
		return Integer.parseInt(w);
	}
	
	public void setWeekParameter(int w)
	{
		saveRequestParameters(WEEK, String.valueOf(w));
	}
	
	public String encodeConfirmAction(Player p, ExternalTeam t, Lineup lu)
	{
		return encodeRequest(PLAYERID, p.getGlobalIdString(), TEAMID, t.getGlobalIdString(), ACTION, ACTION_CONFIRM, WEEK, String.valueOf(lu.getWeek()));
	}
	
	public String encodeViewScheduleAction(Player p, ExternalTeam t, Lineup lu)
	{
		return encodeRequest(PLAYERID, p.getGlobalIdString(), TEAMID, t.getGlobalIdString(), ACTION, ACTION_VIEW, WEEK, String.valueOf(lu.getWeek()));
	}
	
	public String encodeRejectAction(Player p, ExternalTeam t, Lineup lu)
	{
		return encodeRequest(PLAYERID, p.getGlobalIdString(), TEAMID, t.getGlobalIdString(), ACTION, ACTION_REJECT, WEEK, String.valueOf(lu.getWeek()));
	}
	
	protected void saveRequestParameters(String key, String value)
	{
		if (value != null)
		{
			requestParameters.put(key, value);
		}
	}
	
	public void clearRequestParameter(String key)
	{
		requestParameters.remove(key);
	}
	
	protected String getRequestParameter(String key)
	{
		return requestParameters.getProperty(key, "");
	}
	
	public void clearRequestParameters()
	{
		requestParameters.clear();
	}

	protected boolean loginPreviousUser()
	{
		String id = getRequestParameter(PLAYERID);
		if (id != null && !id.isEmpty())
		{
			Player p = getHood().getPlayer(Long.parseLong(id));
			if (p != null)
				return loginUser(p);
		}
		String u = Utils.getCookieValue(Utils.USER_COOKIE);
		if (u != null && !u.isEmpty())
			return loginUser(Utils.getCookieValue(Utils.USER_COOKIE), null);
		return false;
	}
	
	public boolean loginUser(String email, String password)
	{
		if (email == null || email.isEmpty())
			return false;
		Player p = getHood().login(email, password);
		setLoginUser(p);
		setCurrentPlayer(p);
		if (p != null)
		{
			Utils.addCookie(Utils.USER_COOKIE, email);
			return true;
		}
		return false;
	}
	
	public boolean loginUser(Player p)
	{
		return loginUser(p.getEmail(), null);
	}
	
	public void logoutUser()
	{
		Utils.addCookie(Utils.USER_COOKIE, "logged_out");
		setLoginUser(null);
		setCurrentPlayer(null);
	}
	
	public Neighborhood getHood()
	{
		return Neighborhood.getSingleton();
	}

	public Player getLoginUser()
	{
		return loginUser;
	}

	public void setLoginUser(Player currentUser)
	{
		println("Login user " + currentUser);
		this.loginUser = currentUser;
	}
	
	public void init()
	{
		id = getNextSessionId();
		browserHeight = getPage().getBrowserWindowHeight();
		browserWidth = getPage().getBrowserWindowWidth();
		println("INIT " + uiType() + " #" + id + " (" + browserWidth + "x" + browserHeight + ")");
		setPollInterval(10000);
	}
	
	public abstract String uiType();
	
	public void println(String s)
	{
		String u = "";
		if (loginUser != null)
		{
			u = ":" + loginUser.getFirstChar() + loginUser.getLastChar();
		}
		Utils.println("[" + id + u + "] " + s);
	}

	public Player getCurrentPlayer()
	{
		return currentPlayer;
	}

	public void setCurrentPlayer(Player currentPlayer)
	{
//		println("Current user " + currentPlayer);
		this.currentPlayer = currentPlayer;
	}

	public int getBrowserHeight()
	{
		return browserHeight;
	}

	public int getBrowserWidth()
	{
		return browserWidth;
	}
	
	public void sendEmail(Player to, String subject, String htmlMessage)
	{
		if (to.isForfeit())
			return;
		try
		{
			HtmlEmail email = Utils.newEmail(subject, htmlMessage);
			email.addTo(to.getEmail());
			getScheduler().schedule(() -> { 
				try
				{
					println("Sending email to:" + to);
					email.send();
				}
				catch (EmailException e) 
				{
					println("Unable to send email to " + to + " '" + subject + "': " + e);
				}

			} , 3, TimeUnit.SECONDS);
		}
		catch (EmailException e) 
		{
			println("Unable to create email to " + to + " '" + subject + "': " + e);
		}

	}
	
	public void sendEmail(Team t, String subject, String htmlMessage)
	{
		t.sendEmail(this, subject, htmlMessage);
	}
	
	public void sendEmail(ArrayList<Player> tos, String subject, String htmlMessage)
	{
		try
		{
			HtmlEmail email = Utils.newEmail(subject, htmlMessage);
			for (Player to : tos)
			{
				if (!to.isForfeit())
					email.addTo(to.getEmail());
			}
			getScheduler().schedule(() -> { 
				try
				{
					println("Sending email:" + new Date());
					email.send();
				}
				catch (EmailException e) 
				{
					println("Unable to send email to " + tos + " '" + subject + "': " + e);
				}

			} , 3, TimeUnit.SECONDS);
		}
		catch (EmailException e) 
		{
			println("Unable to send multiple email '" + subject + "': " + e);
		}
	}

	public static ScheduledExecutorService getScheduler()
	{
		return scheduler;
	}

	public static void setScheduler(ScheduledExecutorService s)
	{
		scheduler = s;
	}

	public ExternalTeam getCurrentExternalTeam()
	{
		return currentExternalTeam;
	}

	public void setCurrentExternalTeam(ExternalTeam currentExternalTeam)
	{
		this.currentExternalTeam = currentExternalTeam;
	}

	public Tournament getCurrentTournament()
	{
		return currentTournament;
	}

	public void setCurrentTournament(Tournament currentTournament)
	{
		this.currentTournament = currentTournament;
	}
}
