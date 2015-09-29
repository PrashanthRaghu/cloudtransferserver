package db;


import objects.JID;

/**
 *
 * Implements operations related to authentication of user sessions
 *
 *
 * Login:
 *
 * 1. For the authentication of users we primarily use the TigUserPlainPw Mysql stored procedure
 * to authenticate the users into the neuer cloud.
 *
 * 2.
 * Created by prashanth on 26/12/14.
 */
public interface AuthenticationRepo {

    public void initConnection(String dburi);

    // We currently support the plain mechanism only.
    public boolean loginUser(JID userJid, String password) throws NeuerCloudDatabaseException;

    public boolean logoutUser(JID userJid) throws NeuerCloudDatabaseException;
}
