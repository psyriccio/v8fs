/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package c1c.v8fs;

import com.google.common.primitives.UnsignedInteger;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Contains chunk header data and address-link to next chunk in chain
 *
 * @author psyriccio
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChunkHeader implements Bufferable {

    private long chainSize;
    private long thisChunkSize;
    private long nextChunkAddress;
    private boolean nextChunkPresent;
    private HashMap<String, Object> containerContext;

    private String get8Chars(ByteBuffer buf) {
        String res = "";
        for (int k = 0; k <= 7; k++) {
            res += (char) buf.get();
        }
        return res;
    }

    private String intToHex(long i) {
        String buf = UnsignedInteger.fromIntBits((int) i).toString(16) ; //.toString(16);
        while (buf.length() < 8) {
            buf = "0" + buf;
        }
        if (buf.length() > 8) {
            buf = buf.substring(0, 7);
        }
        return buf;
    }

        private String intToHexSpec(long i) {
        String buf = UnsignedInteger.fromIntBits((int) i  & 0x7fffffff).toString(16) ; //.toString(16);
        while (buf.length() < 8) {
            buf = "0" + buf;
        }
        if (buf.length() > 8) {
            buf = buf.substring(0, 7);
        }
        return buf;
    }

    private long hexToInt(String hex) {
        return UnsignedInteger.valueOf(hex, 16).longValue();
    }

    private String toStringBuffer() {
        return '\u0d0a' + intToHex(chainSize) + ' ' + intToHex(thisChunkSize) + ' ' + (nextChunkPresent ? intToHex(nextChunkAddress) : intToHexSpec(UnsignedInteger.MAX_VALUE.longValue()) + ' ' +'\u0d0a');
    }

    private void fromStringBuffer(String str) {
        try {
            ByteBuffer buf = ByteBuffer.wrap(str.getBytes("UTF-8"));
            char b;
            b = buf.getChar();
            if (b != '\u0d0a') {
                throw new RuntimeException("Format error");
            }

            chainSize = hexToInt(get8Chars(buf));

            b = (char) buf.get();
            if (b != ' ') {
                throw new RuntimeException("Format error");
            }

            thisChunkSize = hexToInt(get8Chars(buf));

            b = (char) buf.get();
            if (b != ' ') {
                throw new RuntimeException("Format error");
            }

            String nextAddr = get8Chars(buf);
            nextChunkAddress = hexToInt(nextAddr);

            b = (char) buf.get();
            if (b != ' ') {
                throw new RuntimeException("Format error");
            }
            b = buf.getChar();
            if (b != '\u0d0a') {
                throw new RuntimeException("Format error");
            }

            nextChunkPresent = (!nextAddr.equals("7fffffff"));

        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void writeToBuffer(ByteBuffer buffer) {
        try {
            buffer.put(toStringBuffer().getBytes("UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void readFromBuffer(ByteBuffer buffer) {
        byte[] bytes = new byte[31];
        buffer.get(bytes);
        try {
            fromStringBuffer(new String(bytes, "UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

}
