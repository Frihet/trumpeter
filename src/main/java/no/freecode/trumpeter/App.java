package no.freecode.trumpeter;

import static com.sun.akuma.CLibrary.LIBC;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Properties;

import no.freecode.trumpeter.xmpp.XmppManager;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.NestedRuntimeException;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import com.sun.akuma.Daemon;


/**
 * RT Agent main startup class.
 */
public class App {
    private static final String PID_FILE = "var/trumpeter.pid";
    private static final Logger logger = Logger.getLogger(App.class);

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws InterruptedException {
        
        Daemon daemon = new Daemon();
        if (daemon.isDaemonized()) {
            // Initialize as a daemon using the Akuma library.
            try {
                try {
                    // Write the process id to a pid file. This is used when calling the stop command later.
                    FileWriter pidWriter = new FileWriter(PID_FILE);
                    pidWriter.write(Integer.toString(LIBC.getpid()));
                    pidWriter.close();

                } catch (IOException e1) {
                    printerr("Unable to write PID file: " + e1.getMessage());
                }

                daemon.init();
                
            } catch (Exception e) {
                printerr("Failed to start as daemon. Error was: " + e.getMessage());
            }
        }

        CommandLineParser parser = new PosixParser();

        // Create the options
        Options options = new Options();
        options.addOption("h", "help", false, "Print this message.");
        options.addOption("d", "debug", false, "Bring up a window showing the communication with the XMPP server.");
        options.addOption("g", "greeting", false, "Send a greeting message via all agents (reads one line from stdin).");
//        options.addOption(null, "daemonize", false, "Start as a Unix daemon (only works under Linux, Solaris and Mac OS X).");

//        options.addOption("v", "verbose", false, "print more information");

        try {
            // Parse the command line arguments.
            CommandLine line = parser.parse(options, args);

            List<String> leftoverArgs = line.getArgList();

            if (line.hasOption("help") || leftoverArgs.size() == 0) {
                // Generate help statement.
                System.out.println(getApplicationName() + "\n--\n");
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("trumpeter [OPTION]... {run|start|stop}\n", options);

                System.out.println(
//                      "--------------------------------------------------------------------------------" +
                        "\n" +
                        " run             Run the server as a normal application.\n" +
                        " start           Start the server as a background process (only works on Linux,\n" +
                        "                 Solaris and Mac OS X).\n" +
//                        " status          Print the current status of the application.\n" +
                        " stop            Stop the server.\n" +
                        "\n");

            } else {
                if (leftoverArgs.contains("start")) {
                    File pidFile = new File(PID_FILE);
                    
                    if (pidFile.exists()) {
                        // TODO: check the PID, and if it's actually running, print out an error message and exit.
//                        printerr("It seems like trumpeter is already running.");
                    }

                    println("Starting " + getApplicationName() + " as a background process.");

                    if (!daemon.isDaemonized()) {
                        try {
                            daemon.daemonize();
                        } catch (IOException e) {
                            printerr("Failed to start as daemon. Error was: " + e.getMessage());
                        }
                        
                        System.exit(0);
                    }

                } else if (leftoverArgs.contains("stop")) {
                    try {
                        File pidFile = new File(PID_FILE);
                        BufferedReader pidReader = new BufferedReader(new FileReader(pidFile));

                        println("Stopping trumpeter...");

                        // Send KILL signal to the application.
                        int pid = Integer.parseInt(pidReader.readLine());
                        
                        int res = LIBC.kill(pid, 3);

                        logger.debug("Tried to kill process with pid=" + pid + ". Result was: " + res + ".");

                        // TODO: check result (seems to be 0 if ok...?).
//                        println("Tried to kill process " + pid + ". RESULT: " + res);

                        pidReader.close();

                        // Try to delete the PID file.
                        boolean success = pidFile.delete();
                        
//                        if (!success) {
//                            printerr("Unable to delete PID file.");
//                        }

                    } catch (FileNotFoundException e) {
                        printerr("Cannot stop the application. It does not seem to be running.");
                        
                    } catch (NumberFormatException e) {
                        printerr("Unable to parse PID file. Error was: " + e.getMessage());

                    } catch (IOException e) {
                        printerr("Unable to parse PID file. Error was: " + e.getMessage());
                    }

                    System.exit(0);
                }

                if (line.hasOption("debug")) {
                    XMPPConnection.DEBUG_ENABLED = true;
                }
                
                String greeting = null;
                if (line.hasOption("greeting")) {
                    try {
                        greeting = new BufferedReader(new InputStreamReader(System.in)).readLine();
                    } catch (IOException e) {
                        println("Unable to read greeting message from standard input.");
                    }
                }

                logger.info("Starting " + getApplicationName() + "...\n--\n");

                try {
                    AbstractApplicationContext context = new ClassPathXmlApplicationContext("application-context.xml");

                    // Make sure the application runs the @Destroy methods when exiting.
                    context.registerShutdownHook();

                    XmppManager manager = (XmppManager) context.getBean("manager");
                    if (greeting != null) {
                        manager.setGreeting(greeting);
                    }
                    
                    try {
                        manager.connect();
                    } catch (XMPPException e) {
                        throw new BeanInitializationException("Unable to connect to XMPP server.", e);
                    }

                    // Invoke the agents at once (i.e. check RT queues):
                    if (manager.isInvokeOnStartup()) {
                        manager.invokeAgents();
                    }

                } catch (NestedRuntimeException e) {
                    printerr("Error: " + e.getMostSpecificCause().getMessage());
                    logger.error("Error: " + e.getMostSpecificCause().getMessage(), e);
                }
            }

        } catch (ParseException exp) {
            println("Unexpected exception: " + exp.getMessage());
        }
    }

    public static void println(String line) {
        System.out.println(line);
        logger.info(line);
    }

    public static void printerr(String line) {
        System.err.println(line);
        logger.error(line);
    }
    
    private static String getApplicationName() {
        try {
            Properties props = PropertiesLoaderUtils.loadAllProperties("application.properties");
            return props.getProperty("application.name") + " v. " + props.getProperty("application.version");

        } catch (IOException e) {
            printerr("Error! Unable to load application.properties. (" + e.getMessage() + ").");
            return "APP NAME / VERSION";
        }
    }
}
