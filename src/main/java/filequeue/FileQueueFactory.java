package filequeue;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simplifies File Queue creation
 *
 * Try to keep the queue properties as simple as possible and as common
 * across queuing mechanisms as possible.
 *
 * Properties of the queue:
 * 1. Hostname of the queue
 * 2. Port of the queue
 */
public class FileQueueFactory {
    private static final Logger log = Logger.getLogger(FileQueueFactory.class.getName());

    public static ChunkQueue createFileQueue(String queueBackendType){

        ChunkQueue chunkQueue = null;

        if(log.isLoggable(Level.INFO)){
            log.warning("Creating connnection to the queing backend of type: " + queueBackendType);
        }

        try{
            chunkQueue = (ChunkQueue) Class.forName(queueBackendType).getConstructor().newInstance();
        } catch (Exception e) {
            log.severe("Unable to create File Queue of type: " + queueBackendType);
        }

        if(log.isLoggable(Level.INFO)){
            log.warning("Created connnection to the queing backend of type: " + queueBackendType);
        }

        return chunkQueue;
    }
}
