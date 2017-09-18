package net.parsonsrun.mobile;

import com.vaadin.addon.touchkit.ui.NavigationButton;
import com.vaadin.addon.touchkit.ui.VerticalComponentGroup;
import com.vaadin.ui.CheckBox;

import net.parsonsrun.domain.ExternalTeam;
import net.parsonsrun.domain.Tournament;

public class ExternalTeamsView extends MobileBaseView
{
	VerticalComponentGroup group;

	@Override
	public String getCaption()
	{
		return "ALTA/USTA Teams (" + getHood().getExternals().size() + ")";
	}

	@Override
	public void buildUI()
	{
		content.addComponent(getSwipeReminder());
		group = new VerticalComponentGroup("ALTA/USTA Teams");
		content.addComponent(group);
		group.setWidth("100%");
		CheckBox b = new CheckBox("Only show My teams");
		b.setValue(getLoginUser().isOnlyShowTeam());
		b.addValueChangeListener(e -> onlyShowMine(b.getValue()));
		content.addComponent(b);
		update();
	}
	
	protected void onlyShowMine(boolean b)
	{
		getLoginUser().setOnlyShowTeam(b);
		update();
	}
	
	protected void playItForward()
	{
		if (getParentUI().isExternalTeamAction())
			navigateTo(new ExternalTeamView(getCurrentExternalTeam()));
	}

	
	public void update()
	{
		updateUI(() -> {
			if (group != null)
			{
				group.removeAllComponents();
				for (ExternalTeam t : getHood().getExternals(getLoginUser()))
				{
					group.addComponent(new NavigationButton(new ExternalTeamView(t)));
				}
			}
		});
	}

}
