package net.parsonsrun.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import com.vaadin.ui.UI;

import net.parsonsrun.ParsonsRunUI;

/**
 * 	This is the superclass of all Domain objects (anything that will be persistent).
 */
public class DomainObject implements Serializable
{
	private static final long serialVersionUID = 1L;
	protected HashMap<String, Object> generics = new HashMap<String, Object>();
	protected long globalId;
	
	transient ArrayList<UpdateListener> listeners = new ArrayList<UpdateListener>();
	
	
    private void writeObject(java.io.ObjectOutputStream stream) throws java.io.IOException
    {
         stream.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream stream) throws java.io.IOException, ClassNotFoundException
    {
        stream.defaultReadObject();
        listeners = new ArrayList<UpdateListener>();
    }
    
    public long getGlobalId()
    {
    	return globalId;
    }
    
    public String getGlobalIdString()
    {
    	return String.valueOf(getGlobalId());
    }
    
    public void setGlobalId(long id)
    {
    	globalId = id;
    }
	
	public void addListener(UpdateListener l)
	{
		synchronized (listeners)
		{
			listeners.add(l);
		}
	}
	
	public void removeListener(UpdateListener l)
	{
		synchronized (listeners)
		{
			listeners.remove(l);
		}
	}
	
	public void updateListeners()
	{
		synchronized (listeners)
		{
			for (UpdateListener l : listeners)
				l.update();
		}
	}
	
	public String toString()
	{
		return getClass().getSimpleName();
	}
	
	protected HashMap<String, Object> getGenerics()
	{
		if (generics == null)
			generics = new HashMap<String, Object>();
		return generics;
	}

	public String getString(String key)
	{
		return (String)getGenerics().get(key);
	}
	public String setString(String key, String value)
	{
		return (String)getGenerics().put(key, value );
	}
	public boolean getBoolean(String key)
	{
		Boolean b = (Boolean)getGenerics().get(key);
		return b == null ? false : b.booleanValue();
	}
	public void setBoolean(String key, boolean v)
	{
		getGenerics().put(key, v);
	}
	
	public ParsonsRunUI getUI()
	{
		return (ParsonsRunUI) UI.getCurrent();
	}
	
	public Player getLoginUser()
	{
		ParsonsRunUI ui =  getUI();
		return ui == null ? null : ui.getLoginUser();
	}
	public Player getCurrentPlayer()
	{
		return getUI().getCurrentPlayer();
	}
}
