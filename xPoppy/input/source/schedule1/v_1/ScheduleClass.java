/*  -*- Last-Edit:  Wed May 7 10:12:52 1993 by Monica; -*- */
/*  -*- Java translation by Raul Santelices, GA Tech, Feb 2006 -*- */


import java.io.File;
import java.io.FileReader;
import java.lang.System;

//import com.braju.beta.format.*;
//import com.braju.beta.lang.FloatVariable;
//import com.braju.beta.lang.IntegerVariable;


/* A job descriptor. */

public class ScheduleClass {
	static final int NULL = 0;
	
	static final int NEW_JOB        = 1;
	static final int UPGRADE_PRIO   = 2;
	static final int BLOCK          = 3;
	static final int UNBLOCK        = 4; 
	static final int QUANTUM_EXPIRE = 5;
	static final int FINISH         = 6;
	static final int FLUSH          = 7;
	
	static final int MAXPRIO = 3;
	
	static class Ele {
	    Ele		next, prev;	/* Next and Previous in job list. */
	    int		val;		/* Id-value of program. */
	    short	priority;	/* Its priority. */
	};
	
	static class List		/* doubly linked list */
	{
	  Ele first;
	  Ele last;
	  int mem_count;		/* member count */
	}
	
	/*-----------------------------------------------------------------------------
	  new_ele
	     alloates a new element with value as num.
	-----------------------------------------------------------------------------*/
	static Ele new_ele(int new_num)
	{	
	    Ele ele;
	
	    ele = new Ele();
	    ele.next = null;
	    ele.prev = null;
	    ele.val  = new_num;
	    return ele;
	}
	
	/*-----------------------------------------------------------------------------
	  new_list
	        allocates, initializes and returns a new list.
	        Note that if the argument compare() is provided, this list can be
	            made into an ordered list. see insert_ele().
	-----------------------------------------------------------------------------*/
	static List new_list()
	{
	    List list;
	
	    list = new List();
	    
	    list.first = null;
	    list.last  = null;
	    list.mem_count = 0;
	    return (list);
	}
	
	/*-----------------------------------------------------------------------------
	  append_ele
	        appends the new_ele to the list. If list is null, a new
		list is created. The modified list is returned.
	-----------------------------------------------------------------------------*/
	static List append_ele(List a_list, Ele a_ele)
	{
	  if (a_list == null)
	      a_list = new_list();	/* make list without compare function */
	
	  a_ele.prev = a_list.last;	/* insert at the tail */
	  if (a_list.last != null)
	    a_list.last.next = a_ele;
	  else
	    a_list.first = a_ele;
	  a_list.last = a_ele;
	  a_ele.next = null;
	  a_list.mem_count++;
	  return (a_list);
	}
	
	/*-----------------------------------------------------------------------------
	  find_nth
	        fetches the nth element of the list (count starts at 1)
	-----------------------------------------------------------------------------*/
	static Ele find_nth(List f_list, int n)
	{
	    Ele f_ele;
	    int i;
	
	    if (f_list == null)
		return null;
	    f_ele = f_list.first;
	    for (i=1; (f_ele != null) && (i<n); i++)
		f_ele = f_ele.next;
	    return f_ele;
	}
	
	/*-----------------------------------------------------------------------------
	  del_ele
	        deletes the old_ele from the list.
	        Note: even if list becomes empty after deletion, the list
		      node is not deallocated.
	-----------------------------------------------------------------------------*/
	static List del_ele(List d_list, Ele d_ele)
	{
	    if (d_list == null || d_ele == null)
		return (null);
	    
	    if (d_ele.next != null)
		d_ele.next.prev = d_ele.prev;
	    else
		d_list.last = d_ele.prev;
	    if (d_ele.prev != null)
		d_ele.prev.next = d_ele.next;
	    else
		d_list.first = d_ele.next;
	    /* KEEP d_ele's data & pointers intact!! */
	    d_list.mem_count--;
	    return (d_list);
	}
	
	/*-----------------------------------------------------------------------------
	   free_ele
	       deallocate the ptr. Caution: The ptr should point to an object
	       allocated in a single call to malloc.
	-----------------------------------------------------------------------------*/
	static void free_ele(Ele ptr)
	{
	    //free(ptr);
	}
	
	static int alloc_proc_num;
	static int num_processes;
	static Ele cur_proc;
	static List[] prio_queue = new List[MAXPRIO+1]; 	/* 0th element unused */
	static List block_queue;
	
	static void
	finish_process()
	{
	    schedule();
	    if (cur_proc != null)
	    {
		System.out.print(cur_proc.val + " ");// fprintf(stdout, "%d ", cur_proc.val);
		cur_proc = null;//free_ele(cur_proc);
		num_processes--;
	    }
	}
	
	static void
	finish_all_processes()
	{
	    int i;
	    int total;
	    total = num_processes;
	    for (i=0; i<total; i++)
		finish_process();
	}
	
	static void schedule()
	{
	    int i;
	    
	    cur_proc = null;
	    for (i=MAXPRIO; i > 0; i--)
	    {
		if (prio_queue[i].mem_count > 0)
		{
		    cur_proc = prio_queue[i].first;
		    prio_queue[i] = del_ele(prio_queue[i], cur_proc);
		    return;
		}
	    }
	}
	
	static void
	upgrade_process_prio(int prio, float ratio)
	{
	    int count;
	    int n;
	    Ele proc;
	    List src_queue;
	    List dest_queue;
	    
	    if (prio >= MAXPRIO)
		return;
	    src_queue = prio_queue[prio];
	    dest_queue = prio_queue[prio+1];
	    count = src_queue.mem_count;
	
	    if (count > 0)
	    {
		n = (int) (count*ratio + 1);
		proc = find_nth(src_queue, n);
		if (proc != null) {
		    src_queue = del_ele(src_queue, proc);
		    /* append to appropriate prio queue */
		    proc.priority = (short)prio;
		    dest_queue = append_ele(dest_queue, proc);
		}
	    }
	}
	
	static void
	unblock_process(float ratio)
	{
	    int count;
	    int n;
	    Ele proc;
	    int prio;
	    if (block_queue != null)
	    {
		count = block_queue.mem_count;
		n = (int) (count*ratio + 1);
		proc = find_nth(block_queue, n);
		if (proc != null) {
		    block_queue = del_ele(block_queue, proc);
		    /* append to appropriate prio queue */
		    prio = proc.priority;
		    prio_queue[prio] = append_ele(prio_queue[prio], proc);
		}
	    }
	}
	
	static void quantum_expire()
	{
	    int prio;
	    schedule();
	    if (cur_proc != null)
	    {
		prio = cur_proc.priority;
		prio_queue[prio] = append_ele(prio_queue[prio], cur_proc);
	    }	
	}
		
	static void
	block_process()
	{
	    schedule();
	    if (cur_proc != null)
	    {
		block_queue = append_ele(block_queue, cur_proc);
	    }
	}
	
	static Ele  new_process(int prio)
	{
	    Ele proc;
	    proc = new_ele(alloc_proc_num++);
	    proc.priority = (short)prio;
	    num_processes++;
	    return proc;
	}
	
	static void add_process(int prio)
	{
	    Ele proc;
	    proc = new_process(prio);
	    prio_queue[prio] = append_ele(prio_queue[prio], proc);
	}
	
	static void init_prio_queue(int prio, int num_proc)
	{
	    List queue;
	    Ele  proc;
	    int i;
	    
	    queue = new_list();
	    for (i=0; i<num_proc; i++)
	    {
		proc = new_process(prio);
		queue = append_ele(queue, proc);
	    }
	    prio_queue[prio] = queue;
	}
	
	static void initialize()
	{
	    alloc_proc_num = 0;
	    num_processes = 0;
	}
	
	/* test driver */
	public static void main(String[] args) {
		//int argc;
		//char *argv[];
	    /*int*/ C_stdlib.IntWrapper command = new C_stdlib.IntWrapper();
	    /*int*/ C_stdlib.IntWrapper prio = new C_stdlib.IntWrapper();
	    /*float*/ C_stdlib.FloatWrapper ratio = new C_stdlib.FloatWrapper();
	    int status;
	
	    /* after translation, file is now a parameter instead of stdin, but args in doesn't contain exe name as in C */
	    if (args.length < MAXPRIO+1)
	    {
		System.out.print("incorrect usage\n");//fprintf(stdout, "incorrect usage\n");
		return;
	    }
	    
	    initialize();
	    int _prio; /* temporal after translation to Java */
	    for (_prio=MAXPRIO; _prio >= 1; _prio--)
	    {
	    	/* changed '_prio' to '_prio - 1' in translation from C to Java */
	    	int _prio_val = 0; /* added for translation to Java */
	    	try { _prio_val = Integer.parseInt(args[_prio - 1]); } catch (Exception e) { }
	    	init_prio_queue(_prio, _prio_val);//atoi(args[_prio])); 
	    }
		/* added in translation from C to Java */
	    // read whole file into string
	    final int inlen = (int)(new File(args[args.length - 1])).length();
	    char[] inchars = new char[inlen];
	    try {
	    	(new FileReader(args[args.length - 1])).read(inchars);
		    C_stdlib.StringIterator strIt = new C_stdlib.StringIterator(new String(inchars));
		    for (status = C_stdlib.scanInt(strIt, command);//status = fscanf(stdin, "%d", &command);
		    		((status!=C_stdlib.EOF) && status != 0);//((status!=EOF) && status)
		    		status = C_stdlib.scanInt(strIt, command))//status = fscanf(stdin, "%d", &command))
		    {
				switch(command.getValue())
				{
					case FINISH:
					    finish_process();
					    break;
					case BLOCK:
					    block_process();
					    break;
					case QUANTUM_EXPIRE:
					    quantum_expire();
					    break;
					case UNBLOCK:
						C_stdlib.scanFloat(strIt, ratio);//fscanf(stdin, "%f", &ratio);
					    unblock_process(ratio.getValue());
					    break;
					case UPGRADE_PRIO:
						C_stdlib.scanInt(strIt, prio);//fscanf(stdin, "%d", &prio);
						C_stdlib.scanFloat(strIt,ratio); //fscanf(stdin, "%f", &ratio);
					    if (prio.getValue() > MAXPRIO || prio.getValue() <= 0) { 
					    	System.out.print("** invalid priority\n");//fprintf(stdout, "** invalid priority\n");
					    	return;
					    }
					    else 
						upgrade_process_prio(prio.getValue(), ratio.getValue());
					    break;
					case NEW_JOB:
						C_stdlib.scanInt(strIt, prio);//fscanf(stdin, "%d", &prio);
					    if (prio.getValue() > MAXPRIO || prio.getValue() <= 0) {
					    	System.out.print("** invalid priority\n");//fprintf(stdout, "** invalid priority\n");
					    	return;
					    }
					    else 
						add_process(prio.getValue());
					    break;
					case FLUSH:
					    finish_all_processes();
					    break;
				}
		    }
	    }
	    catch (Exception e) {
	    	e.printStackTrace(System.err);
	    }
	}
}

/* Note for Java translation: stdin replaced with 4th parameter that specifies command file
  
  A simple input spec:  
  
  a.out n3 n2 n1

  where n3, n2, n1 are non-negative integers indicating the number of
  initial processes at priority 3, 2, and 1, respectively.

  The input file is a list of commands of the following kinds:
   (For simplicity, comamnd names are integers (NOT strings)
    
  FINISH            ;; this exits the current process (printing its number)
  NEW_JOB priority  ;; this adds a new process at specified priority
  BLOCK             ;; this adds the current process to the blocked queue
  QUANTUM_EXPIRE    ;; this puts the current process at the end
                    ;;      of its prioqueue
  UNBLOCK ratio     ;; this unblocks a process from the blocked queue
                    ;;     and ratio is used to determine which one

  UPGRADE_PRIO small-priority ratio ;; this promotes a process from
                    ;; the small-priority queue to the next higher priority
                    ;;     and ratio is used to determine which process
 
  FLUSH	            ;; causes all the processes from the prio queues to
                    ;;    exit the system in their priority order

where
 NEW_JOB        1
 UPGRADE_PRIO   2 
 BLOCK          3
 UNBLOCK        4  
 QUANTUM_EXPIRE 5
 FINISH         6
 FLUSH          7
and priority is in        1..3
and small-priority is in  1..2
and ratio is in           0.0..1.0

 The output is a list of numbers indicating the order in which
 processes exit from the system.   

*/
