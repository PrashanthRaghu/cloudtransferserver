package filequeue;

import org.msgpack.annotation.Message;

/**
 * An object represents an abstraction of a file which can be passed into the queue:
 *
 * This object can represent:
 * 1. A complete file being uploaded
 * 2. Chunk of a large file
 *
 * The application considers a small file as a file containing a single
 * chunk of data.
 *
 * Description :
 * The file is a simple object which has the contents in the byteArray fileChunk
 * and metadata stored in an instance of FileMetadata compressed using MsgPack
 * before being offloaded onto the queue
 *
 * Serialization of the file must be avoided if possible.
 */

@Message
public class Chunk extends QueueOperation{

    byte[] data;
    ChunkMetadata metadata;

    public Chunk(){
        this.operation = 1;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public ChunkMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(ChunkMetadata metadata) {
        this.metadata = metadata;
    }
}
