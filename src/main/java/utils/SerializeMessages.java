package utils;

import filequeue.ChatDataChunk;
import filequeue.Chunk;
import org.msgpack.MessagePack;
import org.msgpack.packer.Packer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by prashanth on 25/11/14.
 *
 * TODO(prashanth):
 * 1. Replace msgpack with either Apache Thrift / ProtoBuf
 */

public class SerializeMessages {

    private static MessagePack messagePack = new MessagePack();

    /*
        TODO(prashanth):
        The IO exception might cause the whole chunk to be reuploaded
        so handle with care. Try to avoid errors as much as possible.
     */

    /*
        Memory inefficient as it creates another copy of the byteArray
     */

    public static byte[] serializeFile(Chunk chunk) throws IOException {
        ByteArrayOutputStream serializer = new ByteArrayOutputStream();
        Packer packer = messagePack.createPacker(serializer);
        packer.write(chunk);
        return serializer.toByteArray();
    }

    public static byte[] serializeChatFile(ChatDataChunk chunk) throws IOException {
        ByteArrayOutputStream serializer = new ByteArrayOutputStream();
        Packer packer = messagePack.createPacker(serializer);
        packer.write(chunk);
        return serializer.toByteArray();
    }

}
