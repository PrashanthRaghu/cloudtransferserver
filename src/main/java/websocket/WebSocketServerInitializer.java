package websocket;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslHandler;
import objects.SocketType;
import session.NcpSession;
import ssl.SSLContextManagerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

/**
 * Created by prashanth on 1/6/15.
 */
public class WebSocketServerInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        SSLEngine engine = SSLContextManagerFactory.getSslContextManager().createSSLEngine();

        engine.setUseClientMode(false);

        NcpSession session = new NcpSession();
        session.setAllocator(socketChannel.alloc());
        session.setChannel(socketChannel);
        session.setType(SocketType.websocket);
        session.setSessionIP(socketChannel.remoteAddress().toString());

        pipeline.addFirst(new SslHandler(engine, true));
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(65536));
        pipeline.addLast(new WebSocketServerHandler(session));
    }
}
