/* StdXMLParser.java                                               NanoXML/Java
 *
 * $Revision: 1.11 $
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


import java.io.IOException;
import java.io.CharArrayReader;
import java.io.Reader;
import java.util.Enumeration;
import java.util.Properties;


/**
 * StdXMLParser is the core parser of NanoXML.
 *
 * @author Marc De Scheemaecker
 * @version $Name:  $, $Revision: 1.11 $
 */
public class StdXMLParser
    implements IXMLParser
{

    /**
     * The builder which creates the logical structure of the XML data.
     */
    protected IXMLBuilder builder;
    
    
    /**
     * The reader from which the parser retrieves its data.
     */
    protected IXMLReader reader;
    
    
    /**
     * The entity resolver.
     */
    protected XMLEntityResolver entityResolver;
    
    
    /**
     * The validator that will process entity references and validate the XML
     * data.
     */
    protected IXMLValidator validator;
    
    
    /**
     * Creates a new parser.
     */
    public StdXMLParser()
    {
        this.builder = null;
        this.validator = null;
        this.reader = null;
        this.entityResolver = new XMLEntityResolver();
    }
    
    
    /**
     * Sets the builder which creates the logical structure of the XML data.
     *
     * @param builder the non-null builder
     */
    public void setBuilder(IXMLBuilder builder)
    {
        this.builder = builder;
    }
    
    
    /**
     * Sets the validator that will process entity references and validate the
     * XML data.
     *
     * @param validator the non-null validator
     */
    public void setValidator(IXMLValidator validator)
    {
        this.validator = validator;
    }
    
    
    /**
     * Sets the reader from which the parser retrieves its data.
     *
     * @param reader the reader
     */
    public void setReader(IXMLReader reader)
    {
        this.reader = reader;
    }
    
    
    /**
     * Parses the data and lets the builder create the logical data structure.
     *
     * @return the logical structure built by the builder
     *
     * @throws java.io.IOException
     *		if an error occurred reading the data
     */
    public Object parse()
        throws IOException
    {
        try {
            this.builder.startBuilding(this.reader.getLineNr());
            this.scanData();
            return this.builder.getResult();
        } catch (RuntimeException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new XMLParseException(e);
        }
    }
    
    
    /**
     * Scans the XML data for elements.
     *
     * @throws java.io.IOException
     *		if an error occurred reading the data
     */
    protected void scanData()
        throws Exception
    {
        while ((! this.reader.atEOF()) && (this.builder.getResult() == null)) {
            char ch = XMLUtil.read(this.reader, null, '&', this.entityResolver);
            
            switch (ch) {
                case '<':
                    this.scanSomeTag(false /*don't allow CDATA*/);
                    break;
                    
                case ' ':
                case '\t':
                case '\r':
                case '\n':
                    // skip whitespace
                    break;
                    
                default:
                    XMLUtil.errorInvalidInput(this.reader, "`" + ch + "' (0x"
                                              + Integer.toHexString((int) ch)
                                              + ')');
            }
        }
    }
    
    
    /**
     * Scans an XML tag.
     *
     * @param allowCDATA true if CDATA sections are allowed at this point
     *
     * @throws java.io.IOException
     *		if an error occurred reading the data
     */
    protected void scanSomeTag(boolean allowCDATA)
        throws Exception
    {
        char ch = XMLUtil.read(this.reader, null, '&', this.entityResolver);
        
        switch (ch) {
            case '?':
                this.processPI();
                break;
                
            case '!':
                this.processSpecialTag(allowCDATA);
                break;
                
            default:
                this.reader.unread(ch);
                this.processElement();
        }
    }
   
    
    /**
     * Processes a "processing instruction".
     *
     * @throws java.io.IOException
     *		if an error occurred reading the data
     */
    protected void processPI()
        throws Exception
    {
        XMLUtil.skipWhitespace(this.reader, '&', null, null);
        String target = XMLUtil.scanIdentifier(this.reader, '&',
                                               this.entityResolver);
        XMLUtil.skipWhitespace(this.reader, '&', null, null);
        Reader reader = new ContentReader(this.reader, this.entityResolver,
                                          '&', ">?", true, "");
        
        if (! target.equalsIgnoreCase("xml")) {
            this.builder.newProcessingInstruction(target, reader);
        }
        
        reader.close();
    }
    
    
    /**
     * Processes a tag that starts with a bang (<!...>).
     *
     * @param allowCDATA true if CDATA sections are allowed at this point
     *
     * @throws java.io.IOException
     *		if an error occurred reading the data
     */
    protected void processSpecialTag(boolean allowCDATA)
        throws Exception
    {
        char ch = XMLUtil.read(this.reader, null, '&', this.entityResolver);
        
        switch (ch) {
            case '[':
                if (allowCDATA) {
                    this.processCDATA();
                } else {
                    XMLUtil.skipTag(this.reader, '&', this.entityResolver);
                }
                
                return;
                
            case 'D':
                this.processDocType();
                return;
                
            case '-':
                XMLUtil.skipComment(this.reader, this.entityResolver);
                return;
        }        
    }
    
    
    /**
     * Processes a CDATA section.
     *
     * @throws java.io.IOException
     *		if an error occurred reading the data
     */
    protected void processCDATA()
        throws Exception
    {
        if (! XMLUtil.checkLiteral(this.reader, '&', this.entityResolver,
                                   "CDATA[")) {
            XMLUtil.skipTag(this.reader, '&', this.entityResolver);
            return;
        }
        
        this.validator.PCDataAdded(this.reader.getLineNr());
        Reader reader = new ContentReader(this.reader, this.entityResolver,
                                          '&', ">]]", true, "");
        this.builder.addPCData(reader, this.reader.getLineNr());
        reader.close();
    }
        

    /**
     * Processes a document type declaration.
     *
     * @throws java.io.IOException
     *		if an error occurred reading the data
     */
    protected void processDocType()
        throws IOException
    {
        if (! XMLUtil.checkLiteral(this.reader, '&', this.entityResolver,
                                   "OCTYPE")) {
#ifdef F_SP_HD_1
//          XMLUtil.skipTag(this.reader, '&', this.entityResolver);
#else
            XMLUtil.skipTag(this.reader, '&', this.entityResolver);
#endif
            return;
        }
        
        XMLUtil.skipWhitespace(this.reader, '&', null, null);
        String systemID = null;
        StringBuffer publicID = new StringBuffer();
        String rootElement = XMLUtil.scanIdentifier(this.reader, '&',
                                                    this.entityResolver);
        XMLUtil.skipWhitespace(this.reader, '&', null, null);
        char ch = XMLUtil.read(this.reader, null, '&', this.entityResolver);
        
        if (ch == 'P') {
            systemID = XMLUtil.scanPublicID(publicID, reader, '&',
                                            this.entityResolver);
            XMLUtil.skipWhitespace(this.reader, '&', null, null);
            ch = XMLUtil.read(this.reader, null, '&', this.entityResolver);
        } else if (ch == 'S') {
            systemID = XMLUtil.scanSystemID(reader, '&', this.entityResolver);
            XMLUtil.skipWhitespace(this.reader, '&', null, null);
            ch = XMLUtil.read(this.reader, null, '&', this.entityResolver);
        }
        
        if (ch == '[') {
            this.validator.parseDTD(publicID.toString(),
                                    this.reader,
                                    this.entityResolver,
                                    false);
            XMLUtil.skipWhitespace(this.reader, '&', null, null);
            ch = XMLUtil.read(this.reader, null, '&', this.entityResolver);
        }
        
        if (ch != '>') {
            XMLUtil.errorExpectedInput(this.reader, "`>'");
        }
        
        if (systemID != null) {
            Reader reader
                    = this.reader.openStream(publicID.toString(), systemID);
            this.reader.startNewStream(reader);
            this.reader.setSystemID(systemID);
            this.reader.setPublicID(publicID.toString());
            this.validator.parseDTD(publicID.toString(),
                                    this.reader,
                                    this.entityResolver,
                                    true);
        }
    }
    

    /**
     * Processes a regular element.
     *
     * @throws java.io.IOException
     *		if an error occurred reading the data
     */
    protected void processElement()
        throws Exception
    {
        String name = XMLUtil.scanIdentifier(this.reader, '&',
                                             this.entityResolver);
        XMLUtil.skipWhitespace(this.reader, '&', null, null);
        String prefix = null;
        int colonIndex = name.indexOf(':');
        
        if (colonIndex > 0) {
            prefix = name.substring(0, colonIndex);
#ifdef F_SP_HD_2
            name = name.substring(colonIndex);
#else
            name = name.substring(colonIndex + 1);
#endif
        }
        
        this.validator.elementStarted(name, prefix, null,
                                      this.reader.getLineNr());
        this.builder.startElement(name, prefix, null, this.reader.getLineNr());
        char ch;
        
        for (;;) {
            ch = XMLUtil.read(this.reader, null, '&', this.entityResolver);
            
            if ((ch == '/') || (ch == '>')) {
                break;
            }
            
            this.reader.unread(ch);
            this.processAttribute();
            XMLUtil.skipWhitespace(this.reader, '&', null, null);
        }
        
        Properties extraAttributes = new Properties();
        this.validator.elementAttributesProcessed(name, prefix, null,
                                                  extraAttributes);
        Enumeration _enum = extraAttributes.keys();
        
        while (_enum.hasMoreElements()) {
            String key = (String) _enum.nextElement();
            String value = extraAttributes.getProperty(key);
            String attPrefix = null;
            colonIndex = key.indexOf(':');
            
            if (colonIndex > 0) {
                attPrefix = key.substring(0, colonIndex);
                key = key.substring(colonIndex + 1);
            }
            
            this.builder.addAttribute(key, attPrefix, null, value, "CDATA");
        }
        
        this.builder.elementAttributesProcessed(name, prefix, null);
        
        if (ch == '/') {
            if (XMLUtil.read(this.reader, null, '&',
                             this.entityResolver) != '>') {
                XMLUtil.errorExpectedInput(this.reader, "`>'");
            }
            
            this.validator.elementEnded(name, prefix, null);
            this.builder.endElement(name, prefix, null);
            return;
        }
        
        StringBuffer whitespaceBuffer = new StringBuffer(16);
        
        for (;;) {
            whitespaceBuffer.setLength(0);
            boolean fromEntity[] = new boolean[1];
            XMLUtil.skipWhitespace(this.reader, '&', whitespaceBuffer,
                                   fromEntity);
            ch = XMLUtil.read(this.reader, null, '&', this.entityResolver);
            
            if ((ch == '<') && (! fromEntity[0])) {
                ch = reader.read();
                
                if (ch == '/') {
                    XMLUtil.skipWhitespace(this.reader, '&', null, null);
                    String str = XMLUtil.scanIdentifier(this.reader, '&',
                                                        this.entityResolver);
                    
                    if (! str.equals(name)) {
                        XMLUtil.errorWrongClosingTag(this.reader, name, str);
                    }
                    
                    XMLUtil.skipWhitespace(this.reader, '&', null, null);
                    
                    if (XMLUtil.read(this.reader, null, '&',
                                     this.entityResolver) != '>') {
                        XMLUtil.errorClosingTagNotEmpty(this.reader);
                    }
                    
                    this.validator.elementEnded(name, prefix, null);
                    this.builder.endElement(name, prefix, null);
                    break;
                } else {
                    this.reader.unread(ch);
                    this.scanSomeTag(true /*CDATA allowed*/);
                }
            } else {
                this.validator.PCDataAdded(this.reader.getLineNr());
                this.reader.unread(ch);
                Reader reader = new ContentReader(this.reader,
                                                  this.entityResolver,
                                                  '&', "<",
#ifdef F_SP_HD_3
                                                  true,
#else
                                                  false,
#endif
                                                  whitespaceBuffer.toString());
                this.builder.addPCData(reader, this.reader.getLineNr());
                reader.close();
                this.reader.unread('<');
            }
        }
    }
    

    /**
     * Processes an attribute of an element.
     *
     * @throws java.io.IOException
     *		if an error occurred reading the data
     */
    protected void processAttribute()
        throws Exception
    {
        String key = XMLUtil.scanIdentifier(this.reader, '&', this.entityResolver);
        XMLUtil.skipWhitespace(this.reader, '&', null, null);
        
        if (XMLUtil.read(this.reader, null, '&', this.entityResolver) != '=') {
            XMLUtil.errorExpectedInput(this.reader, "`='");
        }
        
        String value = XMLUtil.scanString(this.reader, '&', true, this.entityResolver);
        this.validator.attributeAdded(key, null, null, value);
        this.builder.addAttribute(key, null, null, value, "CDATA");
    }
    
}
