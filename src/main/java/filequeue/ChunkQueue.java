package filequeue;

/**
 * Abstraction of the queing operations for the neuer cloud.
 *
 * It can be implemented using RabbitMQ/ Openstack Zaqar/ Amazon SNS
 *
 * The primary version mainly contains implementation for:
 *
 * 1. RabbitMQ ( Development )
 * 2. Amazon SNS ( Deployment )
 *
 *
 * Created by prashanth on 24/11/14.
 */

public interface ChunkQueue {
    void initializeQueue();
    boolean insertChunk(Chunk chunkToInsert, String type);
}
