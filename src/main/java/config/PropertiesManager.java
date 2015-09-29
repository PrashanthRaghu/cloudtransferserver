package config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Created by prashanth on 29/5/15.
 */
public class PropertiesManager {

    private static Logger log = Logger.getLogger(PropertiesManager.class.getName());
    private static ConcurrentHashMap<String, Object> configurationProperties =
            new ConcurrentHashMap<String, Object>();

    public static void configureProperties(){
        Properties properties = new Properties();
        String propertiesFileName = System.getProperty("user.home") + File.separator +
                ".neuercloud" + File.separator + "cloud-netty.properties";

        InputStream inputStream = null;

        try {
            inputStream = new FileInputStream(propertiesFileName);
            properties.load(inputStream);
        } catch (Exception e) {
            log.warning("Configuration properties file missing. System using predefined defaults. Error: " + e.getMessage());
        }

        Enumeration definedProperties = properties.propertyNames();

        while (definedProperties.hasMoreElements()){
            String propertyKey = (String) definedProperties.nextElement();
            Object propertyValue = properties.getProperty(propertyKey);
            configurationProperties.put(propertyKey, propertyValue);
        }
    }

    public static Map<String, Object> getComponentProperties(String[] requiredProps){
        Map<String, Object> requiredPropsMap = new HashMap<String, Object>();

        for(String prop: requiredProps){
            requiredPropsMap.put(prop, configurationProperties.get(prop));
        }

        return requiredPropsMap;
    }
}
