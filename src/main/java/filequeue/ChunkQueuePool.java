package filequeue;


import config.PropertiesManager;
import filequeue.impl.RabbitMQChunkQueue;

import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

/**
 * Implements a simple Queue Connection pool for management
 * of a connection pool
 *
 * TODO(prashanth):
 *
 * 1. Try to make it generic instead of accepting a specialized RabbitMQChunkQueue
 *
 * Created by prashanth on 28/11/14.
 */

public class ChunkQueuePool implements ChunkQueue {
    private LinkedBlockingQueue<ChunkQueue> chunkQueueObjectPool = new LinkedBlockingQueue<>();
    private static Logger log = Logger.getLogger(ChunkQueuePool.class.getName());
    private static String[] componentProperties = {"queue_pool_size", "queue_host", "in_queue", "queue_username", "queue_user_password", ""};

    private static Map<String, Object> componentPropertiesMap;

    @Override
    public void initializeQueue() {
        Integer queuePoolSize = Integer.valueOf(componentPropertiesMap.get("queue_pool_size").toString());

        for(int numInserted = 0; numInserted < queuePoolSize; numInserted++) {
            chunkQueueObjectPool.offer(new RabbitMQChunkQueue(componentPropertiesMap));
        }

        for(ChunkQueue queue: chunkQueueObjectPool){
            queue.initializeQueue();
        }

        log.info("Initialization of Queue Pool is completed");
    }

    public void initializePropertiesMap(){
        componentPropertiesMap = PropertiesManager.getComponentProperties(componentProperties);
    }

    @Override
    public boolean insertChunk(Chunk chunkToInsert, String type) {
        ChunkQueue chunkQueue;

        try {
            chunkQueue = chunkQueueObjectPool.take();
            chunkQueue.insertChunk(chunkToInsert, type);
        } catch (InterruptedException e) {
            log.warning("Unable to obtain a queue instance");
            return Boolean.FALSE;
        }

        chunkQueueObjectPool.offer(chunkQueue);
        return Boolean.TRUE;
    }
}
