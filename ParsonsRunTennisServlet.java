package net.parsonsrun;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;

import com.vaadin.addon.touchkit.server.TouchKitServlet;
import com.vaadin.addon.touchkit.settings.TouchKitSettings;
import com.vaadin.server.ServiceException;
import com.vaadin.server.SessionInitEvent;
import com.vaadin.server.SessionInitListener;
import com.vaadin.server.UIClassSelectionEvent;
import com.vaadin.server.UIProvider;
import com.vaadin.ui.UI;

import net.parsonsrun.desktop.DesktopUI;
import net.parsonsrun.mobile.MobileUI;

@SuppressWarnings("serial")
@WebServlet(value = "/*", asyncSupported = true, initParams = { @WebInitParam(name = "productionMode", value = "false") })
public class ParsonsRunTennisServlet extends TouchKitServlet {

    private final UIProvider uiProvider = new UIProvider() {
        @Override
        public Class<? extends UI> getUIClass(UIClassSelectionEvent event) 
        {
            String userAgent = event.getRequest().getHeader("user-agent");
            Utils.println("UserAgent: '"+ userAgent + "'");
            if (userAgent == null)
            	return DesktopUI.class;
            String ua = userAgent.toLowerCase();
            // webkit: safari, chrome, android, iOS
            // gecko: FF, IE11
            // trident/6|7: IE10/11
            // windows phone: WP
    		if (ua.contains("webkit") && !ua.contains("ipad"))
    		{
    			if (!Utils.TREAT_CHROME_AS_MOBILE && ua.contains("chrome"))
    				return DesktopUI.class;
   			else
    				return MobileUI.class;
    		}
    		else
    			return DesktopUI.class;
        }
    };

    @Override
    protected void servletInitialized() throws ServletException 
    {
        super.servletInitialized();

        getService().addSessionInitListener(new SessionInitListener() {
            @Override
            public void sessionInit(SessionInitEvent event) throws ServiceException 
            {
                event.getSession().addUIProvider(uiProvider);
            }
        });

        TouchKitSettings s = getTouchKitSettings();
        s.getWebAppSettings().setWebAppCapable(true);
        s.getWebAppSettings().setStatusBarStyle("black");
        String contextPath = getServletConfig().getServletContext().getContextPath();
        s.getApplicationIcons().addApplicationIcon(contextPath + "/VAADIN/themes/prmobile/Tennis.png");
    }
}