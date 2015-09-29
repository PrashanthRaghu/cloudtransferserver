/**
 * Created by prashanth on 29/5/15.
 */
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

/**
 * Discards any incoming data.
 */
public class NcpServerConfig {

    public int getPort() {
        return port;
    }

    private int port;

    private int wsPort;

    public EventLoopGroup getSocketBossGroup() {
        return socketBossGroup;
    }

    public EventLoopGroup getSocketWorkerGroup() {
        return socketWorkerGroup;
    }

    EventLoopGroup socketBossGroup = new NioEventLoopGroup(1); // (1)
    EventLoopGroup socketWorkerGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() * 4);

    EventLoopGroup wsBossGroup = new NioEventLoopGroup(1);
    EventLoopGroup wsWorkerGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() * 4);

    public int getWsPort() {
        return wsPort;
    }

    public void setWsPort(int wsPort) {
        this.wsPort = wsPort;
    }

    public EventLoopGroup getWsBossGroup() {
        return wsBossGroup;
    }

    public void setWsBossGroup(EventLoopGroup wsBossGroup) {
        this.wsBossGroup = wsBossGroup;
    }

    public EventLoopGroup getWsWorkerGroup() {
        return wsWorkerGroup;
    }

    public void setWsWorkerGroup(EventLoopGroup wsWorkerGroup) {
        this.wsWorkerGroup = wsWorkerGroup;
    }

    public NcpServerConfig(int port, int wsPort) {
        this.port = port;
        this.wsPort = wsPort;
    }
}
