package net.parsonsrun.mobile;

import com.vaadin.ui.*;
import net.parsonsrun.domain.*;

public class ExternalAvailAllView extends MobileBaseView
{
	protected ExternalTeam team;
	protected Table table;
		
	public ExternalAvailAllView(ExternalTeam t)
	{
		setTeam(t);
	}
	
	@Override
	public String getCaption()
	{
		String s = getTeam().shouldHideAvailabilities() ? "* " : "";
		return s + "Team Availablity (for " + getTeam().getRoster().size() + " members)";
	}

	@Override
	public void buildUI()
	{
		content.addComponent(getSwipeReminder());
		table = new Table();
		content.addComponent(table);
		table.setStyleName("standings");
		table.setWidth("100%");
		table.setSortEnabled(false);
//		table.addItemClickListener(e -> teamSelected((Integer)e.getItemId()));
		table.addContainerProperty("Week #", String.class, null);
		int wks = getTeam().getNumberOfWeeks();
		String[] hdrs = new String[wks + 1];
		hdrs[0] = "Name";
		for (int i = 1 ; i <= wks; i++)
		{
			String col = "" + i;
			hdrs[i] = col;
			table.addContainerProperty(col,  String.class, null);
			table.setColumnWidth(col, 18);
		}
		table.setPageLength(0);
		update();
	}

	public void update()
	{
		if (!built)
			return;
		updateUI(() -> {
			table.removeAllItems();
			int wks = getTeam().getNumberOfWeeks();
			int id = 0;
			Object [] row = new Object[wks + 1];
			row[0] = "Available (X):";
			int i = 1;
			for (Lineup lu : getTeam().getLineups())
				row[i++] = "" + getTeam().getAvailableFor(lu);
			table.addItem(row, id++);
			for (Player p : getTeam().getRoster())
			{
				row = new Object[wks + 1];
				row[0] = p.getIdName() + (getTeam().hasCheckedAvailability(p) ? "" : "*");
				i = 1;
				for (Lineup lu : getTeam().getLineups())
					row[i++] = lu.isAvailable(p) ? "X" : (lu.isUncertain(p) ? "?" : " ");
				table.addItem(row, id++);
			}
		});
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
