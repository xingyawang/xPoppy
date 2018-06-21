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
// $Id: Filter.java,v 1.10 2000/06/30 18:02:14 carzanig Exp $
//
package siena;

import java.util.Set;
import java.util.Map;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.Iterator;

/**
 *  a <code>Filter</code> is a predicate defined over
 *  <code>Event</code>s.  The form of a <code>Filter</code> is a
 *  conjunction of <em>constraints</em>.  Each constraint poses an
 *  elementary condition on a specific attribute of the event.  For
 *  example a contraint<em>[price &lt; 10]</em> requires that the
 *  event contain an attribute named "price" whose value is a number
 *  less than 10.  A <code>Filter</code> can have more that one
 *  constraint for an attribute.  For example, a <code>Filter</code>
 *  can express things like <em>[model="custom", price &gt; 10, price
 *  &lt;= 20]</em>.<p>
 *
 *  Every <em>constraint</em> in a <code>Filter</code> implicitly
 *  expresses an existential quantifier over the event.  The filter of
 *  the previous example (<em>[model="custom", price &gt; 10, price
 *  &lt;= 20]</em>) requires that an event contain an attribute named
 *  "model", whose value is the string "custom", and an attribute
 *  named "price" whose value is a number between 10 and 20 (20
 *  included).<p>
 *
 *  @see AttributeConstraint
 *  @see Event
 *
 **/
public class Filter {
    /**
       what I really need here is a multimap, but there is no such
       thing in java.util (JDK 1.2.2), so I'm going to implement it as
       a map of lists */
    private Map constraints;

    /** 
	creates an empty filter.  A filter with no constraints. Notice
	that an empty filter does not match any notification.  */
    public Filter() { 
	constraints = new TreeMap();
    }

    /**
       <code>true</code> <em>iff</em> this filter contains no constraints
    */
    public boolean isEmpty() {
	return constraints.isEmpty();
    }

    /**
       put a constraint <em>a</em> on attribute <em>name</em>. Example:
       <pre><code>
       Filter f = new Filter();
       AttributeConstraint a;
       a = new AttrbuteConstraint(AttributeConstraint.SS, "soft") 
       f.addConstraint("subject", a);
       </pre></code>
    */
    public void addConstraint(String name, AttributeConstraint a) {
	Set s = (Set)constraints.get(name);
	if (s == null) {
	    s = new HashSet();
	    constraints.put(name,s);
	}
	s.add(a);
    }

    /**
       put a constraint on attribute <em>name</em> using
       comparison operator <em>op</em> and a <code>String</code>
       argument <em>sval</em>.

       <pre><code>
       Filter f = new Filter();
       f.addConstraint("subject", AttributeConstraint.SS, "soft");
       </pre></code> 
    */
    public void addConstraint(String s, short op, String sval) {
	addConstraint(s,new AttributeConstraint(op,sval));
    };

    /**
       put a constraint on attribute <em>name</em> using
       comparison operator <em>op</em> and a <code>byte[]</code>
       argument <em>sval</em>.
    */
    public void addConstraint(String s, short op, byte[] sval) {
	addConstraint(s,new AttributeConstraint(op,sval));
    };

    /**
       put a constraint on attribute <em>name</em> using
       comparison operator <em>op</em> and a <code>long</code>
       argument <em>lval</em>.
    */
    public void addConstraint(String s, short op, long lval) {
	addConstraint(s,new AttributeConstraint(op,lval));
    };

    /**
       put a constraint on attribute <em>name</em> using
       comparison operator <em>op</em> and a <code>boolean</code>
       argument <em>bval</em>.

       <pre><code>
       Filter f = new Filter();
       f.addConstraint("failed", AttributeConstraint.EQ, true);
       </pre></code> 
    */
    public void addConstraint(String s, short op, boolean bval) {
	addConstraint(s,new AttributeConstraint(op,bval));
    };

    /**
       put a constraint on attribute <em>name</em> using
       comparison operator <em>op</em> and a <code>double</code>
       argument <em>dval</em>.
    */
    public void addConstraint(String s, short op, double dval) {
	addConstraint(s,new AttributeConstraint(op,dval));
    };

    /**
       put a constraint on attribute <em>name</em> using
       the equality operator and a <code>String</code>
       argument <em>sval</em>. Example:
       <pre><code>
       Filter f = new Filter();
       f.addConstraint("name", "Antonio");
       </pre></code> 
    */
    public void addConstraint(String s, String sval) {
	addConstraint(s,new AttributeConstraint(AttributeConstraint.EQ,sval));
    };

    /**
       put a constraint on attribute <em>name</em> using
       the equality operator and a <code>byte[]</code>
       argument <em>sval</em>.
    */
    public void addConstraint(String s, byte[] sval) {
	addConstraint(s,new AttributeConstraint(AttributeConstraint.EQ,sval));
    };

    /**
       put a constraint on attribute <em>name</em> using
       the equality operator and a <code>boolean</code>
       argument <em>bval</em>.
    */
    public void addConstraint(String s, boolean bval) {
	addConstraint(s,new AttributeConstraint(AttributeConstraint.EQ,bval));
    };

    /**
       put a constraint on attribute <em>name</em> using
       the equality operator and a <code>long</code>
       argument <em>lval</em>.
    */
    public void addConstraint(String s, long lval) {
	addConstraint(s,new AttributeConstraint(AttributeConstraint.EQ,lval));
    };

    /**
       put a constraint on attribute <em>name</em> using
       the equality operator and a <code>double</code>
       argument <em>dval</em>.
    */
    public void addConstraint(String s, double dval) {
	addConstraint(s,new AttributeConstraint(AttributeConstraint.EQ,dval));
    };

    /**
        returns an iterator for the set of attribute (constraint) 
        names of this <code>Filter</code>.
     */
    public Iterator constraintNamesIterator() {
	return constraints.keySet().iterator();
    }

    /**
        returns an iterator for the set of constraints over attribute
        <em>name</em> of this <code>Filter</code>.  
     */
    public Iterator constraintsIterator(String name) {
	Set s = (Set)constraints.get(name);
	if (s == null) return null;
	return s.iterator();
    }

    public String toString() {
	return new String(SENP.encode(this));
    }
}
