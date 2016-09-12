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
import lombok.NoArgsConstructor;

/**
 *
 * @author psyriccio
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndexEntry implements Bufferable {

    private long attributesAddress;
    private long contentAddress;
    private long reserved;
    
    @Override
    public void writeToBuffer(ByteBuffer buffer) {

        buffer
                .putInt(Integer.reverseBytes(UnsignedInteger.valueOf(attributesAddress).intValue()))
                .putInt(Integer.reverseBytes(UnsignedInteger.valueOf(contentAddress).intValue()))
                .putInt(Integer.reverseBytes(UnsignedInteger.valueOf(reserved).intValue()));
    }

    @Override
    public void readFromBuffer(ByteBuffer buffer) {
        attributesAddress = UnsignedInteger.fromIntBits(Integer.reverseBytes(buffer.getInt())).longValue();
        contentAddress = UnsignedInteger.fromIntBits(Integer.reverseBytes(buffer.getInt())).longValue();
        reserved = UnsignedInteger.fromIntBits(Integer.reverseBytes(buffer.getInt())).longValue();
    }
    
}
