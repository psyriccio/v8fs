/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package c1c.v8fs;

import java.nio.ByteBuffer;
import java.util.List;
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
public class File implements Bufferable {

    private Attributes attributes;
    private Block content;
    private Container child;

    public void inspectContent() {
        byte[] data = null;
        try {
            content.getDataInflate();
        } catch (Exception ex) {
            data = null;
        }
        if (data == null) {
            if (content != null) {
                data = content.getData();
            } else {
                return;
            }
        }
        Container cont = null;
        try {
            cont = new Container();
            cont.readFromBuffer(ByteBuffer.wrap(data));
        } catch (Exception ex) {
            cont = null;
        }
        if (cont != null) {
            this.child = cont;
        }
    }

    @Override
    public void writeToBuffer(ByteBuffer buffer) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void readFromBuffer(ByteBuffer buffer) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
