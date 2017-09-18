package net.parsonsrun.desktop;

import java.nio.file.FileSystems;
import java.nio.file.Files;

import com.vaadin.ui.*;
import net.parsonsrun.*;

public class LogView extends DesktopBaseView
{
	protected TabSheet sheet;
	protected TextArea log;
	
	public void buildUI()
	{
		Utils.getSingleton().addListener(this);
		addBack();
		sheet = new TabSheet();
		log = new TextArea();
		sheet = new TabSheet();
		sheet.addTab(add(log), "Current Log");
		addPrevs();
		addComponent(sheet);
		update();
	}
	
	protected Panel add(TextArea ta)
	{
		Panel p = new Panel();
		p.setWidth("100%");
		p.setHeight(getBrowserHeight() - 150,Unit.PIXELS);
		VerticalLayout v = new VerticalLayout();
		v.setSizeFull();
		ta.setSizeFull();
		ta.setCursorPosition(ta.getValue().length());
		ta.setReadOnly(true);
		v.addComponent(ta);
		p.setContent(v);
		return p;
	}
	
	protected void addPrevs()
	{
		for (int i = 0; i <= 9; i++)
		{
			TextArea log = new TextArea();
//			log.setWidth("100%");
//			log.setHeight(getBrowserHeight() - 150,Unit.PIXELS);
			log.setValue(readLog(i));
//			log.setCursorPosition(log.getValue().length());
//			log.setReadOnly(true);
			sheet.addTab(add(log), "Log." + i);
		}
	}
	
	protected String readLog(int i)
	{
		try
		{
			return new String(Files.readAllBytes(FileSystems.getDefault().getPath(Utils.logFilename() + "." + i)));
		} catch (Exception e)
		{
			return Utils.getStackTrace(e);
		}
	}
	
	public void update()
	{
		updateUI(() -> {
			log.setReadOnly(false);
			log.setValue(Utils.getLog());
			log.setCursorPosition(log.getValue().length());
			log.setReadOnly(true);
		});
	}
	
	protected void finalize() throws Throwable 
	{
        try {
        	Utils.getSingleton().removeListener(this);
        }
        catch(Throwable t) {
            throw t;
        }
        finally {
            super.finalize();
        }
    }

}
