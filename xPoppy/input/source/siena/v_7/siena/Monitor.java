//
// $Id: Monitor.java,v 1.1 2000/02/21 21:05:42 carzanig Exp $
//
// author: Antonio Carzaniga <carzanig@cs.colorado.edu>
//
// Copyright (c) 1999 Software Engineering Research Laboratory,
// Department of Computer Science, University of Colorado at Boulder.
// All rights reserved.
// 
// This software was developed by the Software Engineering Research
// Laboratory of the University of Colorado at Boulder.  Redistribution
// and use in source and binary forms are permitted provided that:
// 
//    1. The above copyright notice and these paragraphs are duplicated 
// 	 in all such forms and that any documentation, and other 
// 	 materials related to such distribution and use acknowledge 
// 	 that the software was developed by the Software Engineering 
// 	 Research Laboratory of the University of Colorado at Boulder.
//    2. The redistribution and use of this software are solely for 
// 	 non-commercial purposes.
// 
// THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS OR
// IMPLIED WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED
// WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
// 
package siena;

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class Monitor {
    private static InetAddress	Address = null;

    public static final String	SienaIcon = "0";
    public static final String	RedIcon = "1";
    public static final String	BlueIcon = "2";
    public static final String	AntoIcon = "3";
    public static final String	AdaptIcon = "4";
    public static final String	UpdateIcon = "5";
    public static final String	ReconfigureIcon = "6";
    public static final String	InventoryIcon = "7";
    public static final String	RemoveIcon = "8";
    public static final String	ConstrainIcon = "9";

    public static String	HostName = System.getProperty("SienaMonitor", 
							      "milano");
    public static int		Port = 1996;

    private static DatagramSocket sock = null; 

    /**
     * Sends a log message to the Siena monitor.
     * @param method is either "PUB", "NOT", "SUB", "UNS", "ADV", or "UNA".
     * @param from is whoever sends the Siena request 
     *        (in most cases it would be <core>this</code>).
     * @param to is the destination of the Siena request.
     * @param param is the parameter of the Siena request 
     *        (usually an Event or a Filter).
     */
    public static void sendMessage(String method, 
				   Object from, Object to, Object param) {
	try {
	    if (Address == null) 
		Address = InetAddress.getByName(HostName);
	    
	    if (sock == null) 
		sock = new DatagramSocket();

	    String s = new String(method+"\t"+from+"\t"+to+"\t"+param);
	    sock.send(new DatagramPacket(s.getBytes(), s.length(), 
					 Address,  Port));
	} catch (Exception ex) {
	    return;
	}
    }
    public static void setIcon(Object from, String icon) {
	try {
	    if (Address == null) 
		Address = InetAddress.getByName(HostName);
	    
	    if (sock == null) 
		sock = new DatagramSocket();

	    String s = new String("CFG\t"+from+"\t"+from+"\t"+icon);
	    sock.send(new DatagramPacket(s.getBytes(), s.length(), 
					 Address,  Port));
	} catch (Exception ex) {
	    return;
	}
    }
    public static void setAddress(InetAddress a) {
	Address = a;
    }
}
