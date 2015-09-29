package parsers;

import objects.Packet;

import java.nio.ByteBuffer;

/**
 * Created by prashanth on 27/12/14.
 */

public interface PacketParser {
    public void parseData(Packet packetDestination);
}
