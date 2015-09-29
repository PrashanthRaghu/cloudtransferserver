package filequeue;

import org.msgpack.annotation.Message;

/**
 * Created by prashanth on 9/12/14.
 */

@Message
public class FileDeletionRequest extends QueueOperation{

    Long fileId;
    String owner;

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
}
