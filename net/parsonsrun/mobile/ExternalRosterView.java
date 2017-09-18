package net.parsonsrun.mobile;

import com.vaadin.addon.touchkit.ui.VerticalComponentGroup;
import com.vaadin.data.util.MethodProperty;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import net.parsonsrun.Utils;
import net.parsonsrun.desktop.DesktopUI;
import net.parsonsrun.domain.*;

public class ExternalRosterView extends RosterView
{
	protected ExternalTeam team;
	
	public ExternalRosterView(ExternalTeam t)
	{
		super();
		setTeam(t);
		setPlayers(t.getRoster());
		setName(t.getName());
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
