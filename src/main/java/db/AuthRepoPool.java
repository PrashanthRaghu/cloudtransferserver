package db;


import db.impl.MySQLAuthRepo;
import objects.JID;

import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * Created by prashanth on 26/12/14.
 */
public class AuthRepoPool implements AuthenticationRepo{

    private static Logger logger = Logger.getLogger(AuthRepoPool.class.getName());

    // Default pool size = number of processors * 2
    private static int poolSize = Runtime.getRuntime().availableProcessors() * 2;

    private LinkedBlockingQueue<AuthenticationRepo> repoPool =
            new LinkedBlockingQueue<AuthenticationRepo>();

    // Usage analyzers: The connection pool must have the capacity to be auto-resized
    private static AtomicInteger currentCapacity;

    // Statistics measurer
    private static AtomicInteger statistics = new AtomicInteger(0);

    // Number of times the statistics > pool_size to resize the pool
    // TODO(prashanth): Make this configurable

    private static AtomicInteger numberSizeGreater = new AtomicInteger(20);

    private String dbUri;

    private String dbClass;

    public void initConnectionPool(Map<String, Object> settings){

        poolSize = Integer.parseInt((String) settings.get("auth_repo_pool_size"));
        String dbType = (String) settings.get("auth_dbtype");
        dbUri = (String) settings.get("auth_dburi");
        dbClass = MySQLAuthRepo.class.getName();
        currentCapacity = new AtomicInteger(poolSize);

        if(dbType.equals("mysql")){
            dbClass = MySQLAuthRepo.class.getName();
        }

        for(int i=0 ; i < poolSize ; i++){
            try {
                repoPool.offer((AuthenticationRepo) Class.forName(dbClass).newInstance());
            } catch (Exception e) {
                logger.severe("Unable to initialize the repo pool");
            }
        }

        initConnection(dbUri);
    }

    @Override
    public void initConnection(String dburi) {
        for(AuthenticationRepo repo: repoPool){
            repo.initConnection(dburi);
        }
    }

    @Override
    public boolean loginUser(JID userJid, String password) throws NeuerCloudDatabaseException {
        currentCapacity.addAndGet(1);
        resizeIfNeeded();

        currentCapacity.decrementAndGet();

        try {
            AuthenticationRepo repo = repoPool.take();
            boolean result = repo.loginUser(userJid, password);
            repoPool.offer(repo);
            return result;
        } catch (InterruptedException e) {
            logger.severe("Unable to fetch authentication repo for the user repo pool" + e.getMessage());
            throw new NeuerCloudDatabaseException(e.getMessage());
        }
    }

    @Override
    public boolean logoutUser(JID userJid) throws NeuerCloudDatabaseException {
        currentCapacity.addAndGet(1);
        resizeIfNeeded();

        try {
            AuthenticationRepo repo = repoPool.take();
            repo.logoutUser(userJid);
            repoPool.offer(repo);
        } catch (InterruptedException e) {
            logger.severe("Unable to fetch authentication repo for the user repo pool" + e.getMessage());
            throw new NeuerCloudDatabaseException(e.getMessage());
        }

        currentCapacity.decrementAndGet();
        return false;
    }

    public void resizeIfNeeded(){
        if(currentCapacity.get() >= poolSize){
            statistics.addAndGet(1);

            if(statistics.get() > numberSizeGreater.get()){
                synchronized (this){
                    poolSize *= 2;

                    for(int i=0 ; i < poolSize / 2 ; i++){
                        try {
                            AuthenticationRepo repo = (AuthenticationRepo) Class.forName(dbClass).newInstance();
                            repoPool.offer(repo);
                            repo.initConnection(dbUri);
                        } catch (Exception e) {
                            logger.severe("Unable to initialize the repo pool");
                        }
                    }
                }
            }
        }
    }

}
