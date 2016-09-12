/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package c1c.v8fs;

import com.google.common.collect.Lists;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.Date;
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
public class Attributes implements Bufferable {

    private Date creationDate;
    private Date modifyDate;
    private long reserved;
    private String name;
    
    private byte[] codeDateTime(Date dateTime) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dateTime);
        long val = cal.getTimeInMillis();
        return UnsignedLong.fromLongBits(val).bigIntegerValue().toByteArray();
    }
    
    private Date decodeDateTime(byte[] val) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(UnsignedLong.valueOf((new BigInteger(val))).longValue());
        return cal.getTime();
    }
    
    @Override
    public void writeToBuffer(ByteBuffer buffer) {
        buffer.put(codeDateTime(creationDate)).put(codeDateTime(modifyDate)).putInt(UnsignedInteger.valueOf(reserved).intValue());
        try {
            buffer.put(name.getBytes("UTF-16"));
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void readFromBuffer(ByteBuffer buffer) {
        byte[] buf = new byte[8];
        buffer.get(buf);
        buf = Bytes.toArray(Lists.reverse(Bytes.asList(buf)));
        creationDate = decodeDateTime(buf);
        buffer.get(buf);
        buf = Bytes.toArray(Lists.reverse(Bytes.asList(buf)));
        modifyDate = decodeDateTime(buf);
        reserved = UnsignedInteger.fromIntBits(buffer.getInt()).longValue();
        name = "";
        boolean skip = false;
        while(buffer.hasRemaining()) {
            char ch = (char) buffer.get();
            if(!skip) {
                name += ch;
            }
            skip = !skip;
        }
    }
    
}
