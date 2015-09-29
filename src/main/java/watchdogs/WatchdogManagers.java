package watchdogs;


import config.PropertiesManager;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by prashanth on 21/1/15
 */
public class WatchdogManagers {

    private String[] componentPropertiesList = {"connections_timeout_interval",
            "connection_timeout_threshold_factor", "gc_interval"};

    private Scheduler scheduler = null;

    private Logger logger = Logger.getLogger(WatchdogManagers.class.getName());

    Map<String, Object> componentProperties;

    public WatchdogManagers(){
        initializeProperties();
    }

    public void initializeJobs(){

        try {

            scheduler = new StdSchedulerFactory().getScheduler();

            JobKey connectionTimeoutJobKey = new JobKey("connection_timeout", "ncp_watchdogs");

            JobDetail connectionTimeoutJob = JobBuilder.newJob(ConnectedUsersManager.class)
                    .withIdentity(connectionTimeoutJobKey).build();

            connectionTimeoutJob.getJobDataMap().put("connection_timeout_threshold_factor",
                    componentProperties.get("connection_timeout_threshold_factor"));

            connectionTimeoutJob.getJobDataMap().put("connections_timeout_interval",
                    componentProperties.get("connections_timeout_interval"));


            Integer connectionsTimeoutInterval = Integer.valueOf(
                    (String) componentProperties.get("connections_timeout_interval"));

            Integer gcInterval = Integer.valueOf(
                    (String) componentProperties.get("gc_interval")
            );

            Trigger connectionTimeoutTrigger = TriggerBuilder
                    .newTrigger()
                    .withIdentity("connection_timeout_trigger", "ncp_watchdogs")
                    .withSchedule(
                            SimpleScheduleBuilder.simpleSchedule()
                                    .withIntervalInSeconds(connectionsTimeoutInterval).repeatForever()).
                    build();


            JobKey gcJob = new JobKey("gc_interval", "ncp_watchdogs");

            JobDetail gcJobDetail = JobBuilder.newJob(GCManager.class)
                    .withIdentity(gcJob).build();

            Trigger gcTrigger = TriggerBuilder
                    .newTrigger()
                    .withIdentity("manual_gc_trigger", "ncp_watchdogs")
                    .withSchedule(
                            SimpleScheduleBuilder.simpleSchedule()
                                    .withIntervalInSeconds(gcInterval).repeatForever()).
                            build();


            scheduler.scheduleJob(connectionTimeoutJob, connectionTimeoutTrigger);
            scheduler.scheduleJob(gcJobDetail, gcTrigger);
            scheduler.start();

            if(logger.isLoggable(Level.INFO)){
                logger.info("Watchdog scheduler successfully initialized.");
            }
        } catch (SchedulerException e) {
            logger.severe("Unable to schedule the watchdogs for the system: " + e.getMessage());
        }
    }

    public void initializeProperties(){
        this.componentProperties = PropertiesManager.getComponentProperties(componentPropertiesList);
    }
}
