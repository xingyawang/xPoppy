//
//  This file is part of Siena, a wide-area event notification system.
//  See http://www.cs.colorado.edu/serl/dot/siena.html
//
//  Author: Antonio Carzaniga <carzanig@cs.colorado.edu>
//  See the file AUTHORS for full details. 
//
//  Copyright (C) 1998-2000 University of Colorado
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
// $Id: SienaId.java,v 1.1 2000/03/29 23:35:35 carzanig Exp $
//
package siena;

import java.util.Calendar;

public class SienaId {
    static private byte counter = 0;
    /**
     * Creates a new unique id.
     */
    static public String getId() {
	Long s = new Long(System.currentTimeMillis());
	String hn;
	try {
	    hn = java.net.InetAddress.getLocalHost().getHostName();
	} catch (java.net.UnknownHostException ex) {
	    hn = "-";
	}
	return s.toString() +  "." + (new Byte(counter++)).toString() 
	    + "." + hn;
    } 
}


