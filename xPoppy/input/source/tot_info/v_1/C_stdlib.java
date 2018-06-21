import java.io.BufferedReader;
import java.io.IOException;



public class C_stdlib {
	public static class StringIterator {
		private String strSource;
		private int pos;
		public StringIterator(String source) {
			this.strSource = source;
			pos = 0;
		}
		public int getChar() {
			return (pos < strSource.length())? strSource.charAt(pos) : -1; 
		}
		public void goNext() {
			if (pos < strSource.length())
				++pos;
		}
		public void goPrev() {
			if (pos > 0)
				--pos;
		}
		public boolean atEnd() {
			return pos == strSource.length(); 
		}
	}
	
	public static class IntWrapper {
		private int value;
		
		public IntWrapper() {}
		public IntWrapper(int i) { value = i; }
		
		public int getValue() { return value; }
		public void setValue(int i) { value = i; }
	}
	public static class FloatWrapper {
		private float value;
		
		public FloatWrapper() {}
		public FloatWrapper(float f) { value = f; }
		
		public float getValue() { return value; }
		public void setValue(float f) { value = f; }
	}
	
	public static final int EOF = -1;
	
	public static int scanInt(StringIterator si, IntWrapper dest) {
		// trim leading whitespace first
		int c;
		while ((c = si.getChar()) == ' ' ||
				c == '\t' ||
				c == '\n' ||
				c == '\r' ||
				c == '\f')
			si.goNext();
		
		return scanIntNoWhitespace(si, dest, true);
	}
	/**
	 * Implements the behavior of the C expression sscanf(source,"%d",dest). Skips all whitespace before first
	 * integer character. First character can be the sign (+ or -). If no integer is found (i.e. return value is 0),
	 * then 'dest' is left unchanged.
	 * @param si
	 * @param dest
	 * @param allowSign
	 * @return EOF (-1) if cannot read more; 0 if failed to find integer (an  
	 */
	private static int scanIntNoWhitespace(StringIterator si, IntWrapper dest, boolean allowSign) {
		if (si.atEnd())
			return EOF;
		
		String sInt = "";
		
		// check sign, if allowed
		int signLen = 0;
		int c;
		if (allowSign) {
			c = si.getChar();
			if ((c == '-') || (c == '+')) {
				sInt += (char)c;
				si.goNext();
				signLen = 1;
			}
		}
		
		int numDigits = 0;
		while ((c = si.getChar()) >= '0' && c <= '9') {
			sInt += (char)c;
			++numDigits;
			si.goNext();
		}
		if (numDigits > 0)
			dest.setValue(Integer.parseInt(sInt));
		else if (signLen != 0) {
			// invalid integer; backtrack to read sign's position
//			assert signLen == 1;
			si.goPrev(); // backtrack 1 character
			return 0; 
		}
		
//		return signLen + numDigits;
		return 1;
	}

	/* fgets:  get at most n chars from iop */
	public static char[] fgets(char[] s, int n, BufferedReader iop)
	{
		return fgets(s, 0, n, iop);
	}
	
	/** Specialized version of fgets which takes in the starting index from which to 
	 * fill the buffer. This is necessary because we can't pass a pointer to the starting
	 * location in Java similar to C
	 */
	public static char[] fgets(char[] s, int start_idx, int n, BufferedReader iop)
	{
		int c = -1;
		int cs = start_idx;

		try {
			while (--n > 0 && (c = iop.read()) != -1)
				if ((s[cs++] = (char) c) == '\n')
					break;
		} catch (IOException e) {
			return null;
		}
		s[cs] = '\0';
		return (c == -1 && cs == start_idx) ? null : s;
	}
	   
	/**
	 * Implements the behavior of the C expression scanf(source,"%f",dest), except for the exponent part. So
	 * it works for fixed-point numbers encoded in string.
	 * @param si
	 * @param dest
	 * @return
	 */
	public static int scanFloat(StringIterator si, FloatWrapper dest) {
		if (si.atEnd())
			return EOF;
		
		// read integer part
		IntWrapper intPart = new IntWrapper(0); // init to 0, in case int part is not provided
		int intLen = scanInt(si, intPart); // remove leading whitespace
		if (intLen < 0)
			return intLen;
		if (si.getChar() != '.') {
			dest.setValue((float)intPart.getValue());
			return intLen;
		}
		// read mantissa
		si.goNext();
		IntWrapper mantPart = new IntWrapper(0);
		int mantLen = Math.max(scanIntNoWhitespace(si, mantPart, false), 0); // no leading whitespace or sign accepted
		dest.setValue((float)intPart.getValue() +
						(float)mantPart.getValue() / (float)(Math.pow(10.0, (double)mantLen)));
		
		return intLen + mantLen;
	}
}
