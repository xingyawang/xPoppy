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
// $Id: Siena.java,v 1.4 2000/02/21 20:59:00 carzanig Exp $
//
package siena;

public interface Siena {
    //
    // basic interface functions
    //
    /**
     * Publish an event.
     * @param e The event to publish.
     * @see #subscribe
     */
    public void publish(Event e) throws SienaException;
    /**
     * Subscribes for the events matching Filter <b>f</b>.
     * @param n is the subscriber
     * @see #unsubscribe
     */
    public void subscribe(Filter f, Notifiable n) throws SienaException;
    /**
     * Cancels the subscription <b>f</b> posted by <b>n</b>.
     * @param n is the subscriber
     * @see #subscribe
     */
    public void unsubscribe(Filter f, Notifiable n) throws SienaException;
    public void advertise(Filter f) throws SienaException;
    public void unadvertise(Filter f) throws SienaException;

    //
    // utility functions (shortcuts)
    //
    // unsubscribes n completey
    //
    /**
     * Cancels <i>all</i> the subscriptions posted by <b>n</b>.
     * @param n is the subscriber
     * @see #subscribe
     */
    public void unsubscribe(Notifiable n) throws SienaException;

    //
    // ``system'' request. This can be used to access/modify
    // configuration and system parameters. Obviously, this part is
    // ``implementation dependent'' ...work in progress...
    //
    public String sysreq(Filter parameters);
}


