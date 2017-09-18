package net.parsonsrun.mobile;

import java.util.ArrayList;

import com.vaadin.addon.touchkit.ui.VerticalComponentGroup;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;

import net.parsonsrun.ClickLabel;
import net.parsonsrun.domain.Match;
import net.parsonsrun.domain.Player;
import net.parsonsrun.domain.Round;
import net.parsonsrun.domain.UpdateListener;

public class MatchesView extends MobileBaseView
{
	protected ArrayList<Match> matches;
	protected String name;
	protected VerticalComponentGroup currentPage;
	protected Match current;
	
	public MatchesView(String n, ArrayList<Match> m)
	{
		name = n;
		matches =  m;
	}
	
	public String getCaption()
	{
		return name;
	}
	
	@Override
	public void buildUI()
	{
		content.addComponent(getSwipeReminder());
    	Label l = new Label("<p>" + name + "</p>", ContentMode.HTML);
        l.setStyleName(TEXT_DARK);
        content.addComponent(l);
		currentPage = new VerticalComponentGroup();
		currentPage.setWidth("100%");
		content.addComponent(currentPage);
		update();
	}
	
	protected void subBuildUI()
	{
		
	}
	
	public void update()
	{
		super.update();
		updateUI(() -> {
			currentPage.removeAllComponents();
			subBuildUI();
			for (Match m : matches)
			{
				ClickLabel l = new ClickLabel(m.getMobileHtml());
				l.addClickListener(e -> matchSelected(m));
				currentPage.addComponent(l);
			}
		});
	}
	
	protected void matchSelected(Match m)
	{
		if (m.isBye())
			Notification.show("Unable to update a BYE match", Type.ERROR_MESSAGE);
		else
		{
			String warning = m.canEdit(getLoginUser());
			if (warning == null)
			{
				if (current != null)
	        		current.removeListener(this);
				current = m;
				current.addListener(this);
				navigateTo(new MatchView(m));
			}
			else
				Notification.show(warning, Type.ERROR_MESSAGE);
		}
	}
	
	protected void finalize() throws Throwable 
	{
        try {
        	if (current != null)
        		current.removeListener(this);
        }
        catch(Throwable t) {
            throw t;
        }
        finally {
            super.finalize();
        }
    }

}
