package net.parsonsrun;

import java.util.Date;

import com.vaadin.addon.touchkit.ui.DatePicker;
import com.vaadin.addon.touchkit.ui.Switch;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Notification.Type;

import net.parsonsrun.domain.Match;
import net.parsonsrun.domain.Player;
import net.parsonsrun.domain.Side;

public class MatchComponent extends VerticalLayout
{
	MatchEditOwner owner;
	String name; 
	Match match;
	Side a;
	Side b;
	NativeSelect aSets[];
	NativeSelect bSets[];
	CheckBox isFa;
	CheckBox isFb;
	AbstractField<Date> date;
	TextField scores;
	Label warning;
	TextArea comment;
	Button ok;
	CheckBox locked;
	boolean updating = false;

	public MatchComponent(MatchEditOwner o, String n, Match m)
	{
		owner = o;
		name = n;
		match = m;
		a = m.getA();
		b = m.getB();
	}
	
	public MatchComponent(MatchEditOwner o, Match m)
	{
		this(o, "", m);
	}
	
	public String toString()
	{
		return super.toString() + ":" + match;
	}

	
	public String getCaption()
	{
		return match.getLabel();
	}
	
	public void buildUI()
	{
		updating = true;
		setWidth(owner.getEditWidth());
		setSpacing(true);
		if (name != null && !name.isEmpty())
			addComponent(new Label("Enter the match score for " + name));
		addComponent(new Label("<small>NOTE: You only have 5 minutes to enter the score.</small>", ContentMode.HTML));
		addComponent(getName(a));
		isFa = makeDefault(this);
		aSets = setSelects();
		bSets = setSelects();
		addComponent(setLayout(aSets));
		addComponent(setLabels());
		addComponent(setLayout(bSets));
		addComponent(getName(b));
		isFb = makeDefault(this);
		addDateField(this);
		addScores(this);
		comment = new TextArea("Comment on match:");
		comment.setWidth("100%");
		comment.setHeight("5em");
		addComponent(comment);
		addOk(this);
		updating = false;
		loadCurrentMatch();
	}
	
	protected void addOk(VerticalLayout page)
	{
		HorizontalLayout h = new HorizontalLayout();
		h.setSpacing(true);
		ok = new Button("Update Match");
		ok.setWidth("100%");
		ok.addClickListener(e -> updateMatch());
		h.addComponent(ok);
		locked = new CheckBox("Locked");
		locked.setWidth("7em");
		if (owner.canSetup())
		{
			h.addComponent(locked);
			h.setComponentAlignment(locked, Alignment.MIDDLE_RIGHT);
			h.setExpandRatio(ok, 1.0f);
		}
		page.addComponent(h);
	}
	
	protected void loadCurrentMatch()
	{
		isFa.setValue(a.isForfeit());
		isFb.setValue(b.isForfeit());
		setScores(a, aSets);
		setScores(b, bSets);
		if (match.getPlayed() != null)
			date.setValue(match.getPlayed());
		comment.setValue(match.getComment());
		locked.setValue(match.isLocked());
	}
	
	protected void setScores(Side s, NativeSelect[] sets)
	{
		for (int i = 0; i < sets.length; i++)
		{
			int sc = s.getScore(i);
			if (sc != getSelection(sets[i]))
				sets[i].select(new Integer(sc));
		}
	}
	
	protected void updateMatch()
	{
		owner.updateMatch(() -> storeData());
	}
	
	private void storeData()
	{
		if (match.okToSave(owner.getLoginUser()))
		{
			a.setForfeit(isFa.getValue());
			b.setForfeit(isFb.getValue());
			match.setComment(comment.getValue());
			match.setLocked(locked.getValue());
			for (int i = 0; i < aSets.length; i ++)
			{
				match.setScore(i, getSelection(aSets[i]), getSelection(bSets[i]));
			}
			match.setMatchPlayed(date.getValue());
		}
		else
			Notification.show("Sorry, you did not complete the update in time.", Type.ERROR_MESSAGE);
	}
	
	protected void addScores(VerticalLayout page)
	{
		HorizontalLayout h = new HorizontalLayout();
		h.setSpacing(true);
		Label l = new Label("Scores:");
		h.addComponent(l);
		h.setComponentAlignment(l, Alignment.MIDDLE_LEFT);
		scores = new TextField();
		scores.setWidth("8em");
		//scores.setStyleName("inline-label");
		h.addComponent(scores);
		h.setComponentAlignment(scores, Alignment.TOP_CENTER);
		warning = new Label("Test warning");
		warning.addStyleName("bigredtext");
		h.addComponent(warning);
		h.setComponentAlignment(warning, Alignment.MIDDLE_RIGHT);
		page.addComponent(h);
	}
	
	protected void addDateField(VerticalLayout page)
	{
		HorizontalLayout h = new HorizontalLayout();
		h.setSpacing(true);
		page.addComponent(h);
		Label lbl = new Label("Played on: ");
		h.addComponent(lbl);
		date = owner.makeDateField();
		date.setValue(new Date());
		//date.setDateFormat("MM-dd-yyyy");
		h.addComponent(date);
		h.setComponentAlignment(lbl, Alignment.MIDDLE_LEFT);
	}
	
	protected CheckBox makeDefault(VerticalLayout page)
	{
		CheckBox s = owner.makeCheckBox();
		s.setImmediate(true);
		s.addValueChangeListener(e -> checkDefault(s));
		HorizontalLayout h = new HorizontalLayout();
		h.setSpacing(true);
		//h.setWidth("100%");
		Label l = new Label("Did this team default the match?");
		h.addComponent(l);
		//h.setExpandRatio(l,  1.0f);
		h.addComponent(s);
		page.addComponent(h);
		return s;
	}
	
	protected void checkDefault(CheckBox s)
	{
		enableSets(aSets, !s.getValue());
		enableSets(bSets, !s.getValue());
		showScores();
	}
	
	protected void enableSets(NativeSelect sets[], boolean en)
	{
		for (int i = 0; i < sets.length; i++)
			sets[i].setEnabled(en);
	}
	
	protected Label getName(Side s)
	{
		return new Label("<div class='label-centered-large'>" + s.getFullName() + "</div>", ContentMode.HTML);
	}
	
	protected HorizontalLayout setLabels()
	{
		HorizontalLayout h = new HorizontalLayout();
		h.setWidth("100%");
		for (int i = 1 ; i <= match.requiredNumberOfSets() ; i++)
		{
			Label l = new Label("<div class='match-set-labels'>Set " + i + "</div>", ContentMode.HTML);
			h.addComponent(l);
			h.setExpandRatio(l, 1.0f);
		}
		return h;
	}
	
	protected HorizontalLayout setLayout(NativeSelect[] sets)
	{
		HorizontalLayout h = new HorizontalLayout();
		h.setSpacing(true);
		//h.setMargin(true);
		h.setWidth("100%");
		for (int i = 0; i < sets.length ; i++)
		{
			NativeSelect l = sets[i];
			h.addComponent(l);
			h.setComponentAlignment(l, Alignment.MIDDLE_CENTER);
			h.setExpandRatio(l, 1.0f);
		}
		return h;
	}
	
	protected NativeSelect[] setSelects()
	{
		NativeSelect[] sets = new NativeSelect[match.requiredNumberOfSets()];
		for (int i = 0; i < match.requiredNumberOfSets(); i++)
			sets[i] = newSetSelect();
		return sets;
	}
	
	protected NativeSelect newSetSelect()
	{
		NativeSelect s = new NativeSelect();
		s.setNullSelectionAllowed(false);
		s.setStyleName("match"); 
		s.addValueChangeListener(e -> selected(s));
		for (int i = 7; i >= 0; i--)
		{
			s.addItem(new Integer(i));
		}
		return s;
	}
	
	protected void selected(NativeSelect s)
	{
		showScores();
	}
	
	protected void showScores()
	{
		if (updating)
			return;
		updating = true;
		int max = match.requiredNumberOfSets();
		int needed = match.numberOfSetsNeededForWin();
		if (isFa.getValue() || isFb.getValue())
		{
			String who = "";
			String s1 = "Win";
			String s2 = "Win";
			if (isFa.getValue())
			{
				s1 = "Default";
				who = b.getName() + " wins";
			}
			if (isFb.getValue())
			{
				s2 = "Default";
				who = isFa.getValue() ? "no one wins" : a.getName() + " wins";
			}
			scores.setValue(s1 + " - " + s2);
			warning.setValue(who);
			ok.setEnabled(true);
			updating = false;
			return;
		}
		StringBuilder wn = new StringBuilder();
		StringBuilder sb = new StringBuilder();
		String sep = "";
		int winsa = 0;
		int winsb = 0;
		boolean valid = true;
		for (int i = 0; i < max; i++)
		{
			String w = " Set" + (i+1) + " not valid.";
			int a = getSelection(aSets[i]);
			int b = getSelection(bSets[i]);
			if (a >= 0 && b >= 0 && (a+b) > 0)
			{
				sb.append(sep + a + "-" + b);
				sep = " / ";
				boolean good = false;
				if ((a == 7 && (b == 5 || b == 6)) || (a == 6 && b < 5))
				{
					good = true;
					winsa++;
				}
				if ((b == 7 && (a == 5 || a == 6)) || (b == 6 && a < 5))
				{
					good = true;
					winsb++;
				}
				if (!good)
				{
					valid = false;
					wn.append(w);
				}
			}
		}
		boolean enable = false;
		if (valid)
		{
			if (winsa >= needed)
			{
				wn.append(a.getName() + " wins");
				enable = true;
			}
			else if (winsb >= needed)
			{
				wn.append(b.getName() + " wins");
				enable = true;
			}
			else
				wn.append(" Not enough sets.");
		}
		ok.setEnabled(enable);
		scores.setValue(sb.toString());
		warning.setValue(wn.toString());
		updating = false;
	}
	
	protected int getSelection(NativeSelect s)
	{
		Integer i = (Integer)s.getValue();
		return i == null ? -1 : i.intValue();
	}

}
