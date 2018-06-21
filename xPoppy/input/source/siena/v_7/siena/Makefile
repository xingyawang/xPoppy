# Generated automatically from Makefile.in by configure.
#
#  This file is part of Siena, a wide-area event notification system.
#  See http://www.cs.colorado.edu/serl/dot/siena.html
#
#  Author: Antonio Carzaniga <carzanig@cs.colorado.edu>
#  See the file AUTHORS for full details. 
#
#  Copyright (C) 1998-1999 University of Colorado
#
#  This program is free software; you can redistribute it and/or
#  modify it under the terms of the GNU General Public License
#  as published by the Free Software Foundation; either version 2
#  of the License, or (at your option) any later version.
#
#  This program is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU General Public License for more details.
#
#  You should have received a copy of the GNU General Public License
#  along with this program; if not, write to the Free Software
#  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307,
#  USA, or send email to serl@cs.colorado.edu.
#
#
# $Id: Makefile.in,v 1.7 2000/03/29 23:47:18 carzanig Exp $
#

SHELL		= /bin/sh

top_srcdir	= .
srcdir		= .

#prefix		= /nfs/spectre/u6/dohy/siena/Siena_faults
#javadir		= $(prefix)/java



#
# Siena ``base'' stuff
#

JAVAC		= javac

CLASSDIR	= classes
JFLAGS		= -g -d $(CLASSDIR) -classpath $(CLASSDIR)

SOURCES 	= AttributeValue.java Event.java Siena.java \
		  AttributeConstraint.java Filter.java SienaException.java \
		  Notifiable.java Covering.java\
                  SENP.java SENPPacket.java \
                  PacketHandler.java InvalidPacketHandlerException.java \
		  PacketHandlerException.java \
		  TCPPacketHandler.java PacketHandlerFactory.java \
		  HierarchicalDispatcher.java \
		  Monitor.java \
		  SienaId.java Dejavu.java \
		  StartServer.java

CLASSES 	= AttributeValue.class Event.class Siena.class \
		  AttributeConstraint.class Filter.class SienaException.class \
		  Notifiable.class Covering.class\
                  SENP.class SENPPacket.class \
                  PacketHandler.class InvalidPacketHandlerException.class \
		  PacketHandlerException.class \
		  TCPPacketHandler.class PacketHandlerFactory.class \
		  HierarchicalDispatcher.class \
		  Monitor.class \
		  SienaId.class Dejavu.class \
		  StartServer.class

#all: $(CLASSES)

$(CLASSES): $(SOURCES)
	test -d $(CLASSDIR) || mkdir $(CLASSDIR)
	$(JAVAC) $(JFLAGS) $^

#$(PACKAGE).jar: $(SOURCES)
#	test -d $(CLASSDIR) || mkdir $(CLASSDIR)
#	$(JAVAC) $(JFLAGS) $?
#	$(JAR) cvf $@ -C $(CLASSDIR) siena
