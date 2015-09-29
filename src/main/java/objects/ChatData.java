package objects;


import io.netty.buffer.ByteBufAllocator;
import parsers.PacketType;

/**
 * Created by prashanth on 31/12/14.
 */
public class ChatData extends Data {

    JID to;

    public ChatData(ByteBufAllocator allocator){
        super(allocator);
        this.operation = PacketType.chatdata;
    }

    public JID getTo() {
        return to;
    }

    public void setTo(JID to) {
        this.to = to;
    }
}
