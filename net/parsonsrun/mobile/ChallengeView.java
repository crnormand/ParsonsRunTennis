package net.parsonsrun.mobile;

import com.vaadin.addon.touchkit.ui.NavigationButton;
import com.vaadin.addon.touchkit.ui.Popover;
import com.vaadin.addon.touchkit.ui.VerticalComponentGroup;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import net.parsonsrun.ClickLabel;
import net.parsonsrun.domain.Challenge;
import net.parsonsrun.domain.Ladder;
import net.parsonsrun.domain.Match;
import net.parsonsrun.domain.Rung;
import net.parsonsrun.domain.Rung.RungState;
import net.parsonsrun.domain.Team;

public class ChallengeView extends MobileBaseView
{
	protected VerticalComponentGroup group;
	protected Rung current;
	protected Rung rung;
	protected Match pendingMatch;
	
	public ChallengeView(Rung c, Rung r)
	{
		current = c;
		rung = r;
	}
	
	protected Ladder getLadder()
	{
		return rung.getLadder();
	}
	
	protected Team getTeam()
	{
		return current.getTeam();
	}

	@Override
	public String getCaption()
	{
		return rung.getName();
	}
	
	protected String numEvents()
	{
		return " <small>(" + (rung.getPreviousStates().size() + 1) + " events)</small>"; 
	}

	public void buildUI()
	{
		Label l = new Label("<div class='label-centered-large'>" + rung.getTeam().getFullName() +  numEvents() + "</div>", ContentMode.HTML);
		VerticalLayout v = new VerticalLayout();
		v.setWidth("100%");
		v.setMargin(true);
		v.addComponent(l);
		content.addComponent(v);
		group = new VerticalComponentGroup();
		content.addComponent(group);
		group.setWidth("100%");
		update();
	}
	
	protected void challenge()
	{
		saveWhile(() -> {
			Challenge c = getLadder().createChallenge(current, rung);
			StringBuilder s = new StringBuilder();
			s.append("On the ladder '");
			s.append(getLadder().getName());
			s.append("', you have been challenged by ");
			s.append(c.getChallenger().getName());
//			getParentUI().sendEmail(c.getChallengee().getTeam(), "You have been challenged!", htmlMessage);
		});
		StringBuilder s = new StringBuilder();
		s.append("<div><div class='label-centered'><b>Challenge Created!</b></div><br>");
		s.append("<br><br>You have ");
//		s.append(getLadder().getChallengeDays());
		s.append(" days to resolve this challenge.");
		s.append("<br><br><br>Good luck!</div>");
		openPopover(s.toString(), () -> { navigateBack(); } );
	}
	
	public void update()
	{
		updateUI(() -> { 
			group.removeAllComponents();
			if (rung.hasPending())
			{
				if (getTeam().canUpdateChallenge(rung))
				{
					pendingMatch = rung.getPending().getMatch();
					pendingMatch.addListener(this);
					NavigationButton b = new NavigationButton("Update Challenge Match vs " + rung.getPending().pendingTeamInfo(getTeam()), new MatchView(pendingMatch));
					b.setStyleName("rung-pending-self");
					group.addComponent(b);
				}
			}
			else
			{
				if (current != null && current.canChallenge(rung))
				{
					NavigationButton b = new NavigationButton("Challenge " + rung.getTeam().getName() + "?");
					b.setStyleName("rung-pending-self");
					b.addClickListener(e -> challenge());
					group.addComponent(b);
				}
			}
			RungState cur = rung.getCurrentRungState();
			RungState next = null;
			for (RungState r :rung.getPreviousStates())
			{
				next = r;
				group.addComponent(rungComponent(cur, next));
				cur = next;
			}
			if (next == null)
				group.addComponent(rungComponent(cur, null));
			else
				group.addComponent(rungComponent(next, null));
		});
	}
	
	protected HorizontalLayout rungComponent(RungState cur, RungState next)
	{
		Label l = new Label(rung.getUpDownHtml(cur, next), ContentMode.HTML);
		l.setWidth("80px");
		ClickLabel c = new ClickLabel(cur.detailedEventInfo(rung.getTeam()));
		HorizontalLayout layout = new HorizontalLayout();
		layout.addComponent(l);
		layout.addComponent(c);
		layout.setComponentAlignment(l, Alignment.MIDDLE_LEFT);
		layout.setExpandRatio(c, 1.0f);
        layout.setSpacing(false);
        layout.setMargin(false);
        layout.setWidth("100%");
        //layout.addStyleName(r.getBackgroundClass(current, team));
        return layout;
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

}
