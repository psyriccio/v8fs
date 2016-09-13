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
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.eclipse.persistence.oxm.annotations.XmlVariableNode;

/**
 *
 * @author psyriccio
 */
public class FileMapAdapter extends XmlAdapter<FileMapAdapter.AdaptedMap, Map<String, File>> {

    @Override
    public Map<String, File> unmarshal(AdaptedMap v) throws Exception {
        Map<String, File> res = new HashMap<>();
        v.entries.stream().forEach((ent) -> {
            res.put(ent.key, ent.value);
        });
        return res;
    }

    @Override
    public AdaptedMap marshal(Map<String, File> v) throws Exception {
        AdaptedMap res = new AdaptedMap();
        res.entries = new ArrayList<>();
        v.keySet().stream().map((key) -> {
            FileMapAdaptedEntry ent = new FileMapAdaptedEntry();
            ent.key = key;
            ent.value = v.get(key);
            return ent;
        }).forEach((ent) -> {
            res.entries.add(ent);
        });
        return res;
    }
    
    public static class AdaptedMap {
        
        @XmlVariableNode("key") List<FileMapAdaptedEntry> entries = new ArrayList<>();
        
    }
    
    public static class FileMapAdaptedEntry {
        
        public @XmlTransient String key;
        public File value;
        
    }
    
}
