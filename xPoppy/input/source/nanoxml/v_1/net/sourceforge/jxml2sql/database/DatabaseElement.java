package net.sourceforge.jxml2sql.database;

/**
 * Represents standard components in a generic database element
 *
 * @author Adam VanderHook
 * @version $Id: DatabaseElement.java,v 1.2 2001/01/08 15:22:25 acidos Exp $
 * @since 0.1.0-pre1
 */
public class DatabaseElement {

	/**
	 * Name of the element
	 *
	 * @since 0.1.0-pre1
	 */
	protected String name;

	/**
	 * Description of the element
	 *
	 * @since 0.1.0-pre1
	 */
	protected String desc;

	/**
	 * Create a DatabaseElement object with a <code>NULL</code> name and
	 * description.
	 *
	 * @since 0.1.0-pre1
	 */
	public DatabaseElement() {
		setName(null);
		setDescription(null);
	}

	/**
	 * Create a DatabaseElement object with the specified name and a
	 * <code>NULL</code> description.
	 *
	 * @param	name	The name of the element
	 * @since 0.1.0-pre1
	 */
	public DatabaseElement(String name) {
		setName(name);
		setDescription(null);
	}

	/**
	 * Create a DatabaseElement object with the specified name and description.
	 *
	 * @param	name	The name of the element
	 * @param	desc	The description of the element
	 * @since 0.1.0-pre1
	 */
	public DatabaseElement(String name, String desc) {
		setName(name);
		setDescription(desc);
	}

	/**
	 * Sets the name of the element
	 *
	 * @param	name	The name of the element
	 * @since 0.1.0-pre1
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Sets the description of the element
	 *
	 * @param	desc	The description of the element
	 * @since 0.1.0-pre1
	 */
	public void setDescription(String desc) {
		this.desc = desc;
	}

	/**
	 * Retrieves the name of the element
	 *
	 * @return	String	The name of the element
	 * @since 0.1.0-pre1
	 */
	public String getName() {
		return name;
	}

	/**
	 * Retrieves the description of the element
	 *
	 * @return	String	The description of the element
	 * @since 0.1.0-pre1
	 */
	public String getDescription() {
		return desc;
	}

}
