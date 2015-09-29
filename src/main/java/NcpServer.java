import config.PropertiesManager;
import db.AuthRepoPoolFactory;
import filequeue.ChunkQueuePool;
import filequeue.ChunkQueuePoolFactory;
import handlers.NcpChannelInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import ssl.SSLContextManagerFactory;
import watchdogs.WatchdogManagers;
import websocket.WebSocketServerInitializer;

import java.util.logging.Logger;

/**
 * Created by prashanth on 29/5/15.
 */
public class NcpServer {

    private static Logger logger = Logger.getLogger(NcpServer.class.getName());
    NcpServerConfig config = new NcpServerConfig(5555, 5590);

    static {
        PropertiesManager.configureProperties();
        SSLContextManagerFactory.createSSLContextManager();
        AuthRepoPoolFactory.configureAuthRepoPool();
        ChunkQueuePool chunkQueuePool = new ChunkQueuePool();
        chunkQueuePool.initializePropertiesMap();
        chunkQueuePool.initializeQueue();
        ChunkQueuePoolFactory.setChunkQueuePool(chunkQueuePool);
        new WatchdogManagers().initializeJobs();
    }

    public void run() throws Exception {

        try {
            logger.info( "Starting the server for the socket channel on port : 5555" );

            ServerBootstrap serverBootstrap = new ServerBootstrap();

            serverBootstrap.group(config.getSocketBossGroup(), config.getSocketWorkerGroup())
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new NcpChannelInitializer())
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            logger.info( "Successfully started the server for the socket channel on port : 5555" );

            ChannelFuture socketChannel = serverBootstrap.bind(config.getPort()).sync();

            logger.info( "Starting the server for the websocket channel on port : 5590" );

            ServerBootstrap wsServerBootstrap = new ServerBootstrap();
            wsServerBootstrap.group(config.getWsBossGroup(), config.getWsWorkerGroup())
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new WebSocketServerInitializer());

            logger.info( "Successfully started the server for the websocket channel on port : 5590" );

            ChannelFuture websocketChannel = wsServerBootstrap.bind(config.getWsPort()).sync();
            websocketChannel.channel().closeFuture().sync();
            socketChannel.channel().closeFuture().sync();

        } finally {
            config.getSocketBossGroup().shutdownGracefully();
            config.getSocketWorkerGroup().shutdownGracefully();
            config.getWsBossGroup().shutdownGracefully();
            config.getSocketWorkerGroup().shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception{
        NcpServer server = new NcpServer();
        server.run();
    }


}
