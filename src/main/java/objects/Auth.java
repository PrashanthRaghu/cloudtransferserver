package objects;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import objects.exceptions.MalformedJIDException;
import parsers.PacketType;

/**
 * Created by prashanth on 27/12/14.
 */

public class Auth extends Packet{

    JID userJid;
    String password;

    public Auth(ByteBufAllocator allocator){
        this.operation = PacketType.auth;
        this.buf = allocator.directBuffer(128);
    }

    public Auth(ByteBufAllocator allocator, int size){
        this.operation = PacketType.auth;
        this.buf = allocator.directBuffer(size);
    }

    public void setUserJid(String userJidAsString) throws MalformedJIDException{

        try {
            userJid = new JID(userJidAsString);
        }catch (MalformedJIDException e){
            this.setValid(false);
            throw e;
        }
        packetOwner = userJid;

    }

    public void setPassword(String password){
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public void setPacketOwner(JID packetOwner){
        // packetOwner is a read-only field for the auth packets
    }
}
