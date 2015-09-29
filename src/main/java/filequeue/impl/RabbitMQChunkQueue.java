package filequeue.impl;


import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import filequeue.ChatDataChunk;
import filequeue.Chunk;
import filequeue.ChunkQueue;
import utils.SerializeMessages;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by prashanth@neuerbox.com on 24/11/14.
 *
 * NOTE:
 * 1. Add custom thread pool when you face scalability issues
 *
 *   rabbitConnectionPool = Executors.newFixedThreadPool((Integer) queueProperties.get("queue-pool-size"));
 *   connectionFactory.setSharedExecutor(rabbitConnectionPool);
 *
 *   TODO(prashanth):
 *
 *   1. Add support for queue name customization
 *   2. Teardown connections gracefully
 */

public class RabbitMQChunkQueue implements ChunkQueue {

    private static ConnectionFactory connectionFactory = new ConnectionFactory();
    private static final Logger log = Logger.getLogger(RabbitMQChunkQueue.class.getName());
    private Connection connection;
    private Channel chunkQueueChannel;
    private String chunkQueueName;
    private Map<String, Object> queueProperties;

    /*
        Use of custom thread pool must happen only when bottleneck is evidenced

        NOTE:
        1. Default port is used to transport data
     */

    public RabbitMQChunkQueue(Map<String, Object> queueProperties){
        this.queueProperties  = queueProperties;
    }

    public void initializeQueue(){
        log.info("Initializing Rabbit Connection");

        connectionFactory.setHost((String) queueProperties.get("queue_host"));
        this.chunkQueueName = (String) queueProperties.get("in_queue");

        connectionFactory.setUsername((String)queueProperties.get("queue_username"));
        connectionFactory.setPassword((String)queueProperties.get("queue_user_password"));

        try {

            connection = connectionFactory.newConnection();
            chunkQueueChannel = connection.createChannel();
            chunkQueueChannel.queueDeclare(this.chunkQueueName, false, false, false, null);

        } catch (IOException e) {
            log.warning("Unable to insert a message into the queue" + e.getMessage());
        }
    }

    /*
        Ensure that the connection to the queue is closed
     */
    @Override
    public boolean insertChunk(Chunk chunkToInsert, String type) {

        try {

            if(type.equals("chatfile")) {
                chunkQueueChannel.basicPublish("", this.chunkQueueName, null,
                        SerializeMessages.serializeChatFile((ChatDataChunk) chunkToInsert));
            }else {
                chunkQueueChannel.basicPublish("", this.chunkQueueName, null,
                        SerializeMessages.serializeFile(chunkToInsert));
            }

            log.warning("Inserted a chunk into the queue.");
        } catch (IOException e) {
            log.severe("Unable to publish file information to the queue");
            log.severe(e.getMessage());
            return false;
        }

        return true;
    }

    /*
       Rabbit documentation states that the thread pool must be manually cleared
       to avoid JVM stalling.

       Add a thread pool shutdown method when a custom thread pool is being used

       More info: https://www.rabbitmq.com/api-guide.html (Section: Advanced Connection options)
     */
    public void tearDownQueue(){

    }
}
