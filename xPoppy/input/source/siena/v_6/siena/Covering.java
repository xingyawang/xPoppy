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
// $Id: Covering.java,v 1.4 2000/03/19 20:30:22 carzanig Exp $
//
package siena;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class Covering {

    public static boolean apply_operator(short op, 
					 AttributeValue x, AttributeValue y) {
	//
	// semantics of operators in filters
	//
	// apply_operator(op, x, y)  <==>  x op y
	//
	if (x.getType() == AttributeValue.NULL)
	    //
	    // I'm not sure what the best semantics would be here
	    //
	    return y.getType() == AttributeValue.NULL;

	switch(op) {
	case AttributeConstraint.XX: 
	    return true;	
	case AttributeConstraint.EQ: 
	    switch(x.getType()) {
	    case AttributeValue.STRING: 
		return y.getType() == AttributeValue.STRING
		    && x.stringValue().equals(y.stringValue());
	    case AttributeValue.BOOL: 
		return y.getType() == AttributeValue.BOOL 
		    && x.booleanValue() == y.booleanValue();
	    case AttributeValue.INT: 
		return (y.getType() == AttributeValue.INT 
			&& x.intValue() == y.intValue())
		    || (y.getType() == AttributeValue.DOUBLE 
			&& x.intValue() == y.doubleValue());
	    case AttributeValue.DOUBLE: 
		return (y.getType() == AttributeValue.INT 
			&& x.doubleValue() == y.intValue())
		    || (y.getType() == AttributeValue.DOUBLE 
			&& x.doubleValue() == y.doubleValue());
	    default:					// I should probably
		return false;				// throw an exception
	    }
	case AttributeConstraint.NE: 
	    switch(x.getType()) {
	    case AttributeValue.STRING: 
		return y.getType() != AttributeValue.STRING
		    || ! x.stringValue().equals(y.stringValue());
	    case AttributeValue.BOOL: return y.getType() != AttributeValue.BOOL
					  || x.booleanValue() != y.booleanValue();
	    case AttributeValue.INT: 
		switch(y.getType()) {
		case AttributeValue.INT: 
		    return x.intValue() != y.intValue();
		case AttributeValue.DOUBLE: 
		    return x.intValue() != y.doubleValue();
		default: return true;
		}
	    case AttributeValue.DOUBLE:  
		switch(y.getType()) {
		case AttributeValue.INT:
		    return x.doubleValue() != y.intValue();
		case AttributeValue.DOUBLE: 
		    return x.doubleValue() != y.doubleValue();
		default: return true;
		}
	    default:					// I should probably
		return false;				// throw an exception
	    }
	case AttributeConstraint.SS:
	    switch (x.getType()) {
	    case AttributeValue.STRING: 
		return y.getType() == AttributeValue.STRING
		    && x.stringValue().indexOf(y.stringValue()) != -1;
	    default: return false;			// I should probably
	    }						// throw an exception
	case AttributeConstraint.SF: 
	    switch (x.getType()) {
	    case AttributeValue.STRING: 
		return y.getType() == AttributeValue.STRING
		    && x.stringValue().endsWith(y.stringValue());
	    default: return false;			// I should probably
	    }						// throw an exception
	case AttributeConstraint.PF:
	    switch (x.getType()) {
	    case AttributeValue.STRING: 
		return y.getType() == AttributeValue.STRING
		    && x.stringValue().startsWith(y.stringValue());
	    default: return false;			// I should probably
	    }						// throw an exception
	case AttributeConstraint.LT: 
	    switch(x.getType()) {
	    case AttributeValue.STRING: 
		return y.getType() == AttributeValue.STRING
				   && x.stringValue().compareTo(y.stringValue()) < 0;
	    case AttributeValue.INT: 
		return (y.getType() == AttributeValue.INT 
			&& x.intValue() < y.intValue())
		    || (y.getType() == AttributeValue.DOUBLE 
			&& x.intValue() < y.doubleValue());
	    case AttributeValue.BOOL: 
		return y.getType() == AttributeValue.BOOL 
		    && !x.booleanValue() && y.booleanValue();
	    case AttributeValue.DOUBLE: 
		return (y.getType() == AttributeValue.INT 
			&& x.doubleValue() < y.intValue())
		    || (y.getType() == AttributeValue.DOUBLE 
			&& x.doubleValue() < y.doubleValue());
	    default:					// I should probably
		return false;				// throw an exception
	    }
	case AttributeConstraint.GT: 
	    switch(x.getType()) {
	    case AttributeValue.STRING: 
		return y.getType() == AttributeValue.STRING
		    && x.stringValue().compareTo(y.stringValue()) > 0;
	    case AttributeValue.INT: 
		return (y.getType() == AttributeValue.INT
			&& x.intValue() > y.intValue())
		    || (y.getType() == AttributeValue.DOUBLE
			&& x.intValue() > y.doubleValue());
	    case AttributeValue.BOOL: 
		return y.getType() == AttributeValue.BOOL 
		    && x.booleanValue() && !y.booleanValue();
	    case AttributeValue.DOUBLE: 
		return (y.getType() == AttributeValue.INT 
			&& x.doubleValue() > y.intValue())
		    || (y.getType() == AttributeValue.DOUBLE 
			&& x.doubleValue() > y.doubleValue());
	    default:					// I should probably
		return false;				// throw an exception
	    }
	case AttributeConstraint.LE: 
	    switch(x.getType()) {
	    case AttributeValue.STRING: 
		return y.getType() == AttributeValue.STRING
		    && x.stringValue().compareTo(y.stringValue()) <= 0;
	    case AttributeValue.INT: 
		return (y.getType() == AttributeValue.INT 
			&& x.intValue() <= y.intValue())
		    || (y.getType() == AttributeValue.DOUBLE 
			&& x.intValue() <= y.doubleValue());
	    case AttributeValue.BOOL: 
		return y.getType() == AttributeValue.BOOL 
		    && (!x.booleanValue() || y.booleanValue());
	    case AttributeValue.DOUBLE: 
		return (y.getType() == AttributeValue.INT 
			&& x.doubleValue() <= y.intValue())
		    || (y.getType() == AttributeValue.DOUBLE 
			&& x.doubleValue() <= y.doubleValue());
	    default:					// I should probably
		return false;				// throw an exception
	    }
	case AttributeConstraint.GE: 
	    switch(x.getType()) {
	    case AttributeValue.STRING: 
		return y.getType() == AttributeValue.STRING
		    && x.stringValue().compareTo(y.stringValue()) >= 0;
	    case AttributeValue.INT: 
		return (y.getType() == AttributeValue.INT 
			&& x.intValue() >= y.intValue())
		    || (y.getType() == AttributeValue.DOUBLE 
			&& x.intValue() >= y.doubleValue());
	    case AttributeValue.BOOL: 
		return y.getType() == AttributeValue.BOOL 
		    && (x.booleanValue() || !y.booleanValue());
	    case AttributeValue.DOUBLE: 
		return (y.getType() == AttributeValue.INT 
			&& x.doubleValue() >= y.intValue())
		    || (y.getType() == AttributeValue.DOUBLE 
			&& x.doubleValue() >= y.doubleValue());
	    default:					// I should probably
		return false;				// throw an exception
	    }
	default:
	    return false;				// exception ?
	}
    }

    public static boolean covers(AttributeConstraint af1, 
				 AttributeConstraint af2) {
	//
	// true when af1 defines a set of events S_af1 that contains the
	// set of events S_af2 defined by af2, in other words true iff af2
	// ==> af1 , i.e., for every Attribute x: x op2 f2 ==> x op1 f1
	// where op2 is the operator defined by af2 and f2 is the value
	// defined by af2. Same thing for af1.
	//
	// this is a crucial function! it is also quite tricky, think
	// twice before you change this implementation
	//
	// All Siena operators define transitive relations, except for
	// NE.
	//
	// trivial cases
	//
	// {x any} covers everything
	//
	if (af1.op == AttributeConstraint.XX) return true;
	//
	// nothing covers {x any} (except {x any}, see above)
	//
	if (af2.op == AttributeConstraint.XX) return false;
	//
	// {x != a} C {x op b} <-- not a op b
	//
	if (af1.op == AttributeConstraint.NE) 
	    return !apply_operator(af2.op,af1.value, af2.value);
	//
	// same operators (we already excluded af1.op == NE)
	//
	if (af2.op == af1.op)
	    return apply_operator(af1.op, af2.value, af1.value) 
		|| apply_operator(AttributeConstraint.EQ, 
				  af2.value, af1.value);
	//
	// else I must consider the implications between DIFFERENT operators
	//
	switch(af2.op) {
	case AttributeConstraint.EQ: return apply_operator(af1.op, 
							   af2.value, 
							   af1.value);
	case AttributeConstraint.LT: return af1.op == AttributeConstraint.LE 
		     && apply_operator(AttributeConstraint.LT,
				       af2.value, af1.value);
	case AttributeConstraint.LE: return af1.op == AttributeConstraint.LT
		     && apply_operator(AttributeConstraint.GT,
				       af1.value, af2.value);
	case AttributeConstraint.GT: return af1.op == AttributeConstraint.GE
		     && apply_operator(AttributeConstraint.LE,
				       af1.value, af2.value);
	case AttributeConstraint.GE: return af1.op == AttributeConstraint.GT
		     && apply_operator(AttributeConstraint.LT,
				       af1.value, af2.value);
	case AttributeConstraint.SF: return af1.op == AttributeConstraint.SS
		     && apply_operator(AttributeConstraint.SS,
				       af2.value , af1.value);
	case AttributeConstraint.PF: 
	    switch(af1.op) {
	    case AttributeConstraint.SS: 
		return apply_operator(AttributeConstraint.SS,
				      af2.value,af1.value);
	    case AttributeConstraint.GT: 
		return apply_operator(AttributeConstraint.LE,
				      af1.value, af2.value)
		    && !apply_operator(AttributeConstraint.PF,
				       af1.value, af2.value);
	    case AttributeConstraint.LT: 
		return apply_operator(AttributeConstraint.GE,
				      af1.value, af2.value)
		    && !apply_operator(AttributeConstraint.PF,
				       af1.value, af2.value);
	    case AttributeConstraint.GE: 
		return apply_operator(AttributeConstraint.LT,
				      af1.value, af2.value);
	    case AttributeConstraint.LE: 
		return apply_operator(AttributeConstraint.GT,
				      af1.value, af2.value);
	    default: return false;
	    }
	default: return false;
	}
    }

    public static boolean apply(AttributeConstraint ac, AttributeValue av) {
	return apply_operator(ac.op, av, ac.value);
    }

    //
    // semantics of subscriptions
    //
    public static boolean apply(Filter f, Event e) {
	for(Iterator i = f.constraintNamesIterator(); i.hasNext();) {
	    String name = (String)i.next();
	    AttributeValue ea = e.getAttribute(name);
	    if (ea == null) return false;
	    Iterator li = f.constraintsIterator(name);
	    while(li.hasNext())
		if (!apply((AttributeConstraint)li.next(), ea))
		    return false;
	}
	return true;
    }

    public static boolean covers(Filter f1, Filter f2)
    {
	//
	// true iff f2 ==> f1  
	// I think this expression translates into:
	// for each attribute filter af1 in f1, there exist at least one
	// corresponding (same name) attribute filter af2 in f2 such that 
	// af1 covers af2
	//
	// I'm not 100% sure of the demonstration though, the idea is that
	// attribute filters define ``connected'' subsets of ``ordered''
	// sets (which has changed since I added NE)... I think I
	// should also assume that f2 is not `null' (i.e., contradictory)
	// ...work in progress...
	//
	Iterator fi1;
	Iterator fi2;
	for(fi1 = f1.constraintNamesIterator(); fi1.hasNext();) {
	    String name = (String)fi1.next();
	    Iterator i = f1.constraintsIterator(name);
	    while(i.hasNext()) {
		    boolean found = false;
		    Iterator i2 = f2.constraintsIterator(name);
		    if (i2 == null) return false;
		    AttributeConstraint c = (AttributeConstraint)i.next();
		    while(!found && i2.hasNext()) 
			found = covers(c, (AttributeConstraint)i2.next());
		    if (!found) return false;
		}
	}
	return true;
    }
}
