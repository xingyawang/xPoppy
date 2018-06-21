package net.sourceforge.jxml2sql.output;

/**
 * Programming interface for creating new output types
 *
 * @author Adam VanderHook
 * @version $Id: OutputInterface.java,v 1.2 2001/01/08 15:20:27 acidos Exp $
 * @since 0.1.0-pre1
 * @deprecated See (#link net.sourceforge.jxml2sql.Output)
 * @deprecated Overtaken by <b>public abstact class Output</b> in v0.1.0-pre2.
 */
public interface OutputInterface {

	/**
	 * Generates and returns the output represented by the class implementing
	 * OutputInterface.  Since output can be a vairiety of things, such as a
	 * String or an Image, outputs are "down-cast" into the Object object before
	 * they are returned.  For exmaple, lets say we want our output to be one
	 * line: the name of the database.  We could construct the following code
	 * for this method:
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
	 * @since 0.1.0-pre1
	 */
	public Object getOutput();

}
