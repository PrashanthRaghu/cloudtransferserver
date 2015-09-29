package ssl;

import config.PropertiesManager;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by prashanth on 9/1/15.
 */
public class SSLContextManager {

    private String[] componentProperties = {"certspath", "certspwd", "trustspath", "trustspwd", "tls_hostname", "tls_port"};

    private Map<String, Object> propertiesMap;

    private SSLContext sslContext;

    private static Logger logger = Logger.getLogger(SSLContextManager.class.getName());

    public SSLEngine createSSLEngine(){

        SSLEngine engine = sslContext.createSSLEngine( (String) propertiesMap.get("tls_hostname"),
                Integer.parseInt((String)propertiesMap.get("tls_port")) );

        return engine;

    }

    public void initializeComponent(String protocol){

        propertiesMap = PropertiesManager.getComponentProperties(componentProperties);

        String certsPath = (String) propertiesMap.get("certspath");
        String certsPwd = (String) propertiesMap.get("certspwd");

        String trustsPath = (String) propertiesMap.get("trustspath");
        String trustspwd = (String) propertiesMap.get("trustspwd");

        try {

            KeyStore ksKeys = KeyStore.getInstance("JKS");
            ksKeys.load(new FileInputStream(certsPath), certsPwd.toCharArray());

            KeyStore ksTrust = KeyStore.getInstance("JKS");
            ksTrust.load(new FileInputStream(trustsPath), trustspwd.toCharArray());

            KeyManagerFactory kmf =
                    KeyManagerFactory.getInstance("SunX509");
            kmf.init(ksKeys, certsPwd.toCharArray());

            TrustManagerFactory tmf =
                    TrustManagerFactory.getInstance("SunX509");
            tmf.init(ksTrust);

            sslContext = SSLContext.getInstance(protocol);

            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());

            if(logger.isLoggable(Level.INFO))
                logger.info("---- SSL context successfully initialized -----");

        } catch (Exception e) {
            logger.warning("Error while creating SSL context:" + e.getMessage());
        }

    }
}


