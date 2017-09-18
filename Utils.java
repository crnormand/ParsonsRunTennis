package net.parsonsrun;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;

import javax.servlet.http.Cookie;

import org.apache.commons.mail.*;

import com.vaadin.server.VaadinService;

import net.parsonsrun.DataAccess.Command;
import net.parsonsrun.domain.Neighborhood;
import net.parsonsrun.domain.Player;
import net.parsonsrun.domain.UpdateListener;

public class Utils
{
	private static Utils singleton = new Utils();
	
	public static final boolean TREAT_CHROME_AS_MOBILE = false;
	public static final boolean ALLOW_DB_SAVES = true;

	public static final int COOKIE_AGE = Integer.MAX_VALUE;
	public static final String USER_COOKIE = "prtennisuser";
	
	public static final String DB_NAME_Property = "PRTennis.DbName";
	public static final String IMPORT_Property = "PRTennis.import";
	public static final String LOG_Property = "PRTennis.log";

	public static SimpleDateFormat timestamp = new SimpleDateFormat("MM/dd HH:mm:ss");
	public static SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy");
	public static DateTimeFormatter localdateFormat = DateTimeFormatter.ofPattern("MM/dd/yy");
	
	public static Properties defaults;
	
	public static final String[] defaultData = 
		{
			DB_NAME_Property, "C:/ParsonsRunTennis/PRTennis.db",
			IMPORT_Property, "C:/ParsonsRunTennis/parsonsrun.import",
			LOG_Property, "C:/ParsonsRunTennis/PRTennis.log"
		};
	
	public static int [] EloDifferences = { 0, 20, 40, 60, 80, 100, 120, 140, 160, 180, 200, 300, 400 };
	public static double [] ExpectedScores = { 0.5, 0.53, 0.58, 0.62, 0.66, 0.69, 0.73, 0.76, 0.79, 0.82, 0.84, 0.93, 0.97 };

	
	private StringBuilder log = new StringBuilder();
	transient ArrayList<UpdateListener> listeners = new ArrayList<UpdateListener>();

	public static Utils getSingleton()
	{
		return singleton;
	}
	
	private Utils()
	{
	}
	
	public static void initializeSystem()
	{
		getSingleton().rotateLogs();
	}
	
	private void rotateLogs()
	{
		String n = getProperty(LOG_Property);
		if (n == null || n.isEmpty())
			return;
		File f = new File(n + ".9");
		if (f.exists())
			f.delete();
		for (int i = 8 ; i >= 0 ; i--)
		{
			f = new File(n + "." + i);
			if (f.exists())
				f.renameTo(new File(n + "." + (i + 1)));
		}
		f = new File(n);
		if (f.exists())
			f.renameTo(new File(n + ".0"));
		_println("Log opened");
	}
	
	public void addListener(UpdateListener l)
	{
		synchronized (listeners)
		{
			listeners.add(l);
		}
	}
	
	public static String getLog()
	{
		return getSingleton()._getLog();
	}
	
	private String _getLog()
	{
		return log.toString();
	}
	
	public void removeListener(UpdateListener l)
	{
		synchronized (listeners)
		{
			listeners.remove(l);
		}
	}
	
	public void updateListeners()
	{
		synchronized (listeners)
		{
			for (UpdateListener l : listeners)
				l.update();
		}
	}
	
	public static String printDate(Date d)
	{
		if (d == null)
			return "n/a";
		return dateFormat.format(d);
	}
	public static String printDate(LocalDate d)
	{
		return d.format(localdateFormat);
	}
	
	public static void println(String s)
	{
		singleton._println(s);
	}
	

	public static Neighborhood importData(Neighborhood h)
	{
		String f = Utils.getProperty(Utils.IMPORT_Property);
		System.out.println("Reading data from " + f);
		new DataAccess(h, f).exec(Command.READ_USERS);
		
		System.out.println("Adding demo data (Utils.importData())");
		Tester.createData(h);

		return h;
	}
	
	public static String escapeHTML(String in) 
	{
		boolean prevSpace = false;
		String s = in.trim();
	    StringBuilder out = new StringBuilder(Math.max(16, s.length()));
	    for (int i = 0; i < s.length(); i++) 
	    {
	        char c = s.charAt(i);
	        if (c == ' ' && prevSpace)
	        	out.append("&nbsp;");
	        else if (c > 127 || c == '"' || c == '<' || c == '>' || c == '&' || c == '\'') {
	            out.append("&#");
	            out.append((int) c);
	            out.append(';');
	        } 
	        else if (c == '\n')
	        {
	        	out.append("<br>");
	        }
	        else {
	            out.append(c);
	        }
	        prevSpace = (c == ' ');
	    }
	    return out.toString();
	}
	
	public static String googleMapsUrl(String loc)
	{
		String[] s = loc.split(" ");
		StringBuilder b = new StringBuilder();
		b.append("https://www.google.com/maps/place/");
		int i = 0;
		if (s.length > 0)
		{
			b.append(s[i++]);
			while (i < s.length)
			{
				b.append("+");
				b.append(s[i++]);
			}
		}
		return b.toString();
	}
	
	private void _println(String s)
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append(timestamp.format(new Date()));
		sb.append(' ');
		sb.append(s);
		sb.append('\n');
		log.append(sb.toString());
		output(sb.toString());
	}
	
	private synchronized void output(String s)
	{
		System.out.print(s);
		updateListeners();
		
		String n = getProperty(LOG_Property);
		if (n == null || n.isEmpty())
			return;

		try
		{
			BufferedWriter w = new BufferedWriter(new FileWriter(n, true));
			w.write(s);
			w.newLine();
			w.close();	
		}
		catch (IOException e)
		{
			System.out.println("Error writing to log '" + n + "':" + e);
		}
	}
	
	public static Properties getDefaults()
	{
		if (defaults == null)
		{
			defaults = new Properties();
			for (int i = 0; i < defaultData.length ; i += 2)
				defaults.put(defaultData[i], defaultData[i+1]);
		}
		return defaults;
	}
	
//  http://gobase.org/studying/articles/elo/
public static double expectedScore(int diff)
{
	int len = EloDifferences.length;
	double score = 0.50;
	for (int i = 0; i < len; i++)
	{
		if (diff < EloDifferences[i])
			return score;
		score = ExpectedScores[i];
	}
	return score;
}
	
	public static String dbName()
	{
		return getProperty(DB_NAME_Property);
	}
	
	public static String getProperty(String key)
	{
		return System.getProperty(key, getDefaults().getProperty(key));
	}
	
	public static HtmlEmail newEmail(String subject, String htmlMessage) throws EmailException
	{
		HtmlEmail email = new HtmlEmail();
		email.setHostName("smtp.gmail.com"); // "outbound.att.net");
		email.setSmtpPort(465);
		email.setSSLOnConnect(true);
		email.setAuthentication(Player.SYSTEM_EMAIL, Player.SYSTEM_EMAIL_PASSWORD);
		email.setFrom(Player.SYSTEM_EMAIL);
		email.setSubject(subject);
		String pre = "<span style='font-size:0.75em'>(Please do not respond to this email. &nbsp;This address is not monitored)</span><br><br>";
		email.setHtmlMsg(pre + htmlMessage);
		return email;
	}
	
	public static String rot13(String input)
	{
	   StringBuilder sb = new StringBuilder();
	   for (int i = 0; i < input.length(); i++) {
	       char c = input.charAt(i);
	       if       (c >= 'a' && c <= 'm') c += 13;
	       else if  (c >= 'A' && c <= 'M') c += 13;
	       else if  (c >= 'n' && c <= 'z') c -= 13;
	       else if  (c >= 'N' && c <= 'Z') c -= 13;
	       sb.append(c);
	   }
	   return sb.toString();
	}
	
	public static String cryptoHash(String password)
	{
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
			messageDigest.update(password.getBytes());
			return new String(messageDigest.digest());
		}
		catch (NoSuchAlgorithmException e)
		{
			return rot13(password);
		}
	}
	
	public static String encodeBase64(String s)
	{
		return new String(Base64.getEncoder().encode(s.getBytes()));
	}
	
	public static String encode(String s)
	{
		return encodeBase64(rot13(s));
	}
	public static String decodeBase64(String s)
	{
		return new String(Base64.getDecoder().decode(s.getBytes()));
	}
	
	public static String decode(String s)
	{
		return rot13(decodeBase64(s));
	}
	
	public static void addCookie(String cookieName, String cookieValue)
	{
		if (VaadinService.getCurrentRequest() != null)
		{
			Cookie c = new Cookie(cookieName, cookieValue);
			c.setMaxAge(COOKIE_AGE);
			c.setPath(VaadinService.getCurrentRequest().getContextPath());
			VaadinService.getCurrentResponse().addCookie(c);
		}
	}
	
	public static Cookie getCookieByName(String name) 
	{ 
		// Fetch all cookies from the request 
		Cookie[] cookies = VaadinService.getCurrentRequest().getCookies();
		if (cookies == null)
			return null;
		// Iterate to find cookie by its name 
		for (Cookie cookie : cookies) 
		{ 
			if (name.equals(cookie.getName()))
			{ 
				return cookie; 
			} 
		}
		return null; 
	}

	public static String getCookieValue(String name) 
	{
		Cookie c = getCookieByName(name);
		return (c == null) ? "" : c.getValue();
	}

	public static Neighborhood loadData()
	{
		File f = new File(dbName());
		if (f.exists())
		{
			println("Reading Neighborhood data from: " + f);
			try
			{
				InputStream file = new FileInputStream(f);
				InputStream buffer = new BufferedInputStream(file);
				ObjectInput input = new ObjectInputStream(buffer);
				try
				{
					Neighborhood n = (Neighborhood)input.readObject();
					n.postLoad();
					return n;
				}
				finally
				{
					input.close();
				}
			}
			catch (ClassNotFoundException ex)
			{
				println("Cannot perform input. Class not found." + ex);
			}
			catch (IOException ex)
			{
				println("Cannot perform input." + ex);
			}
		}
		else
		{
			return importData(new Neighborhood());
		}
		return null;
	}

	protected static void primSaveData()
	{
		if (ALLOW_DB_SAVES)
		{
			long t1 = System.currentTimeMillis();
			try 
			{
				FileOutputStream file = new FileOutputStream(new File(dbName()));
				OutputStream buffer = new BufferedOutputStream(file);
				ObjectOutput output = new ObjectOutputStream(buffer);
				try
				{
					output.writeObject(Neighborhood.getSingleton());
				}
				finally
				{
					output.close();
				} 
			}
		    catch (IOException ex)
			{
		    	System.out.println("Cannot perform output." + ex);
		    }
			long t2 = System.currentTimeMillis();
			//if ((t2 - t1) > 100)
				println("DB saved.  " + (t2 - t1) + "ms");
		}
	}
	
	public static void saveData()
	{
		synchronized (Neighborhood.getSingleton())
		{
			primSaveData();
		}
	}


	/**
	 * Execute the Runnable (usually sent as a Lambda expression) in a synchronized block (so only one
	 * process can change data at the same time) and then save the data.
	 */
	public static void saveWhile(Runnable r)
	{
		try
		{
			synchronized (Neighborhood.getSingleton())
			{
				r.run();
				saveData();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new RuntimeException("Unable to invoke: " + e);
		}
	}
}
