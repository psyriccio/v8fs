/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package c1c.v8fs;

import java.nio.ByteBuffer;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.persistence.oxm.annotations.XmlPath;

/**
 *
 * @author psyriccio
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "file")
public class File implements Bufferable {

    private @XmlID @XmlAttribute String name;
    private @XmlPath(".") @XmlElement Attributes attributes;
    private @XmlIDREF @XmlAttribute(name = "chain") Chain content;
    private @XmlElement(name = "container") Container child;

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
