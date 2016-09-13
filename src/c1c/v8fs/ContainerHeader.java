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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/**
 *
 * @author psyriccio
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class ContainerHeader implements Bufferable {

    private @XmlAttribute long firstFreeChunkAddress;
    private @XmlAttribute long defaultChunkSize;
    private @XmlAttribute long reservedMaybeFilesCount;
    private @XmlAttribute long reservedUnknown;
    
    @Override
    public void writeToBuffer(ByteBuffer buffer) {
        buffer
                .putInt(Integer.reverseBytes(UnsignedInteger.valueOf(firstFreeChunkAddress).intValue()))
                .putInt(Integer.reverseBytes(UnsignedInteger.valueOf(defaultChunkSize).intValue()))
                .putInt(Integer.reverseBytes(UnsignedInteger.valueOf(reservedMaybeFilesCount).intValue()))
                .putInt(Integer.reverseBytes(UnsignedInteger.valueOf(reservedUnknown).intValue()));
    }

    @Override
    public void readFromBuffer(ByteBuffer buffer) {
        firstFreeChunkAddress = UnsignedInteger.fromIntBits(Integer.reverseBytes(buffer.getInt())).longValue();
        defaultChunkSize = UnsignedInteger.fromIntBits(Integer.reverseBytes(buffer.getInt())).longValue();
        reservedMaybeFilesCount = UnsignedInteger.fromIntBits(Integer.reverseBytes(buffer.getInt())).longValue();
        reservedUnknown = UnsignedInteger.fromIntBits(Integer.reverseBytes(buffer.getInt())).longValue();
    }
    
}
