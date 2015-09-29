package utils;

import java.util.regex.Pattern;

/**
 * Created by prashanth on 26/12/14.
 */
public class FormatVerifiers {


    /*
        Format: -> Any valid Base64 character string followed by our domain JID
     */
    private static Pattern jidPattern = Pattern.compile("^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)@"
            + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})/([A-Za-z0-9])+");

    public static boolean verifyJID(String jidAsString){
        return jidPattern.matcher(jidAsString).matches();
    }
}
