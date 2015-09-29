package session;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

/**
 * Created by prashanth on 11/6/15.
 */
public class UserConnectManagementThread implements Runnable{

    NcpSession session;

    public NcpSession getSession() {
        return session;
    }

    public void setSession(NcpSession session) {
        this.session = session;
    }

    int numTimesClosed = 0;

    /*
        Check if socket is inactive for 5 minutes
     */

    @Override
    public void run() {
        ChannelHandlerContext handler = session.getContext();

        System.out.println("Num tIMES CLOSED:" + numTimesClosed);

        if(!handler.channel().isOpen()){
            numTimesClosed++;
        }

        if(numTimesClosed == 5){
            handler.close();
        }
    }
}
