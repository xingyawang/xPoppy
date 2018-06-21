package net.sourceforge.jxml2sql.output;

import java.util.Vector;
import java.util.StringTokenizer;
import net.sourceforge.jxml2sql.database.*;

/**
 * A class for generating SQL output of a Database object
 *
 * @author Adam VanderHook
 * @version $Id: SQLOutput.java,v 1.3 2001/05/24 00:16:40 acidos Exp $
 * @since 0.1.0-pre1
 */
public class SQLOutput extends Output {

	/**
	 * Create an SQLOutput object with a <code>NULL</code> database.
	 *
	 * @since 0.1.0-pre1
	 */
	public SQLOutput() {
		super();
	}

	/**
	 * Create an SQLOutput object with the specified database.
	 *
	 * @param	db	The database to output
	 * @since 0.1.0-pre1
	 */
	public SQLOutput(Database db) {
		super(db);
	}

	/**
	 * Generates a String object (down-casted to an Object upon return) that is
	 * a complete SQL script (comments included).
	 * 
	 * @return	Object	The SQL output of the database (String object 
	 *          down-casted to Object upon return)
	 * @since 0.1.0-pre1
	 */
	public Object getOutput() {
		String result = new String();
		DatabaseTable tempTB = null;

		result += "### Generated on " + getGenerationTime() + "\n";
		result += "### by JXML2SQL (http://jxml2sql.sourceforge.net/)\n###\n";
		result += "### Database: " + db.getName() + "\n";

		StringTokenizer temp = new StringTokenizer(db.getDescription(), "\n");

		while(temp.hasMoreTokens())
			result += "###\t" + temp.nextToken().trim() + "\n";

		result += "###\n### Tables: \n";
		
		Vector tables = db.getTables();

		for(int i = 0; i < tables.size(); i++) {
			tempTB = (DatabaseTable)tables.elementAt(i);

			result += "###\t" + tempTB.getName() + "\t\t-";
			
//			StringTokenizer tempST = new StringTokenizer(tempTB.getDescription(), "\n");
//			while(tempST.hasMoreTokens()) { result += tempST.nextToken().trim(); }
			result += getFormattedDescription(tempTB.getDescription()) + "\n";
//			result += "\n";
//			result += tempTB.getDescription() + "\n";
		}

		for(int i = 0; i < tables.size(); i++) {
			DatabaseField tempField = null;
			tempTB = (DatabaseTable)tables.elementAt(i);

			result += "\n# Table: " + tempTB.getName();
			result += "\n#\n# Columns:\n";

			Vector fields = tempTB.getFields();

			for(int x = 0; x < fields.size(); x++) {
				tempField = (DatabaseField)fields.elementAt(x);

				result += "#\t" + tempField.getName() + "\t\t-";
//				StringTokenizer tempST = new StringTokenizer(tempField.getDescription(), "\n");
//				while(tempST.hasMoreTokens()) { result += tempST.nextToken().trim(); }
				result += getFormattedDescription(tempField.getDescription()) + "\n";
//				result += "\n";
//				result += tempField.getDescription() + "\n";
			}

			result += "\nCREATE TABLE " + tempTB.getName() + " (\n";

			for(int x = 0; x < fields.size(); x++) {
				tempField = (DatabaseField)fields.elementAt(x);

				if(x != 0)
					result += ",\n";

				result += "\t" + tempField.getName() + " ";
				result += tempField.getType().toUpperCase();

				if(tempField.getLength() != null)
					result += "(" + tempField.getLength() + ")";

				Vector ops = tempField.getOptions();

				for(int y = 0; y < ops.size(); y++)
					result += " " + (String)ops.elementAt(y);
			}

			result += "\n);\n";
		}

		return (Object)result;
	}

}
