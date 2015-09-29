package objects;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import parsers.PacketType;

/**
 * Created by prashanth on 31/12/14.
 */
public class Data extends Packet {

    String token;

    public Data(ByteBufAllocator allocator){
        this.operation = PacketType.data;
        this.buf = allocator.directBuffer(256 * 1024);
    }

    public Data(ByteBufAllocator allocator, int size){
        this.operation = PacketType.data;
        this.buf = allocator.directBuffer(10);
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
