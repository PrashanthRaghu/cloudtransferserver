package db;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Is a cache of prepared statements
 */
public class PreparedStatementCache {

    Map<String, PreparedStatement> cache = new ConcurrentHashMap<String, PreparedStatement>();

    Map<String, CallableStatement> callableStatementCache = new ConcurrentHashMap<String, CallableStatement>();

    public void initPreparedStatement(String key, PreparedStatement query){
        cache.put(key, query);
    }

    public void initCallableStatement(String key, CallableStatement query){
        callableStatementCache.put(key, query);
    }

    public PreparedStatement getPreparedStatement(String key){
        return cache.get(key);
    }

    public CallableStatement getCallableStatement(String key){
         return callableStatementCache.get(key);
    }
}
