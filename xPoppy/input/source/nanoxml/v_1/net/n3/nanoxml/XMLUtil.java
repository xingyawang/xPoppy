/* XMLUtil.java                                                    NanoXML/Java
 *
 * $Revision: 1.2 $
 * $Date: 2001/05/06 16:18:37 $
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
import java.io.Reader;
import java.io.CharArrayReader;


/**
 * Utility methods for NanoXML.
 *
 * @author Marc De Scheemaecker
 * @version $Name:  $, $Revision: 1.2 $
 */
class XMLUtil
{
    
    /**
     * Skips the remainder of a comment.
     * It is assumed that &lt;!- is already read.
     *
     * @param reader         the reader
     * @param entityResolver the entity resolver
     *
     * @throws java.io.IOException
     *		if an error occurred reading the data
     */
    static void skipComment(IXMLReader        reader,
                            XMLEntityResolver entityResolver)
        throws IOException
    {
        if (reader.read() != '-') {
            XMLUtil.skipTag(reader, '\0', entityResolver);
            return;
        }
        
        int dashesRead = 0;
        
        for (;;) {
            char ch = reader.read();
            
            switch (ch) {
                case '-':
                    dashesRead++;
                    break;
                    
                case '>':
                    if (dashesRead == 2) {
                        return;
                    }
                    
                default:
                    dashesRead = 0;
            }
        }
    }
    
    
    /**
     * Skips the remainder of the current XML tag.
     *
     * @param reader         the reader
     * @param escapeChar     the escape character (&amp; or %)
     * @param entityResolver the entity resolver
     *
     * @throws java.io.IOException
     *		if an error occurred reading the data
     */
    static void skipTag(IXMLReader        reader,
                        char              escapeChar,
                        XMLEntityResolver entityResolver)
        throws IOException
    {
        int level = 1;
        
        while (level > 0) {
            char ch = XMLUtil.read(reader, null, escapeChar, entityResolver);
            
            switch (ch) {
                case '<':
                    ++level;
                    break;
                    
                case '>':
                    --level;
                    break;
            }
        }
    }
    
    
    /**
     * Scans a public ID.
     *
     * @param publicID       will contain the public ID
     * @param reader         the reader
     * @param escapeChar     the escape character (&amp; or %)
     * @param entityResolver the entity resolver
     *
     * @return the system ID
     *
     * @throws java.io.IOException
     *		if an error occurred reading the data
     */
    static String scanPublicID(StringBuffer      publicID,
                               IXMLReader        reader,
                               char              escapeChar,
                               XMLEntityResolver entityResolver)
        throws IOException
    {
        if (! XMLUtil.checkLiteral(reader, escapeChar, entityResolver,
                                   "UBLIC")) {
            return null;
        }
        
        XMLUtil.skipWhitespace(reader, escapeChar, null, null);
        publicID.append(XMLUtil.scanString(reader, escapeChar, false,
                                           entityResolver));
        XMLUtil.skipWhitespace(reader, escapeChar, null, null);
        return XMLUtil.scanString(reader, escapeChar, false, entityResolver);
    }
    

    /**
     * Scans a system ID.
     *
     * @param reader         the reader
     * @param escapeChar     the escape character (&amp; or %)
     * @param entityResolver the entity resolver
     *
     * @return the system ID
     *
     * @throws java.io.IOException
     *		if an error occurred reading the data
     */
    static String scanSystemID(IXMLReader        reader,
                               char              escapeChar,
                               XMLEntityResolver entityResolver)
        throws IOException
    {
        if (! XMLUtil.checkLiteral(reader, escapeChar, entityResolver,
                                   "YSTEM")) {
            return null;
        }
        
        XMLUtil.skipWhitespace(reader, escapeChar, null, null);
        return XMLUtil.scanString(reader, escapeChar, false, entityResolver);
    }
    
    
    /**
     * Retrieves an identifier from the data.
     *
     * @param reader         the reader
     * @param escapeChar     the escape character (&amp; or %)
     * @param entityResolver the entity resolver
     *
     * @throws java.io.IOException
     *		if an error occurred reading the data
     */
    static String scanIdentifier(IXMLReader        reader,
                                 char              escapeChar,
                                 XMLEntityResolver entityResolver)
        throws IOException
    {
        StringBuffer result = new StringBuffer();
        
        for (;;) {
            char ch = XMLUtil.read(reader, null, escapeChar, entityResolver);
            
            if ((ch == '_') || (ch == ':') || (ch == '-') || (ch == '.')
                    || ((ch >= 'a') && (ch <= 'z'))
                    || ((ch >= 'A') && (ch <= 'Z'))
                    || ((ch >= '0') && (ch <= '9')) || (ch > '\u007E')) {
                result.append(ch);
            } else {
                reader.unread(ch);
                break;
            }
        }
        
        return result.toString();
    }
    
    
    /**
     * Retrieves a delimited string from the data.
     *
     * @param reader              the reader
     * @param escapeChar          the escape character (&amp; or %)
     * @param normalizeWhitespace if all whitespace chars need to be converted
     *                            to spaces
     * @param entityResolver      the entity resolver
     *
     * @throws java.io.IOException
     *		if an error occurred reading the data
     */
    static String scanString(IXMLReader        reader,
                             char              escapeChar,
                             boolean           normalizeWhitespace,
                             XMLEntityResolver entityResolver)
        throws IOException
    {
        StringBuffer result = new StringBuffer();
        boolean isEntity[] = new boolean[1];
        char delim = XMLUtil.read(reader, null, escapeChar, entityResolver);
        
        if ((delim != '\'') && (delim != '"')) {
            XMLUtil.errorExpectedInput(reader, "delimited string");
        }
        
        for (;;) {
            char ch = XMLUtil.read(reader, isEntity, escapeChar, entityResolver);
            
            if ((! isEntity[0]) && (ch == escapeChar)) {
                reader.startNewStream(XMLUtil.scanEntity(isEntity, reader,
                                                         escapeChar,
                                                         entityResolver));
                ch = reader.read();
            }
                    
            if ((! isEntity[0]) && (ch == delim)) {
                break;
            } else if (normalizeWhitespace && (ch < ' ')) {
                result.append(' ');
            } else {
                result.append(ch);
            }
        }
        
        return result.toString();
    }


    /**
     * Processes an entity.
     *
     * @param isCharLiteral  will contain true if the entity is a char literal
     * @param reader         the reader
     * @param escapeChar     the escape character (&amp; or %)
     * @param entityResolver the entity resolver
     *
     * @return a reader from which the entity value can be retrieved
     *
     * @throws java.io.IOException
     *		if an error occurred reading the data
     */
    static Reader scanEntity(boolean[]         isCharLiteral,
                             IXMLReader        reader,
                             char              escapeChar,
                             XMLEntityResolver entityResolver)
        throws IOException
    {
        char ch = reader.read();
        StringBuffer keyBuf = new StringBuffer();
        
        while (ch != ';') {
            keyBuf.append(ch);
            ch = reader.read();
        }
        
        String key = keyBuf.toString();
        
        if (key.charAt(0) == '#') {
            if (isCharLiteral != null) {
                isCharLiteral[0] = true;
            }
            
            char[] chArr = new char[1];
            
            if (key.charAt(1) == 'x') {
                chArr[0] = (char) Integer.parseInt(key.substring(2), 16);
            } else {
                chArr[0] = (char) Integer.parseInt(key.substring(1), 10);
            }
            
            return new CharArrayReader(chArr);
        } else {
            Reader entityReader = entityResolver.getEntity(reader, key);
            
            if (entityReader == null) {
                XMLUtil.errorInvalidEntity(reader, key);
            }
            
            return entityReader;
        }
    }
    

    /**
     * Skips whitespace from the reader.
     *
     * @param reader         the reader
     * @param escapeChar     the escape character (&amp; or %)
     * @param buffer         where to put the whitespace; null if the
     *                       whitespace does not have to be stored.
     * @param isEntity       if not null, will contain true if the data 
     *                       following the whitespace is an entity
     *
     * @throws java.io.IOException
     *		if an error occurred reading the data
     */
    static void skipWhitespace(IXMLReader   reader,
                               char         escapeChar,
                               StringBuffer buffer,
                               boolean[]    isEntity)
        throws IOException
    {
        char ch;
        
        if (buffer == null) {
            do {
                ch = reader.read();
            } while ((ch == ' ') || (ch == '\t') || (ch == '\n')
                     || (ch == '\r'));
        } else {
            for (;;) {
                ch = reader.read();
            
                if ((ch != ' ') && (ch != '\t') && (ch != '\n')
                        && (ch != '\r')) {
                    break;
                }
                
                buffer.append(ch);
            }
        }
        
        reader.unread(ch);
        
        if (isEntity != null) {
            isEntity[0] = (ch == escapeChar);
        }
    }
    

    /**
     * Reads a character from the reader.
     *
     * @param reader         the reader
     * @param isEntityValue  if the character is the first character in an
     *                       entity
     * @param escapeChar     the escape character (&amp; or %)
     * @param entityResolver the entity resolver
     *
     * @throws java.io.IOException
     *		if an error occurred reading the data
     */
    static char read(IXMLReader        reader,
                     boolean[]         isEntityValue,
                     char              escapeChar,
                     XMLEntityResolver entityResolver)
        throws IOException
    {
        char ch = reader.read();
        
        if (isEntityValue != null) {
            isEntityValue[0] = false;
        }
        
        if (ch == escapeChar) {
            boolean[] charLiteral = new boolean[1];
            reader.startNewStream(XMLUtil.scanEntity(charLiteral, reader,
                                                     escapeChar,
                                                     entityResolver));
            
            if (charLiteral[0]) {
                ch = reader.read();
                
                if (isEntityValue != null) {
                    isEntityValue[0] = true;
                }
            } else {
                ch = XMLUtil.read(reader, null, escapeChar, entityResolver);
            }
        }
                    
        return ch;
    }
    
    
    /**
     * Returns true if the data starts with <I>literal</I>.
     * Enough chars are read to determine this result.
     *
     * @param reader         the reader
     * @param escapeChar     the escape character (&amp; or %)
     * @param entityResolver the entity resolver
     * @param literal        the literal to check
     *
     * @throws java.io.IOException
     *		if an error occurred reading the data
     */
    static boolean checkLiteral(IXMLReader reader,
                                char escapeChar,
                                XMLEntityResolver entityResolver,
                                String literal)
        throws IOException
    {
        for (int i = 0; i < literal.length(); i++) {
            char ch = XMLUtil.read(reader, null, escapeChar, entityResolver);

            if (ch != literal.charAt(i)) {
                return false;
            }
        }
        
        return true;
    }
    
    
    /**
     * Throws an XMLParseException to indicate that an expected string is not
     * encountered.
     *
     * @param reader         the reader
     * @param expectedString the string that is expected
     */
    static void errorExpectedInput(IXMLReader reader,
                                   String     expectedString)
    {
        throw new XMLParseException(reader.getLineNr(),
                                    "Expected: " + expectedString);
    }

    
    /**
     * Throws an XMLParseException to indicate that an entity could not be
     * resolved.
     *
     * @param reader the reader
     * @param key    the name of the entity
     */
    static void errorInvalidEntity(IXMLReader reader,
                                   String     key)
    {
        throw new XMLParseException(reader.getLineNr(),
                                    "Invalid entity: `&" + key + ";'");
    }
    
    
    /**
     * Throws an XMLParseException to indicate that a string is not expected
     * at this point.
     *
     * @param reader           the reader
     * @param unexpectedString the string that is unexpected
     */
    static void errorInvalidInput(IXMLReader reader,
                                  String     unexpectedString)
    {
        throw new XMLParseException(reader.getLineNr(),
                                    "Invalid input: " + unexpectedString);
    }
    
    
    /**
     * Throws an XMLParseException to indicate that the closing tag of an
     * element does not match the opening tag.
     *
     * @param reader       the reader
     * @param expectedName the name of the opening tag
     * @param wrongName    the name of the closing tag
     */
    static void errorWrongClosingTag(IXMLReader reader,
                                     String     expectedName,
                                     String     wrongName)
    {
        throw new XMLParseException(reader.getLineNr(),
                                    "Closing tag does not match opening tag: `"
                                    + wrongName + "' != `" + expectedName
                                    + "'");
    }


    /**
     * Throws an XMLParseException to indicate that extra data is encountered
     * in a closing tag.
     *
     * @param reader       the reader
     */
    static void errorClosingTagNotEmpty(IXMLReader reader)
    {
        throw new XMLParseException(reader.getLineNr(),
                                    "Closing tag must be empty");
    }

}
