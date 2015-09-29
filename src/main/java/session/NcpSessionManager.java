package session;

import config.PropertiesManager;
import objects.JID;
import objects.Packet;
import packetprocessors.IncomingPacketWorker;
import packetprocessors.OutgoingPacketWorker;
import parsers.PacketType;
import processors.PacketProcessorsIfc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * Created by prashanth on 29/5/15.
 */
public class NcpSessionManager {
    private static Logger logger = Logger.getLogger(NcpSessionManager.class.getName());

    /*
        Helps data routing when data is to be passed to the sockets.
     */

    private static ConcurrentHashMap<JID, NcpSession> jidMappedSocketSessions =
            new ConcurrentHashMap<JID, NcpSession>(5000);

    private static ConcurrentHashMap<Integer, NcpSession> hashCodeMappedSessions =
            new ConcurrentHashMap<Integer, NcpSession>();

    public static LinkedBlockingQueue<Packet> getActivePacketsList() {
        return activePacketsList;
    }

    private static LinkedBlockingQueue<Packet> activePacketsList =
            new LinkedBlockingQueue<Packet>();

    private static LinkedBlockingQueue<Packet> outgoingPacketsList =
            new LinkedBlockingQueue<Packet>();

    private static List<NcpSession> activeSessions = Collections.synchronizedList(new ArrayList<NcpSession>());

    private static Map<String, Integer> sessionsPerIp = new ConcurrentHashMap<>();

    private static Map<String, Integer> sessionsPerUser = new ConcurrentHashMap<>();

    private static IncomingPacketWorker[] incomingPacketWorkers;

    private static OutgoingPacketWorker[] outgoingPacketWorkers;


    /*
        All packet processors have to be configured statically in the configuration files

        using:

        1. allIfcs/auth/processors
        2. allIfcs/data/processors

        The packet is then processed by each of these processors on a single thread

     */
    private static ConcurrentHashMap<PacketType, List<PacketProcessorsIfc>> packetProcessors =
            new ConcurrentHashMap<PacketType, List<PacketProcessorsIfc>>();

    private static String[] processorProps = {"allIfcs/auth/processors",
            "allIfcs/data/processors", "allIfcs/chatdata/processors"};

    static {

        Map<String, Object> sessionManagerProps = PropertiesManager.getComponentProperties(processorProps);

        String authProcessors = (String)sessionManagerProps.get("allIfcs/auth/processors");
        String dataProcessors = (String)sessionManagerProps.get("allIfcs/data/processors");
        String chatDataProcessors = (String)sessionManagerProps.get("allIfcs/chatdata/processors");

        String[] authProcessorsList = authProcessors.split(",");
        String[] dataProcessorsList = dataProcessors.split(",");
        String[] chatDataProcessorsList = chatDataProcessors.split(",");

        List<PacketProcessorsIfc> authProcessorsImpls = new ArrayList<PacketProcessorsIfc>();
        List<PacketProcessorsIfc> dataProcessorsImpls = new ArrayList<PacketProcessorsIfc>();
        List<PacketProcessorsIfc> chatDataProcessorsImpls = new ArrayList<PacketProcessorsIfc>();

        try {
            for(String authProcessor: authProcessorsList){
                PacketProcessorsIfc processorsIfc = (PacketProcessorsIfc) Class.forName(authProcessor).newInstance();
                authProcessorsImpls.add(processorsIfc);
            }

            packetProcessors.put(PacketType.auth, authProcessorsImpls);

            for(String dataProcessor: dataProcessorsList){
                PacketProcessorsIfc processorsIfc = (PacketProcessorsIfc) Class.forName(dataProcessor).newInstance();
                dataProcessorsImpls.add(processorsIfc);
            }

            packetProcessors.put(PacketType.data, dataProcessorsImpls);

            for(String chatDataProcessor: chatDataProcessorsList){
                PacketProcessorsIfc processorsIfc = (PacketProcessorsIfc) Class.forName(chatDataProcessor).newInstance();
                chatDataProcessorsImpls.add(processorsIfc);
            }

            packetProcessors.put(PacketType.chatdata, chatDataProcessorsImpls);

            int numberOfWorkers = Runtime.getRuntime().availableProcessors();

            incomingPacketWorkers = new IncomingPacketWorker[numberOfWorkers];
            outgoingPacketWorkers = new OutgoingPacketWorker[numberOfWorkers];

            for (int i = 0; i< numberOfWorkers; i++){
                incomingPacketWorkers[i] = new IncomingPacketWorker();
                incomingPacketWorkers[i].setName("IncomingPacketProcessorThread-" + i);
                incomingPacketWorkers[i].start();
            }

            for(int i = 0 ;i < numberOfWorkers; i++){
                outgoingPacketWorkers[i] = new OutgoingPacketWorker();
                outgoingPacketWorkers[i].setName("OutgoingPacketProcessorThread-" + i);
                outgoingPacketWorkers[i].start();
            }

        } catch (Exception e) {
            logger.severe("Unable to add auth/ data processor to the list of processors:" + e.getMessage());
        }
    }

    /*
        We might have to create more mappings from different kinds of user information
        to the actual user session information.
     */

    private static AtomicInteger numberUserSessions = new AtomicInteger(0);

    public static void addUserSession(NcpSession session){
        numberUserSessions.addAndGet(1);
    }

    public static void removeUserSession(NcpSession session){

        if(session.getUserJID() != null)
            jidMappedSocketSessions.remove(session.getUserJID());

        hashCodeMappedSessions.remove(session.hashCode());

        if(session.getUserJID() != null)
            sessionsPerUser.remove(session.getUserJID().getBareJID());

        sessionsPerIp.remove(session.getSessionIP());
        numberUserSessions.decrementAndGet();
    }

    public static void setJidMappedSocketSessions(JID jid, NcpSession session){
        jidMappedSocketSessions.put(jid, session);
    }

    public static NcpSession getJidMappedSocketSession(JID jid){
        return jidMappedSocketSessions.get(jid);
    }

    public static void addPacketToPendingPacketsList(Packet packet){
        activePacketsList.offer(packet);
    }

    public static List<PacketProcessorsIfc> getPacketProcessors(PacketType type){
        return packetProcessors.get(type);
    }

    public static ConcurrentHashMap<PacketType, List<PacketProcessorsIfc>> getPacketProcessors(){
        return packetProcessors;
    }

    public static void addPacketToOutgoingPacketsList(Packet packet){
        outgoingPacketsList.offer(packet);
    }

    public static LinkedBlockingQueue<Packet> getOutgoingPacketsList(){
        return outgoingPacketsList;
    }

    public static void addHashCodeMappedSession(Integer hash, NcpSession session){
        hashCodeMappedSessions.put(hash, session);
        activeSessions.add(session);
    }

    public static NcpSession getHashCodeMappedSession(Integer hash){
        return hashCodeMappedSessions.get(hash);
    }

    /*
        We eliminate the mappings for the user session and close the socket.
     */
    public static void closeUserSession(Integer hashCode){
        NcpSession session = hashCodeMappedSessions.get(hashCode);
        hashCodeMappedSessions.remove(hashCode);
    }

    public static List<NcpSession> getActiveSessions(){
        return activeSessions;
    }

    public static boolean userSessionExists(JID jid){
        return jidMappedSocketSessions.containsKey(jid);
    }

    public static NcpSession getNcpSessionHashcodeMappedSession(Integer hashCode){
        return hashCodeMappedSessions.get(hashCode);
    }

    public static boolean isValidNumberOfSessionsPerUser(String user){
        Integer numberOfSessions = sessionsPerUser.get(user);

        if(numberOfSessions == null)
            numberOfSessions = 0;

        if(numberOfSessions > 5){
            return false;
        }

        numberOfSessions++;
        sessionsPerUser.put(user, numberOfSessions);
        return true;
    }

    public static boolean validNumberOfSessionsPerIp(String ip){
        Integer numberOfSessions = sessionsPerIp.get(ip);

        if(numberOfSessions == null)
            numberOfSessions = 0;

        if(numberOfSessions > 3)
            return false;

        numberOfSessions++;
        sessionsPerIp.put(ip, numberOfSessions);
        return true;
    }
}
