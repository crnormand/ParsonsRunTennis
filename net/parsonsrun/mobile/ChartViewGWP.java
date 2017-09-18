package net.parsonsrun.mobile;

import com.vaadin.addon.charts.model.ChartType;
import com.vaadin.addon.charts.model.PlotOptionsLine;
import com.vaadin.addon.charts.model.YAxis;

import net.parsonsrun.domain.Tournament;

public class ChartViewGWP extends ChartView
{

	public ChartViewGWP(Tournament t)
	{
		super(t);
	}
	
	public String getButtonLabel()
	{
		return "G%";
	}
	
	protected String getToolTipFormat()
	{
		return "<span style=\"color:{point.color}\">\u25CF</span> {series.name}: <b>{point.y}%</b>";
	}

	
	public void addData() throws Exception
	{
		tourny.addStandingsData(s -> s.getGamesWinPercentage(), configuration);
	}
	
	public String getDataFormat()
	{
		return "{point.y:.1f}%";
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
		return "Games Won%";
	}
	
	public YAxis addYAxis()
	{
		YAxis y = super.addYAxis();
		y.setCeiling(100.0);
		y.setFloor(0.0);
		y.setExtremes(0.0, 100.0, true);
		return y;
	}

	@Override
	public String getCaption()
	{
		return "Games Won%";
	}

}
