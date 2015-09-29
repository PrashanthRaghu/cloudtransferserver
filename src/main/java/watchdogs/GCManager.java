package watchdogs;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * One very important point to note:
 *
 * http://stackoverflow.com/questions/15632734/can-we-call-the-garbage-collector-explicitly
 *
 * Created by prashanth on 21/1/15.
 */
public class GCManager implements Job {

    private static Logger logger = Logger.getLogger(GCManager.class.getName());

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        if(logger.isLoggable(Level.INFO)){
            logger.info(" Invoking a request for Garbage Collection ");
        }

        System.gc();
    }
}
