// import JXML2SQL;

/**
 * Class that parses command-line options and invokes methods in the XML2SQL
 * class to generate the desired output.  This is the "main class" of jxml2sql.
 *
 * $Id: JXML2SQLApp.java,v 1.2 2001/01/08 15:27:05 acidos Exp $
 *
 * @author Adam VanderHook
 */
public class JXML2SQLApp {

	private final String VERSION = "0.2.0";

	private String outType = "sql";
	private String xmlFile = null;
	private boolean showHelp = false;
	private boolean showVersion = false;

	private JXML2SQL theApp;

	/**
	 * Creates a JXML2SQLApp object using the specified command-line
	 * arguments.
	 *
	 * @param	args[]		An array representing the command-line arguments
	 */
	private JXML2SQLApp(String args[]) throws Exception {
		// Do some initial testing of arguments
		if(args.length < 1) {
			displayHelp();
			System.exit(1);
		}

		// Minimum Number of Arguments, lets configure
		for(int i = 0; i < args.length; i++) {
			if(args[i].startsWith("--")) {
				if(args[i].endsWith("output"))
					outType = args[i + 1].toLowerCase();
				else if(args[i].endsWith("version"))
					showVersion = true;
				else if(args[i].endsWith("help"))
					showHelp = true;
				else {
					System.err.println("Error: unknown option " + args[i]);
					System.exit(5);
				}
			} else if(args[i].toLowerCase().endsWith(".xml"))
				xmlFile = args[i];
		}

		// If we are showing information that is all that we have to do
		if(showVersion) {
			displayVersion();
			System.exit(0);
		} else if(showHelp) {
			displayHelp();
			System.exit(0);
		}

		// Exit with help if we don't have an XML file
		if(xmlFile == null) {
			displayHelp();
			System.exit(4);
		}

		theApp = new JXML2SQL(xmlFile);

		if(outType.equals("sql"))
			System.out.println(theApp.generateSQL());
		else if(outType.equals("html"))
			System.out.println(theApp.generateHTML());
		else {
			displayHelp();
			System.exit(5);
		}

		// Everything went ok, exit cleanly
		System.exit(0);
	}

	/**
	 * Prints the version of JXML2SQL as well as the copy of libjxml2sql to
	 * standard out.
	 */
	private void displayVersion() {
		String msg = "";
		msg += "   jxml2sql version: " + VERSION + "\n";
		msg += "libjxml2sql version: " + theApp.getVersion() + "\n";

		System.out.println(msg);
	}

	/**
	 * Prints a usage summary to standard out.
	 */
	private void displayHelp() {
		String msg = "";
		msg += "Usgage: jxml2sql [ options ] [ --output output-type ] xml-file";
		msg += "\n\nOptions:\n";
		msg += "\t--version\t\tDisplays version information\n";
		msg += "\t--help\t\t\tDisplays this screen\n\n";
		msg += "Output Types:\n";
		msg += "\tsql\t\t\tSQL output\n";
		msg += "\thtml\t\t\tHTML output\n";

		System.out.println(msg);
	}
	
	public static void main(String args[])throws Exception {
		JXML2SQLApp app = new JXML2SQLApp(args);
	}

}
