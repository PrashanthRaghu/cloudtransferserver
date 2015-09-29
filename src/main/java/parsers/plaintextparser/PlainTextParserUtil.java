package parsers.plaintextparser;

/**
 * Created by prashanth on 8/1/15.
 */
public class PlainTextParserUtil {

    public static String getToken(byte[] packetdata){
        return new String(packetdata, 16 , 344);
    }

}
