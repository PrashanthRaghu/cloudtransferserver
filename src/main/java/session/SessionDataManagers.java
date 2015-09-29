package session;


import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import objects.JID;
import objects.Packet;
import objects.SocketType;
import parsers.PacketType;
import parsers.plaintextparser.PacketIdentifier;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * Act as an intermediate layer to transfer data into the user sessions
 *
 * Created by prashanth on 31/12/14.
 */

public class SessionDataManagers {

    static Charset charset = Charset.forName("UTF-8");
    private static Logger logger = Logger.getLogger(SessionDataManagers.class.getName());

    public static void addDataIntoUserSession(NcpSession session, ByteBuf buffer) throws IOException {

        NcpSession userSession = session;

        ByteBuffer nioBuffer = buffer.nioBuffer();

        SocketType type = session.getType();

        if(userSession == null){
            if(logger.isLoggable(Level.INFO))
                logger.info("Session terminated waiting for removal of user session");
            return;
        }

        /*
            An over head to transfer all data into a byte array
            creates multiple copies to be GC'ed.

            Can optimize here.
         */

        ChannelHandlerContext channelToWrite = session.getContext();

        byte[] userData = new byte[nioBuffer.remaining()];
        nioBuffer.get(userData);

        String userDataPrefix = null;
        String userDataSuffix = null;

        PacketType packetType = null;

        if(userData.length > 16)
            userDataPrefix = new String(userData,0, 16);
        else
            userDataPrefix = new String(userData);

        if(userData.length > 32)
            userDataSuffix = new String(userData, userData.length - 16, 16);
        else
            userDataSuffix = new String(userData);

        if(userSession.getIncomingPacket() == null){

            packetType = PacketIdentifier.getPacketType(userDataPrefix);

            if(packetType.equals(PacketType.invalid)){
                return;
            }

            userSession.resetUserPacket(packetType);
            userSession.getIncomingPacket().setStartedFilling(true);

        }else if( userSession.getIncomingPacketComplete()
                && PacketIdentifier.isBeginningOfPacket(userDataPrefix)){

            packetType = PacketIdentifier.getPacketType(userDataPrefix);

            if(packetType == PacketType.data || packetType == PacketType.chatdata ){

                /*
                    Just Hypothetically set at 1MB per second.
                    Will be made configurable to acceptable limits

                    If he's hogging more than 10MBps then he needs to
                    go off.

                    Clients also need to have control over this parameter.

                */

                if( userSession.getSessionTransferRate() > 10000000 ){
                    if(type.equals(SocketType.websocket))
                        channelToWrite.channel().writeAndFlush(new TextWebSocketFrame("--3%"));
                    else
                        channelToWrite.writeAndFlush(ByteBuffer.wrap("--3%".getBytes()));

                    NcpSessionManager.removeUserSession(userSession);
                }
            }

            userSession.resetUserPacket(packetType);
            userSession.getIncomingPacket().setStartedFilling(true);
        }

        /*
            Finds if the packet in transit is of valid size
            Maximum size of a data chunk is 5MB.
         */

        packetType = userSession.getIncomingPacket().getOperation();

        if( packetType == PacketType.data && !userSession.isAuthenticated() ){
            /*
                Error code: -2 denotes that the user has not authenticated to the system.
             */

            if(logger.isLoggable(Level.WARNING))
                logger.warning("Unauthenticated file transfer attempted, " +
                        "Hence terminating connection from socket Channel:" + userSession.getChannel());

                if(type.equals(SocketType.websocket))
                    channelToWrite.channel().writeAndFlush(new TextWebSocketFrame("--2%"));
                else
                    channelToWrite.writeAndFlush(ByteBuffer.wrap("--2%".getBytes()));
            channelToWrite.close();
        }

        if(userSession.isValidDataPacketSize()) {
            userSession.addDataToIncomingPacket(userData);

            Packet currentPacket = userSession.getIncomingPacket();

            if( packetType == PacketType.data && currentPacket.hasLengthChanged()
                    && currentPacket.getDataLength() % 11 == 0 ) {

                ByteBuffer writeBuffer = userSession.getIncomingPacket().getWriteBuffer();
                writeBuffer.clear();
                writeBuffer.put(("#" + String.valueOf(userSession.getIncomingPacket().getDataLength())).getBytes());
                writeBuffer.flip();

                if(type.equals(SocketType.websocket))
                    channelToWrite.channel().writeAndFlush(new TextWebSocketFrame(
                            "#" + String.valueOf(userSession.getIncomingPacket().getDataLength())));
                else
                    channelToWrite.writeAndFlush(writeBuffer);
            }

        }else{
            /*
                Report an error to the user stating packet size is too big
                Error code: -3

                Error code: -2 denotes that the user has not authenticated to the system.
             */

                if(type.equals(SocketType.websocket))
                    channelToWrite.channel().writeAndFlush(new TextWebSocketFrame("--2%"));
                else
                    channelToWrite.writeAndFlush(ByteBuffer.wrap("--2%".getBytes()));

            channelToWrite.close();
        }

        if(PacketIdentifier.isLastPartOfPacket(userDataSuffix)){
            userSession.setIncomingPacketComplete();
            Packet userPacket = userSession.getIncomingPacket();

            if( userPacket.getOperation() == PacketType.data || userPacket.getOperation() == PacketType.chatdata ){
                userPacket.setPacketOwner(userSession.getUserJID());
            }

            userPacket.setSessionHashCode(userSession.hashCode());
            NcpSessionManager.addPacketToPendingPacketsList(userSession.getIncomingPacket());
            userPacket.setEndTime(System.currentTimeMillis());
        }
    }

    public static boolean userSessionExists(JID jid){
        return NcpSessionManager.userSessionExists(jid);
    }

    public static void addJidMappedNcpSession(JID jid, NcpSession session){
        NcpSessionManager.setJidMappedSocketSessions(jid, session);
    }
}
