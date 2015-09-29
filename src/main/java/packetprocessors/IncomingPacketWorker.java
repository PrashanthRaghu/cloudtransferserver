package packetprocessors;

import objects.JID;
import objects.Packet;
import parsers.PacketType;
import parsers.plaintextparser.PlainTextParser;
import processors.PacketProcessorsIfc;
import session.NcpSession;
import session.NcpSessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Created by prashanth on 31/12/14.
 */
public class IncomingPacketWorker extends Thread {

    private static Logger logger = Logger.getLogger(IncomingPacketWorker.class.getName());
    private static boolean keepRunning = true;
    private ConcurrentHashMap<PacketType, List<PacketProcessorsIfc>> packetProcessors;
    private PlainTextParser plainTextParser = new PlainTextParser();

    @Override
    public void run(){

        Packet packetToProcess = null;

        while (keepRunning) {

            try {
                packetToProcess = NcpSessionManager.getActivePacketsList().take();
            } catch (InterruptedException e) {
                logger.warning("Exception while selecting a packet from active packets list:" + e.getMessage());
                continue;
            }

            if(packetProcessors == null){
                packetProcessors = NcpSessionManager.getPacketProcessors();
            }

            List<PacketProcessorsIfc> processorsForPacket = packetProcessors.get(
                    packetToProcess.getOperation());

            plainTextParser.parseData(packetToProcess);

            /*
                You will have to fetch the sessions for the user from the JID
                and if null fetch from the socket channel in order to set it
                on the JID of the user
             */

            for(PacketProcessorsIfc processor: processorsForPacket){
                List<Packet> packetResults = new ArrayList<Packet>();

                NcpSession userSession = packetToProcess.getSession();

                processor.process(packetToProcess, packetResults, userSession);

                for(Packet packet: packetResults){
                    NcpSessionManager.addPacketToOutgoingPacketsList(packet);
                }
            }

            /*
                Helps reduce memory usage
             */
            packetToProcess.flushPacket();
        }
    }
}
