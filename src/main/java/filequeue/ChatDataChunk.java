package filequeue;

import org.msgpack.annotation.Message;

/**
 * Created by prashanth on 18/12/14.
 */

@Message
public class ChatDataChunk extends Chunk {

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    private String to;

    public ChatDataChunk(){
        this.operation = 4;
    }
}
