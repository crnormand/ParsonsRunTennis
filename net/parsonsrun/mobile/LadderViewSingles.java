package net.parsonsrun.mobile;

import com.vaadin.addon.touchkit.ui.Popover;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import net.parsonsrun.domain.*;

public class LadderViewSingles extends LadderView
{
	public LadderViewSingles(League l)
	{
		super(l);
	}
	
	protected void init()
	{
		super.init();
		Team t = getHood().getSingles(getLoginUser());
		if (getLadder().contains(t))
			setTeam(t);
	}
	
	protected boolean hasTeam()
	{
		return getTeam() != null;
	}
	
	protected void addTeamLabel()
	{
		StringBuilder s = new StringBuilder();
		s.append("<div class='label-centered'>");
		s.append(FontAwesome.LIST_OL.getHtml());
		s.append(" Ladder for ");
		s.append("<span class='match-self'>");
		s.append(getTeam().getName());
		s.append("</span><div>");
		Label l = new Label(s.toString(), ContentMode.HTML);
		HorizontalLayout h = new HorizontalLayout();
		h.setWidth("100%");
		h.setMargin(true);
		h.addComponent(l);
		group.addComponent(h);
	}
	
	protected void addAddButton()
	{
		StringBuilder s = new StringBuilder();
		s.append("<div class='label-centered'>");
		s.append(FontAwesome.LIST_OL.getHtml());
		s.append(" You are not currently on this ladder<div>");
		Label l = new Label(s.toString(), ContentMode.HTML);

		VerticalLayout v = new VerticalLayout();
		v.setWidth("100%");
		v.setMargin(true);
		v.addComponent(l);
		
		Button b = new Button("Add me to the Ladder!");
		b.addClickListener(e -> addNewTeam());
		v.addComponent(b);
		group.addComponent(v);
	}
	
	protected void addNewTeam()
	{
		openNewMemberPopover();
		Team t = Neighborhood.getSingleton().getSingles(getLoginUser());
		if (!getLadder().contains(t))
		{
			setTeam(t);
			getLadder().addNewTeam(t);
			update();
		}
	}
	
	protected void buildSubUI()
	{
		if (hasTeam())
		{
			addTeamLabel();
		}
		else
		{
			addAddButton();
		}
	}

	protected void openNewMemberPopover()
	{
		StringBuilder s = new StringBuilder();
		s.append("<div><div class='label-centered'><b>Welcome to ");
		s.append(getLadder().getName());
		s.append("</b></div><br>");
		s.append("<br>");
		if (getLadder().isStrict())
		{
			s.append("This is a TOURNAMENT Ladder.   You may challenge ");
			s.append(getLadder().getChallengeRungs());
			s.append(" rungs above you.   They will be highlighted in <b><span class='rung-challenge'>YELLOW</span></b>.");
			s.append("<br><br>If you win your challenge, you will move up into their position on the ladder, and ");
			s.append("everyone between your old position and your new position will be bumped down one rung.");
			s.append("&nbsp;&nbsp;If you lose your challenge, you will stay in your current position.");
			s.append("<br><br>Note: When you first enter the ladder, you may challenge anyone.");
			s.append("<br><br>Rungs highlighted in <b><span class='rung-pending'>GRAY</span></b>");
			s.append(" are currently in a challenge, and cannot be challenged.");
			s.append("<br><br>A rung highlighted in <b><span class='rung-pending-self'>GREEN</span></b>");
			s.append(" is currently being challenged BY YOU!");
		}
		else
		{
			s.append("This is an OPEN ladder. You may challenge anyone on the ladder highlighted in <b><span class='rung-challenge'>YELLOW</span></b>.");
			s.append("<br><br>If you win your challenge and you challenged someone above you, you will move up into their position on the ladder, and ");
			s.append("everyone between your old position and your new position will be bumped down one rung.");
			s.append("&nbsp;&nbsp;If you lose your challenge, you will stay in your current position.");
			s.append("<br><br>If you challenge someone below you and win, you will stay in your current position.");
			s.append("&nbsp;&nbsp;If you lose your challenge, they will move into your position and you will be bumped down one rung.");
			s.append("<br><br>Rungs highlighted in <b><span class='rung-pending'>GRAY</span></b>");
			s.append(" are currently in a challenge, and cannot be challenged.");
			s.append("<br><br>A rung highlighted in <b><span class='rung-pending-self'>GREEN</span></b>");
			s.append(" is currently being challenged BY YOU!");
		}
		s.append("<br><br><div class='label-centered-large'>Good Luck!</div>");
		s.append("<br></div>");
		openPopover(s.toString());
	}
}
