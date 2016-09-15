/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package c1c.v8fs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.eclipse.persistence.oxm.annotations.XmlNamedAttributeNode;
import org.eclipse.persistence.oxm.annotations.XmlVariableNode;

/**
 * JAXB XmlAdapter. Improve JAXB-serialization of chains index HashMap
 *
 * @author psyriccio
 */
public class ChainIndexAdapter extends XmlAdapter<ChainIndexAdapter.ChainIndexAdaptedMap, Map<Long, Chain>> {

    @Override
    public Map<Long, Chain> unmarshal(ChainIndexAdaptedMap v) throws Exception {
        Map<Long, Chain> res = new HashMap<>();
        v.entries.stream().forEach((ent) -> {
            res.put(Long.parseUnsignedLong(ent.key, 16), ent.value);
        });
        return res;
    }

    @Override
    public ChainIndexAdaptedMap marshal(Map<Long, Chain> v) throws Exception {
        ChainIndexAdaptedMap res = new ChainIndexAdaptedMap();
        res.entries = new ArrayList<>();
        v.keySet().stream().map((key) -> {
            ChainIndexAdaptedEntry ent = new ChainIndexAdaptedEntry();
            ent.key = Main.to8Digits(Long.toHexString(key));
            ent.value = v.get(key);
            return ent;
        }).forEach((ent) -> {
            res.entries.add(ent);
        });
        return res;
    }

    public static class ChainIndexAdaptedMap {

        private static int nextID = 0;

        private @XmlTransient String id = "";

        @XmlID
        @XmlAttribute(name = "id")
        public String getIDXML() {
            if (this.id.isEmpty()) {
                this.id = Integer.toHexString(nextID++);
            }
            return this.id;
        }

        @XmlID
        @XmlAttribute(name = "id")
        public void setIDXML(String id) {
            this.id = id;
        }

        @XmlVariableNode(value = "key") List<ChainIndexAdaptedEntry> entries = new ArrayList<>();

    }

    @XmlNamedAttributeNode(value = "key")
    public static class ChainIndexAdaptedEntry {

        public @XmlTransient String key;
        public @XmlIDREF @XmlAttribute(name = "chain") Chain value;

    }

}
