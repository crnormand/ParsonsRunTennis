package net.parsonsrun;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import com.vaadin.ui.UI;

@WebListener 
public class BackgroundJobManager implements ServletContextListener {

    private ScheduledExecutorService scheduler;

    @Override
    public void contextInitialized(ServletContextEvent event) {
        scheduler = Executors.newSingleThreadScheduledExecutor();
		Date d = new Date();
		SimpleDateFormat fm = new SimpleDateFormat("m");
		int min = Integer.parseInt(fm.format(d));
		int wait = Math.max(0, 55 - min);
        scheduler.scheduleAtFixedRate(new BackgroundJobHourly(), wait, 60, TimeUnit.MINUTES);
    	System.out.println("Starting Background Job Manager " + d + ", waiting " + wait + " minutes");  // Background job at :55 every hour
    	ParsonsRunUI.setScheduler(scheduler);
    	Utils.initializeSystem();
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
    	System.out.println("Stopping background job manager");
        scheduler.shutdownNow();
    }
}