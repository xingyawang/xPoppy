public class tcas {
#ifdef F_T_HD_13
	public static int OLEV=700;
#else
	public static int OLEV=600;
#endif
#ifdef F_T_HD_14
	public static int MAXALTDIFF=650;
#else
	public static int MAXALTDIFF=600;
#endif
#ifdef F_T_HD_15
	public static int MINSEP=650;
#else
	public static int MINSEP=300;
#endif
	public static int NOZCROSS=100;
	

	public static int Cur_Vertical_Sep;
	public static boolean High_Confidence;
	public static boolean Two_of_Three_Reports_Valid;

	public static int Own_Tracked_Alt;
	public static int Own_Tracked_Alt_Rate;
	public  static int Other_Tracked_Alt;

	public static int Alt_Layer_Value;		/* 0, 1, 2, 3 */
	//public static int[] Positive_RA_Alt_Thresh;
	static int Positive_RA_Alt_Thresh_0;
	static int Positive_RA_Alt_Thresh_1;
	static int Positive_RA_Alt_Thresh_2;
	static int Positive_RA_Alt_Thresh_3;

	public static int Up_Separation;
	public  static int Down_Separation;
	

	/* state variables */
	public  static int Other_RAC;			/* NO_INTENT, DO_NOT_CLIMB, DO_NOT_DESCEND */
	public static int NO_INTENT=0;
	public static int DO_NOT_CLIMB= 1;
	public static int DO_NOT_DESCEND=2;
	
	public static int Other_Capability;		/* TCAS_TA, OTHER */
	public static int TCAS_TA=1;
	public static int OTHER=2;
	
	public static int Climb_Inhibit;		/* true/false */
	
	public static int UNRESOLVED=0;
	public static int UPWARD_RA=1;
#ifdef F_T_HD_36
	public static int DOWNWARD_RA=1;
#else
	public static int DOWNWARD_RA=2;
#endif
	void initialize()
	{
#ifdef F_T_HD_16
		Positive_RA_Alt_Thresh_0= 401;
#else
	    Positive_RA_Alt_Thresh_0= 400;
#endif
#ifdef F_T_HD_7
	    Positive_RA_Alt_Thresh_1= 550;
#elif F_T_HD_17
	    Positive_RA_Alt_Thresh_1= 501;
#else
	    Positive_RA_Alt_Thresh_1= 500;
#endif
#ifdef F_T_HD_18
	    Positive_RA_Alt_Thresh_2= 690;
#else
	    Positive_RA_Alt_Thresh_2= 640;
#endif
#ifdef F_T_HD_8
	    Positive_RA_Alt_Thresh_3= 700;
#elif F_T_HD_19
	    Positive_RA_Alt_Thresh_3= 760;
#else
	    Positive_RA_Alt_Thresh_3= 740;
#endif
	}
	int ALIM ()
	{
#ifdef F_T_HD_37
		// if(Alt_Layer_Value==0) 
			return Positive_RA_Alt_Thresh_0;
		// else if(Alt_Layer_Value==1) 
		// 	return Positive_RA_Alt_Thresh_1;
		// else if(Alt_Layer_Value==2) 
		// 	return Positive_RA_Alt_Thresh_2;
		// else 
		// 	return Positive_RA_Alt_Thresh_3;
#else
		if(Alt_Layer_Value==0) 
			return Positive_RA_Alt_Thresh_0;
		else if(Alt_Layer_Value==1) 
			return Positive_RA_Alt_Thresh_1;
		else if(Alt_Layer_Value==2) 
			return Positive_RA_Alt_Thresh_2;
		else 
			return Positive_RA_Alt_Thresh_3;
#endif
	}

	 int  Inhibit_Biased_Climb ()
	{
#ifdef F_T_HD_28
		if(Climb_Inhibit==0)
#elif F_T_HD_35
		if(Climb_Inhibit<=0)
#else
		if(Climb_Inhibit >0)
#endif
#ifdef F_T_HD_2
			return Up_Separation + MINSEP;
#elif F_T_HD_29
			return Up_Separation;
#else
			return Up_Separation + NOZCROSS;
#endif
		else 
#ifdef F_T_HD_30
			return Up_Separation + NOZCROSS;
#else
			return Up_Separation;
#endif
	    //return (Climb_Inhibit ? Up_Separation + NOZCROSS : Up_Separation);
	}

	 boolean Non_Crossing_Biased_Climb()
	{
	    int upward_preferred;
	    int upward_crossing_situation;
	    boolean result;
#ifdef F_T_HD_20
	    if(Inhibit_Biased_Climb() >= Down_Separation){
#elif F_T_HD_21
	    if((Up_Separation + NOZCROSS) > Down_Separation){
#elif F_T_HD_22
	    if(Up_Separation > Down_Separation){
#else
	    if(Inhibit_Biased_Climb() > Down_Separation){
#endif
	    	upward_preferred=1;
	    }
	    else{
	    	upward_preferred=0;
	    }
	    if (upward_preferred!=0)
	    {
	
#ifdef F_T_HD_1
		result = !(Own_Below_Threat()) || ((Own_Below_Threat()) && (!(Down_Separation > ALIM()))); 
#elif F_T_HD_40
		result = ((Own_Below_Threat()) && (!(Down_Separation >= ALIM()))); 
#else
		result = !(Own_Below_Threat()) || ((Own_Below_Threat()) && (!(Down_Separation >= ALIM()))); 
#endif
	    }
	    else
	    {	
#ifdef F_T_HD_4
	    result = Own_Above_Threat() && (Cur_Vertical_Sep >= MINSEP) || (Up_Separation >= ALIM());
#elif F_T_HD_41
	    result = (Cur_Vertical_Sep >= MINSEP) && (Up_Separation >= ALIM());
#else
		result = Own_Above_Threat() && (Cur_Vertical_Sep >= MINSEP) && (Up_Separation >= ALIM());
#endif		
	    }
	    return result;
	}

	boolean Non_Crossing_Biased_Descend()
	{
	    int upward_preferred;
	    int upward_crossing_situation;
	    boolean result;
#ifdef F_T_HD_9
	    if(Inhibit_Biased_Climb() >= Down_Separation){
#elif F_T_HD_23
	    if( (Up_Separation + NOZCROSS) > Down_Separation){
#elif F_T_HD_24
	    if(Up_Separation > Down_Separation){
#else
	    if(Inhibit_Biased_Climb() > Down_Separation){
#endif
	    	upward_preferred=1;
	    }
	    else{
	    	upward_preferred=0;
	    }
	    //upward_preferred = Inhibit_Biased_Climb() > Down_Separation;
	    if (upward_preferred!=0)
	    {
		result = Own_Below_Threat() && (Cur_Vertical_Sep >= MINSEP) && (Down_Separation >= ALIM());
	    }
	    else
	    {
#ifdef F_T_HD_25
	    result = !(Own_Above_Threat()) || ((Own_Above_Threat()) && (Up_Separation > ALIM()));
#elif F_T_HD_39
	    result = !(Own_Above_Threat()) || ((Own_Above_Threat()) && (Up_Separation > ALIM()));
#else
		result = !(Own_Above_Threat()) || ((Own_Above_Threat()) && (Up_Separation >= ALIM()));
#endif
	    }
	    return result;
	}

	 boolean Own_Below_Threat()
	{
#ifdef F_T_HD_6
		return (Own_Tracked_Alt <=Other_Tracked_Alt);
#else	
	    return (Own_Tracked_Alt < Other_Tracked_Alt);
#endif
	}

	 boolean Own_Above_Threat()
	{
#ifdef F_T_HD_10
		return (Other_Tracked_Alt <= Own_Tracked_Alt);
#else
	    return (Other_Tracked_Alt < Own_Tracked_Alt);
#endif
	}

	 int alt_sep_test()
	{
	    boolean enabled, tcas_equipped, intent_not_known;
	    boolean need_upward_RA, need_downward_RA;
	    int alt_sep;

#ifdef F_T_HD_5
	    enabled = High_Confidence && (Own_Tracked_Alt_Rate <= OLEV);// && (Cur_Vertical_Sep > MAXALTDIFF);
#elif F_T_HD_12
 		enabled = High_Confidence || (Own_Tracked_Alt_Rate <= OLEV) && (Cur_Vertical_Sep > MAXALTDIFF);
#elif F_T_HD_26
 		enabled = High_Confidence && (Cur_Vertical_Sep > MAXALTDIFF);
#elif F_T_HD_27
 		enabled = High_Confidence && (Own_Tracked_Alt_Rate <= OLEV) ;
#else
	    enabled = High_Confidence && (Own_Tracked_Alt_Rate <= OLEV) && (Cur_Vertical_Sep > MAXALTDIFF);
#endif
	    tcas_equipped = Other_Capability == TCAS_TA;
#ifdef F_T_HD_3
	    intent_not_known = Two_of_Three_Reports_Valid || Other_RAC == NO_INTENT;
#else
	    intent_not_known = Two_of_Three_Reports_Valid && Other_RAC == NO_INTENT;
#endif
	    
	    alt_sep = UNRESOLVED;

#ifdef F_T_HD_34
	    if (enabled && tcas_equipped && intent_not_known || !tcas_equipped)
#else	    
	    if (enabled && ((tcas_equipped && intent_not_known) || !tcas_equipped))
#endif
	    {
#ifdef F_T_HD_31
	    need_upward_RA = Non_Crossing_Biased_Climb();
#else
		need_upward_RA = Non_Crossing_Biased_Climb() && Own_Below_Threat();
#endif
#ifdef F_T_HD_32
		need_downward_RA = Non_Crossing_Biased_Descend();
#else
		need_downward_RA = Non_Crossing_Biased_Descend() && Own_Above_Threat();
#endif
#ifdef F_T_HD_11
		if (need_upward_RA)
#else
		if (need_upward_RA && need_downward_RA)
#endif
	        /* unreachable: requires Own_Below_Threat and Own_Above_Threat
	           to both be true - that requires Own_Tracked_Alt < Other_Tracked_Alt
	           and Other_Tracked_Alt < Own_Tracked_Alt, which isn't possible */
		    alt_sep = UNRESOLVED;
		else if (need_upward_RA)
		    alt_sep = UPWARD_RA;
		else if (need_downward_RA)
		    alt_sep = DOWNWARD_RA;
		else
		    alt_sep = UNRESOLVED;
	    }
	    
	    return alt_sep;
	}

        public static void main(String[] argv) {
            //int a1=0,a2=0,a3=0,a4=0,a5=0,a6=0,a7=0,a8=0,a9=0,a10=0,a11=0,a12=0;
            int argc=argv.length+1;
	    if(argc<13){
		System.out.println("Error: Command line arguments are");
		System.out.println("Cur_Vertical_Sep, High_Confidence, Two_of_Three_Reports_Valid");
		System.out.println("Own_Tracked_Alt, Own_Tracked_Alt_Rate, Other_Tracked_Alt");
		System.out.println("Alt_Layer_Value, Up_Separation, Down_Separation");
		System.out.println("Other_RAC, Other_Capability, Climb_Inhibit");
		System.exit(1);
	    }
            tcas newtcas=new tcas();
            newtcas.initialize();
            newtcas.Cur_Vertical_Sep =Integer.parseInt(argv[0]+"");
	    if(Integer.parseInt(argv[1]+"")==0){
	    	newtcas.High_Confidence=false;
	    }
	    else{
	    	newtcas.High_Confidence=true;
	    }
		if(Integer.parseInt(argv[2]+"")==0){
	    	newtcas.Two_of_Three_Reports_Valid=false;
	    }
	    else{
	    	newtcas.Two_of_Three_Reports_Valid=true;
	    }
	    newtcas.Own_Tracked_Alt = Integer.parseInt(argv[3]+"");
	    newtcas.Own_Tracked_Alt_Rate = Integer.parseInt(argv[4]+"");
	    newtcas.Other_Tracked_Alt = Integer.parseInt(argv[5]+"");
	    newtcas.Alt_Layer_Value = Integer.parseInt(argv[6]+"");
	    newtcas.Up_Separation = Integer.parseInt(argv[7]+"");
	    newtcas.Down_Separation = Integer.parseInt(argv[8]+"");
	    newtcas.Other_RAC = Integer.parseInt(argv[9]+"");
	    newtcas.Other_Capability = Integer.parseInt(argv[10]+"");
	    newtcas.Climb_Inhibit = Integer.parseInt(argv[11]+"");
            //System.out.println("begin");
	    System.out.println(newtcas.alt_sep_test());
	}

}

