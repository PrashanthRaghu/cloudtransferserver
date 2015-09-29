package filequeue.impl;


import filequeue.Chunk;
import filequeue.ChunkQueue;

import java.util.logging.Logger;

/**
 * Created by prashanth on 24/11/14.
 */
public class ZaqarChunkQueue implements ChunkQueue {

    private static final Logger log = Logger.getLogger(ZaqarChunkQueue.class.getName());

    @Override
    public void initializeQueue() {

    }

    @Override
    public boolean insertChunk(Chunk chunkToInsert, String type) {
        return false;
    }

}
