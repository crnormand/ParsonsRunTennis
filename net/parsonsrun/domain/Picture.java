package net.parsonsrun.domain;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import javax.imageio.ImageIO;

import org.imgscalr.Scalr;

import com.vaadin.data.util.MethodProperty;
import com.vaadin.event.MouseEvents.ClickListener;
import com.vaadin.server.FileResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ChameleonTheme;

import de.steinwedel.messagebox.MessageBox;
import net.parsonsrun.ParsonsRunUI;
import net.parsonsrun.Utils;

public class Picture extends NotableObject
{
	private static final long serialVersionUID = 1L;

	protected Neighborhood hood;
	protected Player owner;
	protected DomainObject association;
	protected long pictureId;
	protected String filename;
	protected int width;
	protected int height;
	
	public static Picture rawPicture(Neighborhood h, Player o, DomainObject a)
	{
		return new Picture(h, o, a);
	}
	
	public static Picture newPicture(Neighborhood h, Player o, DomainObject a)
	{
		Picture p = rawPicture(h, o , a);
		p.setPictureId();
		return p;
	}

	protected Picture(Neighborhood h, Player o, DomainObject a)
	{
		setHood(h);
		setOwner(o);
		setAssociation(a);
		setNote("");
	}
	
	protected void setPictureId()
	{
		pictureId = getHood().getNextPictureId();
		setFilename(makeFilename());
	}
    
    public void resize()
    {
    	try
    	{
    		BufferedImage img = ImageIO.read(getFile()); 
    		BufferedImage scaledImg = Scalr.resize(img, Utils.PICTURE_WIDTH, Utils.PICTURE_HEIGHT);
    		img.flush();
    		saveSize(scaledImg);
    		ImageIO.write(scaledImg, "jpg", getFile());
    		scaledImg.flush();
    	}
    	catch (IOException e)
    	{
    		Utils.println("Unable to resize " + getFilename() + "\n" + e);
    	}
    }
    
    public boolean isWider()
    {
    	return width > height;
    }
    
    public Image newImage()
    {
    	return new Image(null, getSource());
    }
    
    public OutputStream uploadStream() 
	{
		FileOutputStream fos = null; // Stream to write to
		File file = null;
		try 
		{
			file = new File(getFilename());
			fos = new FileOutputStream(file);
		} 
		catch (final FileNotFoundException e) 
		{
			Notification.show("Could not open file<br>",
			   e.getMessage(),
			   Notification.Type.ERROR_MESSAGE);
			return null;
		}
		return fos; // Return the output stream to write to
	}
    
    public void saveSize(BufferedImage bi)
    {
		width = bi.getWidth();
		height = bi.getHeight();
    }
    
    public boolean canEdit(Player p)
    {
    	return owner.equals(p) || p.isAdmin();
    }
	
    public FileResource getSource()
    {
    	return new FileResource(new File(getFilename()));
    }
    
    public void rotate(int degrees)
    {
    	try
    	{
    		BufferedImage in = readFile();
    		if (in == null)
    			return;
    		BufferedImage out = Utils.rotate(in, degrees);
    		saveSize(out);
    		ImageIO.write(out, "jpg", getFile());
    		in.flush();
    		out.flush();
    	}
    	catch (IOException e)
    	{
    		Utils.println("Unable to rotate image: " + getFilename() + "\n" + e);
    	}
    }
    
    public BufferedImage readFile()
    {
    	try
    	{
    		return ImageIO.read(getFile());
    	} 
    	catch (IOException e)
    	{
    		Utils.println("Unable to read image: " + getFilename() + "\n" + e);
    	}
    	return null;
    }
      
    protected StreamResource get90RightSource()
    {
    	return new StreamResource(new StreamSource()
    			{
    				public InputStream getStream()
    				{
    				   	ByteArrayOutputStream imagebuffer = new ByteArrayOutputStream();
    			    	BufferedImage in = readFile();
    			    	BufferedImage out = Utils.rotate(in, 90);
    			    	try
    			    	{
    			    		ImageIO.write(out, "jpg", imagebuffer);
    			    	}
    			    	catch (IOException e)
    			    	{
    			    	}
    				    return new ByteArrayInputStream(imagebuffer.toByteArray());
	
    				}
    			}, getFilename());
    }
    
    public Image get90RightImage()
    {
    	return new Image(null, get90RightSource());
    }
    
    public File getFile()
    {
    	return new File(getFilename());
    }
    
    public URL getUrl()
    {
    	URI u = getFile().toURI();
    	try
    	{
    		return u.toURL();
    	}
    	catch (MalformedURLException e)
    	{}
    	return null;
    }
    
    public int getWidthFor(int target)
    {
		double h1 = (double)target;
		double h2 = (double)height;
		double w = (double)width;
		return (int)((h1 / h2) * w);
    }
    
    public void delete()
    {
    	if (getFile().exists())
    		getFile().delete();
    }
    
	protected String makeFilename()
	{
		StringBuilder s = new StringBuilder();
		s.append(Utils.pictureDirectory());
		if (pictureId < 10)
			s.append('0');
		long test = 100000;
		while (test > 0)
		{
			if (pictureId < test)
				s.append('0');
			test = test / 10;
		}
		s.append(pictureId);
		s.append("-");
		s.append(getOwner().getShortLast());
		s.append(getOwner().getFirstChar());
		s.append(".jpg");
		return s.toString();
	}
		
	public Player getOwner()
	{
		return owner;
	}
	public void setOwner(Player owner)
	{
		this.owner = owner;
	}

	public Neighborhood getHood()
	{
		return hood;
	}

	public void setHood(Neighborhood hood)
	{
		this.hood = hood;
	}

	public String getFilename()
	{
		return filename;
	}
	
	public Component newImage(int targetHeight, ClickListener listener)
	{
		Image image = newImage();
		image.setHeight(targetHeight + "px");
		image.addClickListener(listener);
		Label l = new Label("<div style='width: 100%;text-align: center;font-size: 0.6em;'>" + getNote() + "</div>", ContentMode.HTML);
		GridLayout v = new GridLayout(1, 2);
		v.addComponent(image, 0, 0);
		v.addComponent(l, 0, 1);
		return v;
	}
	
	public String getNote()
	{
		String n = super.getNote();
		return n == null ? "" : n;
	}

	public void setFilename(String filename)
	{
		this.filename = filename;
	}

	public DomainObject getAssociation()
	{
		return association;
	}

	public void setAssociation(DomainObject association)
	{
		this.association = association;
	}

	public int getWidth()
	{
		return width;
	}

	public int getHeight()
	{
		return height;
	}
}
