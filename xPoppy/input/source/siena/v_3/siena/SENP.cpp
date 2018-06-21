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
// $Id: SENP.java,v 1.11 2000/04/13 21:04:45 carzanig Exp $
//
package siena;

import java.util.Iterator;
import java.util.HashSet;

class ByteBuf {
    int pos;
    private byte[] buf;

    public ByteBuf(byte [] x) {
	pos = 0;
    }

    public ByteBuf() {
	buf = new byte[SENP.MaxPacketLen];
	pos = 0;
    }

    public void append(byte b) {
	buf[pos++] = b;
    }

    public void append(int x) {
	buf[pos++] = (byte)x;
    }

    public void append(byte[] bytes) {
	for(int i = 0; i < bytes.length; ++i) 
	    buf[pos++] = bytes[i];
    }

    public void append(String s) {
	append(s.getBytes());
    }

    public void reset() {
	pos = 0;
    }

    public byte[] bytes() {
	byte[] res = new byte[pos];
	for(int i = 0; i < pos; ++i)
	    res[i] = buf[i];
	return res;
    }
}

class Tokenizer {
    //
    // WARNING: now, since Java doesn't have byte literals in the form
    // of ascii characters like good old 'a' '*' '\n' etc.  I'll have
    // to use the corresponding decimal values.  Which is the `right'
    // way of doing that, according to some clowns out there...
    //

    //
    // token types
    //
    public static final int	T_EOF		= -1;
    public static final int	T_UNKNOWN	= -2;
    //
    // keywords
    //
    public static final int	T_ID		= -3;
    public static final int	T_STR		= -4;
    public static final int	T_INT		= -5;
    public static final int	T_DOUBLE	= -6;
    public static final int	T_BOOL		= -7;
    public static final int	T_OP		= -8;
    public static final int	T_LPAREN	= -9;
    public static final int	T_RPAREN	= -10;

    public short	oval;
    public byte[]	sval;
    public long		ival;
    public boolean	bval;
    public double	dval;

    private byte[] buf;
    private int pos;
    private ByteBuf tmp;

    private int nextByte() {
	if (++pos >= buf.length) return -1;
	return buf[pos];
    }

    private int currByte() {
	if (pos >= buf.length) return -1;
	return buf[pos];
    }

    private void pushBack() {
	if (pos > 0) pos--;
    }

    private boolean isCurrentFirstIdentChar() {
	if (pos >= buf.length) return false;
	return (buf[pos] >= 0x41 && buf[pos] <= 0x5a)	/* 'A' -- 'Z' */
	    || (buf[pos] >= 0x61 && buf[pos] <= 0x7a)	/* 'a' -- 'z' */
	    || buf[pos] == 0x5f;				/* '_' */
    }

    private boolean isCurrentIdentChar() {
	if (pos >= buf.length) return false;
	return isCurrentFirstIdentChar() 
	    || buf[pos] == 0x2e || buf[pos] == 0x2f;	/* '.', '/' */
    }


    private byte read_octal() { 
				/* '0' -- '7' */
	byte nb = 0;
	int i = 3;
	do {
	    nb = (byte)(nb * 8 + currByte() - 0x30);
	} while(--i > 0 && ++pos < buf.length 
		&& buf[pos] >= 0x30 && buf[pos] <= 0x37);
	return nb;
    }

    private int read_string() {
	//
	// here buf[pos] == '"'
	//
	tmp.reset();
	while(++pos < buf.length)
	    switch (buf[pos]) {
	    case 0x22 /* '"' */: sval = tmp.bytes(); ++pos; return T_STR;
	    case 0x5c /* '\\' */: 
		if (++pos >= buf.length) return T_UNKNOWN;
		switch (buf[pos]) {
		case 0x76 /* 'v' */: 
		    tmp.append(0x0b /* '\v' */);break;
		case 0x66 /* 'f' */: 
		    tmp.append(0x0c /* '\f' */);break;
		case 0x72 /* 'r' */: 
		    tmp.append(0x0d /* '\r' */);break; 
		case 0x6e /* 'n' */: 
		    tmp.append(0x0a /* '\n' */); break;
		case 0x74 /* 't' */: 
		    tmp.append(0x09 /* '\t' */); break;
		case 0x62 /* 'b' */: 
		    tmp.append(0x08 /* '\b' */); break;
		case 0x61 /* 'a' */: 
		    tmp.append(0x07 /* '\a' */); break;
		default:
		    if (buf[pos] >= 0x30 && buf[pos] <= 0x37) { 
			tmp.append(read_octal());
		    } else {
			tmp.append(buf[pos]);
		    }
		}
		break;
	    default:
		tmp.append(buf[pos]);
	    }
	return T_UNKNOWN;
    }

    private int read_id() {
	tmp.reset();
	do {
	    tmp.append(buf[pos++]);
	} while((buf[pos] >= 0x41 && buf[pos] <= 0x5a)	 /* 'A' -- 'Z' */
		|| (buf[pos] >= 0x61 && buf[pos] <= 0x7a) /* 'a' -- 'z' */
		|| buf[pos] == 0x5f                      /* '_' */
		|| buf[pos] == 0x2e || buf[pos] == 0x2f);	 /* '.', '/' */
	sval = tmp.bytes();
	return T_ID;
    }

    private int read_int() {
	boolean negative = false;
	//
	// here buf[pos] is either a digit or '-'
	//
	if (buf[pos] == 0x2d /* '-' */) {
	    negative = true;
	    ival = 0;
	    if (++pos >= buf.length || buf[pos] < 0x30 || buf[pos] > 0x39)
		return T_UNKNOWN;
	} else {
	    ival = buf[pos] - 0x30;
	    if (++pos >= buf.length || buf[pos] < 0x30 || buf[pos] > 0x39)
		return T_INT;
	}
	do {
	    ival = ival * 10 + buf[pos] - 0x30;
	} while (++pos < buf.length && buf[pos] >= 0x30 && buf[pos] <= 0x39);
	if (negative) ival = -ival;
	return T_INT;
    }

    private int read_number() {
	boolean negative = false;
	//
	// here buf[pos] is either a digit or '-'
	//
	if (buf[pos] == 0x2d /* '-' */) {
	    negative = true;
	    if (++pos >= buf.length || buf[pos] < 0x30 || buf[pos] > 0x39)
		return T_UNKNOWN;
	}
	int type;
	if (read_int() == T_UNKNOWN) return T_UNKNOWN;
	type = T_INT;
	dval = ival;
	if (pos < buf.length && buf[pos] == 0x2e /* '.' */) {
	    type = T_DOUBLE;
	    if (++pos >= buf.length || buf[pos] < 0x30 || buf[pos] > 0x39) {
		return T_UNKNOWN;
	    } else {
		dval += read_decimal();
	    }
	}
	if (pos < buf.length)
	    if (buf[pos] == 101 /* 'e' */ || buf[pos] == 69) /* 'E' */ {
		type = T_DOUBLE;
		if (++pos >= buf.length 
		    || ((buf[pos] < 0x30 || buf[pos] > 0x39) 
			&& buf[pos] != 0x2d /* '-' */))
		    return T_UNKNOWN;
		if (read_int() == T_UNKNOWN) return T_UNKNOWN;
		dval *= java.lang.Math.pow(10,ival);
	    }
	if (negative) {
	    if (type == T_INT) {
		ival = -ival;
	    } else {
		dval = -dval;
	    }
	}
	return type;
    }

    private double read_decimal() {
	//
	// here buf[pos] is a digit
	//
	long intpart = 0;
	long divisor = 1;
	do {
	    intpart = intpart*10 + (buf[pos] - 0x30);
	    divisor *= 10;
	} while(++pos < buf.length && buf[pos] >= 0x30 && buf[pos] <= 0x39);
	return (1.0 * intpart) / divisor;
    }

    public Tokenizer(byte[] b) {
	buf = b;
	pos = 0;
	tmp = new ByteBuf();
    }

    public int nextToken() {
	while (true) {
	    switch(currByte()) {
	    case -1: return T_EOF;
	    case 0x22 /* '"' */: return read_string();
	    case 123 /* '{' */: ++pos; return T_LPAREN;
	    case 125 /* '}' */: ++pos; return T_RPAREN;
	    case 33 /* '!' */: 
		switch(nextByte()) {
		case 0x3d /* '=' */: 
		    oval = AttributeConstraint.NE; ++pos; return T_OP;
		default: return T_UNKNOWN;
		}
	    case 42 /* '*' */: 
		switch(nextByte()) {
		case 60 /* '<' */: 
		    oval = AttributeConstraint.SF; ++pos; return T_OP;
		default: 
		    oval = AttributeConstraint.SS; return T_OP;
		}
	    case 0x3d /* '=' */: 
		oval = AttributeConstraint.EQ; ++pos; return T_OP;
	    case 62 /* '>' */: 
		switch(nextByte()) {
		case 42 /* '*' */: 
		    oval = AttributeConstraint.PF; ++pos; return T_OP;
		case 0x3d /* '=' */: 
		    oval = AttributeConstraint.GE; ++pos; return T_OP;
		default:
		    oval = AttributeConstraint.GT; return T_OP;
		}
	    case 60 /* '<' */: 
		switch(nextByte()) {
		case 0x3d /* '=' */: 
		    oval = AttributeConstraint.LE; ++pos; return T_OP;
		default:
		    oval = AttributeConstraint.LT; return T_OP;
		}
	    default:
		if ((buf[pos] >= 0x30 && buf[pos] <= 0x39) /* '0' -- '9' */
		     || buf[pos] == 0x2d /* '-' */) {
		    return read_number();
		} else if (isCurrentFirstIdentChar()) { 
		    return read_id();
		} else {
		    //
		    // I simply ignore characters that I don't understand 
		    //
		    ++pos;
		}
	    }
	}
    }
}

class SENPInvalidFormat extends Exception {
    int		expected_type;
    String	expected_value;
    int		line_number;

    public SENPInvalidFormat() {
	super();
    }
    public SENPInvalidFormat(String v) {
	super("expecting: `" + v + "'");
	expected_value = v;
    }
    public SENPInvalidFormat(int t, String v) {
	this(v);
	expected_type = t;
    }
    public SENPInvalidFormat(int t) {
	this();
	expected_type = t;
    }
    
    public int getExpectedType() {
	return expected_type;
    }

    public String getExpectedValue() {
	return expected_value;
    }

    public int getLineNumber() {
	return expected_type;
    }
}


public class SENP {

    public static final byte ProtocolVersion = 1;

    public static final byte[] Version
    = {0x76, 0x65, 0x72, 0x73, 0x69, 0x6f, 0x6e};	// version
    public static final byte[] To 
    = {0x74, 0x6F};					// to
    public static final byte[] Method 
    = {0x6d, 0x65, 0x74, 0x68, 0x6f, 0x64};		// method
    public static final byte[] Id 
    = {0x69, 0x64};					// id
    public static final byte[] Handler 
    = {0x68, 0x61, 0x6e, 0x64, 0x6c, 0x65, 0x72};	// handler
    public static final byte[] Ttl 
    = {0x74, 0x74, 0x6c};				// ttl

    public static final int	DefaultTtl		= 30;

    public static final int	MaxPacketLen		= 65536;

    public static final byte NOP = 0;
    public static final byte PUB = 1;
    public static final byte SUB = 2;
    public static final byte UNS = 3;
    public static final byte ADV = 4;
    public static final byte UNA = 5;
    public static final byte HLO = 6;
    public static final byte BYE = 7;
    public static final byte SUS = 8;
    public static final byte RES = 9;
    public static final byte MAP = 10;
    public static final byte WHO = 11;
    public static final byte INF = 12;

    public static final byte[][] Methods = 
    {
	{ 0x4E, 0x4F, 0x50 },	// NOP
	{ 0x50, 0x55, 0x42 },	// PUB
	{ 0x53, 0x55, 0x42 },	// SUB
	{ 0x55, 0x4E, 0x53 },	// UNS
	{ 0x41, 0x44, 0x56 },	// ADV
	{ 0x55, 0x4E, 0x41 },	// UNA
	{ 0x48, 0x4C, 0x4F },	// HLO
	{ 0x42, 0x59, 0x45 },	// BYE
	{ 0x53, 0x55, 0x53 },	// SUS
	{ 0x52, 0x45, 0x53 },	// RES
	{ 0x4D, 0x41, 0x50 },	// MAP
	{ 0x57, 0x48, 0x4f },	// WHO
	{ 0x49, 0x4e, 0x46 }	// INF
    };

    //
    //  WARNING:  don't mess up the order of operators in this array
    //            it must correspond to the definitions of 
    //            AttributeConstraint.EQ, AttributeConstraint.LT, etc.
    // 
    public static final byte[][] operators = { 
	{0x3f}, // ?
	{0x3d}, // "="
	{0x3c}, // "<"
	{0x3e}, // ">"
	{0x3e, 0x3d}, // ">="
	{0x3c, 0x3d}, // "<="
	{0x3e, 0x2a}, // ">*"
	{0x2a, 0x3c}, // "*<"
	{0x61, 0x6e, 0x79}, // any, 
	{0x21, 0x3d}, // "!="
	{0x2a} // "*" 
    };
    //
    // default port numbers
    //
    public static final int	CLIENT_PORT		= 1936;
    public static final int	SERVER_PORT		= 1969;
    public static final int	DEFAULT_PORT		= 1969;

    public static final byte[] KwdSeparator = { 0x20 }; // ' '
    public static final byte[] KwdSenp 
    = {0x73, 0x65, 0x6e, 0x70}; // senp
    public static final byte[] KwdEvent 
    = {0x65, 0x76, 0x65, 0x6e, 0x74}; // event
    public static final byte[] KwdFilter 
    = {0x66, 0x69, 0x6c, 0x74, 0x65, 0x72}; // filter
    public static final byte[] KwdPattern 
    = {0x70, 0x61, 0x74, 0x74, 0x65, 0x72, 0x6e}; // pattern // not used yet...
    public static final byte[] KwdLParen 
    = {0x7b}; // {
    public static final byte[] KwdRParen 
    = {0x7d}; // }
    public static final byte[] KwdEquals 
    = {0x3d}; // =
    public static final byte[] KwdTrue 
    = {0x74, 0x72, 0x75, 0x65}; // true
    public static final byte[] KwdFalse 
    = {0x66, 0x61, 0x6c, 0x73, 0x65}; // false

    public static boolean match(byte[] x, byte[] y) {
	if (x.length != y.length) return false;
	for(int i = 0; i < x.length; ++i)
	    if (x[i] != y[i]) return false;
	return true;
    }

    public static byte[] encode(AttributeValue a) {
	ByteBuf sb = new ByteBuf();
	return encode(sb, a).bytes();
    }

    public static byte[] encode(long number) {
	ByteBuf sb = new ByteBuf(new byte[20]);
	return encode_decimal(sb, number).bytes();
    }

    private static ByteBuf encode_octal(ByteBuf sb, byte x) {
	sb.append((byte)((x >> 6) & 3) + 0x30);
#ifdef F_SP_HD_1
	sb.append((byte)((x >> 6) & 7) + 0x30);
#else
	sb.append((byte)((x >> 3) & 7) + 0x30);
#endif
	sb.append((byte)(x & 7) + 0x30);
	return sb; 
    }

    private static ByteBuf encode_decimal(ByteBuf sb, long x) {
	byte[] buf = new byte[20]; // Log(MAX_LONG)+1
	int pos = 0;
	boolean negative = (x<0);
	if (negative) x = -x;

	do {
	    buf[pos++] = (byte)(x % 10 + 0x30 /* '0' */);
	    x /= 10;
	} while (x > 0);
	if (negative) buf[pos++] = 0x2d; /* '-' */
	
	while(pos-- > 0) sb.append(buf[pos]);
	return sb; 
    }

    private static ByteBuf encode_double(ByteBuf sb, long x) {
	
	byte[] buf = new byte[20]; // Log(MAX_LONG)+1
	int pos = 0;
	boolean negative = (x<0);
	if (negative) x = -x;

	do {
	    buf[pos++] = (byte)(x % 10 + 0x30 /* '0' */);
	    x /= 10;
	} while (x > 0);
	if (negative) buf[pos++] = 0x2d; /* '-' */
	
	while(pos-- > 0) sb.append(buf[pos]);
	return sb; 
    }

    private static ByteBuf encode(ByteBuf sb, byte[] bv) {
	sb.append(0x22 /* '"' */);
	for(int i = 0; i < bv.length; ++i)
	    switch(bv[i]) {
	    case 11 /* '\v' */: 
		sb.append(0x5c /* '\\' */);
		sb.append(0x76 /* 'v' */); 
		break;
	    case 12 /* '\f' */: 
		sb.append(0x5c /* '\\' */);
		sb.append(0x66 /* 'f' */); 
		break;
	    case 13 /* '\r' */: 
		sb.append(0x5c /* '\\' */);
		sb.append(0x72 /* 'r' */); 
		break;
	    case 10 /* '\n' */: 
		sb.append(0x5c /* '\\' */);
		sb.append(0x6e /* 'n' */); 
		break;
	    case 9 /* '\t' */: 
		sb.append(0x5c /* '\\' */);
		sb.append(0x74 /* 't' */); 
		break;
	    case 8 /* '\b' */: 
		sb.append(0x5c /* '\\' */);
		sb.append(0x62 /* 'b' */); 
		break;
	    case 7 /* '\a' */: 
		sb.append(0x5c /* '\\' */);
		sb.append(0x61 /* 'a' */); 
		break;
	    case 0x22 /* '"' */:
#ifdef F_SP_HD_2
		sb.append(0x22 /* '\\' */);
#else
		sb.append(0x5c /* '\\' */);
#endif
		sb.append(0x22 /* '"' */); 
		break;
	    case 0x5c /* '\\' */:
		sb.append(0x5c /* '\\' */);
		sb.append(0x5c /* '\\' */); 
		break;
	    default:
		if (bv[i] < 0x20 || bv[i] >= 0x7F) {
		    //
		    // here I handle other non-printable characters with
		    // the \xxx octal notation ...work in progress...
		    //
		    sb.append(0x5c);
		    encode_octal(sb, bv[i]);
		} else {
		    sb.append(bv[i]);
		}
	    }
	sb.append(0x22 /* '"' */);
	return sb;
    }

    public static ByteBuf encode(ByteBuf sb, AttributeValue a) {
	switch(a.getType()) {
	case AttributeValue.INT: 
	    encode_decimal(sb, a.intValue()); break;
	case AttributeValue.BOOL: sb.append(a.booleanValue() ?
					    KwdTrue : KwdFalse); break;
	case AttributeValue.DOUBLE:
	    sb.append(Double.toString(a.doubleValue())); break;
	case AttributeValue.BYTEARRAY: 
	    return encode(sb, a.byteArrayValue());
	default:
	    return sb;	// should throw an exception here 
				// ...work in progress...
	}
	return sb;
    }

    public static byte[] encode(SENPPacket p) {
	ByteBuf b = new ByteBuf();
	return encode(b, p).bytes();
    }

    public static ByteBuf encode(ByteBuf sb, SENPPacket p) {
	sb.append(KwdSenp);
	sb.append(KwdLParen);

	sb.append(Version);
	sb.append(KwdEquals);
	encode_decimal(sb, p.version);
	sb.append(KwdSeparator);

	sb.append(Method);
	sb.append(KwdEquals);
	encode(sb, Methods[p.method]);
	sb.append(KwdSeparator);

	sb.append(Ttl);
	sb.append(KwdEquals);
	encode_decimal(sb, p.ttl);

	if (p.id != null) {
	    sb.append(KwdSeparator);
	    sb.append(Id);
	    sb.append(KwdEquals);
	    encode(sb, p.id);	    
	}

	if (p.to != null) {
	    sb.append(KwdSeparator);
	    sb.append(To);
	    sb.append(KwdEquals);
	    encode(sb, p.to);	    
	}

	if (p.handler != null) {
	    sb.append(KwdSeparator);
	    sb.append(Handler);
	    sb.append(KwdEquals);
	    encode(sb, p.handler);	    
	}

	sb.append(KwdRParen);

	if (p.event != null) {
	    sb.append(KwdSeparator);
	    encode(sb, p.event);	    
	} else if (p.filter != null) {
	    sb.append(KwdSeparator);
	    encode(sb, p.filter); 
	}

	return sb;
    }

    public static byte[] encode(Event e) {
	ByteBuf b = new ByteBuf();
	return encode(b, e).bytes();
    }

    public static ByteBuf encode(ByteBuf sb, Event e) {
	sb.append(KwdEvent);
	sb.append(KwdLParen);
	Iterator i = e.attributeNamesIterator();
	while(i.hasNext()) {
	    sb.append(KwdSeparator);
	    String name = (String)i.next();
	    sb.append(name);
	    sb.append(KwdEquals);
	    encode(sb, e.getAttribute(name));
	}
	sb.append(KwdRParen);
	return sb;
    }

    public static byte[] encode(Filter f) {
	ByteBuf b = new ByteBuf();
	return encode(b, f).bytes();
    }

    public static ByteBuf encode(ByteBuf sb, Filter f) {
	sb.append(KwdFilter);
	sb.append(KwdLParen);
	Iterator i = f.constraintNamesIterator();
	while(i.hasNext()) {
	    String name = (String)i.next();
	    Iterator j = f.constraintsIterator(name); 
	    while(j.hasNext()) {
		sb.append(KwdSeparator);
		sb.append(name + " ");
		encode(sb, (AttributeConstraint)j.next());
	    }
	}
	sb.append(KwdRParen);
	return sb;
    }


    public static byte[] encode(AttributeConstraint a) {
	ByteBuf sb = new ByteBuf();
	return encode(sb, a).bytes();
    }

    public static ByteBuf encode(ByteBuf sb, AttributeConstraint a) {
	sb.append(operators[a.op]);
	if (a.op == AttributeConstraint.XX) return sb;
	return 	encode(sb, a.value);
    }

    private static AttributeValue readAttribute(Tokenizer t) 
	throws SENPInvalidFormat {
	switch(t.nextToken()) {
	case Tokenizer.T_ID: return new AttributeValue(t.sval);
	case Tokenizer.T_STR: return new AttributeValue(t.sval);
	case Tokenizer.T_INT: return new AttributeValue(t.ival);
	case Tokenizer.T_BOOL: return new AttributeValue(t.bval);
	case Tokenizer.T_DOUBLE: return new AttributeValue(t.dval);
	default:
	    throw(new SENPInvalidFormat("<int>, <string>, <bool> or <double>"));
	}
    }

    private static AttributeConstraint readAttributeConstraint(Tokenizer t) 
	throws SENPInvalidFormat {
	switch (t.nextToken()) {
	case Tokenizer.T_ID: 
	    if (match(t.sval, operators[AttributeConstraint.XX])) { 
		return new AttributeConstraint(AttributeConstraint.XX);
	    } else {
		throw(new SENPInvalidFormat(Tokenizer.T_OP));
	    }
	case Tokenizer.T_OP: {
	    short op = t.oval;
	    return new AttributeConstraint(op, readAttribute(t));
	}
	default:
	    throw(new SENPInvalidFormat(Tokenizer.T_OP));
	}
    }

    private static final String ErrAttrName = "<attribute-name>";

    public static SENPPacket decode(byte[] pkt) throws SENPInvalidFormat {
	Tokenizer tokenizer;
	tokenizer = new Tokenizer(pkt);
	
	if (tokenizer.nextToken() != Tokenizer.T_ID
	    || !match(tokenizer.sval,KwdSenp)) {
	    throw(new SENPInvalidFormat(Tokenizer.T_ID, new String(KwdSenp)));
	}
	if (tokenizer.nextToken() != Tokenizer.T_LPAREN) 
	    throw(new SENPInvalidFormat(Tokenizer.T_LPAREN, 
				 new String(KwdLParen)));
	SENPPacket res = new SENPPacket();
	int ttype;
	String name;
	while ((ttype = tokenizer.nextToken()) != Tokenizer.T_RPAREN) {
	    byte[] attrname;
	    AttributeValue av;
	    if (ttype != Tokenizer.T_ID)
		throw(new SENPInvalidFormat(Tokenizer.T_ID, ErrAttrName));
	    attrname = tokenizer.sval;
	    if (tokenizer.nextToken() != Tokenizer.T_OP 
		|| tokenizer.oval != AttributeConstraint.EQ)
		throw(new SENPInvalidFormat(Tokenizer.T_OP,
					    new String(KwdEquals)));
	    av = readAttribute(tokenizer);
	    if (res.method == NOP && match(attrname, Method)) {
		if (av.getType() == AttributeValue.BYTEARRAY) {
		    byte mi;
		    for (mi = 0; mi < Methods.length; ++mi)
			if (match(av.byteArrayValue(), Methods[mi]))
			    break;
		    if (mi < Methods.length) 
			res.method = mi;
		}
	    } else if (res.ttl == SENP.DefaultTtl && match(attrname, Ttl)) {
		if (av.getType() == AttributeValue.INT) 
		    res.ttl = (byte)av.intValue();
	    } else if (match(attrname, Version)) {
		if (av.getType() == AttributeValue.INT) 
		    res.version = (byte)av.intValue();
	    } else if (res.id == null && match(attrname, Id)) {
		if (av.getType() == AttributeValue.BYTEARRAY) 
		    res.id = (av.byteArrayValue());
	    } else if (res.to == null && match(attrname, To)) {
		if (av.getType() == AttributeValue.BYTEARRAY) 
		    res.to = (av.byteArrayValue());
	    } else if (res.handler == null && match(attrname, Handler)) {
		if (av.getType() == AttributeValue.BYTEARRAY) 
		    res.handler = (av.byteArrayValue());
	    }
	}
	//
	// now reads the optional parameter: either a filte or an event
	//
	switch (tokenizer.nextToken()) {
	case Tokenizer.T_EOF: return res;
	case Tokenizer.T_ID: 
	    if (match(tokenizer.sval, KwdFilter)) {
		if (tokenizer.nextToken() != Tokenizer.T_LPAREN) 
		    throw(new SENPInvalidFormat(Tokenizer.T_LPAREN, 
						new String(KwdLParen)));
		res.filter = new Filter();
		while ((ttype = tokenizer.nextToken()) != Tokenizer.T_RPAREN) {
		    if (ttype != Tokenizer.T_ID && ttype != Tokenizer.T_STR)
			throw(new SENPInvalidFormat(Tokenizer.T_ID,
						    ErrAttrName));
		    name = new String(tokenizer.sval);
		    res.filter.addConstraint(name, 
					     readAttributeConstraint(tokenizer));
		}
		return res;
	    } else if (match(tokenizer.sval, KwdEvent)) {
		if (tokenizer.nextToken() != Tokenizer.T_LPAREN) 
		    throw(new SENPInvalidFormat(Tokenizer.T_LPAREN, 
						new String(KwdLParen)));
		res.event = new Event();
		while ((ttype = tokenizer.nextToken()) != Tokenizer.T_RPAREN) {
		    if (ttype != Tokenizer.T_ID && ttype != Tokenizer.T_STR)
			throw(new SENPInvalidFormat(Tokenizer.T_ID, ErrAttrName));
		    name = new String(tokenizer.sval);
		    if (tokenizer.nextToken() != Tokenizer.T_OP 
			|| tokenizer.oval != AttributeConstraint.EQ)
			throw(new SENPInvalidFormat(Tokenizer.T_OP,
						    new String(KwdEquals)));
		    res.event.putAttribute(name, readAttribute(tokenizer));
		}
		return res;
	    }
	default:
	    throw(new SENPInvalidFormat("expecting `event' or `filter'"));
	}
    }
}




