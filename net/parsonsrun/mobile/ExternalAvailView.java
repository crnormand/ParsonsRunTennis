package net.parsonsrun.mobile;

import com.vaadin.addon.touchkit.ui.Switch;
import com.vaadin.addon.touchkit.ui.VerticalComponentGroup;
import com.vaadin.server.ExternalResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import net.parsonsrun.desktop.DesktopUI;
import net.parsonsrun.domain.*;

public class ExternalAvailView extends MobileBaseView
{
	protected ExternalTeam team;
	protected VerticalComponentGroup group;
	protected Label label;
	
	public ExternalAvailView(ExternalTeam t)
	{
		setTeam(t);
	}
	
	@Override
	public String getCaption()
	{
		return "Set your Availability";
	}

	@Override
	public void buildUI()
	{
		content.addComponent(getSwipeReminder());
		Label l = new Label("<div style='text-align:center'>" + getTeam().getName() + "</div>", ContentMode.HTML);
		content.addComponent(l);
		label = new Label("", ContentMode.HTML);
		content.addComponent(label);
		group = new VerticalComponentGroup();
		content.addComponent(group);
		for (Lineup lu : getTeam().getLineups())
		{
			group.addComponent(getAvailWeek(lu));
		}
		getTeam().checkedAvailability(getLoginUser());
		updateLabel();
		clearAction();
	}
	
	protected void updateLabel()
	{
		int y = getTeam().getNumberAvailable(getLoginUser());
		int n = getTeam().getNumberUnAvailable(getLoginUser());
		int u = getTeam().getNumberUnCertain(getLoginUser());
		label.setValue("<div style='text-align:center'><small>[Available:" + y + " Not Available:" + n + " Don't know:" + u + "]</small></div>");
	}
	
	protected HorizontalLayout getAvailWeek(Lineup lu)
	{
		Player p = getLoginUser();
		HorizontalLayout h = new HorizontalLayout();
		h.setSizeFull();
		h.setSpacing(true);
		VerticalLayout v = new VerticalLayout();
		Label l = new Label(lu.getWeekString() + " : " + lu.getDateString());
		v.addComponent(l);
		if (lu.isAway() && !lu.getLocation().isEmpty())
		{
			Link lnk = new Link(lu.getName(), new ExternalResource(lu.getLocationHtml()));
			lnk.setTargetName("_blank");
			v.addComponent(lnk);
		}
		else
		{
			Label opp = new Label(lu.getName());
			opp.addStyleName(DesktopUI.HOME_BKGD);
			v.addComponent(opp);
		}
		h.addComponent(v);
		h.setExpandRatio(v, 0.6f);
		VerticalLayout v2 = new VerticalLayout();
		v2.setMargin(false);
		v2.setSpacing(false);
		CheckBox s = new CheckBox("Available", lu.isAvailable(p));
		s.setStyleName(MobileUI.THIN_CHECKBOX);
		s.setValue(!lu.notAvailable(getLoginUser()));
		s.setEnabled(getTeam().includes(getLoginUser()));
		h.addComponent(v2);
		h.setExpandRatio(v2, 0.3f);
		v2.addComponent(s);
		CheckBox u = new CheckBox("Don't know", lu.isUncertain(p));
		u.setStyleName(MobileUI.THIN_CHECKBOX);
		v2.addComponent(u);
		//h.setComponentAlignment(v, Alignment.TOP_RIGHT);
		s.addValueChangeListener(e -> saveWhile(() ->
		{
			boolean b = s.getValue();
			if (b)
			{
				lu.removeUnavailable(p);
				lu.removeUnCertain(p);
				u.setValue(false);
			}
			else
				lu.addUnavailable(p);
			updateLabel();
			println("Change avail for " + p + " " + lu.getWeekString() + " to " + b);
		}));
		u.addValueChangeListener(e -> saveWhile(() ->
		{
			boolean b = u.getValue();
			if (b)
			{
				lu.addUnCertain(p);
				lu.addUnavailable(p);
				s.setValue(false);
			}
			else
				lu.removeUnCertain(p);
			updateLabel();
			println("Change uncertain for " + p + " " + lu.getWeekString() + " to " + b);
		}));
		h.setEnabled(getTeam().getNextWeekIndex() < lu.getWeek());
		return h;
	}
	
	public ExternalTeam getTeam()
	{
		return team;
	}

	public void setTeam(ExternalTeam team)
	{
		this.team = team;
	}

}
