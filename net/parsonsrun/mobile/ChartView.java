package net.parsonsrun.mobile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.model.*;

import net.parsonsrun.domain.League;
import net.parsonsrun.domain.Match;
import net.parsonsrun.domain.Round;
import net.parsonsrun.domain.Team;
import net.parsonsrun.domain.Tournament;

public abstract class ChartView extends MobileBaseView
{
	protected Configuration configuration;
	protected Tournament tourny;
	
	public ChartView(Tournament t)
	{
		tourny = t;
	}
	
	protected abstract void addData() throws Exception;
	
	protected abstract void addXAxis();
	
	protected YAxis addYAxis()
	{
	    YAxis yAxis = configuration.getyAxis();
	    yAxis.setTitle(new AxisTitle(getYAxisTitle()));
	    yAxis.getTitle().setAlign(VerticalAlign.MIDDLE);
	    return yAxis;
	}
	
	public abstract String getButtonLabel();
	
	public String toString()
	{
		return super.toString() + ":" + tourny;
	}
	
	protected String getToolTipFormat()
	{
		return "<span style=\"color:{point.color}\">\u25CF</span> {series.name}: <b>{point.y}</b>";
	}
	
	protected abstract String getYAxisTitle();

	
	protected abstract String getDataFormat();

	@Override
	public void buildUI()
	{
		Chart chart = new Chart();
		content.addComponent(chart);
//		chart.setHeight((getBrowserHeight() - 70) + "px");
//	    chart.setWidth("100%");
		chart.setSizeFull();

	    configuration = chart.getConfiguration();
	    configuration.getChart().setType(ChartType.LINE);
//	    configuration.getChart().setMarginRight(130);
//	    configuration.getChart().setMarginBottom(25);

	    configuration.getTitle().setText(getCaption());
	    configuration.getSubTitle().setText("Touch a point to see data");
	    addXAxis();

	    YAxis yAxis = configuration.getyAxis();
	    yAxis.setTitle(new AxisTitle(getYAxisTitle()));
	    yAxis.getTitle().setAlign(VerticalAlign.MIDDLE);
	    
        configuration.getTooltip().setPointFormat(getToolTipFormat());
        configuration.getTooltip().setValueDecimals(1);

	    PlotOptionsLine plotOptions = new PlotOptionsLine();
	    plotOptions.getDataLabels().setEnabled(true);
	    if (getDataFormat() != null)
		   plotOptions.getDataLabels().setFormat(getDataFormat());
	    configuration.setPlotOptions(plotOptions);
        Legend legend = configuration.getLegend();
        legend.setEnabled(false);
        //legend.setFloating(false);
        legend.setLayout(LayoutDirection.VERTICAL);
	    legend.setAlign(HorizontalAlign.RIGHT);
	    legend.setVerticalAlign(VerticalAlign.TOP);
	    //legend.setX(-10d);
	    //legend.setY(100d);
	    legend.setBorderWidth(0);
	    try
	    {
	    	addData();
	    }
	    catch (Exception e)
	    {
	    	System.out.println("Unable to add data to graph:" + e);
	    	e.printStackTrace();
	    }
	    chart.drawChart(configuration);
	}

}
