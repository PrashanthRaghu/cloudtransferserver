package ssl;

/**
 * Created by prashanth on 30/5/15.
 */

public class SSLContextManagerFactory {

    private static SSLContextManager sslContextManager;

    public static SSLContextManager getTlsContextManager() {
        return tlsContextManager;
    }

    private static SSLContextManager tlsContextManager;


    public static void createSSLContextManager(){
        sslContextManager = new SSLContextManager();
        sslContextManager.initializeComponent("SSL");

        tlsContextManager = new SSLContextManager();
        tlsContextManager.initializeComponent("TLS");
    }

    public static SSLContextManager getSslContextManager(){
        return sslContextManager;
    }


}

