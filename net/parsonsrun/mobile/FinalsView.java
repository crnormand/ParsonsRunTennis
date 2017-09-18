package net.parsonsrun.mobile;

import java.util.*;
import com.vaadin.ui.VerticalLayout;

import net.parsonsrun.domain.League;
import net.parsonsrun.domain.Standing;
import net.parsonsrun.domain.Team;

public class FinalsView extends MobileBaseView
{
	protected League tourny;
	
	public FinalsView(League t)
	{
		tourny = t;
	}
	
	public String getCaption()
	{
		return tourny.getName() + " finals";
	}
	
	public String toString()
	{
		return super.toString() + ":" + tourny;
	}

	public void buildUI()
	{
		int w = getBrowserWidth();
		int h = getBrowserHeight();
		VerticalLayout v = new VerticalLayout();
		v.setSizeFull();
		content.addComponent(v);
	}
}
