package handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import session.NcpSession;
import session.SessionDataManagers;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Created by prashanth on 29/5/15.
 */

public class NcpServerDataHandler extends ChannelInboundHandlerAdapter {

    private static Logger logger = Logger.getLogger(NcpServerDataHandler.class.getName());

    private NcpSession session;

    public NcpServerDataHandler(NcpSession session){
        this.session = session;
    }

    @Override
    public void channelRead(ChannelHandlerContext context, Object msg) {

        ByteBuf msgAsByteBuf = (ByteBuf) msg;

        if(session.getContext() == null){
            session.setContext(context);
        }

        try {
            SessionDataManagers.addDataIntoUserSession(session , msgAsByteBuf);
        } catch (IOException e) {
            logger.warning("Exception in data reading:" + e.getMessage());
        }finally {
            msgAsByteBuf.release();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause) {
        cause.printStackTrace();
        context.close();
    }
}

