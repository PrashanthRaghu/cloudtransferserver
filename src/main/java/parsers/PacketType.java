package parsers;

/**
 * Created by prashanth on 30/12/14.
 */

/*
    Rules of every packet:

    1. Auth -> An auth packet begins with - and is , separated.
    2. Data -> A data packet begins with ! and is also comma seperated
 */
public enum PacketType {
    auth, data, chatdata, PacketType, invalid
}
