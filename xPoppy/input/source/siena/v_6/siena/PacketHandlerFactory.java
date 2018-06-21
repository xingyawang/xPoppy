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
// $Id: PacketHandlerFactory.java,v 1.1 2000/03/29 23:36:18 carzanig Exp $
//
package siena;

import java.io.IOException;

public class PacketHandlerFactory {
    static public PacketHandler getHandler(String handler) 
	throws InvalidPacketHandlerException {
	//
	// this parses a handler of the form:
	// <schema> "://" <host> [":" <port>] ["/" <path>]
	//
	int pos = handler.indexOf(":");
	if (pos < 0) throw (new InvalidPacketHandlerException("can't find schema"));
	if (handler.substring(0,pos).equals("senp")) {
	    try {
		return new TCPPacketHandler(handler.substring(pos+1, 
							      handler.length()));
	    } catch (IOException ex) {
		throw new InvalidPacketHandlerException(ex.getMessage());
	    }
	} else { 
	    throw (new InvalidPacketHandlerException("unknown schema: " + handler));
	}
    }
}
