/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package c1c.v8fs;

import com.google.common.collect.Lists;
import com.google.common.escape.CharEscaper;
import com.google.common.escape.CharEscaperBuilder;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a special chain, contained file attributes
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
    private Chain chain;
    private HashMap<String, Object> containerContext;

    private byte[] codeDateTime(Date dateTime) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dateTime);
        BigInteger divCn = new BigInteger("10");
        long val = cal.getTimeInMillis();
        byte[] res = UnsignedLong.fromLongBits(val)
                .bigIntegerValue().multiply(divCn).toByteArray();
        //ArrayUtils.reverse(res);
        cal.add(cal.YEAR, 1969);
        return res;
    }

    private Date decodeDateTime(byte[] val) {
        byte[] arr = Arrays.copyOf(val, val.length);
        BigInteger divCn = new BigInteger("10");
        //ArrayUtils.reverse(arr);
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(
                UnsignedLong.valueOf(
                        new BigInteger(arr).divide(divCn)
                ).longValue()
        );
        cal.add(cal.YEAR, -1969);
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
        while (buffer.hasRemaining()) {
            char ch = (char) buffer.get();
            if (!skip) {
                name += ch;
            }
            skip = !skip;
        }
        name = name.replaceAll("[\\x00-\\x1F]", "");
    }

}
