package net.parsonsrun.mobile;import java.util.ArrayList;

import com.vaadin.addon.touchkit.ui.NavigationButton;
import com.vaadin.addon.touchkit.ui.VerticalComponentGroup;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.renderers.HtmlRenderer;
import com.vaadin.ui.Grid.RowReference;

import net.parsonsrun.domain.*;

public abstract class LadderView extends LeaguesView
{
	protected Team team;
	protected VerticalComponentGroup group;
	protected Match pendingMatch;
	
	public LadderView(League l)
	{
		super(l);
	}
	
	public Ladder getLadder()
	{
		return (Ladder)getLeague();
	}
	
	public String toString()
	{
		return super.toString() + ":" + getLadder();
	}

	public String getCaption()
	{
		return getLadder().getName() + " (" + getLadder().getTeams().size() + " teams)";
	}
	
	public void buildUI()
	{
		content.addComponent(getSwipeReminder());
		group = new VerticalComponentGroup();
		content.addComponent(group);
		group.setWidth("100%");
		update();
	}
	
	protected abstract void buildSubUI();
	
	public void update()
	{
		updateUI(() -> { 
			if (group == null)
				return;
			group.removeAllComponents(); 
			Rung current = getLadder().getRungFor(getTeam());
			if (current != null && current.hasPending())
			{
				if (pendingMatch != null)
					pendingMatch.removeListener(this);
				pendingMatch = current.getPending().getMatch();
				pendingMatch.addListener(this);
				NavigationButton b = new NavigationButton("Update Challenge Match vs " + current.getPending().pendingTeamInfo(getTeam()), new MatchView(pendingMatch));
				b.setStyleName("rung-pending-self");
				group.addComponent(b);
			}
			buildSubUI();
			for (Rung r : getLadder().getRungs())
			{
				Label l = new Label(r.getUpDownHtml(), ContentMode.HTML);
				l.setWidth("80px");
				Label l2 = new Label(r.getTeam().getName());
				NavigationButton b = new NavigationButton(r.getButtonLabel(), new ChallengeView(current, r));
				b.setStyleName("small");
				HorizontalLayout layout = new HorizontalLayout();
				layout.addComponent(l);
				layout.addComponent(b);
				layout.setComponentAlignment(l, Alignment.MIDDLE_LEFT);
				layout.setExpandRatio(b, 1.0f);
		        layout.setSpacing(false);
		        layout.setMargin(false);
		        layout.setWidth("100%");
		        layout.addStyleName(r.getBackgroundClass(current));
		        group.addComponent(layout);
			}
		});
	}
	
	protected void finalize() throws Throwable 
	{
        try {
        	if (pendingMatch != null)
        		pendingMatch.removeListener(this);
        }
        catch(Throwable t) {
            throw t;
        }
        finally {
            super.finalize();
        }
    }


	public Team getTeam()
	{
		return team;
	}

	public void setTeam(Team team)
	{
		this.team = team;
	}
}
