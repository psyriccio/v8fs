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
 * One element of file index. Contains address-links to attributes and content
 * chains
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

    public String getAttributesAddressXML() {
        return Main.to8Digits(Long.toHexString(this.attributesAddress));
    }

    public void setAttributesAddressXML(String val) {
        this.attributesAddress = Long.parseUnsignedLong(val, 16);
    }

    public String getContentAddressXML() {
        return Main.to8Digits(Long.toHexString(this.contentAddress));
    }

    public void setContentAddressXML(String val) {
        this.contentAddress = Long.parseUnsignedLong(val, 16);
    }

    public String getReservedXML() {
        return Main.to8Digits(Long.toHexString(this.reserved));
    }

    public void setReservedXML(String val) {
        this.reserved = Long.parseUnsignedLong(val, 16);
    }

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
