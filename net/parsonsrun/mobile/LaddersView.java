package net.parsonsrun.mobile;

import java.util.List;

import com.vaadin.addon.touchkit.ui.NavigationButton;
import com.vaadin.addon.touchkit.ui.NavigationView;
import com.vaadin.addon.touchkit.ui.VerticalComponentGroup;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Label;

import net.parsonsrun.domain.*;

public class LaddersView extends MobileBaseView
{
	VerticalComponentGroup group1;
	
	public String getCaption()
	{
		return "Ladders (" + getHood().getLadders(getLoginUser()).size() + ")";
	}
	
	@Override
	public void buildUI()
	{
		content.removeAllComponents();
		content.addComponent(getSwipeReminder());
		group1 = new VerticalComponentGroup("Ladders");
		group1.setWidth("100%");
		content.addComponent(group1);
		CheckBox b = new CheckBox("Only show My tournaments");
		b.setValue(getLoginUser().isOnlyShowTournament());
		b.addValueChangeListener(e -> onlyShowMine(b.getValue()));
		content.addComponent(b);
		update();
	}
	
	public synchronized void update()
	{
		super.update();
		updateUI(() -> {
			if (group1 != null)
			{
				group1.removeAllComponents();
				List<Ladder> l = getHood().getSinglesLadders(getLoginUser());
				if (!l.isEmpty())
				{
					group1.addComponent(new Label("Singles"));
					for (Ladder ladder : l)
					{
						group1.addComponent(new NavigationButton(new LadderViewSingles(ladder)));
					}
				}
				group1.addComponent(new Label(""));
				l = getHood().getDoublesLadders(getLoginUser());
				if (!l.isEmpty())
				{
					group1.addComponent(new Label("Doubles"));
					for (Ladder ladder : l)
					{
						group1.addComponent(new NavigationButton(new LadderViewDoubles(ladder)));
					}
				}
			}
		});
	}
	
	protected void onlyShowMine(boolean b)
	{
		getLoginUser().setOnlyShowLadder(b);
		update();
	}
}
