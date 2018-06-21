package net.sourceforge.jxml2sql.database;

import java.util.*;

/**
 * Represents an SQL database and its tables
 *
 * @author Adam VanderHook
 * @version $Id: Database.java,v 1.2 2001/01/08 15:22:25 acidos Exp $
 * @since 0.1.0-pre1
 */
public class Database extends DatabaseElement {

	protected Vector tables = new Vector();

	/**
	 * Create a Database object with a <code>NULL</code> name and description.
	 *
	 * @since 0.1.0-pre1
	 */
	public Database() {
		super();
	}

	/**
	 * Create a Database object with the specified name and a <code>NULL</code>
	 * description.
	 *
	 * @param	String	The name of the database
	 * @since 0.1.0-pre1
	 */
	public Database(String name) {
		super(name);
	}

	/**
	 * Create a Database object with the specified tables.
	 *
	 * @param	Vector	A collection of tables for the database to own
	 * @since 0.1.0-pre1
	 */
	public Database(Vector tables) {
		super();
		setTables(tables);
	}

	/**
	 * Create a Database object with the specified name and tables, and with
	 * a <code>NULL</code> description.
	 *
	 * @param	String	The name of the database
	 * @param	Vector	A collection of tables for the database to own
	 * @since 0.1.0-pre1
	 */
	public Database(String name, Vector tables) {
		super(name);
		setTables(tables);
	}

	/**
	 * Sets the tables that the database owns
	 *
	 * @param	Vector	A collection of tables for the database to own
	 * @since 0.1.0-pre1
	 */
	public void setTables(Vector tables) {
		this.tables = tables;
	}

	/**
	 * Adds a table to the database
	 *
	 * @param	DatabaseTable	A new table for the database
	 * @since 0.1.0-pre1
	 */
	public void addTable(DatabaseTable table) {
		tables.addElement((Object)table);
	}

	/**
	 * Removes a table from the database
	 *
	 * @param	int		The id of the table
	 * @since 0.1.0-pre1
	 */
	public void removeTable(int id) {
		tables.removeElementAt(id);
	}

	/**
	 * Returns a collection of all the tables in the database
	 *
	 * @return	Vector	All the tables in the database
	 * @since 0.1.0-pre1
	 */
	public Vector getTables() {
		return tables;
	}

}
