package v0;

import java.io.*;

import v0.C_stdlib.FloatWrapper;
import v0.C_stdlib.IntWrapper;
import v0.C_stdlib.StringIterator;

public class Schedule2 {

    static final int MAXPRIO = 3;
    static final int MAXLOPRIO = 2;
    static final int BLOCKPRIO = 0;
    static final int CMDSIZE = 20;

    /* Scheduling commands */
    static final int  NEW_JOB = 1;
    static final int  UPGRADE_PRIO = 2;
    static final int  BLOCK = 3 ;
    static final int  UNBLOCK = 4;
    static final int  QUANTUM_EXPIRE = 5;
    static final int  FINISH = 6;
    static final int  FLUSH = 7;

        /* stati */
    static final int  OK = 0;
    static final int  TRUE = 1;
    static final int  FALSE = 0;
    static final int  BADNOARGS = -1 /* Wrong number of arguments */;
    static final int  BADARG = -2    /* Bad argument (< 0) */;
    static final int  MALLOC_ERR = -3;
    static final int  BADPRIO = -4   /* priority < 0 or > MAXPRIO */;
    static final int  BADRATIO = -5  /* ratio < 0 or > 1 */;
    static final int  NO_COMMAND = -6 /* No such scheduling command */;

    static class process
    {
        int pid;
        int priority;
        process next;
    }

    static class process_ptr
    {
        process pr;
        process_ptr(process pr) {
            this.pr = pr;
        }
    }

    static process current_job;
    static int next_pid = 0;

    static int enqueue(int prio, process new_process)
    {
        int status;
        //changed in translation
        status = put_end(prio, new_process);
#ifdef F_S2_HD_10
//      if(status != 0) return(status); /* Error */
#else
        if(status != 0) return(status); /* Error */
#endif
        return(reschedule(prio));
    }

    static class queue
    {
        int length;
        process head;
    };

    static queue[] prio_queue = new queue[MAXPRIO + 1]; /* blocked queue is [0] */


    public static void main(String[] argv)
    {
        IntWrapper command = new IntWrapper(0);
        IntWrapper prio = new IntWrapper(0);
        FloatWrapper ratio = new FloatWrapper((float)0.0);
        int nprocs, status, pid;
        process process;
        for (int i = 0; i < MAXPRIO+1; i++)
        	prio_queue[i] = new queue();

        int argc = argv.length;
        if(argc != MAXPRIO) exit_here(BADNOARGS);
        int _prio; /* temporal after translation to Java */
        for(_prio = MAXPRIO; _prio > 0; _prio--)
        {
            if((nprocs = Integer.parseInt(argv[MAXPRIO - _prio])) < 0)
            	exit_here(BADARG);
            for(; nprocs > 0; nprocs--)
            {
            	status = new_job(_prio);
                if(status != 0) exit_here(status);
            }
        }
        
        /* while there are commands, schedule it */
        try {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
//        BufferedReader in = new BufferedReader(new FileReader("/home/kunal/objects/Siemens/schedule2/inputs/input/ft.6"));
        while((status = get_command(command, prio, ratio, in)) > 0)
        {
            schedule(command.getValue(), prio.getValue(), ratio.getValue());
        }
        in.close();
        if(status < 0) exit_here(status); /* Real bad error */
        } catch(IOException e) {}
        exit_here(OK);
    }

    static int 
        get_command(IntWrapper command, IntWrapper prio, FloatWrapper ratio, BufferedReader
                file) throws IOException
    {
        int status = OK;
        char[] buf = new char[Schedule2.CMDSIZE];
        String buffer;
        buffer = file.readLine();
        if (buffer != null) {
#ifdef F_S2_HD_6
            prio.setValue(1);
#else
            prio.setValue(-1);
#endif
        	command.setValue(-1); 
            ratio.setValue((float)-1.0);
            StringIterator strIt = new StringIterator(buffer);
            C_stdlib.scanInt(strIt, command);
            switch(command.getValue()) {
            case NEW_JOB:
            	C_stdlib.scanInt(strIt, prio);
            	break;
            case UNBLOCK:
            	C_stdlib.scanFloat(strIt, ratio);
            	break;
            case UPGRADE_PRIO:
            	C_stdlib.scanInt(strIt, prio);
            	C_stdlib.scanFloat(strIt, ratio);
            	break;
            }
            /* Find end of  line of input if no EOF */
            //while(buf[strlen(buf)-1] != '\n' && fgets(buf, CMDSIZE, stdin));
//            line.close();
            return(TRUE);
        }
        else return(FALSE);
    }

    static void exit_here(int status)
    {
        System.exit(Math.abs(status));
    }


    static int 
        new_job(int prio) /* allocate new pid and process block. Stick at end */
    {
        int pid, status = OK;
#ifdef F_S2_HD_5
        process new_process;    if (prio < 1) return (BADPRIO);
#else
        process new_process;
#endif
        pid = next_pid++;
        new_process = new process();
        if(new_process == null) status = MALLOC_ERR;
        else
        {
            new_process.pid = pid;
            new_process.priority = prio;
            new_process.next = null;
            status = enqueue(prio, new_process);
            if(status != 0)
            {
                //free(new_process); /* Return process block */
                new_process = null;
            }
        }
        if(status != 0) next_pid--; /* Unsuccess. Restore pid */
        return(status);
    }

    static int upgrade_prio(int prio, float ratio) /* increment priority at ratio in queue */
    {
        int status;
        process job = new process();
        process_ptr jobptr = new process_ptr(job);
#ifdef F_S2_HD_1
//      if(prio < 1 || prio > MAXLOPRIO) return(BADPRIO); MISSING CODE        
#else
        if(prio < 1 || prio > MAXLOPRIO) return(BADPRIO);
#endif
        if((status = get_process(prio, ratio, jobptr)) <= 0) return(status);
        /* We found a job in that queue. Upgrade it */
        jobptr.pr.priority = prio + 1;
        return(enqueue(prio + 1, jobptr.pr));
    }

    static int
        block() /* Put current job in blocked queue */
        {
            process job;
            job = get_current();
            if(job != null)
            {
                current_job = null; /* remove it */
                return(enqueue(BLOCKPRIO, job)); /* put into blocked queue */
            }
            return(OK);
        }

    static int
        unblock(float ratio) /* Restore job @ ratio in blocked queue to its queue */
    {
        int status;
        process job = new process();
        process_ptr jobptr = new process_ptr(job);
        if((status = get_process(BLOCKPRIO, ratio, jobptr)) <= 0)
            return(status);
        /* We found a blocked process. Put it where it belongs. */
        return(enqueue(jobptr.pr.priority, jobptr.pr));
    }

    static int
        quantum_expire() /* put current job at end of its queue */
        {
            process job;
            job = get_current();
            if(job != null)
            {
                current_job = null; /* remove it */
                return(enqueue(job.priority, job));
            }
            return(OK);
        }

    static int
        finish() /* Get current job, print it, and zap it. */
        {
    		process job;
            job = get_current();
            if(job != null)
            {
                current_job = null;
                reschedule(0);
              //  fprintf(stdout, " %d", job.pid);
                System.out.print(" " + job.pid);
                //free(job);
                job = null;
                return(FALSE);
            }
            else return(TRUE);
        }

    static int
        flush() /* Get all jobs in priority queues & zap them */
        {
          //  while(!finish());
            while(finish() == 0);
    		System.out.print("\n");
            return(OK);
        }

    static process  
        get_current() /* If no current process, get it. Return it */
        {
            int prio;
            process_ptr pptr = new process_ptr(current_job);
            if(current_job == null)
            {
            	for(prio = MAXPRIO; prio > 0; prio--)
                { /* find head of highest queue with a process */
                    if(get_process(prio, (float)0.0, pptr) > 0) {
                    	break;
                    }
                }
            }
            current_job = pptr.pr;
            return(current_job);
        }

    static int
        reschedule(int prio) /* Put highest priority job into current_job */
    {
        if(current_job != null && prio > current_job.priority)
        {
            put_end(current_job.priority, current_job);
            current_job = null;
        }
        get_current(); /* Reschedule */
        return(OK);
    }

    static int 
        schedule(int command, int prio, float ratio)
    {
        int status = OK;
        switch(command)
        {
            case NEW_JOB :
                status = new_job(prio);
                break;
            case QUANTUM_EXPIRE :
                status = quantum_expire();
                break;
            case UPGRADE_PRIO :
                status = upgrade_prio(prio, ratio);
                break;
            case BLOCK :
                status = block();
                break;
            case UNBLOCK :
                status = unblock(ratio);
                break;
            case FINISH :
                finish();
                System.out.print("\n");
                break;
            case FLUSH :
                status = flush();
                break;
            default:
//            	System.out.println(" no command " + command);
                status = NO_COMMAND;
        }
        return(status);
    }




    static int 
        put_end(int prio, process process) /* Put process at end of queue */
    {
//        process_ptr next;
#ifdef F_S2_HD_8
//      if(prio > MAXPRIO || prio < 0) return(BADPRIO); /* Somebody goofed */
#else
        if(prio > MAXPRIO || prio < 0) return(BADPRIO); /* Somebody goofed */
#endif
        /* find end of queue */
        process nxt = prio_queue[prio].head;
        if (nxt == null) {
        	prio_queue[prio].head = process;
        } else {
        for (; nxt.next != null; nxt = nxt.next);
        	nxt.next = process;
        }
    
//        for(next = new process_ptr(prio_queue[prio].head); next.pr != null;
//                next = new process_ptr(next.pr.next));
//        next.pr = process;
        prio_queue[prio].length++;
        return(OK);
    }

    static int 
        get_process(int prio, float ratio, process_ptr job)
    {
        int length, index;
        process_ptr next;
        if(prio > MAXPRIO || prio < 0) return(BADPRIO); /* Somebody goofed */
#ifdef F_S2_HD_3
//      if(ratio < 0.0 || ratio > 1.0) return(BADRATIO); /* Somebody else goofed */
#elif F_S2_HD_7
        if(ratio < 0.0 || ratio >= 1.0) return(BADRATIO); /* Somebody else goofed */
#else
        if(ratio < 0.0 || ratio > 1.0) return(BADRATIO); /* Somebody else goofed */
#endif
        length = prio_queue[prio].length;
        index = (int)(ratio * length);
        index = index >= length ? length -1 : index; /* If ratio == 1.0 */
//        System.out.println("prio " +prio +" idx "+ index + " " +length);
// MODIFIED FROM ORIGINAL TO MAKE IT MORE JAVA LIKE AND UNDERSTANDABLE
//        for(next = new process_ptr(prio_queue[prio].head); 
//                (index > 0) && next.pr != null; index--) {
//        	next = new process_ptr(next.pr.next); /* Count up to it */
//        }
//        job.pr = next.pr;
//        if(job.pr != null)
//        {
//        	next.pr = next.pr.next; /* Mend the chain */
//            job.pr.next = null; /* break this link */
//            prio_queue[prio].length--;
//            return(TRUE);
//        }
        process pr, prev = null;
        for (pr = prio_queue[prio].head; (index > 0) && pr.next != null; index--) {
        	prev = pr;
        	pr = pr.next;
        }
        job.pr = pr;
        if (job.pr != null) {
        	if (prev == null) {
        		prio_queue[prio].head = pr.next;
        	} else {
        		prev.next = pr.next;
        	}
        	job.pr.next = null;
        	prio_queue[prio].length--;
        	return (TRUE);
        }
        else return(FALSE);
    }

}
