package net.sourceforge.jxml2sql.database;

import java.util.*;

/**
 * Represents an SQL field in a database table
 *
 * @author Adam VanderHook
 * @version $Id: DatabaseField.java,v 1.2 2001/01/08 15:22:25 acidos Exp $
 */
public class DatabaseField extends DatabaseElement {

	/**
	 * Data-type of the databse field
	 */
	protected String type = null;

	/**
	 * Length of the field.  <code>NULL</code> (default) means no length.
	 */
	protected String length = null;

	/**
	 * Options associated with the field
	 */
	protected Vector options = new Vector();

	/**
	 * Create a <code>DatabaseField</code> object with no options, and
	 * <code>NULL</code> name and description.
	 */
	public DatabaseField() {
		super();
	}

	/**
	 * Create a <code>DatabaseField</code> object with the specified options
	 * and name, and a <code>NULL</code> description.
	 *
	 * @param	String	Name of the field
	 * @param	Vector	Options of the field
	 */
	public DatabaseField(String name, Vector options) {
		super(name);
		setOptions(options);
	}

	/**
	 * Create a <code>DatabaseField</code> object with the specified name,
	 * <code>NULL</code> description and no options.
	 *
	 * @param	String	Name of the field
	 */
	public DatabaseField(String name) {
		super(name);
	}

	/**
	 * Create a <code>DatabaseField</code> object with the specified options,
	 * and <code>NULL</code> name and description.
	 *
	 * @param	Vector	Options of the field
	 */
	public DatabaseField(Vector options) {
		super();
		setOptions(options);
	}

	/**
	 * Sets the data-type of the field
	 *
	 * @param	type	The data-type of the field
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Sets the length of the field
	 *
	 * @param	length	The length of the field
	 */
	public void setLength(String length) {
		this.length = length;
	}

	/**
	 * Sets the options assigned to the field
	 *
	 * @param	Vector	A collection of options owned by the field
	 */
	public void setOptions(Vector options) { this.options = options; }

	/**
	 * Adds an option to the field
	 *
	 * @param	String	A field option
	 */
	public void addOption(String option) { options.addElement((Object)option); }

	/**
	 * Removes an option from the field
	 *
	 * @param	int		The id of the option
	 */
	public void removeOption(int id) { options.removeElementAt(id); }

	/**
	 * Retrieve the data-type of the field
	 *
	 * @returns	String	The data-type of the field
	 */
	public String getType() {
		return type;
	}

	/**
	 * Retrieve the length of the field
	 *
	 * @param	length	The length of the field
	 */
	public String getLength() {
		return length;
	}

	/**
	 * Retrieves a collection of all the options in the field
	 *
	 * @returns	Vector	All the options in the field
	 */
	public Vector getOptions() {
		return options;
	}

}
