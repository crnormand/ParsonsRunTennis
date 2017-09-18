package net.parsonsrun.desktop;

import java.awt.Point;

import com.vaadin.data.util.MethodProperty;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

import de.steinwedel.messagebox.MessageBox;
import net.parsonsrun.*;
import net.parsonsrun.domain.*;

public abstract class DesktopBaseView extends VerticalLayout implements View, UpdateListener
{
	protected boolean built = false;
	protected ParsonsRunUI parentUI;
	protected Neighborhood hood;
	protected Picture currentPic;
	
	public void attach() 
	{
        super.attach();
        if (!built)
        {
    		built = true;
        	init();
        	buildUI();
        }
        println("Opening Desktop:" + this);
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

	protected synchronized Neighborhood getHood()
	{
		if (hood == null)
		{
			hood = Neighborhood.getSingleton();
			hood.addListener(this);
		}
		return hood;
	}
	public void addBack()
	{
		addBack("");
	}
	
	public void addBack(String title)
	{
		addComponent(getBack(title));
	}
	
	public Component getBack(String title)
	{
		HorizontalLayout h = new HorizontalLayout();
		h.setSpacing(true);
		Button b = new Button("<= Back");
		b.addStyleName(ValoTheme.BUTTON_PRIMARY);
		b.addStyleName(ValoTheme.BUTTON_TINY);
		b.addClickListener(e -> backClicked());
		h.addComponent(b);
		Label lbl = new Label(title, ContentMode.HTML);
		lbl.setStyleName("windowtitle");
		h.addComponent(lbl);
		return h;
	}
	
	public void update()
	{
//		System.out.println("Update: " + this);

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
	
	public String toString()
	{
		return getClass().getSimpleName();
	}
	
	public void println(String s)
	{
		getParentUI().println(s);
	}
	
	public void updateUI(Runnable r)
	{
		UI ui = UI.getCurrent();
		if (ui != null)
			ui.access(r);
	}
	
	public void backClicked()
	{
		getParentUI().navigateBack();
	}
	
	protected void viewResized()
	{
	
	}
	
	public void saveWhile(Runnable r)
	{
		Utils.saveWhile(r);
	}
	
	public boolean isAdminUser()
	{
		return getLoginUser().isAdmin();
	}
	
	public int getBrowserWidth()
	{
		return getParentUI().getBrowserWidth();  // - 75;  // If setMargin == false, use 0;
	}
	
	public int getBrowserHeight()
	{
		return getParentUI().getBrowserHeight();
	}
	
	public DesktopUI getParentUI()
	{
		if (parentUI == null)
			parentUI = (ParsonsRunUI)UI.getCurrent();
		return (DesktopUI)parentUI;
	}
	
	public void init()
	{
		setWidth("100%");
		setMargin(true);
		setSpacing(true);
		getParentUI();
	}
	
	public Player getLoginUser()
	{
		return getParentUI().getLoginUser();
	}
	
	public Player getCurrentPlayer()
	{
		return getParentUI().getCurrentPlayer();
	}
	
	public ExternalTeam getCurrentExternalTeam()
	{
		return getParentUI().getCurrentExternalTeam();
	}
	
	public Tournament getCurrentTournament()
	{
		return getParentUI().getCurrentTournament();
	}
	
	public void setCurrentTournament(Tournament t)
	{
		getParentUI().setCurrentTournament(t);
	}
	
	public void setCurrentExternalTeam(ExternalTeam t)
	{
		getParentUI().setCurrentExternalTeam(t);
	}
	
	public void setCurrentPlayer(Player p)
	{
		getParentUI().setCurrentPlayer(p);
	}
	
	public DesktopBaseView navigateTo(String n)
	{
		return getParentUI().navigateTo(n);
	}
	public DesktopBaseView navigateBack()
	{
		return getParentUI().navigateBack();
	}
	
	public Label createSpacer()
	{
		Label sz = new Label("");
		sz.setWidth(null);
		sz.setHeight("30px");
		return sz;
	}
	
	public abstract void buildUI();
	
	public void addTitle(String title)
	{
		Label lbl = new Label(title);
		lbl.setStyleName("windowtitle");
		addComponent(lbl);
	}
	
	@Override
	public void enter(ViewChangeEvent event)
	{
		updateUI(() -> playItForward());
	}
	
	protected DomainObject getPictureAssociation()
	{
		return null;
	}

	protected Tuple<HorizontalLayout, Point> getPictureViewer(boolean showClose)
	{
		int w = getBrowserWidth() - 200;
		int h = getBrowserHeight() - 100;
		int hi = h - 40;
		HorizontalLayout hl = new HorizontalLayout();
		hl.setSizeFull();
		hl.setMargin(true);
		Button b1 = new Button();
		b1.setStyleName(ValoTheme.BUTTON_PRIMARY);
		b1.addStyleName(ValoTheme.BUTTON_HUGE);
		b1.setIcon(FontAwesome.ARROW_LEFT);
		hl.addComponent(b1);
		hl.setComponentAlignment(b1, Alignment.MIDDLE_LEFT);
		VerticalLayout pnl = new VerticalLayout();
		TextField comment = new TextField();
		comment.addValueChangeListener(e -> getCurrentPic().setNote(comment.getValue()));
		Label owner = new Label();
		Runnable updater = () -> {
			pnl.removeAllComponents(); 
			if (currentPic != null)
			{
				Image i = new Image(null, currentPic.getSource());
				i.setHeight(hi + "px");
				pnl.addComponent(i); 
				pnl.setComponentAlignment(i, Alignment.TOP_CENTER);
				comment.setReadOnly(false);
				comment.setValue(currentPic.getNote());
				comment.setReadOnly(!currentPic.canEdit(getLoginUser()));
				owner.setValue("Uploaded by " + currentPic.getOwner().firstLastName());
			}
			else
			{
				comment.setValue("");
				owner.setValue("");
			}
		};
		updater.run();
		HorizontalLayout buttons = new HorizontalLayout();
		VerticalLayout v = new VerticalLayout(pnl, owner, buttons);
		v.setSpacing(false);
		v.setHeight(h + "px");
		v.setExpandRatio(pnl, 1);
		v.setComponentAlignment(pnl, Alignment.TOP_CENTER);
		v.setComponentAlignment(owner, Alignment.BOTTOM_RIGHT);
		v.setComponentAlignment(buttons, Alignment.BOTTOM_CENTER);
		Button bl = new Button();
		bl.setIcon(FontAwesome.ROTATE_LEFT);
		buttons.addComponent(bl);
		bl.addClickListener(e -> { 
			currentPic.rotate(270); 
			updater.run();
			});
		comment.setWidth((w / 2) + "px");
		buttons.addComponent(comment);
		buttons.setComponentAlignment(comment, Alignment.BOTTOM_CENTER);
		buttons.setExpandRatio(comment, 1);
		Button br = new Button();
		br.setIcon(FontAwesome.ROTATE_RIGHT);
		buttons.addComponent(br);
		br.addClickListener(e -> { 
			currentPic.rotate(90); 
			updater.run();
			});
		buttons.setComponentAlignment(bl, Alignment.BOTTOM_LEFT);
		buttons.setComponentAlignment(br, Alignment.BOTTOM_RIGHT);
		hl.addComponent(v);
		hl.setComponentAlignment(v, Alignment.MIDDLE_CENTER);
		hl.setExpandRatio(v, 1);
		Button b2 = new Button();
		b2.setIcon(FontAwesome.ARROW_RIGHT);
		b2.setStyleName(ValoTheme.BUTTON_PRIMARY);
		b2.addStyleName(ValoTheme.BUTTON_HUGE);
		hl.addComponent(b2);
		hl.setComponentAlignment(b2, Alignment.MIDDLE_RIGHT);
		b1.addClickListener(e -> { 
			currentPic = getHood().getNextPicture(currentPic, getPictureAssociation(), -1);  
			updater.run();
			});
		b2.addClickListener(e -> { 
			currentPic = getHood().getNextPicture(currentPic, getPictureAssociation(), 1);  
			updater.run();
			});
		Button del = new Button();
		del.setIcon(FontAwesome.TRASH_O);
		buttons.addComponent(del);
		buttons.setComponentAlignment(del, Alignment.BOTTOM_RIGHT);
		del.addClickListener(e -> {
			MessageBox.createQuestion()
				.withCaption("Delete Picture")
				.withMessage("Do you really want to delete the picture?")
				.withYesButton(() -> saveWhile(() -> { currentPic = getHood().deletePicture(currentPic); updater.run(); }))
				.withNoButton()
				.open();
			});
		if (!currentPic.canEdit(getLoginUser()))
		{
			bl.setVisible(false);
			br.setVisible(false);
			del.setVisible(false);
			comment.setReadOnly(true);
		}
		Button close = new Button("Close");
		close.setIcon(FontAwesome.CLOSE);
		buttons.addComponent(close);
		buttons.setComponentAlignment(close, Alignment.BOTTOM_RIGHT);
		close.addClickListener(e_-> getParentUI().closeDialog());
		close.setVisible(showClose);
		return new Tuple(hl, new Point(w, h));
	}
	
	protected Picture getCurrentPic()
	{
		return currentPic;
	}
}
