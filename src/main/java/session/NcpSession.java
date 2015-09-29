package session;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannel;
import objects.*;
import parsers.PacketType;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 *
 * Represents the basic state of a user connected to the NCP protocol.
 *
 * NCP authenticates users using the tigase authentication mechanism, but flexibility will be added to
 * customize the SQL query which can be used to authenticate the users
 *
 * Created by prashanth on 25/12/14.
 */
public class NcpSession {

    private boolean isAuthenticated = false;

    private SocketType type;

    private JID userJID;

    private SocketChannel channel;

    public long getLastActiveTime() {
        return lastActiveTime;
    }

    public void setLastActiveTime(long lastActiveTime) {
        this.lastActiveTime = lastActiveTime;
    }

    private long lastActiveTime = 0L;

    public void setAllocator(ByteBufAllocator allocator) {
        this.allocator = allocator;
    }

    public ByteBufAllocator getAllocator() {
        return allocator;
    }

    UserConnectManagementThread userConnectManagementThread = new UserConnectManagementThread();

    ByteBufAllocator allocator;

    public ChannelHandlerContext getContext() {
        return context;
    }

    public void setContext(ChannelHandlerContext context) {
        this.context = context;
        userConnectManagementThread.setSession(this);
    }

    ChannelHandlerContext context;

    // We will have to accumulate all user packets which arrive to the system
    // as bytebuffers. We will have to identify the end of the packet in order to
    // determine the end of the packet. This can be identified with an end of packet header
    // A user on a session can transfer a single packet at a time.

    private Packet incomingPacket;

    public SocketType getType() {
        return type;
    }

    public void setType(SocketType type) {
        this.type = type;
    }

    public long getLoggedInAt() {
        return loggedInAt;
    }

    public void setLoggedInAt(long loggedInAt) {
        this.loggedInAt = loggedInAt;
    }

    // Represents the time the user logged into the system
    private long loggedInAt = 0L;

    // Can be used to measure the transfer rate of the user.
    // If transfer rate > a particular margin, then the user
    // session can be terminated

    private long numBytesTransferred = 0l;

    // Number of times a user login has failed so that we
    // can terminate connections above a particular limit

    private short numFailedLogins = 0;

    // Clients  can store any preference related information here.
    private Map<String, Object> sessionData = new HashMap<String, Object>();

    public String getSessionIP() {
        return sessionIP;
    }

    public void setSessionIP(String sessionIP) {
        this.sessionIP = sessionIP;
    }

    private String sessionIP;

    public NcpSession(){
        loggedInAt = System.currentTimeMillis();
    }

    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    public void setAuthenticated(boolean isAuthenticated) {
        this.isAuthenticated = isAuthenticated;
    }

    public void setSessionDataInfo(String key, Object value){
        sessionData.put(key, value);
    }

    public Object getSessionDataInfo(String key){
        return sessionData.get(key);
    }

    public boolean isConnected(){
        return channel.isOpen();
    }

    public void resetUserPacket(PacketType type){
        if(type.equals(PacketType.auth)){
            incomingPacket = new Auth(getAllocator());
        }else if(type.equals(PacketType.data)){
            incomingPacket = new Data(getAllocator());
        }else if(type.equals(PacketType.chatdata)){
            incomingPacket = new ChatData(getAllocator());
        }
        incomingPacket.setSession(this);
        incomingPacket.setStartTime(System.currentTimeMillis());
    }

    public void addDataToIncomingPacket(byte[] dataPart){
        incomingPacket.appendData(dataPart);
        this.lastActiveTime = System.currentTimeMillis();

        this.numBytesTransferred += dataPart.length;
    }

    public SocketChannel getChannel() {
        return channel;
    }

    public void setChannel(SocketChannel channel) {
        this.channel= channel;
    }

    public void setIncomingPacketComplete(){
        this.incomingPacket.setComplete();
    }

    public boolean getIncomingPacketComplete(){
        return this.incomingPacket.isComplete();
    }

    public Packet getIncomingPacket() {
        return incomingPacket;
    }

    public void incrementNumberFailedLogins(){
        this.numFailedLogins++;
    }

    public short getNumFailedLogins(){
        return this.numFailedLogins;
    }

    /*
        Max size of packets:

        1. Auth - 2KB
        2. ChatData - 10 MB
        3. Data - 20MB
        4. Invalid Packets 2KB ( Avoid Ddos )
     */

    public boolean isValidDataPacketSize(){
        if(incomingPacket instanceof Auth)
            return incomingPacket.getDataLength() < 2 * 1024;
        else if(incomingPacket instanceof ChatData)
            return incomingPacket.getDataLength() < 20 * 1024 * 1024;
        else if(incomingPacket instanceof Data)
            return incomingPacket.getDataLength() < 20 * 1024 * 1024;
        else
            return incomingPacket.getDataLength() < 2 * 1024;
    }

    public JID getUserJID() {
        return userJID;
    }

    public void setUserJID(JID userJID) {
        this.userJID = userJID;
    }

    @Override
    public String toString(){

        StringBuilder builder = new StringBuilder();
        if(userJID != null){
            builder.append("<jid>" + userJID + "</jid>");
        }

        builder.append("<lastActive>" + lastActiveTime + "</lastActive>");
        return builder.toString();
    }

    public long getSessionTransferRate(){
        long timeActiveInSeconds = (System.currentTimeMillis() - loggedInAt) / 1000;

        if(timeActiveInSeconds > 0)
            return numBytesTransferred / timeActiveInSeconds;
        else
            return 0;
    }


}
