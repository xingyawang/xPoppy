package net.sourceforge.jxml2sql.database;

import java.util.*;

/**
 * Represents an SQL table in a database
 *
 * @author Adam VanderHook
 * @version $Id: DatabaseTable.java,v 1.2 2001/01/08 15:22:25 acidos Exp $
 */
public class DatabaseTable extends DatabaseElement {

	/**
	 * Fields belonging to the table
	 */
	protected Vector fields = new Vector();

	/**
	 * Create a <code>DatabaseTable</code> object with no fields and
	 * <code>NULL</code> name and description.
	 */
	public DatabaseTable() {
		super();
	}

	/**
	 * Create a <code>DatabaseTable</code> object with the specified name and
	 * fields, and a <code>NULL</code> description.
	 *
	 * @param	String	Name of the table
	 * @param	Vector	A collection of fields that the table owns
	 */
	public DatabaseTable(String name, Vector fields) {
		super(name);
		setFields(fields);
	}

	/**
	 * Create a <code>DatabaseTable</code> object with the specified name, no
	 * fields and a <code>NULL</code> description.
	 *
	 * @param	String	Name of the table
	 */
	public DatabaseTable(String name) {
		super(name);
	}

	/**
	 * Create a <code>DatabaseTable</code> object with the specified fields, and
	 * <code>NULL</code> name and description.
	 *
	 * @param	Vector	A collection of fields that the table owns
	 */
	public DatabaseTable(Vector fields) {
		super();
		setFields(fields);
	}

	/**
	 * Sets the fields that the table owns
	 *
	 * @param	Vector	A collection of fields that the table owns
	 */
	public void setFields(Vector fields) { this.fields = fields; }

	/**
	 * Adds a field to the table
	 *
	 * @param	DatabaseField	A field owned by the table
	 */
	public void addField(DatabaseField field) {
		fields.addElement((Object)field);
	}

	/**
	 * Removes a field from the table
	 *
	 * @param	int		The id of the field
	 */
	public void removeField(int id) { fields.removeElementAt(id); }

	/**
	 * Retrieves a collection of all the fields in the table
	 *
	 * @returns	Vector	All the fields in the table
	 */
	public Vector getFields() {
		return fields;
	}

}
