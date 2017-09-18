package net.parsonsrun.mobile;

import java.util.ArrayList;

import com.vaadin.addon.touchkit.ui.SwipeView;
import com.vaadin.addon.touchkit.ui.VerticalComponentGroup;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;

import de.steinwedel.messagebox.ButtonOption;
import de.steinwedel.messagebox.MessageBox;
import net.parsonsrun.ClickLabel;
import net.parsonsrun.domain.Match;
import net.parsonsrun.domain.Round;
import net.parsonsrun.domain.Tournament;

public class RoundsView extends AbstractRoundsView
{
	protected Tournament tourny;
	
	public RoundsView(Tournament t, int i)
	{
		tourny = t;
		tourny.addListener(this);
		setCurrentPageIndex(i);
	}
	
	protected int getPagesCount()
	{
		return getRounds().size();
	}
	
	public String toString()
	{
		return super.toString() + " (" + (currentPageIndex + 1) + "):" + tourny;
	}

	protected AbstractRoundsView nextView(int i)
	{
		return new RoundsView(getTourny(), i);
	}
	protected void finalize() throws Throwable 
	{
        try {
        	tourny.removeListener(this);
        }
        catch(Throwable t) {
            throw t;
        }
        finally {
            super.finalize();
        }
    }
	
	protected String pageName()
	{
		return "Round #" + (currentPageIndex + 1);
	}
	
	protected String fullPageName()
	{
		return getRound(currentPageIndex).getName();
	}

	public String getCaption()
	{
		return tourny.getName() + " (Round #" + (currentPageIndex + 1) + ")";
	}

	protected void buildMatchList()
	{
		Round r = getRound(currentPageIndex);
		for (Match m : r.getMatches())
		{
			ClickLabel l = new ClickLabel(m.getMobileHtml());
			l.addClickListener(e -> matchSelected(m));
			currentPage.addComponent(l);
		}
	}
	
	public void matchSelected(Match m)
	{
		if (m.isBye())
			Notification.show("Unable to update a BYE match", Type.WARNING_MESSAGE);
		else
		{
			if (m.isFuture())
				openInviteDialog(m);
			else
				openMatchEditor(m);
		}
	}
	
	protected void openInviteDialog(Match m)
	{
		MessageBox.createQuestion()
			.withCaption("Future Match")
			.withMessage("Do you want to\n\nSend an Invite to the Match\n\nor\n\nEnter the Score?")
			.withOkButton(() -> sendMatchInvite(m), ButtonOption.caption("Invite"))
			.withOkButton(() -> openMatchEditor(m), ButtonOption.caption("Score"))
			.withCancelButton()
			.open();
	}
	
	protected void sendMatchInvite(Match m)
	{
		m.sendInvite(getTourny(), getTourny().getRoundFor(m));
	}
	
	protected void openMatchEditor(Match m)
	{
		String warning = m.canEdit(getLoginUser());
		if (warning == null)
		{
			navigateTo(new MatchView(m));
		}
		else
			Notification.show(warning, Type.ERROR_MESSAGE);
	}
	
	protected ArrayList<Round> getRounds()
	{
		return getTourny().getRounds();
	}
	
	protected Round getRound(int i)
	{
		return getTourny().getRound(i);
	}
	

	public Tournament getTourny()
	{
		return tourny;
	}

	public void setTourny(Tournament tourny)
	{
		this.tourny = tourny;
	}

}
