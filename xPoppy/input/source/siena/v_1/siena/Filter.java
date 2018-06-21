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
// $Id: Filter.java,v 1.6 2000/03/19 20:30:22 carzanig Exp $
//
package siena;

import java.util.Set;
import java.util.Map;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.Iterator;

public class Filter {
    private Map constraints;

    public Filter() { 
	constraints = new TreeMap();
    }

    public boolean isEmpty() {
	return constraints.isEmpty();
    }

    public void addConstraint(String name, AttributeConstraint a) {
	Set s = (Set)constraints.get(name);
	if (s == null) {
	    s = new HashSet();
	    constraints.put(name,s);
	}
	s.add(a);
    }

    public void addConstraint(String s, short op, String sval) {
	addConstraint(s,new AttributeConstraint(op,sval));
    };

    public void addConstraint(String s, short op, byte[] sval) {
	addConstraint(s,new AttributeConstraint(op,sval));
    };

    public void addConstraint(String s, short op, int ival) {
	addConstraint(s,new AttributeConstraint(op,ival));
    };

    public void addConstraint(String s, short op, boolean bval) {
	addConstraint(s,new AttributeConstraint(op,bval));
    };

    public void addConstraint(String s, short op, double dval) {
	addConstraint(s,new AttributeConstraint(op,dval));
    };

    /**
     *  returns an iterator for the set of attribute (constraint) 
     *  names of this event.
     */
    public Iterator constraintNamesIterator() {
	return constraints.keySet().iterator();
    }

    public Iterator constraintsIterator(String name) {
	Set s = (Set)constraints.get(name);
	if (s == null) return null;
	return s.iterator();
    }

    public String toString() {
	return new String(SENP.encode(this));
    }
}
