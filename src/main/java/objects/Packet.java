package objects;


import io.netty.buffer.ByteBuf;
import parsers.PacketType;
import session.NcpSession;

import java.nio.ByteBuffer;

/**
 *
 * A packet is a simple implementation of data which flows through the NCP
 * protocol.
 *
 * There are two kinds of packets:
 *
 * 1. Operation Packet -> Useful for session negotiation and authentication
 * 2. Data transfer Packet -> Which contains the data and meta information about the file
 * being transferred on the wire.
 *
 * We differentiate these packets by using the operation parameter specified in the Packet class
 *
 * NOTE:
 *
 * 1. Operation has been made a string to simplify
 *
 * Created by prashanth on 27/12/14.
 */

public class Packet {

    PacketType operation;
    byte[] data;
    ByteBuf buf;
    long startTime;
    long endTime;

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public String getTransferTime(){
        return "^" + String.valueOf(System.currentTimeMillis() - startTime);
    }

    public NcpSession getSession() {
        return session;
    }

    public void setSession(NcpSession session) {
        this.session = session;
    }

    NcpSession session;

    /*
        @TODO(prashanthr)

        As a part of the transitioning plan try to move to fixed ByteBuffers.

        Create a data transfer session and allocate the packet accordingly.
     */
    boolean isComplete = false;
    boolean isValid = true;
    JID packetOwner;
    private int sessionHashCode;
    boolean validSession = true;
    boolean isStartedFilling = false;
    long length = 0l;
    long lastLength = 0l;

    public ByteBuffer getWriteBuffer() {
        return writeBuffer;
    }

    public void setWriteBuffer(ByteBuffer writeBuffer) {
        this.writeBuffer = writeBuffer;
    }

    private ByteBuffer writeBuffer = ByteBuffer.allocate(10);

    public boolean isStartedFilling() {
        return isStartedFilling;
    }

    public void setStartedFilling(boolean isStartedFilling) {
        this.isStartedFilling = isStartedFilling;
    }

    public boolean isValidSession() {
        return validSession;
    }

    public void setValidSession(boolean validSession) {
        this.validSession = validSession;
    }

    public int getSessionHashCode() {
        return sessionHashCode;
    }

    public void setSessionHashCode(int sessionHashCode) {
        this.sessionHashCode = sessionHashCode;
    }

    public JID getPacketOwner() {
        return packetOwner;
    }

    public void setPacketOwner(JID packetOwner) {
        this.packetOwner = packetOwner;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean isValid) {
        this.isValid = isValid;
    }

    public PacketType getOperation() {
        return operation;
    }

    public void setOperation(PacketType operation) {
        this.operation = operation;
    }

    public Boolean hasLengthChanged() {
        return lastLength != length;
    }

    public void appendData(byte[] dataPart){

        if(!isComplete) {
            buf.writeBytes(dataPart);
            lastLength = length;
            length += dataPart.length;
        }
    }

    public void setComplete(){
        if(isComplete)
            return;

        isComplete = true;

        if(data == null){
            ByteBuffer buffer = buf.nioBuffer();
            data = new byte[buffer.remaining()];
            buffer.get(data);
        }

        buf.release();
    }

    public boolean isComplete(){
        return isComplete;
    }

    public byte[] getData(){
        setComplete();
        return data;
    }

    @Override
    public String toString(){
        return operation + ":<completed:" + isComplete + ">:" + "length:" + this.length;
    }

    public ByteBuf toWire(){
        return buf;
    }

    public long getDataLength(){
        return length;
    }


    /*
        Make the data ready for garbage collection
     */

    public void flushPacket(){
        data = null;
    }
}
