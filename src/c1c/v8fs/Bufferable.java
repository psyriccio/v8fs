/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package c1c.v8fs;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Reading/Writing from/to buffer base feature interface
 *
 * @author psyriccio
 */
public interface Bufferable {

    public void writeToBuffer(ByteBuffer buffer);

    public void readFromBuffer(ByteBuffer buffer);

    default public byte[] asByteArray() {
        ByteBuffer buf = ByteBuffer.allocate(1024*1024*30);
        writeToBuffer(buf);
        int size = buf.position();
        buf.limit(size);
        return Arrays.copyOfRange(buf.compact().slice().array(), 0, size);
    }
    
}
