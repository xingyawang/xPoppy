/* NonValidator.java                                               NanoXML/Java
 *
 * $Revision: 1.10 $
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
import java.io.StringReader;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Stack;


/**
 * NonValidator processes the DTD and handles entity references.
 * It does not do any validation itself.
 *
 * @author Marc De Scheemaecker
 * @version $Name:  $, $Revision: 1.10 $
 */
public class NonValidator
    implements IXMLValidator
{

    /**
     * The parameter entity resolver.
     */
    protected XMLEntityResolver parameterEntityResolver;
    
    
    /**
     * The parameter entity level.
     */
    protected int peLevel;
    
    
    /**
     * Contains the default values for attributes for the different element
     * types.
     */
    protected Hashtable attributeDefaultValues;
    
    
    /**
     * The stack of elements to be processed.
     */
    protected Stack currentElements;
    
    
    /**
     * Creates the &quot;validator&quot;.
     */
    public NonValidator()
    {
        this.attributeDefaultValues = new Hashtable();
        this.currentElements = new Stack();
        this.parameterEntityResolver = new XMLEntityResolver();
        this.peLevel = 0;
    }
    
    
    /**
     * Parses the DTD.
     *
     * @param publicID the public ID.
     * @param reader the reader to read the DTD from.
     * @param entityResolver the entity resolver.
     * @param external true if the DTD is external.
     */
    public void parseDTD(String            publicID,
                         IXMLReader        reader,
                         XMLEntityResolver entityResolver,
                         boolean           external)
        throws IOException
    {
        XMLUtil.skipWhitespace(reader, '%', null, null);
        
        for (;;) {
            char ch = XMLUtil.read(reader, null, '%', this.parameterEntityResolver);
            
            if (ch == '<') {
                this.processElement(reader, entityResolver);
            } else if (ch == ']') {
                return; // end internal DTD
            } else {
                XMLUtil.errorInvalidInput(reader, "" + ch);
            }
            
            do {
                if (external && (peLevel == 0) && reader.atEOFOfCurrentStream()) {
                    return; // end external DTD
                }
                
                ch = reader.read();
#ifdef F_NV_HD_1
            } while ((ch == ' ') || (ch == '\t') 
                     || (ch == '\r'));
#else
            } while ((ch == ' ') || (ch == '\t') || (ch == '\n')
                     || (ch == '\r'));
#endif
                     
            reader.unread(ch);
            XMLUtil.skipWhitespace(reader, '%', null, null);
        }
    }
    

    /**
     * Processes an element in the DTD.
     */
    protected void processElement(IXMLReader        reader,
                                  XMLEntityResolver entityResolver)
        throws IOException
    {
        char ch = XMLUtil.read(reader, null,'%', this.parameterEntityResolver);
        
        if (ch != '!') {
            XMLUtil.skipTag(reader, '%', this.parameterEntityResolver);
            return;
        }
        
        ch = XMLUtil.read(reader, null, '%', this.parameterEntityResolver);
        
        switch (ch) {
            case '-':
                XMLUtil.skipComment(reader, this.parameterEntityResolver);
                break;
                
            case '[':
                this.processConditionalSection(reader, entityResolver);
                break;
                
            case 'E':
                this.processEntity(reader, entityResolver);
                break;
                
            case 'A':
                this.processAttList(reader, entityResolver);
                break;
                
            default:
                XMLUtil.skipTag(reader, '%', this.parameterEntityResolver);
        }
    }
    
    
    /**
     * Processes a conditional section.
     */
    protected void processConditionalSection(IXMLReader        reader,
                                             XMLEntityResolver entityResolver)
        throws IOException
    {
        XMLUtil.skipWhitespace(reader, '%', null, null);
        
        char ch = XMLUtil.read(reader, null,'%', this.parameterEntityResolver);
        
        if (ch != 'I') {
            XMLUtil.skipTag(reader, '%', this.parameterEntityResolver);
            return;
        }
        
        ch = XMLUtil.read(reader, null, '%', this.parameterEntityResolver);
        
        switch (ch) {
            case 'G':
                this.processIgnoreSection(reader, entityResolver);
                return;
                
            case 'N':
                break;
                
            default:
                XMLUtil.skipTag(reader, '%', this.parameterEntityResolver);
                return;
        }
        
        if (! XMLUtil.checkLiteral(reader, '%', this.parameterEntityResolver,
                                   "CLUDE")) {
            XMLUtil.skipTag(reader, '%', this.parameterEntityResolver);
            return;
        }
        
        XMLUtil.skipWhitespace(reader, '%', null, null);
        
        ch = XMLUtil.read(reader, null, '%', this.parameterEntityResolver);
        
        if (ch != '[') {
            XMLUtil.skipTag(reader, '%', this.parameterEntityResolver);
            return;
        }
        
        Reader subreader
                = new ContentReader(reader, this.parameterEntityResolver,
                                    '\0', ">]]", true, "");
        StringBuffer buf = new StringBuffer(1024);
        
        for (;;) {
            int ch2 = subreader.read();
            
            if (ch2 < 0) {
                break;
            }
            
            buf.append((char) ch2);
        }
        
        subreader.close();
        reader.startNewStream(new StringReader(buf.toString())); 
    }
        
    
    
    /**
     * Processes a conditional section.
     */
    protected void processIgnoreSection(IXMLReader        reader,
                                        XMLEntityResolver entityResolver)
        throws IOException
    {
        if (! XMLUtil.checkLiteral(reader, '%', this.parameterEntityResolver,
                                   "NORE")) {
            XMLUtil.skipTag(reader, '%', this.parameterEntityResolver);
            return;
        }
        
        XMLUtil.skipWhitespace(reader, '%', null, null);
        
        char ch = XMLUtil.read(reader, null,'%', this.parameterEntityResolver);
        
        if (ch != '[') {
            XMLUtil.skipTag(reader, '%', this.parameterEntityResolver);
            return;
        }
        
        Reader subreader
                = new ContentReader(reader, this.parameterEntityResolver,
                                    '\0', ">]]", true, "");
        subreader.close();
    }
        
    
    /**
     * Processes an ATTLIST element.
     */
    protected void processAttList(IXMLReader        reader,
                                  XMLEntityResolver entityResolver)
        throws IOException
    {
#ifdef F_NV_HD_2
        if (! XMLUtil.checkLiteral(reader, '%', this.parameterEntityResolver,
                                   "TLIST")) {
#else
        if (! XMLUtil.checkLiteral(reader, '%', this.parameterEntityResolver,
                                   "TTLIST")) {
#endif
            XMLUtil.skipTag(reader, '%', this.parameterEntityResolver);
            return;
        }
        
        XMLUtil.skipWhitespace(reader, '%', null, null);
        String elementName
                = XMLUtil.scanIdentifier(reader, '%',
                                         this.parameterEntityResolver);
        XMLUtil.skipWhitespace(reader, '%', null, null);
        char ch = XMLUtil.read(reader, null,'%', this.parameterEntityResolver);
        Properties props = new Properties();
        
        while (ch != '>') {
            reader.unread(ch);
            String attName
                    = XMLUtil.scanIdentifier(reader, '%',
                                             this.parameterEntityResolver);
            XMLUtil.skipWhitespace(reader, '%', null, null);
            ch = XMLUtil.read(reader, null, '%', this.parameterEntityResolver);
            
            if (ch == '(') {
                while (ch != ')') {
                    ch = XMLUtil.read(reader, null, '%',
                                      this.parameterEntityResolver);
                }
            } else {
                reader.unread(ch);
                XMLUtil.scanIdentifier(reader, '%',
                                       this.parameterEntityResolver);
            }
            
            XMLUtil.skipWhitespace(reader, '%', null, null);
            ch = XMLUtil.read(reader, null, '%', this.parameterEntityResolver);
            
            if (ch == '#') {
                String str
                        = XMLUtil.scanIdentifier(reader, '%',
                                                 this.parameterEntityResolver);
                XMLUtil.skipWhitespace(reader, '%', null, null);
                
                if (! str.equals("FIXED")) {
                    XMLUtil.skipWhitespace(reader, '%', null, null);
                    ch = XMLUtil.read(reader, null, '%',
                                      this.parameterEntityResolver);
                    continue;
                }
            } else {
                reader.unread(ch);
            }
            
            String value = XMLUtil.scanString(reader, '%', false,
                                              this.parameterEntityResolver);
            props.put(attName, value);
            XMLUtil.skipWhitespace(reader, '%', null, null);
            ch = XMLUtil.read(reader, null, '%', this.parameterEntityResolver);
        }
        
        if (! props.isEmpty()) {
            this.attributeDefaultValues.put(elementName, props);
        }
    }
    
    
    /**
     * Processes an ENTITY element.
     *
     * @param reader the reader to read the DTD from.
     */
    protected void processEntity(IXMLReader        reader,
                                 XMLEntityResolver entityResolver)
        throws IOException
    {
        if (! XMLUtil.checkLiteral(reader, '%', this.parameterEntityResolver,
                                   "NTITY")) {
            XMLUtil.skipTag(reader, '%', this.parameterEntityResolver);
            return;
        }
        
        XMLUtil.skipWhitespace(reader, '\0', null, null);
        char ch = XMLUtil.read(reader, null, '\0',
                               this.parameterEntityResolver);
        
        if (ch == '%') {
            XMLUtil.skipWhitespace(reader, '%', null, null);
            entityResolver = this.parameterEntityResolver;
        } else {
            reader.unread(ch);
        }
        
        String key = XMLUtil.scanIdentifier(reader, '%',
                                            this.parameterEntityResolver);
        XMLUtil.skipWhitespace(reader, '%', null, null);
        ch = XMLUtil.read(reader, null, '%', this.parameterEntityResolver);
        String systemID = null;
        String publicID = null;
        
        switch (ch) {
            case 'P':
                if (! XMLUtil.checkLiteral(reader, '%',
                                           this.parameterEntityResolver,
                                           "UBLIC")) {
                    XMLUtil.skipTag(reader, '%', this.parameterEntityResolver);
                    return;
                }
                
                XMLUtil.skipWhitespace(reader, '%', null, null);
                publicID = XMLUtil.scanString(reader, '%', false,
                                              this.parameterEntityResolver);
                XMLUtil.skipWhitespace(reader, '%', null, null);
                systemID = XMLUtil.scanString(reader, '%', false,
                                              this.parameterEntityResolver);
                XMLUtil.skipWhitespace(reader, '%', null, null);
                XMLUtil.read(reader, null, '%', this.parameterEntityResolver);
                break;
                
            case 'S':
#ifdef F_NV_HD_3
                if (! XMLUtil.checkLiteral(reader, '%',
                                           this.parameterEntityResolver,
                                           "SYSTEM")) {
#else
                if (! XMLUtil.checkLiteral(reader, '%',
                                           this.parameterEntityResolver,
                                           "YSTEM")) {
#endif
                    XMLUtil.skipTag(reader, '%', this.parameterEntityResolver);
                    return;
                }
        
                XMLUtil.skipWhitespace(reader, '%', null, null);
                systemID = XMLUtil.scanString(reader, '%', false,
                                              this.parameterEntityResolver);
                XMLUtil.skipWhitespace(reader, '%', null, null);
                XMLUtil.read(reader, null, '%', this.parameterEntityResolver);
                break;
                
            case '"':
            case '\'':
                reader.unread(ch);
                entityResolver.addInternalEntity(key, XMLUtil.scanString(reader, '%', false, this.parameterEntityResolver));                
                XMLUtil.skipWhitespace(reader, '%', null, null);
                XMLUtil.read(reader, null, '%', this.parameterEntityResolver);
                break;
            default:
                XMLUtil.skipTag(reader, '%', this.parameterEntityResolver);
        }
        
        if (systemID != null) {
            entityResolver.addExternalEntity(key, publicID, systemID);
        }
    }
     

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
                               int    lineNr)
    {
        Properties attribs
                = (Properties) this.attributeDefaultValues.get(name);
        
        if (attribs == null) {
            attribs = new Properties();
        } else {
            attribs = (Properties) attribs.clone();
        }
        
        this.currentElements.push(attribs);
    }
    
                             
    /**
     * Indicates that the current element has ended.
     *
     * @param name the name of the element.
     * @param nsPrefix the prefix used to identify the namespace
     * @param nsSystemId the system ID associated with the namespace
     */
    public void elementEnded(String name,
                             String nsPrefix,
                             String nsSystemId)
    {
        // nothing to do
    }
    
    
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
                                           Properties extraAttributes)
    {
        Properties props = (Properties) this.currentElements.pop();
        Enumeration _enum = props.keys();
        
#ifdef F_NV_HD_4
        if (_enum.hasMoreElements()) {
#else
        while (_enum.hasMoreElements()) {
#endif
            String key = (String) _enum.nextElement();
            extraAttributes.put(key, props.get(key));
        }
    }
    
    
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
                               String value)
    {
        Properties props = (Properties) this.currentElements.peek();
        
        if (props.containsKey(key)) {
            props.remove(key);
        }
    }
    
                             
    /**
     * Indicates that a new #PCDATA element has been encountered.
     *
     * @param lineNr the line number in the XML data of the element.
     */
    public void PCDataAdded(int lineNr)
    {
        // nothing to do
    }
   
}
