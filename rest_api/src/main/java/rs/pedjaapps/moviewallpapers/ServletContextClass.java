package rs.pedjaapps.moviewallpapers;

import java.io.IOException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class ServletContextClass implements ServletContextListener
{
    public void contextInitialized(ServletContextEvent arg0)
    {
        try
        {
            DatabaseManager.init();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }


    public void contextDestroyed(ServletContextEvent arg0)
    {
        if(DatabaseManager.isInitialized())
        DatabaseManager.getInstance().close();
    }

}