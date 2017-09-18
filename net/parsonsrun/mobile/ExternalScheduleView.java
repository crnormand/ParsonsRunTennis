package net.parsonsrun.mobile;

import com.vaadin.data.util.MethodProperty;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.themes.ValoTheme;

import de.steinwedel.messagebox.MessageBox;
import net.parsonsrun.ClickLabel;
import net.parsonsrun.Utils;
import net.parsonsrun.desktop.DesktopUI;
import net.parsonsrun.domain.*;

public class ExternalScheduleView extends AbstractRoundsView
{
	protected ExternalTeam team;
	
	public ExternalScheduleView(ExternalTeam t, int index)
	{
		setTeam(t);
		setCurrentPageIndex(index);
	}
	
	@Override
	protected int getPagesCount()
	{
		return getTeam().getNumberOfWeeks();
	}

	@Override
	protected AbstractRoundsView nextView(int i)
	{
		return new ExternalScheduleView(getTeam(), i);
	}
	
	protected Lineup getCurrentLineup()
	{
		return getTeam().getLineups().get(getCurrentPageIndex());
	}

	@Override
	protected String pageName()
	{
		return getCurrentLineup().getWeekString();
	}

	@Override
	protected String fullPageName()
	{
		if (!getCurrentLineup().isAway() || getCurrentLineup().getLocation().isEmpty())
			return getCurrentLineup().getName();
		StringBuilder b = new StringBuilder();
		b.append("<a href='");
		b.append(getCurrentLineup().getLocationHtml());
		b.append("'>");
		b.append(getCurrentLineup().getName());
		b.append("</a>");
		return b.toString(); 
	}

	@Override
	public String getCaption()
	{
		return "Schedule (Current week: " + (getTeam().getNextWeekIndex() + 1) + " of " + getTeam().getNumberOfWeeks() + ")";
	}

	public ExternalTeam getTeam()
	{
		return team;
	}

	public void setTeam(ExternalTeam team)
	{
		this.team = team;
	}
	
	protected void buildMatchList()
	{
		Lineup lu = getCurrentLineup();
		Button results = new Button("Email results");
		for (Line li : lu.getLines())
		{
			currentPage.addComponent(buildLine(lu, li, results));
		}
		if (canSetup())
		{
			results.setIcon(FontAwesome.ENVELOPE_O);
			results.setEnabled(lu.isCompleted());
			results.addClickListener(e -> emailResults(lu, results));
			currentPage.addComponent(results);		
		}
		Label l = new Label("* Player has not confirmed lineup");
		l.setStyleName("small");
		currentPage.addComponent(l);
	}
	
	protected void emailResults(Lineup lu, Button results)
	{
		String subject = "Results for " + lu.subjectLine();
		StringBuilder b2 = new StringBuilder();
		b2.append(subject);
		b2.append(" are:<br><br>");
		b2.append("<table border='1' cellpadding='5'>");
		for (Line li : lu.getLines())
		{
			b2.append("<tr><th>Line: ");
			b2.append(li.getIndex());
			b2.append("</th><th>");
			if (li.isWon())
				b2.append("WIN");
			else
				b2.append("Loss");
			b2.append("</th><th>");
			b2.append(li.getA().firstLastName());
			b2.append(" / ");
			b2.append(li.getB().firstLastName());
			b2.append("</th><th>");
			b2.append(li.getScore());
			b2.append("</th></tr>");
		}
		b2.append("</table>");
		b2.append("<br>");
		Panel pnl = new Panel("Subject: " + subject);
		VerticalLayout v = new VerticalLayout();
		pnl.setContent(v);
		Label l1 = new Label(b2.toString(), ContentMode.HTML);
		l1.setStyleName(ValoTheme.LABEL_SMALL);
		TextArea input = new TextArea("Optional message:");
		input.setWidth("100%");
		input.setRows(7);
		v.addComponent(l1);
		v.addComponent(input);
		StringBuilder b = new StringBuilder();
		b.append(getTeam().getGoodbye());
		Label l2 = new Label(b.toString(), ContentMode.HTML);
		v.addComponent(l2);
		MessageBox.createInfo()
			.withCaption("Weekly results email")
			.withWidth(getBrowserWidth() + "px")
			.withHeight(getBrowserHeight() + "px")
			.withMessage(pnl)
			.withOkButton(() -> {
				getParentUI().sendEmail(getTeam().getRoster(), subject, b2.toString() + Utils.escapeHTML(input.getValue()) + "<br><br>" + b.toString());
				Notification.show("Weekly results email sent!");
				saveWhile(() -> lu.setSentEmail(true));
				results.setEnabled(false);
			})
			.withCancelButton()
			.open();
	}
	
	protected Component buildLine(Lineup lu, Line li, Button results)
	{
		Panel pnl = new Panel();
		HorizontalLayout h = new HorizontalLayout();
		if (li.isWon())
			h.setStyleName(DesktopUI.CONFIRMED);
		h.setMargin(false);
		h.setSpacing(true);
		pnl.setContent(h);
		Label lbl = new Label("#" + li.getIndex());
		h.addComponent(lbl);
		h.setComponentAlignment(lbl, Alignment.MIDDLE_LEFT);
		VerticalLayout v = new VerticalLayout();
		v.setSpacing(false);
		v.setMargin(false);
		h.addComponent(v);
		TextField c = new TextField(null, new MethodProperty<Boolean>(li, "assign"));
		c.setWidth((getBrowserWidth() - 50) + "px");
		c.setReadOnly(true);
		v.addComponent(c);
		HorizontalLayout h2 = new HorizontalLayout();
		v.addComponent(h2);
		CheckBox won = new CheckBox("Won", new MethodProperty<Boolean>(li, "won"));
		CheckBox lost = new CheckBox("Lost", new MethodProperty<Boolean>(li, "lost"));
		won.setStyleName("small");
		lost.setStyleName("small");
		won.addValueChangeListener(e -> saveWhile(() -> {
			if (won.getValue()) 
			{
				lost.setValue(false); 
				h.setStyleName(DesktopUI.CONFIRMED);
				h.markAsDirty();
			}
			results.setEnabled(lu.isCompleted()); }));
		lost.addValueChangeListener(e -> saveWhile(() -> { 
			if (lost.getValue()) 
			{
				won.setValue(false); 
				h.removeStyleName(DesktopUI.CONFIRMED);
				h.markAsDirty();
			}
			results.setEnabled(lu.isCompleted()); }));
		h2.setSpacing(true);
		h2.addComponent(won);
		h2.addComponent(lost);
		Label l2 = new Label("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Score:", ContentMode.HTML);
		l2.setStyleName("small");
		h2.addComponent(l2);
		h2.setComponentAlignment(l2, Alignment.MIDDLE_RIGHT);
		TextField sc = new TextField(new MethodProperty<String>(li, "score"));
		sc.setImmediate(true);
		sc.addValueChangeListener(e -> saveData("Score for " + li + ":" + sc.getValue()));
		sc.setStyleName("small");
		if (canEdit(li))
			sc.addStyleName("textborder");
		sc.setColumns(13);
		h2.addComponent(sc);
		won.setReadOnly(!canEdit(li));
		lost.setReadOnly(!canEdit(li));
		sc.setReadOnly(!canEdit(li));
		return pnl;
	}
	
	protected boolean canEdit(Line li)
	{
		return (canSetup() || li.contains(getLoginUser())) && li.canBeScored();
	}
	
	public void saveData(String s)
	{
		println(s);
		saveData();
	}
	
	protected boolean canSetup()
	{
		Player p = getLoginUser();
		if (p.isAdmin())
			return true;
		return getTeam() != null && getTeam().includesCaptain(p);
	}
}
