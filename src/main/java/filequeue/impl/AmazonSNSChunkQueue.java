package filequeue.impl;

import filequeue.*;

import java.util.logging.Logger;

/**
 *
 * The development has been stalled because of the size limitations of the
 * queing system.
 *
 * Amazon SQS has a maximum message size limit of 256KB.
 *
 * Created by prashanth on 24/11/14.
 */
public class AmazonSNSChunkQueue implements ChunkQueue {

    private static final Logger log = Logger.getLogger(AmazonSNSChunkQueue.class.getName());

    @Override
    public void initializeQueue() {

    }

    @Override
    public boolean insertChunk(Chunk chunkToInsert, String type) {
        return false;
    }
}
