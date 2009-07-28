package no.freecode.trumpeter;

import java.io.IOException;
import java.util.Properties;

import no.freecode.trumpeter.xmpp.XmppManager;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.Logger;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.NestedRuntimeException;
import org.springframework.core.io.support.PropertiesLoaderUtils;

/**
 * RT Agent main startup class.
 */
public class App {
    private static final Logger logger = Logger.getLogger(App.class);

    public static void main(String[] args) throws InterruptedException {
        CommandLineParser parser = new PosixParser();

        // Create the options
        Options options = new Options();
        options.addOption("h", "help", false, "print this message");
//        options.addOption("v", "verbose", false, "print more information");

        try {
            // Parse the command line arguments.
            CommandLine line = parser.parse(options, args);
            
            String[] leftoverArgs = line.getArgs();

            if (line.hasOption("help")) {
                // Generate help statement.
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp(getApplicationName(), options);

            } else {
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

        } catch (ParseException exp) {
            System.out.println("Unexpected exception: " + exp.getMessage());
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
