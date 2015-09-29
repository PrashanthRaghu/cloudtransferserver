package filequeue;

/**
 * Created by prashanth on 9/12/14.
 */
public class ChunkQueuePoolFactory {
    private static ChunkQueuePool chunkQueuePool;

    public static void setChunkQueuePool(ChunkQueuePool chunkQueuePool){
        ChunkQueuePoolFactory.chunkQueuePool = chunkQueuePool;
    }

    public static ChunkQueuePool getChunkQueuePool(){
        return chunkQueuePool;
    }
}
