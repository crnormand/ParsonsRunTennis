package net.parsonsrun;

import java.text.SimpleDateFormat;
import java.util.Date;

import net.parsonsrun.domain.Neighborhood;

public class BackgroundJobHourly implements Runnable 
{	
    public void run() 
    {
    	try
    	{
    		Neighborhood n = Neighborhood.getSingleton();
    		if (n != null)
    		{
    			synchronized (n)
    			{
					Date d = new Date();
					n.processHourly(d);
					SimpleDateFormat fd = new SimpleDateFormat("u");
					SimpleDateFormat fh = new SimpleDateFormat("H");
					int day = Integer.parseInt(fd.format(d));
					int hour = Integer.parseInt(fh.format(d));
					if (hour == 1)
						n.processNightly(d);   // Every morning between 1am and 2am (1:55am)
					if (day == 5 && hour == 0) // Friday morning between midnight and 1am (12:55am)
						n.processFriday1am(d);
					if (day == 7 && hour == 0) // Sunday morning between midnight and 1am (12:55am)
						n.processSunday1am(d);
					if (day == 7 && hour == 23) // Sunday night between 11 and midnight (11:55pm)
						n.processSundayMidnight(d);
					if (day == 1 && hour == 0) // Monday morning between midnight and 1am (12:55am)
						n.processMonday1am(d);
    			}
    		}
    	} catch (Exception e)
    	{
    		Utils.println("Caught exception in background process: " + e);
    		Utils.println(Utils.getStackTrace(e));
    	}
    }
}
