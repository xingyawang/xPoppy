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
// $Id: TCPPacketHandler.java,v 1.1 2000/03/29 23:35:51 carzanig Exp $
//
package siena;

import java.net.InetAddress;
import java.net.Socket;
import java.io.IOException;

public class TCPPacketHandler implements PacketHandler {
    private InetAddress		ip_address;
    private int			port;

    public TCPPacketHandler(String h) 
	throws InvalidPacketHandlerException, java.net.UnknownHostException {

	if (h.indexOf("//", 0) != 0)
	    throw (new InvalidPacketHandlerException("expecting `//'"));

	int port_end_pos = -1;
	int host_end_pos = h.indexOf(":", 2);

	if (host_end_pos < 0) {
	    port = -1;
	    host_end_pos = h.indexOf("/", 2);
	    if (host_end_pos < 0) host_end_pos = h.length();
	} else {
	    port_end_pos = h.indexOf("/", host_end_pos);
	    if (port_end_pos < 0) port_end_pos = h.length();

	    if (host_end_pos+1 < port_end_pos) {
		port = Integer.decode(h.substring(host_end_pos+1,
						  port_end_pos)).intValue();
	    } else {
		port = -1;
	    }
	}
	String hostname = h.substring(2, host_end_pos);
	if (port == -1) {
	    port = SENP.SERVER_PORT;
	}
	ip_address = InetAddress.getByName(hostname);
    }

    public void send(byte[] packet) throws PacketHandlerException {
	try {
	    Socket s = new Socket(ip_address, port);
	    s.getOutputStream().write(packet);
	    s.close();
	} catch (IOException ex) {
	    throw new PacketHandlerException(ex.getMessage());
	}
    }

    public String representation() {
	return "senp://" + ip_address.getHostAddress() 
	    + ":" + Integer.toString(port);
    }
}
