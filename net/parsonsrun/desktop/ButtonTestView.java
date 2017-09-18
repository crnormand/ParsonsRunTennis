package net.parsonsrun.desktop;

import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

public class ButtonTestView extends DesktopBaseView
{

	@Override
	public void buildUI()
	{
		addBack();
		Button b1 = new Button("Test Button 1");
		b1.setStyleName(ValoTheme.BUTTON_DANGER);
		addComponent(b1);
		Button b2 = new Button("Test Button 2");
		b2.setStyleName(ValoTheme.BUTTON_FRIENDLY);
		addComponent(b2);
	}
}
