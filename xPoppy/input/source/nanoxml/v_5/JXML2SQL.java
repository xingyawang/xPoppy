

import java.io.*;
import java.util.*;
import net.n3.nanoxml.*;
import net.sourceforge.jxml2sql.database.*;
import net.sourceforge.jxml2sql.output.*;

/**
 * The core object for libjxml2sql
 *
 * @author Adam VanderHook
 * @version $Id: JXML2SQL.java,v 1.5 2001/05/24 00:14:47 acidos Exp $
 * @since 0.1.0-pre1
 */
public class JXML2SQL {

	/**
	 * Our representation of the database
	 *
	 * @since 0.1.0-pre1
	 */
	private Database db;

	/**
	 * XML we have to work with
	 *
	 * @since 0.1.0-pre1
	 */
	private XMLElement line;

	/**
	 * Version of this release
	 *
	 * @since 0.1.0-pre1
	 */
	private static final String VERSION = "0.3.0";

	/**
	 * Creates a JXML2SQL object reading data from the specified filename
	 *
	 * @param	String	Filename of XML document
	 * @since 0.1.0-pre1
	 */
	public JXML2SQL(String fn)throws Exception {
			db		= null;
			line	= getXMLData(fn);
			createDatabase(line);
	}

	/**
	 * Retrieves the version information of the current buildof libjxml2sql
	 * being employed
	 *
	 * @return	String	The version of libjxml2sql being used
	 * @since 0.1.0-pre1
	 */
	public static String getVersion() {
		return VERSION;
	}

	/**
	 * Returns an SQL representation of the XML document
	 *
	 * @return	String	An SQL representation of the document
	 * @since 0.1.0-pre1
	 */
	public String generateSQL() {
		SQLOutput output = new SQLOutput(db);

		return (String)output.getOutput();
	}

	/**
	 * Returns an HTML representation of the XML document
	 *
	 * @return	String	An HTML representation of the HTML document
	 * @since 0.1.0-pre1
	 */
	public String generateHTML() {
		HTMLOutput output = new HTMLOutput(db);

		return (String)output.getOutput();
	}

	/**
	 * Creates a Database object out of the XML document.
	 *
	 * @param	xmlData		A parsed XMLElement conforming to sql-standard.dtd
	 * @since 0.1.0-pre1 
	 */
	private void createDatabase(XMLElement xmlData) {
		if(tagsMatch(xmlData, "database")) {
			db = new Database();
			Enumeration subDB = xmlData._enumerateChildren();

			while(subDB.hasMoreElements()) {
				XMLElement tempSubDB = (XMLElement)subDB.nextElement();

				if(tagsMatch(tempSubDB, "name"))
					db.setName(tempSubDB.getContent());
				else if(tagsMatch(tempSubDB, "description"))
					db.setDescription(tempSubDB.getContent());
				else if(tagsMatch(tempSubDB, "table")) {
					DatabaseTable tb = new DatabaseTable();
					Enumeration subTB = tempSubDB._enumerateChildren();

					while(subTB.hasMoreElements()) {
						XMLElement tempSubTB = (XMLElement)subTB.nextElement();

						if(tagsMatch(tempSubTB, "name"))
							tb.setName(tempSubTB.getContent());
						else if(tagsMatch(tempSubTB, "description"))
							tb.setDescription(tempSubTB.getContent());
						else if(tagsMatch(tempSubTB, "field")) {
							DatabaseField field = new DatabaseField();
							Enumeration subField = tempSubTB._enumerateChildren();

							while(subField.hasMoreElements()) {
								XMLElement tempSubField = (XMLElement)subField.nextElement();

								if(tagsMatch(tempSubField, "name"))
									field.setName(tempSubField.getContent());
								else if(tagsMatch(tempSubField, "description"))
									field.setDescription(tempSubField.getContent());
								else if(tagsMatch(tempSubField, "type"))
									field.setType(tempSubField.getContent());
								else if(tagsMatch(tempSubField, "length"))
									field.setLength(tempSubField.getContent());
								else if(tagsMatch(tempSubField, "option"))
									field.addOption(tempSubField.getContent());
							}

							tb.addField(field);
						}
					}

					db.addTable(tb);
				}
			}
		}
	}

	/**
	 * Determine whether or not the two specified tags match
	 *
	 * @param	xmlData		The current tag being examined
	 * @param	tag			The tag we want to test xmlData against
	 * @since 0.1.0-pre1
	 */
	private boolean tagsMatch(XMLElement xmlData, String tag) {
		return xmlData.getName().toLowerCase().equals(tag.toLowerCase());
	}

	/**
	 * Reads in the XML document, parses it, and returns the object
	 *
	 * @param	fn			Filename of the XML document
	 * @return	XMLElement	The parsed XML data
	 * @since 0.1.0-pre1
	 */
	private XMLElement getXMLData(String fn) throws Exception
	 {
		
	      IXMLParser parser = XMLParserFactory.createDefaultXMLParser();
		//IXMLReader reader = StdXMLReader.fileReader(fn);
	      IXMLReader reader =(IXMLReader) new StdXMLReader(new FileReader(fn));
		parser.setReader(reader);
		XMLElement result = (XMLElement) parser.parse();
		return result;
	}

}
