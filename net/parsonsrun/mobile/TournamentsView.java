package net.parsonsrun.mobile;

import com.vaadin.addon.touchkit.ui.NavigationButton;
import com.vaadin.addon.touchkit.ui.VerticalComponentGroup;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Label;

import net.parsonsrun.domain.Neighborhood;
import net.parsonsrun.domain.Tournament;
import net.parsonsrun.domain.UpdateListener;

public class TournamentsView extends MobileBaseView
{
	VerticalComponentGroup group;
		
	@Override
	public String getCaption()
	{
		return "Tournaments (" + getHood().getTournaments(getLoginUser()).size() + ")";
	}

	@Override
	public void buildUI()
	{
		content.addComponent(getSwipeReminder());
		group = new VerticalComponentGroup("Tournaments");
		content.addComponent(group);
		group.setWidth("100%");
		CheckBox b = new CheckBox("Only show My tournaments");
		b.setValue(getLoginUser().isOnlyShowTournament());
		b.addValueChangeListener(e -> onlyShowMine(b.getValue()));
		content.addComponent(b);
		update();
	}
	
	protected void playItForward()
	{
		if (getParentUI().isTournamentAction())
			navigateTo(new TournamentView(getCurrentTournament()));
	}
	
	protected void onlyShowMine(boolean b)
	{
		getLoginUser().setOnlyShowTournament(b);
		update();
	}

	
	public void update()
	{
		updateUI(() -> {
			if (group != null)
			{
				group.removeAllComponents();
				for (Tournament t : getHood().getTournaments(getLoginUser()))
				{
					group.addComponent(new NavigationButton(new TournamentView(t)));
				}
			}
		});
	}

}
