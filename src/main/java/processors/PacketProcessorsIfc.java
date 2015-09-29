package processors;


import objects.Packet;
import session.NcpSession;

import java.util.List;

/**
 * Created by prashanth on 31/12/14.
 */
public interface PacketProcessorsIfc {
    public void process(Packet packet, List<Packet> results, NcpSession session);
}
