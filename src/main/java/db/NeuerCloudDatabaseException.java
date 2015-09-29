package db;

/**
 * Created by prashanth on 9/12/14.
 */

public class NeuerCloudDatabaseException extends Exception {

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public NeuerCloudDatabaseException(String message){
        this.message = message;
    }
    private String message;
}
