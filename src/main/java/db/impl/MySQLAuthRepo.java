package db.impl;


import db.AuthenticationRepo;
import db.NeuerCloudDatabaseException;
import db.PreparedStatementCache;
import objects.JID;


import java.sql.*;
import java.util.logging.Logger;

/**
 * Created by prashanth on 26/12/14.
 */
public class MySQLAuthRepo implements AuthenticationRepo {

    private static final Logger log = Logger.getLogger(MySQLAuthRepo.class.getName());

    private static final String NEUERAUTOLOGIN = "{call NEUERAUTOLOGIN(?, ?)}";
    private static final String LOGOUT_USER_PLAIN = "{call TigUserLogout(?)}";


    private static Connection connection;

    private static String[] prepared_statements = {
            NEUERAUTOLOGIN, LOGOUT_USER_PLAIN
    };

    private static PreparedStatementCache cache = new PreparedStatementCache();

    public void initConnection(String dbUri){
        try {
            connection = DriverManager.getConnection(dbUri);
        } catch (SQLException e) {
            log.severe("Unable to create connection to the DB instance " + e.getMessage());
        }

        this.initStatements();
    }

    @Override
    public boolean loginUser(JID userJid, String password) throws NeuerCloudDatabaseException {
        PreparedStatement statement = cache.getPreparedStatement(NEUERAUTOLOGIN);

        try{

            statement.setString(1, userJid.getBareJID());
            statement.setString(2, password);

            statement.execute();
            ResultSet rs = statement.getResultSet();

            rs.next();
            Object result = rs.getObject(1);

            if(result instanceof String){
                if(((String) result).contains("/"))
                    return ((String)result).equals(userJid.getFullJID());
                else
                    return ((String)result).equals(userJid.getBareJID());
            }
        }catch (SQLException e){
            throw new NeuerCloudDatabaseException(e.getMessage());
        }
        return false;
    }

    @Override
    public boolean logoutUser(JID userJid) throws NeuerCloudDatabaseException {
        PreparedStatement statement = cache.getPreparedStatement(LOGOUT_USER_PLAIN);

        try{
            statement.setString(1, userJid.getBareJID());
            statement.execute();
        }catch (SQLException e){
            throw new NeuerCloudDatabaseException(e.getMessage());
        }

        return true;
    }

    public void initStatements(){
        try {
            for(String preparedStatement: prepared_statements) {
                cache.initPreparedStatement(preparedStatement, connection.prepareCall(preparedStatement));
            }
        } catch (SQLException e) {
            log.severe("Unable to prepare statement for the DB" + e.getMessage());
        }
    }
}
