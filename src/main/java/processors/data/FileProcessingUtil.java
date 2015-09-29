package processors.data;

import filequeue.ChunkMetadata;

import java.util.Map;

/**
 * Created by prashanth on 21/1/15.
 */
public class FileProcessingUtil {

    public static ChunkMetadata extractChunkMetadata(Map<String, String> properties, long chunkSize){
        ChunkMetadata chunkMetadata = new ChunkMetadata();
        chunkMetadata.setChunkNum(Integer.valueOf(properties.get("chunkinfo")));
        chunkMetadata.setChunkOwner(properties.get("owner"));
        chunkMetadata.setChunkNumber(Integer.valueOf(properties.get("chunkinfo")));
        chunkMetadata.setClientType(properties.get("devicetype"));
        chunkMetadata.setFolderPath(Long.valueOf(properties.get("folderid")));
        chunkMetadata.setChunkSize(chunkSize - 376);
        chunkMetadata.setMediaType(properties.get("mediatype"));
        chunkMetadata.setName(properties.get("name"));
        chunkMetadata.setCompressionType(properties.get("cmptype"));
        chunkMetadata.setTransferId(String.valueOf(properties.get("transferid")));
        chunkMetadata.setFileId(Long.valueOf(properties.get("fileid")));
        return chunkMetadata;
    }
}
