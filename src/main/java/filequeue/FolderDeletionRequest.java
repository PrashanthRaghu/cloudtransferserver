package filequeue;

import org.msgpack.annotation.Message;

/**
 * Created by prashanth on 16/12/14.
 */

@Message
public class FolderDeletionRequest extends QueueOperation {

    Long folderId;
    String owner;

    public Long getFolderId() {
        return folderId;
    }

    public void setFolderId(Long folderId) {
        this.folderId = folderId;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
}
