package packetprocessors;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import objects.JID;
import objects.Packet;
import objects.SocketType;
import parsers.PacketType;
import session.NcpSession;
import session.NcpSessionManager;
import session.SessionDataManagers;

import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by prashanth on 31/12/14.
 */
public class OutgoingPacketWorker extends Thread {

    private static Logger logger = Logger.getLogger(OutgoingPacketWorker.class.getName());
    private static boolean keepRunning = true;

    @Override
    public void run() {

        while (keepRunning) {

            Packet packetToProcess = null;

            try {
                packetToProcess = NcpSessionManager.getOutgoingPacketsList().take();

                ChannelHandlerContext channelToWrite = packetToProcess.getSession().getContext();

                if (channelToWrite == null) {
                    continue;
                }

                NcpSession userSession = packetToProcess.getSession();
                SocketType type = userSession.getType();

                if (packetToProcess.getOperation() == PacketType.auth) {

                    if (packetToProcess.isValid()) {

                        if (SessionDataManagers.userSessionExists(packetToProcess.getPacketOwner())) {

                            if(type.equals(SocketType.websocket))
                                channelToWrite.writeAndFlush(new TextWebSocketFrame("!0%"));
                            else
                                channelToWrite.writeAndFlush(ByteBuffer.wrap("!0%".getBytes()));

                            channelToWrite.close();
                            NcpSessionManager.removeUserSession(userSession);
                            continue;
                        }

                        userSession.setAuthenticated(true);
                        userSession.setUserJID(packetToProcess.getPacketOwner());
                        userSession.setLoggedInAt(System.currentTimeMillis());
                        SessionDataManagers.addJidMappedNcpSession(packetToProcess.getPacketOwner(), userSession);
                    } else {

                    /*
                        User has attempted to Logout
                     */
                        if (!packetToProcess.isValidSession()) {
                            if(type.equals(SocketType.websocket))
                                channelToWrite.writeAndFlush(new TextWebSocketFrame(packetToProcess.toWire()));
                            else
                                channelToWrite.writeAndFlush(packetToProcess.toWire());

                            channelToWrite.close();
                            NcpSessionManager.removeUserSession(userSession);
                        }

                        userSession.incrementNumberFailedLogins();

                        // More than 3 attempts is not allowed.
                        // TODO(prashanth): Write a session cleanup code logic.

                        if (userSession.getNumFailedLogins() > 3) {
                            NcpSessionManager.removeUserSession(userSession);
                        }
                    }

                    if (packetToProcess.getSession().getChannel().isOpen()) {
                        if(type.equals(SocketType.websocket))
                            channelToWrite.channel().writeAndFlush(new TextWebSocketFrame(packetToProcess.toWire()));
                        else
                            channelToWrite.writeAndFlush(packetToProcess.toWire());
                    }

                } else if (packetToProcess.getOperation() == PacketType.data) {

                    if(type.equals(SocketType.websocket))
                        channelToWrite.writeAndFlush(new TextWebSocketFrame(packetToProcess.toWire()));
                    else
                        channelToWrite.writeAndFlush(packetToProcess.toWire());

                    // User attempting to write data to an invalid (unauthenticated) session.

                    if(type.equals(SocketType.websocket))
                        channelToWrite.writeAndFlush(new TextWebSocketFrame(packetToProcess.getTransferTime()));
                    else
                        channelToWrite.writeAndFlush(packetToProcess.getTransferTime());

                    if (!packetToProcess.isValidSession()) {

                        if (logger.isLoggable(Level.WARNING)) {
                            logger.warning("Closing an invalid user session. User attempting file transfer on a invalid session");
                        }

                        channelToWrite.close();
                        NcpSessionManager.removeUserSession(userSession);

                    } else if (!packetToProcess.isValid()) {
                        // Occurs in case of packets having a mismatch of the MD5 hash

                    }
                }

                // Eliminate the packet and allow faster garbage collection

                packetToProcess.flushPacket();
                packetToProcess = null;
            /*
                You will have to fetch the sessions for the user from the JID
                and if null fetch from the socket channel in order to set it
                on the JID of the user
             */
            } catch (Exception e) {
                e.printStackTrace();
                logger.warning("Exception while processing a packet from outgoing packets list:" + e.getMessage());
            }
        }
    }
}
