package net.parsonsrun.mobile;

import com.vaadin.ui.Label;

public class ToDoView extends MobileBaseView
{
	String cap;
	
	public ToDoView(String c)
	{
		cap = c;
	}
	
 	public String getCaption()
 	{
 		return cap;
 	}
	@Override
	public void buildUI()
	{
		content.addComponent(new Label("Under Construction: " + cap));

	}

}
