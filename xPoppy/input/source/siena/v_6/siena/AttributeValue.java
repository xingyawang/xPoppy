//
//  This file is part of Siena, a wide-area event notification system.
//  See http://www.cs.colorado.edu/serl/dot/siena.html
//
//  Author: Antonio Carzaniga <carzanig@cs.colorado.edu>
//  See the file AUTHORS for full details. 
//
//  Copyright (C) 1998-1999 University of Colorado
//
//  This program is free software; you can redistribute it and/or
//  modify it under the terms of the GNU General Public License
//  as published by the Free Software Foundation; either version 2
//  of the License, or (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this program; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307,
//  USA, or send email to serl@cs.colorado.edu.
//
//
// $Id: AttributeValue.java,v 1.7 2000/06/07 11:50:37 carzanig Exp $
//
package siena;

/**
   Value of an attribute in an event notification.  An
   <code>AttributeValue</code> is a container for a typed vaule of an
   attribute in a notification.  An <code>AttributeValue</code> can be
   of type <code>String</code>, <code>byte[]</code>, <code>int</code>,
   <code>long</code>, <code>double</code>, and <code>boolean</code>.  Example:
   <pre><code>
   AttributeValue v = new AttributeValue("Antonio");
   System.out.println(v.stringValue());
   Event e = new Event();
   e.putAttribute("name", v);
   </pre></code>

   @see #Event
*/
public class AttributeValue {
    /** <em>null</em> type, it is the default type of a Siena attribute */
    public static final int	NULL		= 0;
    /** string of bytes */
    public static final int	BYTEARRAY	= 1;
    /** an alias to <code>BYTEARRAY</code> 
	provided only for backward compatibility */
    public static final int	STRING		= 1;
    /** integer type.  Corresponds to the Java <code>long</code> type. */
    public static final int	LONG		= 2;
    /** integer type.  Corresponds to the Java <code>int</code> type. */
    public static final int	INT		= 2;
    /** double type.  Corresponds to the Java <code>double</code> type. */
    public static final int	DOUBLE		= 3;
    /** boolean type.  Corresponds to the Java <code>boolean</code> type. */
    public static final int	BOOL		= 4;

    protected	int		type;

    protected	byte[]		sval;
    protected	long		ival;
    protected	double		dval;
    protected	boolean		bval;
    // other types here...

    public AttributeValue() {
	type = NULL;
    }

    public AttributeValue(AttributeValue x) {
	type = x.type;
	switch(type) {
	case INT: ival = x.ival; break;
	case BOOL: bval = x.bval; break;
	case DOUBLE: dval = x.dval; break;
	case BYTEARRAY: sval = x.sval; break;
	}
    }

    public AttributeValue(String s) {
	type = BYTEARRAY;
	sval = s.getBytes();
    }

    public AttributeValue(byte[] s) {
	type = BYTEARRAY;
	sval = s;
    }

    public AttributeValue(int i) {
	type = LONG;
	ival = i;
	sval = null;
    }

    public AttributeValue(long i) {
	type = LONG;
	ival = i;
	sval = null;
    }

    public AttributeValue(boolean b) {
	type = BOOL;
	bval = b;
	sval = null;
    }

    public AttributeValue(double d) {
	type = DOUBLE;
	dval = d;
	sval = null;
    }
    //
    // other types here ...work in progress...
    //

    public int getType() {
	return type;
    }

    public int intValue() {
	switch(type) {
	case INT: return (int)ival;
	case BOOL: return bval ? 1 : 0;
	case DOUBLE: return (int)dval;
	case BYTEARRAY: return Integer.decode(new String(sval)).intValue();
	default:
	    return 0; // should throw an exception here 
	              // ...work in progress...
	}
    }

    public long longValue() {
	switch(type) {
	case INT: return ival;
	case BOOL: return bval ? 1 : 0;
	case DOUBLE: return (int)dval;
	case BYTEARRAY: return Long.decode(new String(sval)).intValue();
	default:
	    return 0; // should throw an exception here 
	              // ...work in progress...
	}
    }

    public double doubleValue() {
	switch(type) {
	case INT: return ival;
	case BOOL: return bval ? 1 : 0;
	case DOUBLE: return dval;
	case BYTEARRAY: return Double.valueOf(new String(sval)).doubleValue();
	default:
	    return 0; // should throw an exception here 
	              // ...work in progress...
	}
    }

    public boolean booleanValue() {
	switch(type) {
	case INT: return ival != 0;
	case BOOL: return bval;
	case DOUBLE: return dval != 0;
	case BYTEARRAY: return Boolean.valueOf(new String(sval)).booleanValue();
	default:
	    return false; // should throw an exception here 
	                  // ...work in progress...
	}
    }

    public String stringValue() {
	switch(type) {
	case INT: return String.valueOf(ival);
	case BOOL: return String.valueOf(bval);
	case DOUBLE: return String.valueOf(dval);
	case BYTEARRAY: return new String(sval);
	default:
	    return ""; // should throw an exception here 
	               // ...work in progress...
	}
    }

    public byte[] byteArrayValue() {
	switch(type) {
	case INT: return String.valueOf(ival).getBytes();
	case BOOL: return String.valueOf(bval).getBytes();
	case DOUBLE: return String.valueOf(dval).getBytes();
	case BYTEARRAY: return sval;
	default:
	    return null; // should throw an exception here 
	                 // ...work in progress...
	}
    }

    public boolean isEqualTo(AttributeValue x) {
	switch(type) {
	case BYTEARRAY: return sval.equals(x.sval);
	case INT: return ival == x.longValue();
	case DOUBLE: return dval == x.doubleValue();
	case BOOL: return bval == x.booleanValue();
	default: return false;
	}
    }

    public String toString() {
	return new String(SENP.encode(this));
    }

    public int hashCode() {
	return this.toString().hashCode();
    }
}



