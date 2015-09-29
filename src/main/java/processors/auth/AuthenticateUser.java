package processors.auth;

import db.AuthRepoPool;
import db.AuthRepoPoolFactory;
import db.NeuerCloudDatabaseException;
import objects.Auth;
import objects.JID;
import objects.Packet;
import objects.exceptions.MalformedJIDException;
import parsers.PacketType;
import processors.PacketProcessorsIfc;
import session.NcpSession;
import session.NcpSessionManager;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by prashanth on 31/12/14.
 */
public class AuthenticateUser implements PacketProcessorsIfc {

    private Logger logger = Logger.getLogger(AuthenticateUser.class.getName());

    private AuthRepoPool authRepoPool;

    private boolean loadTestMode = true;

    @Override
    public void process(Packet packet, List<Packet> results, NcpSession session) {

        Packet result = new Auth(packet.getSession().getAllocator(), 10);
        result.setSessionHashCode(packet.getSessionHashCode());

        try {
            ((Auth)result).setUserJid(packet.getPacketOwner().getFullJID());
            result.setSession(packet.getSession());
        } catch (MalformedJIDException e) {
            // Safely ignore the exception
        }


        if(authRepoPool == null) {
            synchronized (this) {
                if (authRepoPool == null) {
                    authRepoPool = AuthRepoPoolFactory.getAuthRepoPool();
                }
            }
        }

        if( session!= null && session.isAuthenticated() ){

            logger.warning("Authenticated user trying to login again, so logging him out" + packet.getPacketOwner());

            try {
                authRepoPool.logoutUser(packet.getPacketOwner());
                session.setAuthenticated(false);
            } catch (NeuerCloudDatabaseException e) {
                logger.warning("Exception while logging out user:" + e);
            }

            result.appendData("!-1%".getBytes());
            result.setValid(false);
            result.setValidSession(false);
            results.add(result);
            return;
        }

        if(packet.getOperation() != PacketType.auth){
            logger.warning("A wrong packet received at auth processor:" + packet);
        }

        Auth authPacket = (Auth) packet;

        JID user = authPacket.getPacketOwner();
        String password = authPacket.getPassword();

        if(!loadTestMode && !NcpSessionManager.isValidNumberOfSessionsPerUser(user.getBareJID())){
            result.appendData("!0%".getBytes());
            result.setValid(false);
            results.add(result);
            return;
        }

        try {
            boolean loginResult = authRepoPool.loginUser(user, password);

            if(logger.isLoggable(Level.FINE))
                logger.fine("Login result for user:" + user + " is:" + loginResult);

            if(loginResult){
                logger.warning("Authenticated user logged in" + packet.getPacketOwner());
                result.appendData("!1%".getBytes());
                result.setValid(true);
            }else {
                result.appendData("!0%".getBytes());
                result.setValid(false);
            }

            results.add(result);

        } catch (NeuerCloudDatabaseException e) {
            logger.warning("Unable to connect to auth repository to authenticate users. Reason: " + e.getMessage());
        }
    }
}
