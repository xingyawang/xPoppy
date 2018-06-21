/* IXMLValidator.java                                              NanoXML/Java
 *
 * $Revision: 1.9 $
 * $Date: 2001/05/27 22:58:29 $
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


import java.io.Reader;
import java.io.IOException;
import java.util.Properties;


/**
 * IXMLValidator processes the DTD and handles entity references.
 *
 * @author Marc De Scheemaecker
 * @version $Name:  $, $Revision: 1.9 $
 */
public interface IXMLValidator
{

    /**
     * Parses the DTD. The validator object is responsible for reading the
     * full DTD.
     *
     * @param publicID the public ID, which may be null.
     * @param reader the reader to read the DTD from.
     * @param entityResolver the entity resolver.
     * @param external true if the DTD is external.
     */
    public void parseDTD(String            publicID,
                         IXMLReader        reader,
                         XMLEntityResolver entityResolver,
                         boolean           external)
        throws IOException;

    
    /**
     * Indicates that an element has been started.
     *
     * @param name the name of the element.
     * @param nsPrefix the prefix used to identify the namespace
     * @param nsSystemId the system ID associated with the namespace
     * @param lineNr the line number in the XML data of the element.
     */
    public void elementStarted(String name,
                               String nsPrefix,
                               String nsSystemId,
                               int    lineNr);
    
    
    /**
     * Indicates that the current element has ended.
     *
     * @param name the name of the element.
     * @param nsPrefix the prefix used to identify the namespace
     * @param nsSystemId the system ID associated with the namespace
     */
    public void elementEnded(String name,
                             String nsPrefix,
                             String nsSystemId);
    
    
    /**
     * This method is called when the attributes of an XML element have been
     * processed.
     * If there are attributes with a default value which have not been
     * specified yet, they have to be put into <I>extraAttributes</I>.
     *
     * @param name the name of the element.
     * @param nsPrefix the prefix used to identify the namespace
     * @param nsSystemId the system ID associated with the namespace
     * @param extraAttributes where to put extra attributes.
     */
    public void elementAttributesProcessed(String     name,
                                           String nsPrefix,
                                           String nsSystemId,
                                           Properties extraAttributes);
    
    
    /**
     * Indicates that an attribute has been added to the current element.
     *
     * @param key the name of the attribute.
     * @param nsPrefix the prefix used to identify the namespace
     * @param nsSystemId the system ID associated with the namespace
     * @param value the value of the attribute.
     */
    public void attributeAdded(String key,
                               String nsPrefix,
                               String nsSystemId,
                               String value);
    
    
    /**
     * Indicates that a new #PCDATA element has been encountered.
     *
     * @param lineNr the line number in the XML data of the element.
     */
    public void PCDataAdded(int lineNr);

}
