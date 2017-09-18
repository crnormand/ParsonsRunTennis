package net.parsonsrun.mobile;

import java.util.concurrent.TimeUnit;

import com.vaadin.addon.touchkit.ui.NavigationManager;
import com.vaadin.addon.touchkit.ui.NavigationView;
import com.vaadin.addon.touchkit.ui.Popover;
import com.vaadin.addon.touchkit.ui.SwipeView;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import net.parsonsrun.ParsonsRunUI;
import net.parsonsrun.Utils;
import net.parsonsrun.domain.DomainObject;
import net.parsonsrun.domain.ExternalTeam;
import net.parsonsrun.domain.League;
import net.parsonsrun.domain.Neighborhood;
import net.parsonsrun.domain.Player;
import net.parsonsrun.domain.UpdateListener;

public abstract class MobileBaseView extends SwipeView implements UpdateListener
{
	public static final String TEXT_DARK = "text-dark";
	
	protected Neighborhood hood;
	protected boolean built = false;
	protected MobileUI parentUI;
	protected CssLayout content;
	
	public void attach() 
	{
        super.attach();
        if (!built)
        {
        	built = true;
        	init();
        	buildUI();
            updateUI(() -> playItForward());
        }
        println("Opening Mobile:" + this);
	}
	
	public void saveData()
	{
		Utils.saveData();
	}
	
	protected void clearAction()
	{
		getParentUI().clearAction();
	}
	
	protected void playItForward()
	{
	}
	
	public int nextInt(int sz)
	{
		return getParentUI().nextInt(sz);
	}
	
	public ExternalTeam getCurrentExternalTeam()
	{
		return getParentUI().getCurrentExternalTeam();
	}
	
	public League getCurrentTournament()
	{
		return getParentUI().getCurrentTournament();
	}
	
	protected void finalize() throws Throwable 
	{
        try {
        	getHood().removeListener(this);
        }
        catch(Throwable t) {
            throw t;
        }
        finally {
            super.finalize();
        }
    }
	
	protected DomainObject getPictureAssociation()
	{
		return null;
	}
	
	public void openPopover(String text, Runnable r)
	{
		Popover p = new Popover();
		p.setSizeFull();
		VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		layout.setMargin(true);
		layout.setSpacing(true);
		p.setContent(layout);
		Label l = new Label(text, ContentMode.HTML);
		layout.addComponent(l);
		layout.setComponentAlignment(l, Alignment.TOP_CENTER);
		Button b = new Button("OK!");
		b.addClickListener(e -> {
			p.close();
			r.run();
		});
		layout.addComponent(b);
		layout.setComponentAlignment(b, Alignment.BOTTOM_CENTER);
		p.showRelativeTo(this);
	}
	
	public void swipedRight()
	{
	}
	
	protected Label getSwipeReminder()
	{
		return new Label("<div style='text-align:center'><small><small>(NOTE: Swipe right to return to a previous page)</small></small></div>", ContentMode.HTML);
	}
	
	public void swipedLeft()
	{
	}
	
	public void openPopover(String text)
	{
		openPopover(text, () -> {});
	}
	
	public String toString()
	{
		return getClass().getSimpleName();
	}
	
	public void updateUI(Runnable r)
	{
		UI ui = UI.getCurrent();
		if (ui != null)
			ui.access(r);
	}
	
	public void saveWhile(Runnable r)
	{
		Utils.saveWhile(r);
	}
	
	public boolean isAdminUser()
	{
		return getLoginUser().isAdmin();
	}
	
	public void setCurrentComponent(Component c)
	{
		getParentUI().setCurrentComponent(c);
	}
	
	public NavigationManager getNavigationManager()
	{
		return getParentUI().getNavigationManager();
	}
	
	public int getBrowserWidth()
	{
		return getParentUI().getBrowserWidth();
	}
	

	
	public int getBrowserHeight()
	{
		return getParentUI().getBrowserHeight();
	}
	
	public abstract String getCaption();
	
	protected void init()
	{
		parentUI = (MobileUI)UI.getCurrent();
		content = new CssLayout();
        content.setSizeFull();
        setContent(content);
	}
	
	protected Neighborhood getHood()
	{
		if (hood == null)
		{
			hood = Neighborhood.getSingleton();
			hood.addListener(this);
		}
		return hood;
	}
	
	public abstract void buildUI();
	
	public void println(String s)
	{
		getParentUI().println(s);
	}
	
	public void debug(String s)
	{
		getParentUI().debug(s);
	}
	
	public MobileUI getParentUI()
	{
		return (MobileUI)parentUI;
	}
	
	public void navigateBack()
	{
		getParentUI().navigateBack();
	}
	
	public void navigateTo(MobileBaseView v)
	{
		getParentUI().navigateTo(v);
	}
	
	public Player getLoginUser()
	{
		return getParentUI() == null ? null: getParentUI().getLoginUser();
	}
	
	public Player getCurrentPlayer()
	{
		return getParentUI().getCurrentPlayer();
	}
	
	public void update()
	{
//		System.out.println("Update: " + this);

	}
}
