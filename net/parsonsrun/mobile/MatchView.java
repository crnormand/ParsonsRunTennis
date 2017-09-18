package net.parsonsrun.mobile;

import java.util.Date;

import com.vaadin.addon.touchkit.ui.DatePicker;
import com.vaadin.addon.touchkit.ui.HorizontalButtonGroup;
import com.vaadin.addon.touchkit.ui.Switch;
import com.vaadin.addon.touchkit.ui.VerticalComponentGroup;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.shared.ui.ui.UIState.NotificationTypeConfiguration;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Notification.Type;

import net.parsonsrun.MatchComponent;
import net.parsonsrun.MatchEditOwner;
import net.parsonsrun.Utils;
import net.parsonsrun.domain.Match;
import net.parsonsrun.domain.Player;
import net.parsonsrun.domain.Side;

public class MatchView extends MobileBaseView implements MatchEditOwner
{
	protected MatchComponent comp;
	protected Match match;
	
	public MatchView(Match m)
	{
		match = m;
		comp = new MatchComponent(this, m);
	}
	
	public String toString()
	{
		return super.toString() + ":" + match;
	}

	public boolean canSetup()
	{
		Player p = getLoginUser();
		if (p.isAdmin())
			return true;
		return match.isOrganizer(p);
	}
	public String getCaption()
	{
		return match.getLabel();
	}
	

	public void buildUI()
	{
		content.addComponent(comp.buildUI(getSwipeReminder()));
	}
	
	public void updateMatch(Runnable r)
	{
		saveWhile(r);
		navigateBack();
	}
	public String getEditWidth()
	{
		return getBrowserWidth() + "px";
	}
	public AbstractField<Date> makeDateField()
	{
		return new DatePicker();
	}
	public CheckBox makeCheckBox()
	{
		return new Switch();
	}

}
