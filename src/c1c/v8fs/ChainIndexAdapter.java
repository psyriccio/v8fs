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
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.eclipse.persistence.oxm.annotations.XmlVariableNode;

/**
 *
 * @author psyriccio
 */
public class ChainIndexAdapter extends XmlAdapter<ChainIndexAdapter.ChainIndexAdaptedMap, Map<Long, Chain>> {

    @Override
    public Map<Long, Chain> unmarshal(ChainIndexAdaptedMap v) throws Exception {
        Map<Long, Chain> res = new HashMap<>();
        v.entries.stream().forEach((ent) -> {
            res.put(Long.parseLong(ent.key, 16), ent.value);
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

        @XmlVariableNode("key") List<ChainIndexAdaptedEntry> entries = new ArrayList<>();

    }

    public static class ChainIndexAdaptedEntry {

        public @XmlTransient String key;
        public @XmlIDREF @XmlAttribute(name = "chain") Chain value;

    }

}
