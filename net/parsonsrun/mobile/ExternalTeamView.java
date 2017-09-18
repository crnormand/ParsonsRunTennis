package net.parsonsrun.mobile;

import com.vaadin.addon.touchkit.ui.NavigationButton;
import com.vaadin.addon.touchkit.ui.VerticalComponentGroup;

import net.parsonsrun.domain.*;

public class ExternalTeamView extends MobileBaseView
{
	protected ExternalTeam team;
	
	public ExternalTeamView(ExternalTeam t)
	{
		setTeam(t);
	}
	
	@Override
	public String getCaption()
	{
		return getTeam().getName() + " (" + getTeam().getCaptainNames() + ")";
	}
	
	public String toString()
	{
		return super.toString() + " " + getTeam();
	}

	@Override
	public void buildUI()
	{
		content.addComponent(getSwipeReminder());
		VerticalComponentGroup group = new VerticalComponentGroup(getTeam().getName());
		content.addComponent(group);
		int w = getTeam().getNextWeekIndex() + 1;
		if (w > getTeam().getNumberOfWeeks())
			w -= 1;
		NavigationButton sched = new NavigationButton("Schedule (Current week: " + w + " of " + getTeam().getNumberOfWeeks() + ")");
		sched.addClickListener(e -> openSchedule());
		group.addComponent(sched); 
		group.addComponent(new NavigationButton(new ExternalRosterView(getTeam())));
		if (getTeam().includes(getLoginUser()))
			group.addComponent(new NavigationButton(new ExternalAvailView(getTeam())));
		if (!getTeam().shouldHideAvailabilities() || canSetup())
			group.addComponent(new NavigationButton(new ExternalAvailAllView(getTeam())));
	}
	
	protected boolean canSetup()
	{
		Player p = getLoginUser();
		if (p.isAdmin())
			return true;
		return getTeam() != null && getTeam().includesCaptain(p);
	}
	
	protected void playItForward()
	{
		if (getParentUI().isAvailAction() && getTeam().includes(getLoginUser()) && !getTeam().hasCheckedAvailability(getLoginUser()))
			navigateTo(new ExternalAvailView(getTeam()));
		
		if (getParentUI().isConfirmRejectAction() && getTeam().includes(getLoginUser()))
		{
			openSchedule();
		}
		if (getParentUI().isScheduleAction())
		{
			openSchedule();
		}
	}
	
	protected void openSchedule()
	{
		int week0 = getTeam().getNextWeekIndex();
		if (week0 >= getTeam().getNumberOfWeeks())
			week0 -= 1;
		for (int i = 0; i <= week0; i++)
		{
			navigateTo(new ExternalScheduleView(getTeam(), i));
		}
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
