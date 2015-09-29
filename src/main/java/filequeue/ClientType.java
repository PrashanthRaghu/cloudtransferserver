package filequeue;

import org.msgpack.annotation.Message;

/**
 *
 * Support for desktop clients will be added at a later stage
 *
 * The client types can be further extended as Mobile
 * Created by prashanth on 24/11/14.
 */

@Message
public enum ClientType {
    android,
    ios,
    browser,
    desktop
}
