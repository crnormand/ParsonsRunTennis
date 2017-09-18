package net.parsonsrun.mobile;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.vaadin.addon.touchkit.ui.*;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

import net.parsonsrun.domain.Round;

public abstract class AbstractRoundsView extends MobileBaseView
{
	protected Label label;
	protected Button back;
	protected int currentPageIndex;
	protected VerticalComponentGroup currentPage;

	public void setCurrentPageIndex(int currentPageIndex)
	{
		this.currentPageIndex = currentPageIndex;
		update();
	}

	public void buildUI()
	{
		currentPage = new VerticalComponentGroup();
		currentPage.setWidth("100%");
		back = new Button("Return to main view", FontAwesome.MAIL_REPLY);
		back.setStyleName(ValoTheme.BUTTON_SMALL);
		back.addStyleName("green");
		back.addClickListener(e -> back());
		label = new Label("", ContentMode.HTML);
		label.setWidth("100%");
		content.addComponent(back);
		content.addComponent(label);
		content.addComponent(currentPage);
		update();
	}
	
	public String toString()
	{
		return super.toString() + " @" + (getCurrentPageIndex() + 1);
	}
	
	protected void back()
	{
		while (getNavigationManager().getCurrentComponent() instanceof AbstractRoundsView)
			navigateBack();
	}
	
	protected abstract int getPagesCount();
	
	public void swipedRight()
	{
		int newIndex = currentPageIndex + 1;
		if (newIndex < getPagesCount())
		{
			getNavigationManager().setNextComponent(nextView(newIndex));
		}
	}
	
	public void swipedLeft()
	{
		int newIndex = currentPageIndex - 1;
		if (newIndex >= 0)
		{
			getNavigationManager().setPreviousComponent(nextView(newIndex));
		}
	}
	
	protected abstract AbstractRoundsView nextView(int i);
	
	protected abstract String pageName();
	
	protected abstract String fullPageName();
	
	public void update()
	{
		super.update();
		if (!built)
			return;
		updateUI(() -> {
			currentPage.removeAllComponents();
			StringBuilder sb = new StringBuilder();
			sb.append("<div style='display: table-cell; min-width: 160px'>");
			sb.append(pageName());
			sb.append("</div><div style='display: table-cell; width:100%; text-align:right'><small>(swipe to change rounds)</small></div>");
			sb.append("<div class='label-centered-large'>");
			sb.append(fullPageName());
			sb.append("</div>");
			label.setValue(sb.toString());
			buildMatchList();
		});
	}
	
	protected abstract void buildMatchList();

	public int getCurrentPageIndex()
	{
		return currentPageIndex;
	}
	
}
