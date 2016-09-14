/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package c1c.v8fs;

import com.google.common.collect.Lists;
import com.google.common.primitives.Bytes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlInlineBinaryData;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.eclipse.persistence.oxm.annotations.XmlNamedAttributeNode;
import org.eclipse.persistence.oxm.annotations.XmlVariableNode;

/**
 *
 * @author psyriccio
 */
public class DataArrayAdapter extends XmlAdapter<DataArrayAdapter.DataAttayAdaptedList, byte[]> {

    @Override
    public byte[] unmarshal(DataArrayAdapter.DataAttayAdaptedList v) throws Exception {
        final List<Byte> lst = Lists.newArrayList();
        v.entities.stream().forEach((itm) -> {
            for (byte b : itm.row) {
                lst.add(b);
            }
        });
        return Bytes.toArray(lst);
    }

    @Override
    public DataArrayAdapter.DataAttayAdaptedList marshal(byte[] v) throws Exception {
        final DataAttayAdaptedList res = new DataAttayAdaptedList();
        int k = 0;
        byte[] buffer = new byte[50];
        for (byte b : v) {
            buffer[k++] = b;
            if (k >= buffer.length) {
                DataArrayAdaptedItem itm = new DataArrayAdaptedItem();
                itm.row = buffer;
                itm.index = Integer.toHexString(res.entities.size());
                res.entities.add(itm);
                buffer = new byte[buffer.length];
                k = 0;
            }
        }
        if (k > 0) {
            byte[] rem = Arrays.copyOfRange(buffer, 0, k - 1);
            DataArrayAdaptedItem itm = new DataArrayAdaptedItem();
            itm.row = rem;
            itm.index = Integer.toHexString(res.entities.size());
            res.entities.add(itm);
        }
        return res;
    }

    @XmlRootElement(name = "data", namespace = "http://psyzzy.net/v8fs-data")
    public static class DataAttayAdaptedList {

        private static int nextId = 0;

        public @XmlTransient int id = nextId++;

        @XmlVariableNode("index")
        public List<DataArrayAdaptedItem> entities = new ArrayList<>();

        @XmlID
        @XmlAttribute(name = "id")
        public String getIdXML() {
            return Integer.toHexString(this.id);
        }

        @XmlID
        @XmlAttribute(name = "id")
        public void setIdXML(String val) {
            this.id = Integer.parseUnsignedInt(val, 16);
        }

    }

    @XmlNamedAttributeNode(value = "index")
    public static class DataArrayAdaptedItem {

        public @XmlTransient String index;

        @XmlInlineBinaryData
        //@XmlSchemaType(name = "hexBinary")
        @XmlValue
        public byte[] row;

    }

}
