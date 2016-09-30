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
import java.util.HashMap;
import java.util.List;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

/**
 * Chain of chunks, representing a logical, solid block of data
 *
 * @author psyriccio
 */
@Data
@Builder
public class Chain implements Bufferable {

    private static int nextID = 0;
    private String id;
    private long address;
    private @Singular List<Chunk> chunks;
    private HashMap<String, Object> containerContext;

    private void setID() {
        this.id = Integer.toHexString(++nextID);
    }

    public Chain() {
        setID();
        chunks = new ArrayList<>();
    }

    public Chain(byte[] data, int chunkSize) {
        setID();
        chunks = new ArrayList<>();
        ByteBuffer buf = ByteBuffer.wrap(data);
        int blockSize = data.length;
        Chunk lastChunk = null;
        while (buf.hasRemaining()) {
            if (chunkSize <= buf.remaining()) {
                Chunk chunk = new Chunk(containerContext, buf.array(), blockSize + 6, false);
                chunks.add(chunk);
                if(lastChunk != null) {
                    lastChunk.setNextChunk(chunk);
                }
                lastChunk = chunk;
                break;
            } else {
                byte[] chd = new byte[chunkSize];
                if(buf.remaining() >= chunkSize) {
                    buf.get(chd);
                } else {
                    buf.get(chd, 0, buf.remaining());
                }
                Chunk chunk = new Chunk(containerContext, chd, blockSize + 6, buf.hasRemaining());
                chunks.add(chunk);
                if(lastChunk != null) {
                    lastChunk.setNextChunk(chunk);
                }
                lastChunk = chunk;
            }
        }
    }

    public Chain(String id, long address, List<Chunk> chunks, HashMap<String, Object> containerContext) {
        this.id = id;
        this.address = address;
        this.chunks = chunks;
        this.containerContext = containerContext;
    }
    
    public byte[] getData() {
        long length = chunks.get(0).getHeader().getBlockSize();
        byte[] res = new byte[(int) length];
        ByteBuffer buf = ByteBuffer.wrap(res);
        Chunk chunk = chunks.stream().findFirst().orElse(null);
        while (chunk != null) {
            byte[] chdata = chunk.getData();
            if (length >= chunk.getHeader().getThisChunkSize()) {
                buf.put(chdata);
                length -= chdata.length;
            } else {
                buf.put(Arrays.copyOfRange(chdata, 0, (int) length));
            }
            chunk = chunk.getNextChunk();
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
        Chunk lastChunk = null;
        while (next) {
            Chunk chunk = new Chunk();
            chunk.setContainerContext(containerContext);
            chunk.readFromBuffer(buffer);
            chunk.setNextChunk(null);
            if(lastChunk != null) {
                lastChunk.setNextChunk(chunk);
            }
            lastChunk = chunk;
            chunks.add(chunk);
            next = chunk.getHeader().isNextChunkPresent();
        }
    }

}
