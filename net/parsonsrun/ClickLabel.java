package net.parsonsrun;

import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

public class ClickLabel extends VerticalLayout
{
	public ClickLabel(String value)
	{
        Label label = new Label (value, ContentMode.HTML);
        addComponent(label);    
    }
	
	public void addClickListener(LayoutClickListener l)
	{
		addLayoutClickListener(l);
	}
}
