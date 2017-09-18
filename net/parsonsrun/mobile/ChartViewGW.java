package net.parsonsrun.mobile;

import com.vaadin.addon.charts.model.YAxis;

import net.parsonsrun.domain.Tournament;

public class ChartViewGW extends ChartView
{

	public ChartViewGW(Tournament t)
	{
		super(t);
	}
	
	public String getButtonLabel()
	{
		return "G";
	}
	
	public void addData() throws Exception
	{
		tourny.addStandingsData(s -> s.getGamesWon(), configuration);
	}
	
	public String getDataFormat()
	{
		return "{point.y}";
	}
	public void addXAxis()
	{
	    int sz = tourny.getRounds().size();
	    String cats[] = new String[sz];
	    for (int i = 0; i < sz; i++)
	    {
	    	cats[i] = "Match #" + (i + 1);
	    }
	    configuration.getxAxis().setCategories(cats);
	}

	public String getYAxisTitle()
	{
		return "Games Won";
	}
	
	public String getCaption()
	{
		return "Games Won";
	}

}
