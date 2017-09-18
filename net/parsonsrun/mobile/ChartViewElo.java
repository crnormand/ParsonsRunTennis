package net.parsonsrun.mobile;

import com.vaadin.addon.charts.model.AxisTitle;
import com.vaadin.addon.charts.model.VerticalAlign;
import com.vaadin.addon.charts.model.YAxis;

import net.parsonsrun.domain.Tournament;

public class ChartViewElo extends ChartView
{

	@Override
	public String getCaption()
	{
		return "ELO";
	}
	
	public String getButtonLabel()
	{
		return "ELO";
	}
	
	public String getDataFormat()
	{
		return null;
	}
	
	public String getYAxisTitle()
	{
		return "ELO Score";
	}
	
	public ChartViewElo(Tournament t)
	{
		super(t);
	}
	
	public void addXAxis()
	{
	    int sz = tourny.getRounds().size();
	    String cats[] = new String[sz + 1];
	    cats[0] = "Start";
	    for (int i = 1; i <= sz; i++)
	    {
	    	cats[i] = "Match #" + i;
	    }
	    configuration.getxAxis().setCategories(cats);
	}
	
	public void addData()
	{
		tourny.addEloData(configuration);
	}

}
