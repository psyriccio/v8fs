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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.eclipse.persistence.oxm.annotations.XmlPath;
import org.eclipse.persistence.oxm.annotations.XmlVariableNode;

/**
 * JAXB XmlAdapter. Improve JAXB-serialization of files HashMap
 *
 * @author psyriccio
 */
public class FileMapAdapter extends XmlAdapter<FileMapAdapter.FileAdaptedMap, Map<String, File>> {

    @Override
    public Map<String, File> unmarshal(FileAdaptedMap v) throws Exception {
        Map<String, File> res = new HashMap<>();
        v.entries.stream().forEach((ent) -> {
            res.put(ent.key, ent.value);
        });
        return res;
    }

    @Override
    public FileAdaptedMap marshal(Map<String, File> v) throws Exception {
        FileAdaptedMap res = new FileAdaptedMap();
        res.entries = new ArrayList<>();
        v.keySet().stream().map((key) -> {
            FileMapAdaptedEntry ent = new FileMapAdaptedEntry();
            if (key != null) {
                ent.key = key;
                ent.value = v.get(key);
            } else {
                ent.key = "@NULL";
                ent.value = new File();
            }
            return ent;
        }).forEach((ent) -> {
            res.entries.add(ent);
        });
        return res;
    }

    public static class FileAdaptedMap {

        @XmlVariableNode("key") List<FileMapAdaptedEntry> entries = new ArrayList<>();

    }

    public static class FileMapAdaptedEntry {

        public @XmlTransient String key;
        public @XmlPath(".") @XmlElement(name = "def") File value;

    }

}
