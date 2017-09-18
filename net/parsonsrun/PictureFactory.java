package net.parsonsrun;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.Upload;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Upload.FailedEvent;
import com.vaadin.ui.Upload.FailedListener;
import com.vaadin.ui.Upload.FinishedEvent;
import com.vaadin.ui.Upload.FinishedListener;
import com.vaadin.ui.Upload.ProgressListener;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.StartedEvent;
import com.vaadin.ui.Upload.StartedListener;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;

import de.steinwedel.messagebox.MessageBox;
import net.parsonsrun.domain.*;

public class PictureFactory implements Receiver, SucceededListener, ProgressListener, FinishedListener, FailedListener, StartedListener
{
	protected Neighborhood hood;
	protected Player owner;
	protected DomainObject association;
	protected Picture current;
	protected ProgressBar bar;
	protected Runnable callback;
	
	public PictureFactory(Neighborhood h, Player o, DomainObject a, Runnable c)
	{
		setHood(h);
		setOwner(o);
		setAssociation(a);
		setCallback(c);
	}
	
	public static Upload makeUpload(Neighborhood h, Player o, DomainObject a, Runnable c)
	{
		PictureFactory factory = new PictureFactory(h, o, a, c);
		return factory.makeUpload();
	}
	
	protected Picture makeNewPicture()
	{
		current = Picture.newPicture(getHood(), getOwner(), getAssociation());
		return current;
	}
	
	public OutputStream receiveUpload(String filename, String mimeType) 
	{
		Utils.debug("Uploaded stream created...");
		makeNewPicture();
		return current.uploadStream();
	}
	
	protected void openProgress()
	{
		ParsonsRunUI ui = (ParsonsRunUI)UI.getCurrent();
		if (ui != null)
		{
			ui.setPollInterval(200);
			bar = new ProgressBar();
			bar.setImmediate(true);
			ui.openDialog(bar, ui.getBrowserWidth() - 50, 70).removeAllCloseShortcuts();
		}
	}
	
    public void uploadSucceeded(SucceededEvent event) 
    {
    	getCurrent().resize();
    	ParsonsRunUI ui = (ParsonsRunUI)UI.getCurrent();
    	int w = ui.getBrowserWidth() - 40;
    	int h = ui.getBrowserHeight() - 150;
    	final VerticalLayout img = new VerticalLayout();
    	TextField tf = new TextField("Add comment:");
    	tf.setSizeFull();
    	HorizontalLayout hb = new HorizontalLayout();
    	hb.setSizeFull();
    	VerticalLayout pnl = new VerticalLayout(img, hb, tf);
    	pnl.setSpacing(true);
		Button bl = new Button();
		bl.setIcon(FontAwesome.ROTATE_LEFT);
		hb.addComponent(bl);
		hb.setComponentAlignment(bl, Alignment.MIDDLE_LEFT);
		Label l = new Label("<- rotate ->");
		hb.addComponent(l);
		hb.setComponentAlignment(l, Alignment.MIDDLE_CENTER);
		Button br = new Button();
		br.setIcon(FontAwesome.ROTATE_RIGHT);
		hb.addComponent(br);
		hb.setComponentAlignment(br, Alignment.MIDDLE_RIGHT);
		Runnable updater = () -> {
			img.removeAllComponents(); 
			Image i = getCurrent().newImage();
			if (getCurrent().isWider())
				i.setWidth((w - 50) + "px");
			else
				i.setHeight((h - 150) + "px");
			img.addComponent(i); 
			img.setComponentAlignment(i, Alignment.TOP_CENTER);
		};
		bl.addClickListener(e -> { 
			getCurrent().rotate(270); 
			updater.run();
		});
		br.addClickListener(e -> { 
			getCurrent().rotate(90); 
			updater.run();
		});
		updater.run();
    	MessageBox.create()
	    	.withMessage(pnl)
	    	.withOkButton(() -> {
	    		getCurrent().setNote(tf.getValue());
	        	Utils.saveWhile(() -> {
	        		getHood().addPicture(getCurrent());
	        		if (getCallback() != null)
	        			getCallback().run();
	        	});
	    	} )
	    	.withWidth(w + "px")
	    	.withDialogPosition(20, 10)
	    	.open();
    }


	@Override
	public void uploadFailed(FailedEvent event)
	{
		getCurrent().delete();
	}

	@Override
	public void uploadFinished(FinishedEvent event)
	{
		Utils.debug("Uploaded finished.");
		synchronized (this)
		{
			bar = null;
			ParsonsRunUI ui = (ParsonsRunUI)UI.getCurrent();
			if (ui != null)
			{
				ui.closeDialog();
				ui.setPollInterval(ParsonsRunUI.POLL_INTERVAL);
			}
		}
	}

	@Override
	public void updateProgress(long readBytes, long contentLength)
	{
		synchronized (this)
		{
			UI ui = UI.getCurrent();
			if (ui != null && bar != null)
			{
				float p = (float)readBytes / (float)contentLength;
				Utils.debug("Upload progress: " + p);
				ui.access(() -> bar.setValue(p));
			}
		}
	}

	@Override
	public void uploadStarted(StartedEvent event)
	{
		Utils.debug("Uploaded started...");
		openProgress();	
	}

	public Neighborhood getHood()
	{
		return hood;
	}
	
	protected Upload makeUpload()
	{
		Upload upload = new Upload(null, this);
		upload.setImmediate(true);
		upload.setButtonCaption("[Upload Pic]");
		upload.addSucceededListener(this);
		upload.addStartedListener(this);
		upload.addFailedListener(this);
		upload.addFinishedListener(this);
		upload.addProgressListener(this);
		return upload;
	}

	public void setHood(Neighborhood hood)
	{
		this.hood = hood;
	}

	public Player getOwner()
	{
		return owner;
	}

	public void setOwner(Player owner)
	{
		this.owner = owner;
	}

	public DomainObject getAssociation()
	{
		return association;
	}

	public void setAssociation(DomainObject association)
	{
		this.association = association;
	}

	public Picture getCurrent()
	{
		return current;
	}

	public void setCurrent(Picture current)
	{
		this.current = current;
	}

	public Runnable getCallback()
	{
		return callback;
	}

	public void setCallback(Runnable callback)
	{
		this.callback = callback;
	}

}
