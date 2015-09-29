package filequeue;

import org.msgpack.annotation.Message;

/**
 * Created by prashanth on 9/12/14.
 */

@Message
public class QueueOperation {

    public Integer getOperation() {
        return operation;
    }

    public void setOperation(Integer operation) {
        this.operation = operation;
    }

    Integer operation;
}
