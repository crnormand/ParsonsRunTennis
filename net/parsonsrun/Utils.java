package net.parsonsrun;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.*;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

import javax.mail.*;
import javax.mail.internet.MimeBodyPart;
import javax.servlet.http.Cookie;

import org.apache.commons.mail.*;

import com.google.common.io.Files;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinService;

import net.parsonsrun.DataAccess.Command;
import net.parsonsrun.domain.Neighborhood;
import net.parsonsrun.domain.Picture;
import net.parsonsrun.domain.Player;
import net.parsonsrun.domain.Team;
import net.parsonsrun.domain.UpdateListener;

public class Utils
{
	private static Utils singleton = new Utils();
	
	public static final boolean TREAT_CHROME_AS_MOBILE = false;
	public static final boolean ALLOW_DB_SAVES = true;
	public static final boolean ALLOW_EMAILS = true;


	public static final int COOKIE_AGE = Integer.MAX_VALUE;
	public static final String USER_COOKIE = "prtennisuser";
	
	public static final String DB_NAME_Property = "PRTennis.DbName";
	public static final String IMPORT_Property = "PRTennis.import";
	public static final String LOG_Property = "PRTennis.log";
	public static final String PICTURE_DIR_Property = "PRTennis.PictureDir";

	public static final String SMTP = "smtp.gmail.com";
	public static final String POP = "pop.gmail.com";
	public static final String EMAIL_REF = "PR_REF: ";
	public static final int EMAIL_DELAY = 1;  // seconds between emails
	public static final int MIN_BYTES = 100;
	public static final int PICTURE_WIDTH = 1280;
	public static final int PICTURE_HEIGHT = 960;
	public static final int PICTURE_THUMBNAIL_HEIGHT = 150;
	public static String BaseUrl = "http://parsonsrun.dynu.net:8080/ParsonsRunTennis";

	public static SimpleDateFormat timestamp = new SimpleDateFormat("MM/dd HH:mm:ss");
	public static SimpleDateFormat timestamp2 = new SimpleDateFormat("MM/dd");
	public static SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy");
	public static DateTimeFormatter localdateFormat = DateTimeFormatter.ofPattern("MM/dd/yy");
	
	public static Properties defaults;
	
	public static final String[] defaultData = 
		{
			DB_NAME_Property, "C:/ParsonsRunTennis/PRTennis.db",
			IMPORT_Property, "C:/ParsonsRunTennis/parsonsrun.import",
			LOG_Property, "C:/ParsonsRunTennis/PRTennis.log",
			PICTURE_DIR_Property, "C:/ParsonsRunTennis/pics/"
		};
	
	public static int [] EloDifferences = { 0, 20, 40, 60, 80, 100, 120, 140, 160, 180, 200, 300, 400 };
	public static double [] ExpectedScores = { 0.5, 0.53, 0.58, 0.62, 0.66, 0.69, 0.73, 0.76, 0.79, 0.82, 0.84, 0.93, 0.97 };

	private boolean debug = false;
	private StringBuilder log = new StringBuilder();
	private StringBuilder recentUsers;
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
	
	public static String getBaseUrl()
	{
		if (BaseUrl == null)
		{
			String uri = Page.getCurrent().getLocation().getSchemeSpecificPart();
			int last = uri.lastIndexOf('/');
			uri = uri.substring(0, last);
			BaseUrl = Page.getCurrent().getLocation().getScheme() + ":" + uri;
			println("Base URL:" + BaseUrl);
		}
		return BaseUrl;
	}
	
	private void rotateLogs()
	{
		String n = logFilename();
		if (rotateFile(n))
			_println("Log opened");
		return;
	}
	
public static boolean rotateFile(String n)
{
	if (n == null || n.isEmpty())
		return false;
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
	return true;
}
	
	public void addListener(UpdateListener l)
	{
		synchronized (listeners)
		{
			listeners.add(l);
		}
	}
	
	public static void setDebugLogging(boolean b)
	{
		println("Debug logging:" + b);
		singleton.setDebug(b);
	}
	
	public static boolean getDebugLogging()
	{
		return singleton.isDebug();
	}

	public void setDebug(boolean debug)
	{
		this.debug = debug;
	}
	
	public boolean isDebug()
	{
		return debug;
	}

	public static String getLog()
	{
		return getSingleton()._getLog();
	}
	
	private String _getLog()
	{
		return log.toString();
	}
	
	public static ArrayList<EmailMessage> getEmails()
	{
		ArrayList<EmailMessage> msg = null;
		try {
			Properties properties = new Properties();
			properties.put("mail.pop3.host", POP);
			properties.put("mail.pop3.port", "995");
			properties.put("mail.pop3.starttls.enable", "true");
			Session emailSession = Session.getDefaultInstance(properties);
			Store store = emailSession.getStore("pop3s");
			store.connect(POP, Player.SYSTEM_EMAIL, Player.SYSTEM_EMAIL_PASSWORD);
			Folder emailFolder = store.getFolder("INBOX");
			emailFolder.open(Folder.READ_WRITE);
			Message [] tmp = emailFolder.getMessages();
			if (tmp != null)
			{
				msg = new ArrayList<EmailMessage>();
				for (int i = 0; i < tmp.length; i++)
				{
					msg.add(new EmailMessage(tmp[i]));
					tmp[i].setFlag(Flags.Flag.DELETED, true);
				}
			}		
			emailFolder.close(true);
			store.close();
	    } catch (NoSuchProviderException e) 
		{
	    	println(getStackTrace(e));
	    } catch (MessagingException e) 
		{
	    	println(getStackTrace(e));
	    } catch (Exception e) 
		{
	    	println(getStackTrace(e));
	    }
		return msg;
	}
	
	public static String getStackTrace(Throwable e)
	{
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return sw.toString(); 
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
	
	public static void debug(String s)
	{
		singleton._debug(s);
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
	
	public static void importManual()
	{
		synchronized (Neighborhood.getSingleton())
		{
			String f = Utils.getProperty(Utils.IMPORT_Property);
			new DataAccess(Neighborhood.getSingleton(), f).exec(Command.IMPORT_MANUAL);
		}
	}
	
	public static String escapeHTML(String in) 
	{
		boolean prevSpace = false;
		String s = in.trim();
	    StringBuilder out = new StringBuilder(Math.max(16, s.length()));
	    for (int i = 0; i < s.length(); i++) 
	    {
	        char c = s.charAt(i);
	        if (Character.isSpaceChar(c) && prevSpace)
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
	        prevSpace = Character.isSpaceChar(c);
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
		sb.append(format1(new Date()));
		sb.append(' ');
		sb.append(s);
		sb.append('\n');
		log.append(sb.toString());
		output(sb.toString());
	}
	
	public static String format1(Date d)
	{
		return timestamp.format(d);
	}
	
	public static String format2(Date d)
	{
		return timestamp2.format(d);
	}
	

	
	private void _debug(String s)
	{
		if (debug)
			_println(s);
	}
	
	private synchronized void output(String s)
	{
		System.out.print(s);
		updateListeners();
		
		String n = logFilename();
		if (n == null || n.isEmpty())
			return;

		try
		{
			BufferedWriter w = new BufferedWriter(new FileWriter(n, true));
			w.write(s);
			w.close();	
		}
		catch (IOException e)
		{
			System.out.println("Error writing to log '" + n + "':" + e);
		}
	}
	
	public static String logFilename()
	{
		return getProperty(LOG_Property);
	}
	
	public static String pictureDirectory()
	{
		return getProperty(PICTURE_DIR_Property);
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
	
	public static void processEmails()
	{
		println("Retrieving emails...");
		long t = System.currentTimeMillis();
		ArrayList<EmailMessage> ms = Utils.getEmails();
		if (ms == null)
		{
			println("No mail. " + (System.currentTimeMillis() - t) + " ms.");
		}
		else
		{
			println("Mail: " + ms.size() + ". " + (System.currentTimeMillis() - t) + " ms.");
			for (EmailMessage m : ms)
				processEmail(m);
		}
	}
	
	protected static void processEmail(EmailMessage m)
	{
		//println("Email:" + m.getContent().length());
		//println("RAW $$" + m.getContent() + "$$");
		String pats = "<div";
		String pate = "</div>";
		int start = m.getContent().indexOf(pats);
		int end = m.getContent().lastIndexOf(pate);
		//println("Start:" + start + " end:" + end);
		if (start >= 0 && end >= 0)
		{
			String plain = m.getContent().substring(0, start);
			//println("Plain: **" + plain + "**");
			String html = m.getContent().substring(start, end + pate.length());
			//println("Html: @@" + html + "@@");
			Player p = getHood().playerForReference(plain);
			if (p != null)
			{
				Player f = getHood().playerForAddresses(m.getFrom());
				if (f != null)
				{
					println("Auto Fwd to: " + p.getEmail() + " from: " + f.getEmail());
					sendEmail(p, f, "Auto Fwd:" + m.getSubject(), "Auto Forwarded email<br>" + html);
				}
				else
					println("Unable Auto Fwd to: " + p.getEmail());
			}
			else
				println("Unable to detect Auto Fwd");
		}
	}
	
	protected static Neighborhood getHood()
	{
		return Neighborhood.getSingleton();
	}
	
	public static void sendEmail(ArrayList<Player> tos, Player from, String subject, String htmlMessage)
	{
		if (!ALLOW_EMAILS)
			return;
		try
		{
			String ref = getHood().emailReferenceFor(from);
			HtmlEmail email = Utils.newGroupEmail(subject, htmlMessage + ref);
			for (Player to : tos)
			{
				if (!to.isForfeit())
					email.addTo(to.getEmail());
			}
			ParsonsRunUI.getScheduler().schedule(() -> { 
				try
				{
					println("Sending email to " + tos.size() + " players");
					email.send();
				}
				catch (EmailException e) 
				{
					println("Unable to send email to " + tos + " '" + subject + "': " + e);
				}

			} , EMAIL_DELAY, TimeUnit.SECONDS);
		}
		catch (EmailException e) 
		{
			println("Unable to send multiple email '" + subject + "': " + e);
		}
	}
	
	public static void sendTeamsEmail(ArrayList<Team> tos, Player from, String subject, String htmlMessage)
	{
		ArrayList<Player> ps = new ArrayList<Player>();
		HashSet<Player> hs = new HashSet<Player>();
		tos.forEach(t -> hs.addAll(t.getPlayers()));
		hs.forEach(p -> ps.add(p));
		sendEmail(ps, from, subject, htmlMessage);
	}
	
	public static void sendTeamsEmail(Team t1, Team t2, Player from, String subject, String htmlMessage)
	{
		ArrayList<Player> ps = new ArrayList<Player>();
		ps.addAll(t1.getPlayers());
		ps.addAll(t2.getPlayers());
		sendEmail(ps, from, subject, htmlMessage);
	}
	
	public static void sendEmail(Player to, Player from, String subject, String htmlMessage)
	{
		if (!ALLOW_EMAILS)
			return;
		if (to.isForfeit())
			return;
		try
		{
			String ref = getHood().emailReferenceFor(from);
			HtmlEmail email = Utils.newEmail(subject, htmlMessage + ref);
			email.addTo(to.getEmail());
			ParsonsRunUI.getScheduler().schedule(() -> { 
				try
				{
					println("Sending email to:" + to);
					email.send();
				}
				catch (EmailException e) 
				{
					println("Unable to send email to " + to + " '" + subject + "': " + e);
				}

			} , EMAIL_DELAY, TimeUnit.SECONDS);
		}
		catch (EmailException e) 
		{
			println("Unable to create email to " + to + " '" + subject + "': " + e);
		}
	}
	
	public static String compactPhone(String p)
	{
		return p.replaceAll("[^0-9]", "").trim();
	}
	
	public static String expandPhone(String s)
	{
		String p = compactPhone(s);
		if (p.length() < 10)
			return s;
		else
			return "(" + p.substring(0, 3) + ") " + p.substring(3, 6) + "-" + p.substring(6, 10);
	}
	
	protected static String getEmailContent(Object content) throws IOException, MessagingException
	{
		StringBuilder sb = new StringBuilder();
		getEmailContent(content, sb);
		return sb.toString();
	}
	
	protected static void getEmailContent(Object content, StringBuilder sb) throws IOException, MessagingException
	{
		InputStream in = null;
		try {
			if (content instanceof Multipart) 
			{
				Multipart multi = ((Multipart)content);
				int parts = multi.getCount();
				for (int j=0; j < parts; ++j)
				{
					MimeBodyPart part = (MimeBodyPart)multi.getBodyPart(j);
					if (part.getContent() instanceof Multipart) 
					{
						getEmailContent(part.getContent(), sb);
					}
					else
					{
						in = part.getInputStream();
						int k;
						while ((k = in.read()) != -1)
						{
							if (k == 10 || k == 13 || (k >= 32 && k <= 127))
								sb.append((char)k);
						}
					}
				}
			}
			else
				sb.append(content.toString());
		}
		finally
		{
			if (in != null) 
				in.close();
		}
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
	
	public static BufferedImage rotate(BufferedImage image, int _thetaInDegrees)
	{
	  /*
	   * Affline transform only works with perfect squares. The following
	   *   code is used to take any rectangle image and rotate it correctly.
	   *   To do this it chooses a center point that is half the greater
	   *   length and tricks the library to think the image is a perfect
	   *   square, then it does the rotation and tells the library where
	   *   to find the correct top left point. The special cases in each
	   *   orientation happen when the extra image that doesn't exist is
	   *   either on the left or on top of the image being rotated. In
	   *   both cases the point is adjusted by the difference in the
	   *   longer side and the shorter side to get the point at the 
	   *   correct top left corner of the image. NOTE: the x and y
	   *   axes also rotate with the image so where width > height
	   *   the adjustments always happen on the y axis and where
	   *   the height > width the adjustments happen on the x axis.
	   *   
	   */
		double _theta = 0.0;
		switch (_thetaInDegrees)
	    {
    	case 90:
    		_theta = 1.5707963268;
    		break;
    	case 180:
    		_theta = 3.1415926536;
    		break;
    	case 270:
    		_theta = 4.7123889804;
    		break;
	    }
	  AffineTransform xform = new AffineTransform();

	  if (image.getWidth() > image.getHeight())
	  {
	    xform.setToTranslation(0.5 * image.getWidth(), 0.5 * image.getWidth());
	    xform.rotate(_theta);

	    int diff = image.getWidth() - image.getHeight();

	    switch (_thetaInDegrees)
	    {
	    case 90:
	      xform.translate(-0.5 * image.getWidth(), -0.5 * image.getWidth() + diff);
	      break;
	    case 180:
	      xform.translate(-0.5 * image.getWidth(), -0.5 * image.getWidth() + diff);
	      break;
	    default:
	      xform.translate(-0.5 * image.getWidth(), -0.5 * image.getWidth());
	      break;
	    }
	  }
	  else if (image.getHeight() > image.getWidth())
	  {
	    xform.setToTranslation(0.5 * image.getHeight(), 0.5 * image.getHeight());
	    xform.rotate(_theta);

	    int diff = image.getHeight() - image.getWidth();

	    switch (_thetaInDegrees)
	    {
	    case 180:
	      xform.translate(-0.5 * image.getHeight() + diff, -0.5 * image.getHeight());
	      break;
	    case 270:
	      xform.translate(-0.5 * image.getHeight() + diff, -0.5 * image.getHeight());
	      break;
	    default:
	      xform.translate(-0.5 * image.getHeight(), -0.5 * image.getHeight());
	      break;
	    }
	  }
	  else
	  {
	    xform.setToTranslation(0.5 * image.getWidth(), 0.5 * image.getHeight());
	    xform.rotate(_theta);
	    xform.translate(-0.5 * image.getHeight(), -0.5 * image.getWidth());
	  }

	  AffineTransformOp op = new AffineTransformOp(xform, AffineTransformOp.TYPE_BILINEAR);

	  BufferedImage newImage = new BufferedImage(image.getHeight(), image.getWidth(), image.getType());
	  return op.filter(image, newImage);
	}
	
	
	
	public static String getProperty(String key)
	{
		return System.getProperty(key, getDefaults().getProperty(key));
	}
	
	public static HtmlEmail newEmail(String subject, String htmlMessage) throws EmailException
	{
		HtmlEmail email = makeEmail(subject);
		String pre = "<span style='font-size:0.75em'>(Please do not reply to this email.  '" + Player.SYSTEM_EMAIL + "' is not monitored)</span><br><br>";
		email.setHtmlMsg(pre + htmlMessage);
		return email;
	}
	
	public static HtmlEmail newGroupEmail(String subject, String htmlMessage) throws EmailException
	{
		HtmlEmail email = makeEmail(subject);
		String pre = "<span style='font-size:0.75em'>(Please do not include this email '" + Player.SYSTEM_EMAIL + "' if you reply. &nbsp;This address is not monitored)</span><br><br>";
		String post = "";
		if (getBaseUrl() != null)
		{
			post = "<br><br><small><a href='" + getBaseUrl() + "'>Parsons Run Tennis</a></small>";
		}
		email.setHtmlMsg(pre + htmlMessage + post);
		return email;
	}
	
	protected static HtmlEmail makeEmail(String subject) throws EmailException
	{
		HtmlEmail email = new HtmlEmail();
		email.setHostName(SMTP); // "outbound.att.net");
		email.setSmtpPort(465);
		email.setSSLOnConnect(true);
		email.setAuthentication(Player.SYSTEM_EMAIL, Player.SYSTEM_EMAIL_PASSWORD);
		email.setFrom(Player.SYSTEM_EMAIL);
		email.setSubject(subject);
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
	
	public static String findGetterName(Class clazz, String name) throws IntrospectionException, NoSuchFieldException, NoSuchMethodException {
	    Method getter = findGetter(clazz, name);
	    if (getter == null) throw new NoSuchMethodException(clazz+" has no "+name+" getter");
	    return getter.getName();
	}

	public static Method findGetter(Class clazz, String name) throws IntrospectionException, NoSuchFieldException {
	    BeanInfo info = Introspector.getBeanInfo(clazz);
	    for ( PropertyDescriptor pd : info.getPropertyDescriptors() )
	        if (name.equals(pd.getName())) return pd.getReadMethod();
	    throw new NoSuchFieldException(clazz+" has no field "+name);
	}

	public static String findSetterName(Class clazz, String name) throws IntrospectionException, NoSuchFieldException, NoSuchMethodException {
	    Method setter = findSetter(clazz, name);
	    if (setter == null) throw new NoSuchMethodException(clazz+" has no "+name+" setter");
	    return setter.getName();
	}

	public static Method findSetter(Class clazz, String name) throws IntrospectionException, NoSuchFieldException {
	    BeanInfo info = Introspector.getBeanInfo(clazz);
	    for ( PropertyDescriptor pd : info.getPropertyDescriptors() )
	        if (name.equals(pd.getName())) return pd.getWriteMethod();
	    throw new NoSuchFieldException(clazz+" has no field "+name);
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

	protected static void primSaveData(String name)
	{
		if (ALLOW_DB_SAVES)
		{
			long t1 = System.currentTimeMillis();
			try 
			{
				File f = new File(name);
				if (f.length() > MIN_BYTES)
					rotateFile(name);
				FileOutputStream file = new FileOutputStream(f);
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
				if (f.length() < MIN_BYTES)
				{
					println("WARNING, WARNING!!!!   Database save file '" + name + "' < " + MIN_BYTES + " bytes!!!!");
				}
			}
		    catch (IOException ex)
			{
		    	println("Cannot save data: " + ex);
		    }
			long t2 = System.currentTimeMillis();
			println("DB saved.  " + (t2 - t1) + "ms");
		}
	}
	
	protected static void primBackupData(String name)
	{
		if (ALLOW_DB_SAVES)
		{
			String b = name + ".backup";
			File f = new File(name);
			if (f.length() > MIN_BYTES)
			{
				rotateFile(b);
			}
			try
			{
				copyFile(f, new File(b));
				println("Nightly backup to '" + b + "' complete.");
			} 
			catch (Exception e)
			{
				println("WARNING!   Unable to copy from '" + name + "' to '" + b + "' " + e);
			}
		}
	}
	
	private static void copyFile(File source, File dest) throws IOException 
	{
		FileChannel inputChannel = null;
		FileChannel outputChannel = null;
		try {
			inputChannel = new FileInputStream(source).getChannel();
			outputChannel = new FileOutputStream(dest).getChannel();
			outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
		} finally {
			inputChannel.close();
			outputChannel.close();
		}
	}
	
	public static void saveData()
	{
		synchronized (Neighborhood.getSingleton())
		{
			primSaveData(dbName());
		}
	}
	
	public static void backupData()
	{
		synchronized (Neighborhood.getSingleton())
		{
			primBackupData(dbName());
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
