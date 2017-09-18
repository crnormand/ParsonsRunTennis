package net.parsonsrun.mobile;

import java.util.*;
import com.vaadin.addon.touchkit.ui.VerticalComponentGroup;
import com.vaadin.data.util.MethodProperty;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import net.parsonsrun.Utils;
import net.parsonsrun.domain.Player;

public class RosterView extends MobileBaseView
{
	protected String name;
	protected ArrayList<Player> players;
	
	@Override
	public String getCaption()
	{
		return "Roster";
	}

	@Override
	public void buildUI()
	{
		content.addComponent(getSwipeReminder());
		VerticalComponentGroup group = new VerticalComponentGroup(name);
		content.addComponent(group);
		HorizontalLayout h = new HorizontalLayout();
		group.addComponent(h);
		VerticalLayout vn = new VerticalLayout();
		vn.setSpacing(false);
		vn.setMargin(false);
		h.addComponent(vn);
		VerticalLayout ve = new VerticalLayout();
		ve.setSpacing(false);
		ve.setMargin(false);
		h.addComponent(ve);
		VerticalLayout vc = new VerticalLayout();
		vc.setSpacing(false);
		vc.setMargin(false);
		h.addComponent(vc);
		for (Player p : getPlayers())
		{
			if (!p.equals(getLoginUser()))
			{
				Label l = new Label(p.firstLastName());
				l.setStyleName("small");
				vn.addComponent(l);
				l = new Label("<a href='mailto:" + p.getEmail() + "'>" + p.getEmail() + "</a>", ContentMode.HTML);
				l.setStyleName("small");
				ve.addComponent(l);
				l = new Label("<a href='tel:" + p.getPhone() + "'>" + p.getPhoneDisplay() + "</a>", ContentMode.HTML);
				l.setStyleName("small");
				vc.addComponent(l);
			}
			else
			{
				TextField n = new TextField();
				n.setColumns(13);
				n.setStyleName(ValoTheme.TEXTFIELD_SMALL);
				n.setValue(p.firstLastName());
				n.setReadOnly(true);
				vn.addComponent(n);
				TextField e = new TextField(new MethodProperty<String>(p, "email"));
				e.setColumns(15);
				e.setStyleName(ValoTheme.TEXTFIELD_SMALL);
				ve.addComponent(e);
				TextField c = new TextField();
				c.setValue(p.getPhoneDisplay());
				c.setColumns(8);
				c.setStyleName(ValoTheme.TEXTFIELD_SMALL);
				if (p.getPhone().isEmpty())
					c.addStyleName("textborder");
				c.addFocusListener(x -> { 		
					c.setValue(Utils.compactPhone(c.getValue()));
				});
				c.addBlurListener(x -> { 		
					c.setValue(Utils.expandPhone(c.getValue()));
				});
				c.addValueChangeListener(x -> saveWhile(() -> {
					p.setPhone(c.getValue());
				}));
				vc.addComponent(c);
			}
		}
	
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public ArrayList<Player> getPlayers()
	{
		return players;
	}

	public void setPlayers(ArrayList<Player> players)
	{
		this.players = players;
	}

}
