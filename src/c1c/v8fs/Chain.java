/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package c1c.v8fs;

import com.google.common.io.ByteStreams;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author psyriccio
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Chain implements Bufferable {

    private long address;
    private List<Chunk> chunks;

    public Chain(byte[] data, int chunkSize) {
        ByteBuffer buf = ByteBuffer.wrap(data);
        int blockSize = data.length;
        while(buf.hasRemaining()) {
            if(chunkSize <= buf.remaining()) {
               Chunk chunk = new Chunk(buf.array(), buf.remaining(), false);
               chunks.add(chunk);
               break;
            } else {
                byte[] chd = new byte[chunkSize];
                buf.get(chd);
                Chunk chunk = new Chunk(chd, blockSize, buf.hasRemaining());
                chunks.add(chunk);
            }
        }
    }
    
    public byte[] getData() {
        long length = chunks.get(0).getHeader().getBlockSize();
        byte[] res = new byte[(int) length];
        ByteBuffer buf = ByteBuffer.wrap(res);
        for(Chunk chunk : chunks) {
            byte[] chdata = chunk.getData();
            if(length >= chunk.getHeader().getThisChunkSize()) {
                buf.put(chdata);
                length -= chdata.length;
            } else {
                buf.put(Arrays.copyOfRange(chdata, 0, (int) length));
            }
        };
        return res;
    }
    
    public byte[] getDataInflate() throws IOException {
        byte[] buf = getData();
        ByteArrayInputStream in = new ByteArrayInputStream(buf);
        InflaterInputStream inf = new InflaterInputStream(in, new Inflater(true));
        return ByteStreams.toByteArray(inf);
    }
    
    @Override
    public void writeToBuffer(ByteBuffer buffer) {
        chunks.stream().forEach((chunk) -> {
            chunk.writeToBuffer(buffer);
        });
    }

    @Override
    public void readFromBuffer(ByteBuffer buffer) {
        chunks = new ArrayList<>();
        boolean next = true;
        address = buffer.position();
        while(next) {
            Chunk chunk = new Chunk();
            chunk.readFromBuffer(buffer);
            chunks.add(chunk);
            next = chunk.getHeader().isNextChunkPresent();
        }
    }
    
}
