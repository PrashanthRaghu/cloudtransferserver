package handlers;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslHandler;
import objects.SocketType;
import session.NcpSession;
import ssl.SSLContextManager;
import ssl.SSLContextManagerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

/**
 * Created by prashanth on 29/5/15.
 */
public class NcpChannelInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    public void initChannel(SocketChannel socketChannel) throws Exception {
        SSLEngine context = SSLContextManagerFactory.getSslContextManager().createSSLEngine();

        NcpSession session = new NcpSession();
        session.setAllocator(socketChannel.alloc());
        session.setChannel(socketChannel);
        session.setSessionIP(socketChannel.remoteAddress().toString());
        session.setType(SocketType.tls);

        context.setUseClientMode(false);
        socketChannel.pipeline().addFirst(new SslHandler(context, true));
        socketChannel.pipeline().addLast(new NcpServerDataHandler(session));
    }
}
