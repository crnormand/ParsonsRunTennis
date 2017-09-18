package net.parsonsrun.mobile;
import com.vaadin.ui.Image;

import net.parsonsrun.domain.DomainObject;
import net.parsonsrun.domain.Picture;

public class PictureView extends MobileBaseView
{
	protected Picture orig;
	protected Picture current;
	protected DomainObject assoc;
	
	public PictureView(DomainObject a)
	{
		assoc = a;
	}

	public PictureView(DomainObject a, Picture o, Picture c)
	{
		assoc = a;
		orig = o;
		current = c;
	}
	
	@Override
	public String getCaption()
	{
		return null;
	}

	@Override
	public void buildUI()
	{
		content.addComponent(getSwipeReminder());
		int w = getBrowserWidth();
		int h = getBrowserHeight() - 13;  // Swipe reminder
		Image img = null;
		if (getCurrent().isWider())
		{
			img = getCurrent().get90RightImage();
		}
		else
		{
			img = getCurrent().newImage();
		}
		img.setWidth(w + "px");
		img.setHeight(h + "px");
		content.addComponent(img);
		img.addClickListener(e -> back());
	}
	
	public void swipedLeft()
	{
		Picture next = getNext(-1);
		if (next != null)
		{
			getNavigationManager().setPreviousComponent(new PictureView(assoc, orig, next));
		}
	}
	
	public void swipedRight()
	{
		Picture next = getNext(1);
		if (next != null)
		{
			getNavigationManager().setNextComponent(new PictureView(assoc, orig, next));
		}
	}

	protected void back()
	{
		while (getNavigationManager().getCurrentComponent() instanceof PictureView)
		{
			navigateBack();
		}
	}
	
	public Picture getCurrent()
	{
		if (orig == null)
		{
			orig = getHood().getPicture(assoc, nextInt(getHood().getPictures(assoc).size()));
			current = orig;
		}
		return current;
	}

	public Picture getNext(int delta)
	{
		current = getHood().getNextPicture(current, assoc, delta);
		if (current == orig)
			return null;
		return current;
	}
}
