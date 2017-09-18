package net.parsonsrun;

import java.util.Date;

import com.vaadin.ui.AbstractField;
import com.vaadin.ui.CheckBox;

import net.parsonsrun.domain.Player;

public interface MatchEditOwner
{
	
	Player getLoginUser();
	boolean canSetup();
	void updateMatch(Runnable r);
	String getEditWidth();
	AbstractField<Date> makeDateField();
	CheckBox makeCheckBox();
}
