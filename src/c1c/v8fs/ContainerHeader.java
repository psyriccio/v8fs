/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package c1c.v8fs;

import java.nio.ByteBuffer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.google.common.primitives.*;
import java.util.HashMap;

/**
 * Container header
 *
 * @author psyriccio
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContainerHeader implements Bufferable {

    private long firstFreeChunkAddress;
    private long defaultChunkSize;
    private long reservedMaybeFilesCount;
    private long reservedUnknown;
    private HashMap<String, Object> containerContext;

    @Override
    public void writeToBuffer(ByteBuffer buffer) {
        buffer
                .putInt(Integer.reverseBytes(Integer.MAX_VALUE))
                .putInt(Integer.reverseBytes(UnsignedInteger.valueOf(defaultChunkSize).intValue()))
                .putInt(Integer.reverseBytes(UnsignedInteger.valueOf(reservedMaybeFilesCount).intValue()))
                .putInt(Integer.reverseBytes(0));
    }

    @Override
    public void readFromBuffer(ByteBuffer buffer) {
        firstFreeChunkAddress = UnsignedInteger.fromIntBits(Integer.reverseBytes(buffer.getInt())).longValue();
        defaultChunkSize = UnsignedInteger.fromIntBits(Integer.reverseBytes(buffer.getInt())).longValue();
        reservedMaybeFilesCount = UnsignedInteger.fromIntBits(Integer.reverseBytes(buffer.getInt())).longValue();
        reservedUnknown = UnsignedInteger.fromIntBits(Integer.reverseBytes(buffer.getInt())).longValue();
    }

}
