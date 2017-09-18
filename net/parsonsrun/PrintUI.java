package net.parsonsrun;

import com.vaadin.server.*;
import com.vaadin.shared.ui.label.*;
import com.vaadin.ui.*;

public class PrintUI extends UI
{
	public static final String CONTENT = "htmlToPrint";

    @Override
    protected void init(VaadinRequest request) {
    	String html = (String)VaadinSession.getCurrent().getAttribute(CONTENT);
        setContent(new Label(html, ContentMode.HTML));

        // Print automatically when the window opens
        JavaScript.getCurrent().execute("setTimeout(function() {print(); self.close();}, 0);");
    }
    
    public static void setHtmlContent(String html)
    {
    	VaadinSession.getCurrent().setAttribute(CONTENT, html);
    }
    
    public static Button printButton(String label)
    {
		BrowserWindowOpener opener = new BrowserWindowOpener(PrintUI.class);
		opener.setFeatures("height=700,width=900,resizable");
		Button print = new Button(label);
		opener.extend(print);
		return print;
    }
}
