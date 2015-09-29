package watchdogs;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import session.NcpSession;
import session.NcpSessionManager;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * Used to check the users who are online in the system
 *
 * If a user is not online we remove his session.
 *
 * We use the standard method for TCP closed socket connection as described here:
 *
 * Created by prashanth on 21/1/15.
 *
 * We use a simple timer based approach to closing sockets.
 *
 * If a user is inactive for:
 *
 * connection_timeout_threshold_factor * connections_timeout_interval
 *
 * Then we conclude that the user is offline.
 */

public class ConnectedUsersManager implements Job{

    private Logger logger = Logger.getLogger(ConnectedUsersManager.class.getName());

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        JobDataMap dataMap = jobExecutionContext.getJobDetail().getJobDataMap();

        Long timeout_threshold = Long.valueOf(
                (String)dataMap.get("connection_timeout_threshold_factor"));

        Long timeout_interval = Long.valueOf(
                (String)dataMap.get("connections_timeout_interval"));

        if(logger.isLoggable(Level.INFO))
            logger.info("Running watchdog timer to check for all active connected users. ");


        Iterator<NcpSession> activeSessions = NcpSessionManager.getActiveSessions().iterator();

        Long systemTime = System.currentTimeMillis();

        while(activeSessions.hasNext()){

            NcpSession session = activeSessions.next();
            Long sessionActiveTime = session.getLastActiveTime();

            if( (systemTime - sessionActiveTime) > (timeout_interval * timeout_threshold * 1000)){

                if(logger.isLoggable(Level.INFO))
                    logger.info("Closing user session with info:" + session);

                NcpSessionManager.removeUserSession(session);
                activeSessions.remove();
            }
        }
    }
}
