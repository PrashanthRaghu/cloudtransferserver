package parsers.plaintextparser;

import objects.Auth;
import objects.ChatData;
import objects.Data;
import objects.Packet;
import parsers.PacketParser;
import parsers.PacketType;

import java.util.regex.Pattern;

/**
 *
 * A simple message parser which is based on the first character of the incoming message.
 *
 * This protocol is based on the RESP protocol specified at http://redis.io/topics/protocol
 *
 * 1. All authentication messages begin with a +
 * 2. Data packets begin with a $
 * 3. Session management messages begin with a !
 *
 * We will also create a server to server binary serialization protocol for intra-cluster communication
 *
 * Notes:
 *
 * 1. All headers to the server are of fixed length to help faster parsing of data
 *
 * Created by prashanth on 27/12/14.
 */

public class PlainTextParser implements PacketParser {

    String authPacketPattern = "!(.*)%";
    String dataPacketPattern = "-(.*)%";

    Pattern authPattern = Pattern.compile(authPacketPattern);
    Pattern dataPattern = Pattern.compile(dataPacketPattern);

    @Override
    public void parseData(Packet packetDestination) {

        if(packetDestination.getOperation() == PacketType.auth){

            String packetData = new String(packetDestination.getData());

            String authPacketData = packetData.substring(16,
                    packetData.length() - 16);

            String [] packetElements = authPacketData.split(",");

            try {
                Auth packet = (Auth) packetDestination;
                packet.setUserJid(packetElements[0]);
                packet.setPassword(packetElements[1]);
                packetDestination.setValid(true);
            }catch(Exception e){
                packetDestination.setValid(false);
            }
        }else if (packetDestination.getOperation() == PacketType.data){

            byte[] packetData = packetDestination.getData();

            try {
                Data packet = (Data) packetDestination;
                packet.setToken(PlainTextParserUtil.getToken(packetData));
                packetDestination.setValid(true);
            }catch(Exception e){
                packetDestination.setValid(false);
            }
        }else if (packetDestination.getOperation() == PacketType.chatdata){
            byte[] packetData = packetDestination.getData();

            try {
                ChatData packet = (ChatData) packetDestination;
                packet.setToken(PlainTextParserUtil.getToken(packetData));
                packetDestination.setValid(true);
            }catch(Exception e){
                packetDestination.setValid(false);
            }
        }
    }
}
