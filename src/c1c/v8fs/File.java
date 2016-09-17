/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package c1c.v8fs;

import java.nio.ByteBuffer;
import java.util.HashMap;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents logical file object. Contains two chains: attributes and data
 *
 * @author psyriccio
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class File implements Bufferable {

    private String name;
    private Attributes attributes;
    private Chain content;
    private Container child;
    private HashMap<String, Object> containerContext;

    public void setAttributes(Attributes val) {
        this.name = val.getName();
        this.attributes = val;
    }

    public void inspectContent() {
        this.name = attributes.getName();
        byte[] data = null;
        try {
            data = content.getDataInflate();
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
            cont = new Container(containerContext, getName());
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
