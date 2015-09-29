package parsers.plaintextparser;

import parsers.PacketType;

import java.util.HashMap;
import java.util.regex.Pattern;

/**
 *
 * Packets are classified as follows:
 *
 * 1. Begins with 9153490440922733 -> Data packet
 * 2. Begins with 4233532102450273 -> Auth packet
 * 3. Begins with 9153490440922734 -> Chat Data packet
 *
 * All packets must end with 7164602279121080
 *`
 * We can use this parsing protocol in order to understand the packet data
 * and pass it to the higher layers for application packet formation
 *
 * Created by prashanth on 30/12/14.
 */

public class PacketIdentifier {

    static HashMap<String, PacketType> typeMapping = new HashMap<>();

    static {
        typeMapping.put("9153490440922733", PacketType.data);
        typeMapping.put("9153490440922734", PacketType.chatdata);
        typeMapping.put("4233532102450273", PacketType.auth);
    }

    public static boolean isBeginningOfPacket(String packetData){
        return typeMapping.containsKey(packetData);
    }

    public static PacketType getPacketType(String packetData){
       PacketType result = typeMapping.get(packetData);
       return result == null ? PacketType.invalid : result;
    }

    public static boolean isLastPartOfPacket(String packetData){
        return packetData.trim().endsWith("7164602279121080");
    }
}
