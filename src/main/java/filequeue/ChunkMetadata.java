package filequeue;

import org.msgpack.annotation.Message;

import java.util.Date;

/**
 * Represents the metadata of the file/chunk
 *
 * Properties of the metadata:
 *
 * 1. Media type - I (g)
 * 2. File/Chunk size - I
 * 3. Owner ( Full JID of the creating user ) - C
 * 4. Time uploaded ( Has to be validated by the server ) - C
 * 5. Number of the chunk - L
 * 6. Compression Type ( Gzip by default ) - I
 * 7. Folder Id ( File Chunk Path : <amazon_bucket>/userjid/folder_path/file_chunk ) -I
 * 8. CheckSum ( SHA 256 checksum of the file chunk to check if it already exists in the database ) - L
 * 9. Device ID - C ( Resource of the client )
 * 10. Client Type ( Android / iOS / Web / Desktop ) - Make an elaborate list of this argument - I
 * 11. Name - I
 * 12.
 * Note:
 *
 * 1. All date time interactions with the server will be using UTC and the clients convert UTC to their
 * respective timezone.
 *
 */

@Message
public class ChunkMetadata {

    private String mediaType;
    private long chunkSize;
    private String chunkOwner;
    private Date timeUploaded;
    private long chunkNumber;
    private String compressionType;
    private Long folderId;
    private String checkSum;
    private String deviceID;
    private String clientType;
    private String name;
    private Integer chunkNum;
    private String transferId;
    private Long fileId = 0L;

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public String getTransferId() {
        return transferId;
    }

    public void setTransferId(String transferId) {
        this.transferId = transferId;
    }

    public Integer getChunkNum() {
        return chunkNum;
    }

    public void setChunkNum(Integer chunkNum) {
        this.chunkNum = chunkNum;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public long getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(long chunkSize) {
        this.chunkSize = chunkSize;
    }

    public String getChunkOwner() {
        return chunkOwner;
    }

    public void setChunkOwner(String chunkOwner) {
        this.chunkOwner = chunkOwner;
    }

    public Date getTimeUploaded() {
        return timeUploaded;
    }

    public void setTimeUploaded(Date timeUploaded) {
        this.timeUploaded = timeUploaded;
    }

    public long getChunkNumber() {
        return chunkNumber;
    }

    public void setChunkNumber(long chunkNumber) {
        this.chunkNumber = chunkNumber;
    }

    public String getCompressionType() {
        return compressionType;
    }

    public void setCompressionType(String compressionType) {
        this.compressionType = compressionType;
    }

    public Long getFolderPath() {
        return folderId;
    }

    public void setFolderPath(Long folderPath) {
        this.folderId = folderPath;
    }

    public String getCheckSum() {
        return checkSum;
    }

    public void setCheckSum(String checkSum) {
        this.checkSum = checkSum;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public String getClientType() {
        return clientType;
    }

    public void setClientType(String clientType) {
        this.clientType = clientType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMetadata(String key, String value){
        if(key.equals("name")){
            this.name = value;
        }else if (key.equals("checksum")){
            this.checkSum = value;
        }else if (key.equals("mediatype")){
            this.mediaType = value;
        }else if (key.equals("size")){
            this.chunkSize = Long.valueOf(value);
        }else if (key.equals("cmptype")){
            this.compressionType = value;
        }else if (key.equals("folder")){
            this.folderId = Long.valueOf(value);
        }else if (key.equals("deviceid")) {
            this.deviceID = value;
        }else if (key.equals("devicetype")) {
            this.clientType = value;
        }else if(key.equals("owner")){
            this.chunkOwner = value;
        }
    }
}
