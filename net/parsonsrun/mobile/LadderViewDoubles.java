package net.parsonsrun.mobile;

import com.vaadin.addon.touchkit.ui.Popover;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;

import net.parsonsrun.domain.*;

public class LadderViewDoubles extends LadderView
{
	public LadderViewDoubles(League l)
	{
		super(l);
	}
	protected void buildSubUI()
	{
		
	}
	protected void openNewMemberPopover()
	{
		Popover p = new Popover();
		CssLayout layout = new CssLayout();
		p.setContent(layout);
		StringBuilder s = new StringBuilder();
		s.append("<div><div class='label-centered'><b>Welcome to ");
		s.append(getLadder().getName());
		s.append("</b></div><br>");
		s.append("<br>");
		s.append("You may challenge rungs above you that are highlighted in <b><span class='rung-challenge'>YELLOW</span></b>");
		s.append("<br><br>Note: When you first enter the ladder, you may challenge anyone.");
		s.append("Rungs highlighted in <b><span class='rung-pending'>GRAY</span></b>");
		s.append("are currently in a challenge, and cannot be challenged.");
		s.append("</div>");
		Label l = new Label(s.toString(), ContentMode.HTML);
		layout.addComponent(l);
		Button b = new Button("OK!");
		b.addClickListener(e -> p.close());
		layout.addComponent(b);
		p.showRelativeTo(this);
	}
}
