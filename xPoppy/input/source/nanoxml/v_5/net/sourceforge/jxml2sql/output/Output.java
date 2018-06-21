package net.sourceforge.jxml2sql.output;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.Vector;
import net.sourceforge.jxml2sql.database.*;

/**
 * Generic object for outputting databases
 *
 * @author Adam VanderHook
 * @version $Id: Output.java,v 1.4 2001/05/24 01:17:53 acidos Exp $
 * @since 0.1.0-pre1
 */
public abstract class Output {

	/**
	 * The database to output
	 *
	 * @since 0.1.0-pre1
	 */
	protected Database db;

	/**
	 * Creates an Output object with a <code>NULL</code> database.
	 */
	public Output() {
		setDatabase(null);
	}

	/**
	 * Creates an Output object with the specified database.
	 *
	 * @param	db	The database to output
	 * @since 0.1.0-pre1
	 */
	public Output(Database db) {
		setDatabase(db);
	}

	/**
	 * Generates and returns the output represented by the class extending
	 * net.sourceforge.jxml2sql.Output.  Since output can be a vairiety of
	 * things, such as a String or an Image, outputs are "down-cast" into the
	 * Object object before they are returned.  For exmaple, lets say we want
	 * our output to be one line: the name of the database.  We could construct
	 * the following code for this method:
	 * <p>
	 * <code>
	 *		String result = "Database name: " + db.getName() + "\n";
	 * </code>
	 * <p>
	 * We would then have to "down-cast" our String into Object before we
	 * return it.  That can be done as follows:
	 * <p>
	 * <code>
	 *		return (Object)result;
	 * </code>
	 * <p>
	 * Now, once we have receive our output, we must "up-class" it back into the
	 * object it orignally was before we can use it.  Say, for instance, we want
	 * to print out output to System.out -- we could do it as follows:
	 * <p>
	 * <code>
	 *		System.out.println((String)ourOutputObject.getOutput());
	 * </code>
	 *
	 * @return	Object	The "down-cast"ed output
	 * @since 0.1.0-pre2
	 */
	public abstract Object getOutput();

	/**
	 * Sets the Database object to output
	 *
	 * @param	db	The database to output
	 * @since 0.1.0-pre1
	 */
	public void setDatabase(Database db) {
		this.db = db;
	}

	/**
	 * Retrieves the database being outputted
	 *
	 * @return	Database	The database to be outputted
	 * @since 0.1.0-pre1
	 */
	public Database getDatabase() {
		return db;
	}

	/**
	 * Strips any newline characters from passed string and returns the result.
	 * Sometimes description fields in the XML will be filled in as follows:
	 * <p>
	 * <code>
	 *		&lt;description&gt;This is my very own<br>
	 *		personal description&lt;/description&gt;
	 * </code>
	 * <p>
	 * This can cause output in SQL (and possibly other forms) as follows:
	 * <p>
	 * <code>
	 * #	myfield		- This is my very own<br>
	 * 	personal description
	 * </code>
	 * <p>
	 * Which of course results in an error when fed into an SQL engine.
	 *
	 * @return	String		The formatted description text
	 * @since	0.3.0
	 */
	protected String getFormattedDescription(String desc) {
		String result = "";

		StringTokenizer stDesc = new StringTokenizer(desc, "\n");

		while(stDesc.hasMoreTokens())
			result += " " + stDesc.nextToken().trim();

		return result;
	}

	/**
	 * Returns the current date and time (hours and minutes) when the
	 * method was called.  Used when timestamping generated output.
	 *
	 * @return		String		A String object represting the current
	 *							date and time
	 * @since		0.3.0
	 */
	protected String getGenerationTime() {
		return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.FULL).format(new Date());
//		String result = "";
//		Calendar tempCalendar = Calendar.getInstance();
//
//		result += tempCalendar.get(Calendar.DATE);
//		result += " ";
//		result += tempCalendar.get(Calendar.HOUR) + ":" + tempCalendar.get(Calendar.MINUTE);
//
//		return result;
	}

}
