package tokenmanagement;


import com.google.gson.Gson;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import utils.IdGenerator;

import java.util.Map;

/**
 *
 *
 * Created by prashanth on 7/1/15.
 */
public class RedisTokenManager {

    private JedisPool jedisPool;
    private Gson gsonBuilder = new Gson();
    private String CHUNK_INFO_CONSTANT = "_chunk";
    private String FILE_INFO_CONSTANT = "_fileid";

    public boolean isTokenValid(String userFullJid, String token){
        Jedis jedisConn = jedisPool.getResource();

        boolean result = jedisConn.exists(userFullJid);

        if(!result){
            return false;
        }

        Map<String, String> resultMap = jedisConn.hgetAll(userFullJid);

        jedisPool.returnResource(jedisConn);

        return resultMap.get("token").equals(token);
    }

    public Map<String, String> getMetadata(String userFullJid){
        Jedis jedisConn = jedisPool.getResource();

        Map<String, String> resultMap = jedisConn.hgetAll(userFullJid);

        jedisPool.returnResource(jedisConn);

        return (Map<String, String>) gsonBuilder.fromJson(resultMap.get("metadata"), Object.class);
    }

    public String manageSessionChunkInfo(String token){
        Jedis jedisConn = jedisPool.getResource();

        String chunkInfo = jedisConn.get(token + CHUNK_INFO_CONSTANT);

        if(chunkInfo == null){
            jedisConn.set(token + CHUNK_INFO_CONSTANT, "2");
            jedisPool.returnResource(jedisConn);
            return "1";
        }else {
            jedisConn.incr(token + CHUNK_INFO_CONSTANT);
            jedisPool.returnResource(jedisConn);
            return chunkInfo;
        }
    }

    public String getFileId(String token){
        Jedis jedisConn = jedisPool.getResource();

        String fileInfo = jedisConn.get(token + FILE_INFO_CONSTANT);

        if(fileInfo == null){
            Long fileIdVal = IdGenerator.generateFileId();
            jedisConn.set(token + FILE_INFO_CONSTANT, String.valueOf(fileIdVal));
            jedisPool.returnResource(jedisConn);
            return String.valueOf(fileIdVal);
        }else {
            jedisPool.returnResource(jedisConn);
            return fileInfo;
        }
    }

    public void initializeConnectionPool(Map<String, Object> settings){
        jedisPool = new JedisPool((String) settings.get("cachehost"),
                Integer.valueOf((String) settings.get("cacheport")));
    }
}
