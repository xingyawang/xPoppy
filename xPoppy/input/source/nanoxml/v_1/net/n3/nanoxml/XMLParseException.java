/* XMLParseException.java                                          NanoXML/Java
 *
 * $Revision: 1.3 $
 * $Date: 2001/05/27 17:13:14 $
 * $Name:  $
 *
 * This file is part of NanoXML 2 for Java.
 * Copyright (C) 2001 Marc De Scheemaecker, All Rights Reserved.
 *
 * This software is provided 'as-is', without any express or implied warranty.
 * In no event will the authors be held liable for any damages arising from the
 * use of this software.
 *
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 *
 *  1. The origin of this software must not be misrepresented; you must not
 *     claim that you wrote the original software. If you use this software in
 *     a product, an acknowledgment in the product documentation would be
 *     appreciated but is not required.
 *
 *  2. Altered source versions must be plainly marked as such, and must not be
 *     misrepresented as being the original software.
 *
 *  3. This notice may not be removed or altered from any source distribution.
 */

package net.n3.nanoxml;


/**
 * An XMLParseException is thrown when the XML passed to the XML parser is not
 * well-formed.
 *
 * @author Marc De Scheemaecker
 * @version $Name:  $, $Revision: 1.3 $
 */
public class XMLParseException
    extends RuntimeException
{

    /**
     * The line number in the XML data where the exception occurred.
     */
    private int lineNr;
    
    
    /**
     * Encapsulated exception.
     */
    private Exception encapsulatedException;
    
    
    /**
     * Creates a new exception.
     *
     * @param msg the message of the exception.
     */
    public XMLParseException(String msg)
    {
        super(msg);
        this.lineNr = -1;
        this.encapsulatedException = null;
    }
    
    
    /**
     * Creates a new exception.
     *
     * @param e the encapsulated exception.
     */
    public XMLParseException(Exception e)
    {
        super(e.getMessage());
        this.lineNr = -1;
        this.encapsulatedException = e;
    }
    
    
    /**
     * Creates a new exception.
     *
     * @param lineNr the line number in the XML data where the exception 
     *               occurred.
     * @param msg the message of the exception.
     */
    public XMLParseException(int    lineNr,
                             String msg)
    {
        super("XML Not Well-Formed at Line " + lineNr + ": " + msg);
        this.lineNr = lineNr;
        this.encapsulatedException = null;
    }
    
    
    /**
     * Returns the line number in the XML data where the exception occurred.
     * If there is no line number known, -1 is returned.
     */
    public int getLineNr()
    {
        return this.lineNr;
    }
    
    
    /**
     * Returns the encapsulated exception, or null if no exception is
     * encapsulated.
     */
    public Exception getException()
    {
        return this.encapsulatedException;
    }
    
}
