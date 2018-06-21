import java.io.*;

/*
	tot_info -- combine information statistics for multiple tables

	last edit:	89/02/06	D A Gwyn

	SCCS ID:	@(#)tot_info.c	1.1 (edited for publication)
 */

public class tot_info {
	public static final int MAXLINE	= 256;

	public static int MAXTBL = 1000;
	public static final int EXIT_FAILURE = 1;
	public static final int EXIT_SUCCESS = 0;
	
	static char[] line = new char[MAXLINE];		/* row/column header input line */
	static long[] f = new long[MAXTBL];		/* frequency tallies */
	static int	r;			/* # of rows */
	static int	c;			/* # of columns */

//	#define	x(i,j)	f[(i)*c+(j)]		/* convenient way to access freqs */

	public static final char COMMENT = '#';			/* comment character */

	/*ARGSUSED*/
	public static void main(String[] argv ) {
		char[]	p;		/* input line scan location */
		int	i;		/* row index */
		int	j;		/* column index */
		double		info;		/* computed information measure */
		C_stdlib.IntWrapper infodf = new C_stdlib.IntWrapper();		/* degrees of freedom for information */
		double		totinfo = 0.0;	/* accumulated information */
		int		totdf;	/* accumulated degrees of freedom */
//		Formatter formatter = new Formatter();
		
		totdf = 0;
		BufferedReader fp = null;
//		try {
//			fp = new BufferedReader(
//					new FileReader("/home/kthakar/tot_info-java/inputs/universe/tst13"));
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		}
		fp = new BufferedReader(new InputStreamReader(System.in));
		while ( C_stdlib.fgets( line, MAXLINE, fp ) != null )	/* start new table */
		{
			int idx = 0;
			for ( p = line; p[idx] != '\0' && Character.isWhitespace(p[idx]); idx++ )
				;

			if ( p[idx] == '\0' )
				continue;	/* skip blank line */

			if ( p[idx] == COMMENT )
			{		/* copy comment through */
				System.out.print( szString(line) );
				continue;
			}

			C_stdlib.StringIterator si = 
				new C_stdlib.StringIterator(new String(p));
			C_stdlib.IntWrapper rInteger = new C_stdlib.IntWrapper();
			C_stdlib.IntWrapper cInteger = new C_stdlib.IntWrapper();
			if (C_stdlib.scanInt(si, rInteger) + C_stdlib.scanInt(si, cInteger) < 2) 
//			if ( sscanf( p, "%d %d\n", &r, &c ) != 2 )
			{
				System.out.println( "* invalid row/column line *\n");
//				return EXIT_FAILURE;
				return;
			}
			r = rInteger.getValue();
			c = cInteger.getValue();

			if ( r * c > MAXTBL )
			{
				System.out.print( "* table too large *\n");
//				return EXIT_FAILURE;
				return;
			}
			
			si = null;
			/* input tallies */
			for ( i = 0; i < r; ++i ) {
				for ( j = 0; j < c; ++j ) {
					if (si == null || si.atEnd()) {
						String str = readNextToken(fp);
						si = new C_stdlib.StringIterator(str);
					}
					C_stdlib.IntWrapper iw = 
						new C_stdlib.IntWrapper();

					if ( ( C_stdlib.scanInt(si, iw) != 1 ))
					{
						System.out.print( "* EOF in table *\n");
						return;
					}
					f[(i)*c+(j)] = iw.getValue();
				}
			}

			/* compute statistic */

			info = InfoTbl( r, c, f, infodf);

			/* print results */

			if ( info >= 0.0 )
			{
				System.out.format("2info = %5.2f\tdf = %2d\tq = %7.4f\n",
						new Object[] {new Double(info), new Integer(infodf.getValue()),
						new Double(QChiSq( info, infodf.getValue())) }
				);
				totinfo += info;
				totdf += infodf.getValue();
			}
			else
				System.out.print( info < -3.5 ? "out of memory\n"
						: info < -2.5 ? "table too small\n"
								: info < -1.5 ? "negative freq\n"
										: "table all zeros\n");
		}

		if ( totdf <= 0 ) {
			System.out.print( "\n*** no information accumulated ***\n");
			return ;
		}

		System.out.format("\ntotal 2info = %5.2f\tdf = %2d\tq = %7.4f\n",
				new Object[] {new Double(totinfo), new Integer(totdf),
				new Double(QChiSq( totinfo, totdf ))}
		);
		return ;
	}

	private static String readNextToken(BufferedReader fp) {
		StringBuffer sb = new StringBuffer();
		boolean leadingSpaceConsumed = false;
		try {
			int ch = fp.read();
			while(((char)ch != ' ' && (char)ch != '\n') || !leadingSpaceConsumed) {
				sb.append((char)ch);
				if ((char)ch != ' ')
					leadingSpaceConsumed = true;
				if (ch == -1) {
					return ""; //EOF
				}
				ch = fp.read();
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

	private static String szString(char[] tok) {
		return szString(tok, 0);
	}
	private static String szString(char[] tok, int start_ind) {
		StringBuffer sb = new StringBuffer();
		int i = start_ind;
    	while(tok[i] != '\0')
			sb.append(tok[i++]);
    	return sb.toString();
	}
	
	/*
	Gamma -- gamma and related functions

	last edit:	88/09/09	D A Gwyn

	SCCS ID:	@(#)gamma.c	1.1 (edited for publication)

Acknowledgement:
	Code based on that found in "Numerical Methods in C".
	 */
	static double
	LGamma(double x )	{
		double[] cof =
		{
			76.18009173,	-86.50532033,	24.01409822,
			-1.231739516,	0.120858003e-2,	-0.536382e-5
		};
		double			tmp, ser;
		int		j;


		if ( --x < 0.0 )	/* use reflection formula for accuracy */
		{
			double	pix = Math.PI * x;

			return Math.log( pix / Math.sin( pix ) ) - LGamma( 1.0 - x );
		}

		tmp = x + 5.5;
		tmp -= (x + 0.5) * Math.log( tmp );

		ser = 1.0;

		for ( j = 0; j < 6; ++j )
			ser += cof[j] / ++x;

		return -tmp + Math.log( 2.50662827465 * ser );
	}

	public static final int ITMAX = 100;
	public static final double EPS = 3.0e-7;

	static double
	gser(double a, double x )
	{
		double		ap, del, sum;
		int	n;

		if ( x <= 0.0 )
			return 0.0;

		del = sum = 1.0 / (ap = a);

		for ( n = 1; n <= ITMAX; ++n )
		{
			sum += del *= x / ++ap;

			if ( Math.abs( del ) < Math.abs( sum ) * EPS )
				return sum * Math.exp( -x + a * Math.log( x ) - LGamma( a ) );
		}
		/*NOTREACHED*/
		return -1.0;
	}

	static double
	gcf(double a,double x )
	{
		int	n;
		double		gold = 0.0, fac = 1.0, b1 = 1.0,
		b0 = 0.0, a0 = 1.0, a1 = x;

		for ( n = 1; n <= ITMAX; ++n )
		{
			double	anf;
			double	an = (double)n;
			double	ana = an - a;

			a0 = (a1 + a0 * ana) * fac;
			b0 = (b1 + b0 * ana) * fac;
			anf = an * fac;
			b1 = x * b0 + anf * b1;
			a1 = x * a0 + anf * a1;

			if ( a1 != 0.0 )
			{		/* renormalize */
				double	g = b1 * (fac = 1.0 / a1);

				gold = g - gold;

				if ( Math.abs( gold ) < EPS * Math.abs( g ) )
					return Math.exp( -x + a * Math.log( x ) - LGamma( a ) ) * g;

				gold = g;
			}
		}
		return a1;

		/*NOTREACHED*/
	}

	static double
	QGamma(double a,double x )
	{

		return x < a + 1.0 ? 1.0 - gser( a, x ) : gcf( a, x );
	}

	static double
	QChiSq(double chisq,int df )
	{
		return QGamma( (double)df / 2.0, chisq / 2.0 );
	}


	/*
	InfoTbl -- Kullback's information measure for a 2-way contingency table

	last edit:	88/09/19	D A Gwyn

	SCCS ID:	@(#)info.c	1.1 (edited for publication)

	Special return values:
		-1.0	entire table consisted of 0 entries
		-2.0	invalid table entry (frequency less than 0)
		-3.0	invalid table dimensions (r or c less than 2)
		-4.0	unable to allocate enough working storage
	 */


	static double
	InfoTbl(int r,int c,final long[] f, C_stdlib.IntWrapper pdf ) {
	/* # rows in table */
	/* # columns in table */
	/* -> r*c frequency tallies */
	/* -> return # d.f. for chi-square */
		int	i;		/* row index */
		int	j;		/* column index */
		double		N;		/* (double)n */
		double		info;		/* accumulates information measure */
		double[] xi;		/* row sums */
		double[] xj;		/* col sums */
		int		rdf = r - 1;	/* row degrees of freedom */
		int		cdf = c - 1;	/* column degrees of freedom */

		if ( rdf <= 0 || cdf <= 0 )
		{
			info = -3.0;
			return info;
		}

		pdf.setValue(rdf * cdf);		/* total degrees of freedom */

		if ( (xi = new double[r]) == null)
		{
			info = -4.0;
			return info;
		}

		if ( (xj = new double[c]) == null)
		{
			info = -4.0;
			return info;
		}

		/* compute row sums and total */

		N = 0.0;

		for ( i = 0; i < r; ++i )
		{
			double	sum = 0.0;	/* accumulator */

			for ( j = 0; j < c; ++j )
			{
				long	k = f[(i)*c+(j)];

				if ( k < 0L )
				{
					info = -2.0;
					return info;
				}

				sum += (double)k;
			}

			N += xi[i] = sum;
		}

		if ( N <= 0.0 )
		{
			info = -1.0;
			return info;
		}

		/* compute column sums */

		for ( j = 0; j < c; ++j )
		{
			double	sum = 0.0;	/* accumulator */

			for ( i = 0; i < r; ++i )
				sum += (double)f[(i)*c+(j)];

			xj[j] = sum;
		}

		/* compute information measure (four parts) */

		info = N * Math.log( N );					/* part 1 */

		for ( i = 0; i < r; ++i )
		{
			double	pi = xi[i];	/* row sum */

			if ( pi > 0.0 )
				info -= pi * Math.log( pi );			/* part 2 */

			for ( j = 0; j < c; ++j )
			{
				double	pij = (double)f[(i)*c+(j)];

				if ( pij > 0.0 )
					info += pij * Math.log( pij );	/* part 3 */
			}
		}

		for ( j = 0; j < c; ++j )
		{
			double	pj = xj[j];	/* column sum */

			if ( pj > 0.0 )
				info -= pj * Math.log( pj );			/* part 4 */
		}

		info *= 2.0;			/* for comparability with chi-square */

//		ret1:
//			free( (pointer)xj );
//		ret2:
//			free( (pointer)xi );
//		ret3:
			return info;
	}
}