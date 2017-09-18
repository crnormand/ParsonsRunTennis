package net.parsonsrun.mobile;

import com.vaadin.addon.charts.model.YAxis;

import net.parsonsrun.domain.Tournament;

public class ChartViewMWP extends ChartView
{

	public ChartViewMWP(Tournament t)
	{
		super(t);
	}

	public String getButtonLabel()
	{
		return "M%";
	}

	public void addData() throws Exception
	{
		tourny.addStandingsData(s -> s.getMatchWinPercentage(), configuration);
	}
	
	protected String getToolTipFormat()
	{
		return "<span style=\"color:{point.color}\">\u25CF</span> {series.name}: <b>{point.y}%</b>";
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
		return "Matches Won%";
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
		return "Matches Won%";
	}

}
