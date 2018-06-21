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
// $Id: HierarchicalDispatcher.java,v 1.11 2000/03/29 23:33:26 carzanig Exp $
//
package siena;

import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.ListIterator;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

class ConnectionHandler implements Runnable {

    ServerSocket		sock;
    HierarchicalDispatcher	siena;

    public ConnectionHandler(ServerSocket s, HierarchicalDispatcher d) {
	sock = s;   // be careful! 
	siena = d;  // these are shared among all the threads
    }

    public void run() {
	Socket conn;
	Dejavu d = new Dejavu();
	try {
	    while(true) {
		conn = sock.accept();
		
		java.io.InputStream input = conn.getInputStream();
		
		byte[] buf = new byte[SENP.MaxPacketLen];
		int offset = 0;
		int res;
		
		while((res = input.read(buf, offset, 
					SENP.MaxPacketLen - offset)) >= 0)
		    offset += res;
		
		conn.close();
		
		byte[] pkt = new byte[offset];
		for(res = 0; res < offset; ++res) pkt[res] = buf[res];
		
		d.write();
		siena.processRequest(SENP.decode(pkt));
	    }
	} catch (Exception ex) {
	    ex.printStackTrace();
	    //
	    // ...work in progress...
	    //
	}
    }
}

class Subscriber {
    public  short	failed_attempts;
    public  long	latest_good;

    private boolean local;
    private boolean suspended;
    private byte type;
    private Object subscriber;

    public final byte[] identity;

    public Subscriber(Notifiable n) {
	Dejavu d = new Dejavu();
	try{
	   d.write();
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
	subscriber = n;
	identity = SENP.encode(n.hashCode());
	local = true;
	suspended = false;
	latest_good = 0;
	failed_attempts = 0;
    }

    public Subscriber(String id, Notifiable n) {
	subscriber = n;
	identity = id.getBytes();
	local = true;
	suspended = false;
	latest_good = 0;
	failed_attempts = 0;
    }

    public Subscriber(String id, String handler) 
    throws InvalidPacketHandlerException {
	subscriber = PacketHandlerFactory.getHandler(handler);
	identity = id.getBytes();
	local = false;
	suspended = false;
	latest_good = 0;
	failed_attempts = 0;
    }

    public boolean isLocal() {
	return local;
    }

    synchronized public void mapHandler(String handler) 
	throws InvalidPacketHandlerException {
	subscriber = PacketHandlerFactory.getHandler(handler);
	suspended = false;
	local = false;;
	latest_good = 0;
	failed_attempts = 0;
    }

    synchronized public void mapHandler(Notifiable n) {
	subscriber = n;
	suspended = false;
	local = true;
	latest_good = 0;
	failed_attempts = 0;
    }

    synchronized public void suspend() {
	suspended = true;
    }

    public int hashCode() {
	return identity.hashCode();
    };

    synchronized public void notify(SENPPacket pkt) {
	if (suspended) return;
	Dejavu d = new Dejavu();
	try {
	    if (local) {
		((Notifiable)subscriber).notify(pkt.event);
	    } else {
	    	d.write();
		System.out.println(new String(SENP.encode(pkt)));
		((PacketHandler)subscriber).send(SENP.encode(pkt));
	    }
	} catch (Exception ex) {
	    ex.printStackTrace();
	    //
	    // log something here? ...work in progress...
	    //
	    if (failed_attempts == 0) latest_good = System.currentTimeMillis();
	    failed_attempts++;
	}
    }

    synchronized public long getMillisSinceGood() {
	if (suspended || failed_attempts == 0) return 0;
	return System.currentTimeMillis() - latest_good;
    }

    synchronized public short getFailedAttempts() {
	if (suspended) return 0;
	return failed_attempts;
    }
}

class Subscription {
    public Set		preset;
    public Set		postset;

    public final Filter	filter;
    public Set		subscribers;

    public Subscription(Filter f) {
	preset = new HashSet();
	postset = new HashSet();
	filter = f;
	subscribers = new HashSet(); 
    }

    public Subscription(Filter f, Subscriber s) {
	preset = new HashSet();
	postset = new HashSet();
	filter = f;
	subscribers = new HashSet();
	subscribers.add(s);
    }
}

class Poset {
    private Set		roots;
    
    public Poset() {
	roots = new HashSet();
    }

    public boolean is_root(Subscription s) {
	return s.preset.isEmpty();
    }

    public boolean empty() {
	return roots.isEmpty();
    }

    public Iterator rootsIterator() {
	return roots.iterator();
    }

    public void insert(Subscription new_sub, 
		       Collection pre, Collection post) {
	//
	// inserts new_sub into the poset between pre and post.  the
	// connections are rearranged in order to maintain the
	// properties of the poset
	//
	Subscription x; // x always represents something in the preset
	Subscription y; // y has to do with the postset
	Iterator xi, yi;

	if (pre.isEmpty()) {
	    roots.add(new_sub);
	} else {
	    xi = pre.iterator();
	    while(xi.hasNext()) {
		x = (Subscription)xi.next();
		yi = post.iterator();
		while(yi.hasNext()) 
		    disconnect(x, (Subscription)yi.next());
		connect(x, new_sub);
	    }
	}
	yi = post.iterator();
	while(yi.hasNext()) {
	    y = (Subscription)yi.next();
	    connect(new_sub, y);
	}
    }

    public void disconnect(Subscription x, Subscription y) {
	if (x.postset.remove(y))
	    y.preset.remove(x);
    }

    public void connect(Subscription x, Subscription y) {
	if (x.postset.add(y))
	    y.preset.add(x);
    }

    public Set remove(Subscription s) {
	//
	// removes s from the poset returning the set of root
	// subscription uncovered by s
	//
	Set result = new HashSet();
	Subscription x, y;
	Iterator xi, yi;
	//
	// 1. disconnect s from every successor of s but maintains s.postest
	//
	yi = s.postset.iterator();
	while(yi.hasNext()) {
	    y = (Subscription)yi.next();
	    y.preset.remove(s);
	}

	if (s.preset.isEmpty()) {
	    //
	    // 2.1 if s is a root subscription, adds as a root every
	    // successor that remains with an empty preset, i.e.,
	    // every subscription that was a successor of s and that
	    // is now a root subscription...
	    //
	    yi = s.postset.iterator();
	    while(yi.hasNext()) {
		y = (Subscription)yi.next();
		if (y.preset.isEmpty()) {
		    roots.add(y);
		    result.add(y);
		}
	    }
	} else {
	    //
	    // 2.2 disconnects every predecessor of s thereby reconnecting
	    // predecessors to successors. A predecessor X is re-connected
	    // to a successor Y only if X does not have an immediate
	    // successor X' that covers Y (see is_indirect_successor).
	    //
	    xi = s.preset.iterator();
	    while(xi.hasNext()) {
		x = (Subscription)xi.next();
		x.postset.remove(s);
		yi = s.postset.iterator();
		while(yi.hasNext()) {
		    y = (Subscription)yi.next();
		    if (!is_indirect_successor(x, y))
			connect(x, y);
		}
	    }
	}
	return result;
    }

    public boolean is_indirect_successor(Subscription x, Subscription y) {
	//
	// says whether x indirectly covers y.
	//
	Iterator i = x.postset.iterator();
	while(i.hasNext()) 
	    if (Covering.covers(((Subscription)i.next()).filter, y.filter))
		return true;
	return false;
    }

    public Set predecessors(Filter f, Subscriber s) {
	//
	// computes the set of predecessors of filter f that do not
	// contain subscriber s.  If the poset contains any
	// subscription covering f that already contains s, then this
	// function returns null.  Otherwise, it returns the
	// collection of predecessors of f.
	// 
	LinkedList to_visit = new LinkedList();
	Set visited = new HashSet();
	Subscription sub, y;
	Iterator i = roots.iterator();
	boolean found_lower;

	while(i.hasNext()) {
	    sub = (Subscription)i.next();
	    if (Covering.covers(sub.filter, f)) {
		if (sub.subscribers.contains(s)) {
		    return null;
		} else {
		    to_visit.addLast(sub);
		}
	    }
	}
	Set result = new HashSet();

	ListIterator li;
	while((li = to_visit.listIterator()).hasNext()) {
	    sub = (Subscription)li.next();
	    i = sub.postset.iterator();
	    found_lower = false;
	    while(i.hasNext()) {
		y = (Subscription)i.next();
		if (visited.add(y)) {
		    if (Covering.covers(y.filter, f)) {
			found_lower = true;
			if (sub.subscribers.contains(s)) {
			    return null;
			} else {
			    to_visit.addLast(y);
			}
		    }
		} else if (!found_lower) {
		    if (Covering.covers(y.filter, f))
			found_lower = true;
		}
	    }
	    li.remove();
	    if(!found_lower) result.add(sub); 
	}
	return result;
    }

    public Set matchingSubscribers(Event e) {
	//
	// computes the set of subscribers that are interested in e.
	// This includes the subscribers of all the subscriptions in
	// the poset that match e
	//
	Set result = new HashSet();
	Iterator i = roots.iterator();
	LinkedList to_visit = new LinkedList();
	Set visited = new HashSet();
	Subscription sub;

	while(i.hasNext()) {
	    sub = (Subscription)i.next();
	    if (Covering.apply(sub.filter, e)) {
		to_visit.addLast(sub);
		result.addAll(sub.subscribers);
	    }
	}

	ListIterator li;
	while((li = to_visit.listIterator()).hasNext()) {
	    sub = (Subscription)li.next();
	    li.remove();
	    i = sub.postset.iterator();
	    while(i.hasNext()) {
		Subscription y = (Subscription)i.next();
		if (visited.add(y) && Covering.apply(y.filter, e)) {
		    to_visit.addLast(y);
		    result.addAll(y.subscribers);
		}
	    }
	}
	return result;
    }

    public Set successors(Filter f, Collection pred) {
	Set succ = new HashSet();
	Iterator i;
	if (pred.isEmpty()) {
	    i = roots.iterator();
	    while(i.hasNext()) {
		Subscription sub = (Subscription)i.next();
		if (Covering.covers(f, sub.filter))
		    succ.add(sub);
	    }
	} else {
	    i = pred.iterator();
	    succ.addAll(((Subscription)i.next()).postset);
	    while(!succ.isEmpty() && i.hasNext()) 
		succ.retainAll(((Subscription)i.next()).postset);
	}
	return succ;
    }

    public Subscription insert_subscription(Filter f, Subscriber s) {
	Set pred = predecessors(f,s);
	Subscription sub; 
	if (pred == null) return null;
	if (pred.size() == 1) {
	    sub = (Subscription)pred.iterator().next();
	    if (Covering.covers(f,sub.filter)) {
		sub.subscribers.add(s);
		clear_subposet(sub, s);
		return sub;
	    }
	}
	sub = new Subscription(f, s);
	insert(sub, pred, successors(f, pred));
	clear_subposet(sub, s);
	return sub;
    }

    public void clear_subposet(Subscription start, Subscriber s) {
	//
	// removes subscriber s from all the subscriptions covered by
	// start, excluding start itself.  This also removes
	// subscriptions that remain with no subscribers.
	//
	LinkedList to_visit = new LinkedList();
	Set visited = new HashSet();

	to_visit.addAll(start.postset);

	ListIterator li;
	while((li = to_visit.listIterator()).hasNext()) {
	Subscription sub = (Subscription)li.next();
	    li.remove();
	    if (visited.add(sub)) {
		if (sub.subscribers.remove(s)) {
		    if (sub.subscribers.isEmpty()) remove(sub);
		} else {
		    to_visit.addAll(sub.postset);
		}
	    }
	}
    }

    public Set to_remove(Filter f, Subscriber s) {
	//
	// removes subscriber s from all the subscriptions covered by
	// f.  Returns the set of empty subscriptions, i.e., those
	// that remained with no subscribers.  If f is null, it
	// removes s from every subscription ins the poset.
	//
	Set result = new HashSet();
	LinkedList to_visit = new LinkedList();

	if (f == null) {
	    //
	    // universal filter (same thing as BYE)
	    // so my starting point is the set of root subscriptions
	    //
	    to_visit.addAll(roots);
	} else {
	    Set pred = predecessors(f, s);
	    if (pred == null) return result;
	    to_visit.addAll(successors(f, pred));
	}

	Subscription sub;
	Set visited = new HashSet();
	ListIterator li;

	while((li = to_visit.listIterator()).hasNext()) {
	    sub = (Subscription)li.next();
	    li.remove();
	    if (visited.add(sub)) {
		if (sub.subscribers.remove(s)) {
		    if (sub.subscribers.isEmpty()) 
			result.add(sub);
		} else {
		    to_visit.addAll(sub.postset);
		}
	    }
	}
	return result;
    }
}

public class HierarchicalDispatcher implements Siena, Runnable {
    public static String	VERSION		= "h-0.3";
    public static int		MAX_RETRY	= 2;
    public static int		LISTENERS	= 5;
    
    private Poset		subscriptions	= new Poset();
    private Map			contacts	= new HashMap();

    private byte[]		master_id	= null;
    private PacketHandler	master		= null;
    private ServerSocket	listener	= null;
    private byte[]		my_handler	= null;
    private byte[]		my_identity	= null;
    
    /**
     * number of failed connections before automatic unsubscription 
     */
    public  int			max_retry	= MAX_RETRY;

    /**
     * number of connection handlers 
     */
    public  int			listeners	= LISTENERS;

    /**
     * creates a dispatcher with a specific identity 
     * @param id is the identity given to the dispatcher
     */ 
    public HierarchicalDispatcher(String id) 
	throws InvalidPacketHandlerException, java.io.IOException {
	my_identity = id.getBytes();
    }

    /**
     * creates a dispatcher 
     */
    public HierarchicalDispatcher()
	throws InvalidPacketHandlerException, java.io.IOException {
	my_identity = SienaId.getId().getBytes();
    }
    
    private void shutdownListener() {
	try {
	    listener.close();
	    //
	    // this should terminate all the connection handlers
	    // attached to that port
	    //
	} catch (IOException ex) {
	    ex.printStackTrace();
	    // what can I do here? ...work in progress
	}
	listener = null;
    }
    
    public void run() {
	Socket conn;
	Dejavu d = new Dejavu();
	try {
	    while(true) {
		try {
		    conn = listener.accept();
		} catch (java.io.IOException ex) {
		    //
		    // listener closed.
		    //
		    return;
		}
		java.io.InputStream input = conn.getInputStream();
		
		byte[] buf = new byte[SENP.MaxPacketLen];
		int offset = 0;
		int res;
		
		while((res = input.read(buf, offset, 
					SENP.MaxPacketLen - offset)) >= 0)
		    offset += res;
		
		conn.close();
		
		byte[] pkt = new byte[offset];
		for(res = 0; res < offset; ++res) pkt[res] = buf[res];
		
	 	d.write();
		processRequest(SENP.decode(pkt));
	    }
	} catch (Exception ex) {
	    ex.printStackTrace();
	    //
	    // ...work in progress...
	    //
	}
    }

    /** 
     * creates a SENP listener for this server
     * @param port is the port number allocated by the listener, 0 allocates 
     * a random available port number, the default value for Siena is 
     * SENP.DEFAULT_PORT
     * @see #shutdown
     */
    synchronized public void setListener(int port) throws IOException {
	if (listener != null) {
	    if (listener.getLocalPort() == port) {
		return;
	    } else {
		shutdownListener();
	    }
	}
	listener = new ServerSocket(port);
	String mh = "senp://" + InetAddress.getLocalHost().getHostName()
	    + ":" + Integer.toString(listener.getLocalPort());
	my_handler = mh.getBytes();
	for(int i = 0; i < listeners; ++i) 
	    (new Thread(this)).start();
	if (master != null) {
	    Dejavu d = new Dejavu();
	    try{
		d.write();
	    } catch (Exception ex) {
	      ex.printStackTrace();
	    }
	    SENPPacket redir = new SENPPacket();
	    redir.method = SENP.MAP;
	    redir.id = my_identity;
	    redir.handler = my_handler;

	    try {
		master.send(SENP.encode(redir));
	    } catch (Exception ex) {
		ex.printStackTrace();
		//
		// I should really do something here
		// ...work in progress...
		//
	    }
	}
    }

    /**
     * connects this dispatcher to a master dispatcher
     * this method is (non strictly) safe with respect to an existing master 
     * server. In fact, it unsubscribes all the existing subscriptions to the 
     * current master and then re-subscribes all of them to the new master.
     * WARNING: at this point, exceptions are silently ignored!
     * @param uri is the URI of the master dispatcher
     */ 
    synchronized public void setMaster(String handler) 
	throws InvalidPacketHandlerException, java.io.IOException {

	disconnectMaster();
	if (!subscriptions.empty()) setListener(0);

	master = PacketHandlerFactory.getHandler(handler);
	for(Iterator i = subscriptions.rootsIterator(); i.hasNext();) {
	    Subscription s = (Subscription)i.next();
	    Dejavu d = new Dejavu();
	    try {
		d.write();
		SENPPacket sub = new SENPPacket();
		sub.method = SENP.SUB;
		sub.ttl = SENP.DefaultTtl;
		sub.id = my_identity;
		sub.handler = my_handler;
		
		master.send(SENP.encode(s.filter));
	    } catch (Exception ex) {
		ex.printStackTrace();
		//
		// of course I should do something here...
		// ...work in progress...
		//
	    }
	}
    }

    /**
     * returns the URI of the master server associated with this server
     * @return URI of the master server or null if the master server is 
     *         not set  
     * @see #setMaster
     */ 
    synchronized public String getMasterURI() {
	if (master == null) return null;
	return master.representation();
    }

    /**
     * returns the URI of the listener associated with this server
     * @return URI of the listener or null if the listener is not set  
     * @see #setListener
     */ 
    synchronized public String getListenerURI() {
	if (listener == null) return null;
	return new String(my_handler);
    }

    synchronized private void disconnectMaster() {
	if (master != null && listener != null) {
	    Dejavu d = new Dejavu();
	    try {
		try{
		  d.write();
		} catch (Exception ex) {
		  ex.printStackTrace();
		}
		SENPPacket req = new SENPPacket();
		req.method = SENP.BYE;
		req.id = my_identity;
		req.to = master.representation().getBytes();
		master.send(SENP.encode(req));
	    } catch (PacketHandlerException ex) {
		ex.printStackTrace();
		//
		// well, what would you do in this case?
		// ...work in progress...
		//
	    }
	    master = null;
	}
    }

    public void processRequest(SENPPacket req) {
	Dejavu d = new Dejavu();
	if (req == null) {
	    //
	    // log something here ...work in progress...
	    //
	    return;
	}
	try{
	  d.write();
	} catch (Exception ex) {
	  ex.printStackTrace();
	}
	System.out.println("Packet: " + new String(SENP.encode(req)));
		
	if (req.ttl <= 0) return;
	req.ttl--;
	try {
	    switch(req.method) {
	    case SENP.PUB: publish(req); break;
	    case SENP.SUB: subscribe(req); break;
	    case SENP.UNS: unsubscribe(req); break;
	    case SENP.WHO: reply_who(req); break;
	    case SENP.INF: get_info(req); break;
	    default:
		System.out.println("Bad method");
		//
		// can't handle this request (yet)
		// ...work in progress...
		//
	    }
	} catch (Exception ex) {
	    ex.printStackTrace();
	    //
	    // log something here ...work in progress...
	    //
	}
    }
    
    private void reply_who(SENPPacket req) {
	Dejavu d = new Dejavu();
	if (req.handler == null || req.ttl == 0) return;
	try {
	    PacketHandler ph;
	    ph = PacketHandlerFactory.getHandler(new String(req.handler));
	    req.method = SENP.INF;
	    req.id = my_identity;
	    d.write();
	    ph.send(SENP.encode(req));
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }

    private void get_info(SENPPacket req) {
	Dejavu d = new Dejavu();
	if (req.handler == null || req.id == null) return;
	try{
	   d.write();
	} catch (Exception ex) {
	  ex.printStackTrace();
	}
	if (SENP.match(req.handler, master.representation().getBytes()))
	    master_id = req.id;
    }

    public void publish(SENPPacket req) {
	Dejavu d = new Dejavu();
	if (req.event == null) {
	    //
	    // log something here ...work in progress...
	    //
	    return;
	}
	byte[] sender = req.id;
	req.id = my_identity;
	req.to = master_id;
	//
	// first I forward to master
	//
	try{
	    d.write();
	} catch (Exception ex) {
	  ex.printStackTrace();
	}
	if (master != null && master_id != null && 
	    (sender == null || !SENP.match(sender, master_id)) &&
	    req.ttl > 0) 
	    try {
		master.send(SENP.encode(req));
	    } catch (Exception ex) {
		ex.printStackTrace();
		//
		// should log something here ...work in progress
		//
	    }

	//
	// then I find all the interested subscribers
	//
	Iterator i;
	i = subscriptions.matchingSubscribers(req.event).iterator();
	while(i.hasNext()) {
	    Subscriber s = (Subscriber)i.next();
	    if ((sender == null || !SENP.match(sender, s.identity)) && 
		(req.ttl > 0 || s.isLocal())) {
		s.notify(req);
	    }
	}
    }

    public void subscribe(SENPPacket req) 
	throws InvalidPacketHandlerException {
	if (req.filter == null) {
	    //
	    // null filters are not allowed in subscriptions this is a
	    // design choice, we could accept null filters with the
	    // semantics of the universal filter: one that matches
	    // every notification
	    //
	    return;
	}

	Dejavu d = new Dejavu();
	Subscriber s = map_subscriber(req);
	if (s == null) return;
	Subscription sub = subscriptions.insert_subscription(req.filter, s);
	if (sub == null) return;
	try{
	    d.write();
	} catch (Exception ex) {
	  ex.printStackTrace();
	}
	if (subscriptions.is_root(sub) && master != null && master_id != null
	    && !SENP.match(s.identity, master_id)  // this should never fail
	    && req.ttl > 0) 
	    try {
		req.id = my_identity;
		req.handler = my_handler;
		req.to = master_id;
		master.send(SENP.encode(req));
	    } catch (Exception ex) {
		ex.printStackTrace();
		//
		// log something here ...work in progress...
		//
	    }
    }

    public void unsubscribe(SENPPacket req) 
	throws InvalidPacketHandlerException {

	Subscriber s = find_subscriber(req);
	if (s == null) return;

	Set to_remove = subscriptions.to_remove(req.filter, s);
	if (to_remove.isEmpty()) return;

	req.id = my_identity;
	req.handler = my_handler;
	req.to = master_id;

	for(Iterator i = to_remove.iterator(); i.hasNext();) {
	Dejavu d = new Dejavu();
	    Subscription sub = (Subscription)i.next();
	    Set new_roots = subscriptions.remove(sub);
	    try{
		d.write();
	    } catch (Exception ex) {
	      ex.printStackTrace();
	    }
	    
	    if (master != null && master_id != null
		&& !SENP.match(s.identity, master_id) // this should never fail
		&& req.ttl > 0) {
		try {
		    req.method = SENP.UNS;
		    req.filter = sub.filter;
		    master.send(SENP.encode(req));
		} catch (Exception ex) {
		    ex.printStackTrace();
		    //
		    // log something here ...work in progress...
		    //
		}
		for(Iterator ri = new_roots.iterator(); i.hasNext();)
		    try {
			req.method = SENP.SUB;
			req.filter = ((Subscription)ri.next()).filter;
			master.send(SENP.encode(req));
		    } catch (Exception ex) {
			ex.printStackTrace();
			//
			// log something here ...work in progress...
			//
		    }
	    }
	}
    }

    private Subscriber map_subscriber(SENPPacket req) 
	throws InvalidPacketHandlerException {
	if (req.id == null)
	    return null;
	String id = new String(req.id);
	Subscriber s = (Subscriber)contacts.get(id);
	if (s == null) {
	    if (req.handler != null) {
		s = new Subscriber(id, new String(req.handler));
		contacts.put(id, s);
	    }
	} else if (req.handler != null) {
	    s.mapHandler(new String(req.handler));
	}
	return s;
    }

    private Subscriber find_subscriber(SENPPacket req) 
	throws InvalidPacketHandlerException {
	if (req.id == null)
	    return null;
	String id = new String(req.id);
	return (Subscriber)contacts.get(id);
    }

    /** 
     * closes the active listener of this server
     * if this dispatcher has an active listener, this method closes 
     * that listener and disconnects it from this server
     * @see #setListener 
     */
    synchronized public void shutdown() {
	disconnectMaster();
	shutdownListener();
    }

    public void publish(Event e) throws SienaException {

//  	int ttl = getTtl(e) - 1;
//  	if (ttl > 0) {
//  	    //
//  	    // fwd to master
//  	    //
//  	    e.system_putAttribute(SENP.Method, SENP.PUB);
//  	    e.system_putAttribute(SENP.Ttl, --ttl);
//  	    e.system_putAttribute(SENP.Id, my_identity);
//  	    e.system_putAttribute(SENP.To, master.representation());
//  	    // master.send(SENP.encode(e));	    
//  	}
//  	if (ttl >= 0) {
	    
	    
//  	}
//  	try {
//  	    //
//  	    // notify locally
//  	    //
//  	    // String fromObj = getSender(e);

//  	    if (ttl <= 1) return;
//  	    //	    do_notify(e, fromObj);
//  	    //purgeNotifiables();
//  	} catch (Exception ex) {
//  	    ex.printStackTrace();
//  	    throw (new SienaException(ex.getMessage()));
//  	    //
//  	    // ...work in progress...
//  	    //
//  	}
    }

    synchronized public void subscribe(Filter f, Notifiable n) 
	throws SienaException {
//  	Subscription s = subscriptions.findSubscription(f);
//  	if (s == null) {
//  	    s = new Subscription(f);
//  	    subscriptions.add(s);
//  	}
//  	s.subscribers.add(n);
//  	if (!s.forwarded && master != null) {
//  	    //
//  	    // tries to forward this subscription to the master 
//  	    //
//  	    try {
//  		if (listener == null) 
//  		    setListener(0);
//  		setForwardParameters(f);
//  		master.subscribe(f, listener.getAddress());
//  		s.forwarded = true;
//  	    } catch (Exception e) {
//  		throw (new SienaException(e.getMessage()));
//  		//
//  		// ...work in progress...
//  		//
//  	    }
//  	}
    }

    synchronized public void unsubscribe(Filter f, Notifiable n) 
	throws SienaException {
//  	failures.remove(n);
//          for(Iterator i = subscriptions.iterator(); i.hasNext();) {
//              Subscription s = (Subscription)i.next();
//  	    if (f == null || f.isEmpty() || 
//  		(Covering.covers(f,s.filter) && Covering.covers(s.filter,f))) {
//  		if (s.subscribers.remove(n)) {
//  		    if (s.subscribers.isEmpty()) {
//  			i.remove();
//  			if (master != null && s.forwarded && listener != null)
//  			    try {
//  				//
//  				// should deal with fwd parameters here
//  				// ...work in progress...
//  				//
//  				master.unsubscribe(s.filter,
//  						   listener.getAddress());
//  				s.forwarded = false;
//  			    } catch (IOException ioe) {
//  				throw (new SienaException(ioe.getMessage()));
//  				//
//  				// ...work in progress...
//  				//
//  			    }
//  		    }
//  		}
//  		if (f != null && !f.isEmpty()) return;
//  	    }
//  	}
    }

//    synchronized private Set findSubscribers(Event e, SENPObject fromObj) {
//  	Set destinations;
//  	for(Iterator i = subscriptions.iterator(); i.hasNext();) {
//  	    Subscription s = (Subscription)i.next();
//  	    if (Covering.apply(s.filter,e)) {
//  		if (destinations == null) destinations = new HashSet();
//  		destinations.addAll(s.subscribers);
//  	    }
//  	}
//  	if (fromObj != null && destinations != null) 
//  	    destinations.remove(fromObj);

//  	if (destinations.isEmpty()) {
//  	    return null; 
//  	} else {
//  	    return destinations;
//  	}
//    }

    private void do_notify(Event e, String id)  {
//  	for(Iterator i = findSubscribers(e,fromObj).iterator(); i.hasNext();) {
//  	    //
//  	    // execution of callbacks is synchronous
//  	    //
//  	    Notifiable to = (Notifiable)i.next();
//  	    try {
//  		e.system_putAttribute(SENP.To, to.toString());
//  		//
//  		// I think it is important that this notify() call be
//  		// outside of any critical section for this
//  		// HierarchicalDispatcher.  You don't want any user
//  		// code to lock the dispatcher.
//  		//
//  		to.notify(e);
//  		successfulNotify(to);
//  	    } catch (SienaException ex) {
//  		failedNotify(to);
//  	    }
//  	}
    }

    public void advertise(Filter f) {}
    public void unadvertise(Filter f) {}

    //
    // utility functions (shortcuts)
    //
    synchronized public void unsubscribe(Notifiable n) throws SienaException {
	unsubscribe(null, n);
    }

    synchronized public String sysreq(Filter parameters) {
	return null;
    }
}
