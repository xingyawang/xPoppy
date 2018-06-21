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
// $Id: AttributeConstraint.java,v 1.4 2000/03/19 20:30:22 carzanig Exp $
//
package siena;

import java.lang.Integer;
import java.lang.String;

public class AttributeConstraint {
    public AttributeValue	value;
    public short		op;
    //
    // op is one of these:
    //
    public static final short	EQ		= 1;
    public static final short	LT		= 2;
    public static final short	GT		= 3;
    public static final short	GE		= 4;
    public static final short	LE		= 5;
    public static final short	PF		= 6;
    public static final short	SF		= 7;
    public static final short	XX		= 8;
    public static final short	NE		= 9;
    public static final short	SS		= 10;

    public AttributeConstraint(short o) {
	value = new AttributeValue();
	op = o;
    }
    public AttributeConstraint(short o, String s) {
	value = new AttributeValue(s);
	op = o;
    }
    public AttributeConstraint(short o, byte[] s) {
	value = new AttributeValue(s);
	op = o;
    }
    public AttributeConstraint(short o, int i) {
	value = new AttributeValue(i);
	op = o;
    }
    public AttributeConstraint(short o, double d) {
	value = new AttributeValue(d);
	op = o;
    }
    public AttributeConstraint(short o, boolean b) {
	value = new AttributeValue(b);
	op = o;
    }
    public AttributeConstraint(short o, AttributeValue x) {
	value = new AttributeValue(x);
	op = o;
    }

    public boolean isEqualTo(AttributeConstraint x) {
	//
	// this is a conservative implementation.
	// 
	return op == x.op && (op == XX || value.isEqualTo(x.value));
    }

    public String toString() {
	return new String(SENP.encode(this));
    }

    public int hashCode() {
	return SENP.encode(this).hashCode();
    }
}
