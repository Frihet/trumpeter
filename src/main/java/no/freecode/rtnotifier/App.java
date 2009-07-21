package no.freecode.rtnotifier;

import java.io.IOException;
import java.util.Properties;

import no.freecode.rtnotifier.xmpp.XmppManager;

import org.apache.log4j.Logger;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.NestedRuntimeException;
import org.springframework.core.io.support.PropertiesLoaderUtils;

/**
 * RT Agent main startup class.
 */
public class App 
{
    private static final Logger logger = Logger.getLogger(App.class);

    public static void main( String[] args ) throws InterruptedException
    {
        System.out.println("Starting " + getApplicationName() + "...\n--\n");

        try {
            AbstractApplicationContext context = new ClassPathXmlApplicationContext("application-context.xml");

            // Make sure the application runs the @Destroy methods when exiting.
            context.registerShutdownHook();

            // Invoke the agents at once (i.e. check RT queues), if configured:
            XmppManager manager = (XmppManager) context.getBean("manager");
            if (manager.isInvokeOnStartup()) {
                manager.invokeAgents();
            }

        } catch (NestedRuntimeException e) {
            System.err.println("Error: " + e.getMostSpecificCause().getMessage());
            logger.error("Error: " + e.getMostSpecificCause().getMessage(), e);
        }
    }

    private static String getApplicationName() {
        try {
            Properties props = PropertiesLoaderUtils.loadAllProperties("application.properties");
            return props.getProperty("application.name") + " v. " + props.getProperty("application.version");

        } catch (IOException e) {
            System.err.println("Error! Unable to load application.properties. (" + e.getMessage() + ").");
            return "APP NAME / VERSION";
        }
    }
}
