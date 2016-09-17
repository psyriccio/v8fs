/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package c1c.v8fs;

import com.google.common.primitives.UnsignedInteger;
import java.nio.ByteBuffer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * A minimal piece of data in matadata containers
 *
 * @author psyriccio
 */
@Data
@Builder
@AllArgsConstructor
public class Chunk implements Bufferable {

    private static int nextID = 0;

    private String id;
    private ChunkHeader header;
    private byte[] data;

    private void setID() {
        this.id = Integer.toHexString(++nextID);
    }

    public Chunk() {
        setID();
    }

    public Chunk(byte[] data, int blockSize, boolean hasNext) {
        setID();
        this.data = data;
        header = new ChunkHeader(blockSize, data.length, 0, hasNext);
    }

    @Override
    public void writeToBuffer(ByteBuffer buffer) {
        if (header.isNextChunkPresent()) {
            header.setNextChunkAddress(buffer.position() + 31 + data.length);
        } else {
            header.setNextChunkAddress(UnsignedInteger.MAX_VALUE.longValue());
        }
        header.writeToBuffer(buffer);
        buffer.put(data);
    }

    @Override
    public void readFromBuffer(ByteBuffer buffer) {
        header = new ChunkHeader();
        header.readFromBuffer(buffer);
        data = new byte[UnsignedInteger.valueOf(header.getThisChunkSize()).intValue()];
        buffer.get(data);
    }

}
