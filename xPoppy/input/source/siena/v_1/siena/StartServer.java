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
// $Id: StartServer.java,v 1.2 2000/03/29 23:32:15 carzanig Exp $
//
package siena;

import java.net.InetAddress;

public class StartServer {

    static void printUsage() {
	System.err.println("usage: StartServer [-master uri] [-id identity] [-port port] [-monitor hostname]");
	System.exit(-1);
    }

    public static void main(String argv[]) {
	try {
	    String master = null;
	    int port = -1;
            HierarchicalDispatcher siena = null;
	    String monitor = null;
	    String identity = null;

	    for (int i = 0; i < argv.length; i++) {
		if (argv[i].equals("-master")) {
		    if ((i + 1) >= argv.length)
			printUsage();
		    master = argv[i + 1];
		} else if (argv[i].equals("-port")) {
		    if ((i + 1) >= argv.length)
			printUsage();
		    try {
			port = Integer.parseInt(argv[i + 1]);
		    } catch (NumberFormatException ex) {
			System.err.println("Messenger: Invalid port number.");
			printUsage();
		    }
		} else if (argv[i].equals("-monitor")) {
		    if ((i + 1) >= argv.length)
			printUsage();
		    monitor = argv[i + 1];
		    Monitor.setAddress(InetAddress.getByName(monitor));
		} else if (argv[i].equals("-id")) {
		    if ((i + 1) >= argv.length)
			printUsage();
		    identity = argv[i + 1];
		}
	    }

	    if (identity == null) {
		siena = new HierarchicalDispatcher();
	    } else {
		siena = new HierarchicalDispatcher(identity);
	    }
	    port = (port == -1) ? SENP.DEFAULT_PORT : port;
	    siena.setListener(port);
	    if (master != null) siena.setMaster(master);
	}
	catch (Exception e) {
	    System.err.println(e.toString());
	}
    }
}
