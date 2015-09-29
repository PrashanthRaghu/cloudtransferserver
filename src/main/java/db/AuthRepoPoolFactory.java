package db;

import config.PropertiesManager;

import java.util.Map;

/**
 * Created by prashanth on 2/1/15.
 */
public class AuthRepoPoolFactory {

    private static String[] authRepoPoolPropKeys = {"auth_repo_pool_size", "auth_dbtype", "auth_dburi"};

    private static Map<String, Object> authRepoPoolProp;

    private static AuthRepoPool authRepoPool = new AuthRepoPool();

    public static void configureAuthRepoPool(){
        authRepoPoolProp = PropertiesManager.getComponentProperties(authRepoPoolPropKeys);
        authRepoPool.initConnectionPool(authRepoPoolProp);
    }

    public static AuthRepoPool getAuthRepoPool(){
        return authRepoPool;
    }
}
