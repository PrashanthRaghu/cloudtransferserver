package processors.data;

import config.PropertiesManager;
import filequeue.*;
import objects.Data;
import objects.Packet;
import parsers.PacketType;
import processors.PacketProcessorsIfc;
import session.NcpSession;
import tokenmanagement.RedisTokenManager;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 *
 *
 * Created by prashanth on 31/12/14.
 */
public class UserFilesProcessor implements PacketProcessorsIfc {

    private Logger logger = Logger.getLogger(UserFilesProcessor.class.getName());

    private RedisTokenManager tokenManager = null;

    private ChunkQueuePool chunkQueuePool = null;

    private String[] plugin_settings = {"cachehost", "cacheport"};

    @Override
    public void process(Packet packet, List<Packet> results, NcpSession session) {

        if(tokenManager == null){
            synchronized (this) {
                if(tokenManager == null) {
                    tokenManager = new RedisTokenManager();
                    tokenManager.initializeConnectionPool(getSettings());
                }
            }
        }

        if(chunkQueuePool == null){
            synchronized (this) {
                if(chunkQueuePool == null) {
                    chunkQueuePool = ChunkQueuePoolFactory.getChunkQueuePool();
                }
            }
        }

        Packet result = new Data(packet.getSession().getAllocator(), 10);
        result.setEndTime(packet.getEndTime());
        result.setStartTime(packet.getStartTime());

        result.setSession(packet.getSession());

        if(session == null || !session.isAuthenticated()){
            result.appendData("--1%".getBytes());
            result.setValidSession(false);
            results.add(result);
            return;
        }

        Data packetAsData = (Data) packet;

        String token = packetAsData.getToken();

        boolean isTokenValid = tokenManager.isTokenValid(packet.getPacketOwner().getFullJID(), token);

        if(!isTokenValid){
            result.appendData("--2%".getBytes());
            results.add(result);
            return;
        }

        Map<String, String> fileMetadata = tokenManager.getMetadata(packet.getPacketOwner().getFullJID());

        System.out.println(fileMetadata);

        if(fileMetadata == null){
            result.appendData("--2%".getBytes());
            results.add(result);
            return;
        }

        fileMetadata.put("owner", packet.getPacketOwner().getBareJID());

        String chunkInfo = "-1";

        if(fileMetadata.get("chunked").equals("1")){
            chunkInfo = tokenManager.manageSessionChunkInfo(token);
        }

        fileMetadata.put("fileid", tokenManager.getFileId(token));

        fileMetadata.put("chunkinfo", chunkInfo);

        ChunkMetadata metadata = FileProcessingUtil.extractChunkMetadata(fileMetadata, packet.getDataLength());

        metadata.setDeviceID(packet.getPacketOwner().getResource());

        if (packetAsData.getOperation() == PacketType.data) {
            Chunk chunkToUpload = new Chunk();
            chunkToUpload.setData(packetAsData.getData());
            chunkToUpload.setMetadata(metadata);
            chunkQueuePool.insertChunk(chunkToUpload, "file");
        }else {
            ChatDataChunk chunkToUpload = new ChatDataChunk();
            chunkToUpload.setData(packetAsData.getData());
            chunkToUpload.setMetadata(metadata);
            chunkToUpload.setTo(fileMetadata.get("to"));
            chunkQueuePool.insertChunk(chunkToUpload, "chatfile");
        }

        result.appendData("-1%".getBytes());
        result.setValidSession(true);
        result.setValid(true);
        results.add(result);
    }

    public Map<String, Object> getSettings(){
        return PropertiesManager.getComponentProperties(plugin_settings);
    }
}
