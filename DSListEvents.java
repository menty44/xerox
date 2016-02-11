package dslistevents;

/**
 * Description: This example demonstrate how to listen to docushare events and
 * display information related to an event.
 *
 * Copyright © 1996-2007 Xerox Corporation. All Rights Reserved.
 *
 * Author: Darby Cacdac
 *
 * This code is provided without a warranty of any kind. This example does not
 * constitute a patch, release, or software problem fix. The use of this code is
 * for sample usage and reference only.
 */

import java.io.InputStreamReader;
import java.lang.reflect.Field;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import com.xerox.docushare.DSAdminSession;
import com.xerox.docushare.DSAuthenticationException;
import com.xerox.docushare.DSException;
import com.xerox.docushare.DSFactory;
import com.xerox.docushare.DSHandle;
import com.xerox.docushare.DSInvalidLicenseException;
import com.xerox.docushare.DSServer;
import com.xerox.docushare.DSSession;
import com.xerox.docushare.event.DSClassEvent;
import com.xerox.docushare.event.DSConfigEvent;
import com.xerox.docushare.event.DSEventObj;
import com.xerox.docushare.event.DSLinkEvent;
import com.xerox.docushare.event.DSListener;
import com.xerox.docushare.event.DSLoginEvent;
import com.xerox.docushare.event.DSObjectEvent;

public class DSListEvents implements DSListener {

    public static final String progName = "DSListEvents";

    private DSSession dssession;
    private DSServer dsserver;

    private static String host = "localhost"; // DS HostName
    private static int rmiPort = 1099; // rmi port
    private static String userName = "admin"; // userName
    private static String password = "admin"; // password
    private static String domain = "DocuShare"; // User domain

    @SuppressWarnings( "static-access" )
    // Method to process/parse the command line argument
    private void processArgs( String[] args ) {

        Options options = new Options();

        try {
            // Make username a required option
            Option userName = OptionBuilder.withArgName( "username" )
                    .hasArg()
                    .withDescription( "Username to login as." )
                    .isRequired()
                    .create( "u" );

            // Make password a required option
            Option password = OptionBuilder.withArgName( "password" )
                    .hasArg()
                    .withDescription( "User password" )
                    .isRequired()
                    .create( "p" );

            Option host = OptionBuilder.withArgName( "host" )
                    .hasArg()
                    .withDescription( "DocuShare host" )
                    .create( "h" );

            Option rmiPort = OptionBuilder.withArgName( "rmiport" )
                    .hasArg()
                    .withDescription( "DS host RMI port" )
                    .create( "port" );

            Option domain = OptionBuilder.withArgName( "domain" )
                    .hasArg()
                    .withDescription( "DocuShare domain" )
                    .create( "d" );

            options.addOption( userName );
            options.addOption( password );
            options.addOption( host );
            options.addOption( rmiPort );
            options.addOption( domain );

            // Parse the command line
            CommandLineParser parser = new GnuParser();
            CommandLine cmd = parser.parse( options, args );

            // Check options
            if ( cmd.hasOption( "u" ) ) {
                this.userName = cmd.getOptionValue( "u" );
            }
            if ( cmd.hasOption( "p" ) ) {
                this.password = cmd.getOptionValue( "p" );
            }
            if ( cmd.hasOption( "h" ) ) {
                this.host = cmd.getOptionValue( "h" );
            }
            if ( cmd.hasOption( "port" ) ) {
                this.rmiPort = Integer.parseInt( cmd.getOptionValue( "port" ) );
            }
            if ( cmd.hasOption( "d" ) ) {
                this.domain = cmd.getOptionValue( "d" );
            }

        } catch ( NumberFormatException e ) {
            System.out.println( "The RMI port must be an integer." );
            System.exit( -1 );
        } catch ( Exception e ) {
            // automatically generate the help statement
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( progName, options, true );
            // e.printStackTrace();
            System.exit( -1 );
        }
    }

    // Login Routine
    private boolean dsLogin() {

        try {
            System.out.println( "Connecting to " + host + ":" + rmiPort );
            dsserver = DSFactory.createServer( host, rmiPort );

            System.out.print( "Logging in to " + domain + " as user "
                    + userName + ".... " );
            dssession = dsserver.createSession( domain, userName, password );
            System.out.println( "Logged in!\n" );
            return true;

        } catch ( DSInvalidLicenseException dse ) {
            System.out.println( "The DocuShare server does not have the require license." );
            return false;
        } catch ( DSAuthenticationException dse ) {
            System.out.println( "Failed!\n" );
            return false;
        } catch ( DSException dse ) {
            return false;
        }
    }

    // Returns the event name
    private String getOpDescription( int event ) {

        try {
            Class cls = Class.forName( "com.xerox.docushare.event.DSEventObj" );
            // Get a list of all the fields DSEventObj
            Field[] fields = cls.getDeclaredFields();

            for ( int i = 0; i < fields.length; i++ ) {
                // Get the value of the field
                int val = fields[i].getInt( null );
                // Compare val to the value of the event passed.
                if ( val == event ) {
                    // we found it!
                    return fields[i].getName();
                }
            }
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        return "Event description not found.";
    }

    // When an event is fired, this method gets called
    public boolean doProcess( DSEventObj event ) {

        try {

            System.out.println( "\n---------------------oo0oo---------------------" );

            // Display the event that was fired
            System.out.println( "\nEvent fired: " + event.getType() + " ("
                    + getOpDescription( event.getType() ) + ") "
                    + event.getPrincipal() );

            if ( event.getType() == DSEventObj.LINK_CHANGED ) {
                DSLinkEvent linkEvent = (DSLinkEvent) event;
                System.out.print( "     Link Types: " );

                String[] linkTypes = linkEvent.getLinkTypes();
                for ( int i = 0; i < linkTypes.length; i++ ) {
                    System.out.print( linkTypes[i] + "  " );
                }
                System.out.println( "     toString(): " + linkEvent.toString() );

            }

            if ( event.getType() == DSEventObj.LOGIN_FAILED ) {
                DSLoginEvent loginEvent = (DSLoginEvent) event;
                System.out.println( " " + loginEvent.getUserName() + " -> "
                        + loginEvent.getDomainName() );
            } else {
                System.out.println( " " + event.getPrincipal() );
            }

            // For all DSObjectEvents type
            if ( event instanceof DSObjectEvent ) {
                DSObjectEvent objEvent = (DSObjectEvent) event;
                System.out.println( "Modified object: "
                        + objEvent.getObjectHandle() );
                // Get a list of modified properties
                String[] props = objEvent.getPropertyNames();
                for ( int i = 0; i < props.length; i++ ) {
                    System.out.println( "Chg prop: " + props[i] + "  " );
                }

                DSHandle[] objModified = objEvent.getObjectHandles();
                for ( int i = 0; i < objModified.length; i++ ) {
                    System.out.println( "Other modified object: "
                            + objModified[i] + "  " );
                }
            }

            // The class label has changed
            if ( event.getType() == DSEventObj.CLASS_LABEL_CHANGED ) {
                DSClassEvent classEvent = (DSClassEvent) event;

                System.out.println( "classes changed?: "
                        + classEvent.classesChanged() );
                // System.out.println( "getClassName: "
                // + classEvent.getClassName() );
                System.out.println( "data changed?: "
                        + classEvent.dataChanged() );
                System.out.println( "string changed?: "
                        + classEvent.stringsChanged() );
                /*
                 * System.out.println( classEvent.toString() );
                 *
                 * Map map = classEvent.getOriginalClasses( dssession );
                 * System.out.println( "Map size: " + map.size() );
                 *
                 * DSSchemaChangeList cl = classEvent.getChangeList();
                 * DSSchemaChangeList.ClassChanges[] classChgs =
                 * cl.getClassChanges(); System.out.println( "Number of class
                 * changes: " + classChgs.length );
                 *
                 * DSModifySchemaDetails[] schemaChanges =
                 * classEvent.getInstanceDetails(); System.out.println( "Number
                 * of schema changes: " + schemaChanges.length );
                 *
                 * for ( int i = 0; i < schemaChanges.length; i++ ) {
                 * System.out.println( schemaChanges[i].getHandle() );
                 * System.out.println( schemaChanges[i].getOriginalValue() );
                 * System.out.println( schemaChanges[i].getPropertyName() ); }
                 */

            }

            if ( event.getType() == DSEventObj.CONFIG_CHANGED ) {
                DSConfigEvent configEvent = (DSConfigEvent) event;

                System.out.println( "Description: "
                        + configEvent.getDescription() );
                System.out.println( "toString: " + configEvent.toString() );
            }

        } catch ( Exception e ) {
            System.out.println( "Encountered another error" );
            e.printStackTrace();
        }

        return true;
    }

    public static void main( String[] args ) {

        DSListEvents test = new DSListEvents();
        test.processArgs( args );

        try {
            if ( test.dsLogin() ) {
                // Get an admin session
                DSAdminSession adminSession = test.dssession.getAdminSession();
                // Add an event listener
                //adminSession.addEventListener( test, DSEventObj.ALL_EVENTS );
                test.dssession.addEventListener( test, DSEventObj.ALL_EVENTS );
                
                
                
                
                
                
                
            }
        } catch ( DSException dse ) {
            dse.printStackTrace();
        }

        // Wait for a key to be pressed
        InputStreamReader is = new InputStreamReader( System.in );
        System.out.println( "Press CTRL C to exit...." );
    }
}