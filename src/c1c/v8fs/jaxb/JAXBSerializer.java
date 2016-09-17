/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package c1c.v8fs.jaxb;

import c1c.v8fs.Attributes;
import c1c.v8fs.Chain;
import c1c.v8fs.Chunk;
import c1c.v8fs.ChunkHeader;
import c1c.v8fs.Container;
import c1c.v8fs.ContainerHeader;
import c1c.v8fs.Index;
import c1c.v8fs.IndexEntry;
import com.google.common.io.Resources;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import lombok.Getter;
import org.eclipse.persistence.jaxb.JAXBContextFactory;
import org.eclipse.persistence.jaxb.JAXBContextProperties;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.util.ConfigurationBuilder;

/**
 *
 * @author psyriccio
 */
public class JAXBSerializer {

    private final Reflections reflections;
    private final @Getter JAXBContext jaxbContext;
    private final @Getter JAXBContext jaxbChunkDataContext;
    private final @Getter Marshaller jaxbMarshaller;
    private final @Getter Marshaller jaxbChunkDataMarshaller;
    private final @Getter Unmarshaller jaxbUnmarshaller;
    private final @Getter Unmarshaller jaxbChunkDataUnmarshaller;

    public JAXBSerializer() throws JAXBException {
        this.reflections = new Reflections(
                new ConfigurationBuilder()
                .forPackages("c1c/v8fs/jaxb/bindings")
                .setScanners(new ResourcesScanner()));

        List<Object> bindings = reflections.getResources(
                (nm) -> (nm != null ? nm.matches(".+-bindings\\.xml") : false)
        ).stream()
                .map((nm) -> Resources.getResource(nm))
                .collect(Collectors.toList());

        Map<String, Object> bindingMap = new HashMap<>();
        bindingMap.put("c1c.v8fs", bindings);

        Map<String, Object> properties = new HashMap<>();
        properties.put(JAXBContextProperties.OXM_METADATA_SOURCE, bindingMap);

        this.jaxbContext = JAXBContextFactory.createContext(
                new Class[]{
                    Container.class,
                    ContainerHeader.class,
                    Chain.class,
                    Chunk.class,
                    ChunkHeader.class,
                    Attributes.class,
                    c1c.v8fs.File.class,
                    Index.class,
                    IndexEntry.class
                }, properties
        );

        List<Object> bindingsSep = reflections.getResources(
                (nm) -> (nm != null ? nm.matches(".+-bindings-sep\\.xml") : false)
        ).stream()
                .map((nm) -> Resources.getResource(nm))
                .collect(Collectors.toList());

        Map<String, Object> bindingMapSep = new HashMap<>();
        bindingMapSep.put("c1c.v8fs", bindingsSep);

        Map<String, Object> propertiesSep = new HashMap<>();
        propertiesSep.put(JAXBContextProperties.OXM_METADATA_SOURCE, bindingMapSep);

        this.jaxbChunkDataContext = JAXBContextFactory.createContext(
                new Class[]{
                    Chunk.class
                }, propertiesSep
        );

        this.jaxbMarshaller = this.jaxbContext.createMarshaller();
        this.jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        this.jaxbChunkDataMarshaller = this.jaxbChunkDataContext.createMarshaller();
        this.jaxbChunkDataMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        this.jaxbUnmarshaller = this.jaxbContext.createUnmarshaller();
        this.jaxbChunkDataUnmarshaller = this.jaxbChunkDataContext.createUnmarshaller();

    }

    protected File prepareDir(File baseDir, String name) {
        File dir = new File(baseDir, name);
        dir.mkdirs();
        for (File file : dir.listFiles()) {
            file.delete();
        }
        return dir;
    }

    protected void serializeData(Container cont, File dir, String name) throws JAXBException {

        File dataDir = new File(dir, name + ".data/");
        dataDir.mkdirs();
        for (File fl : dataDir.listFiles()) {
            fl.delete();
        }

        for (Chain chain : cont.getChains()) {
            for (Chunk chunk : chain.getChunks()) {
                File chunkFile = new File(dir, chunk.getDataFileName());
                jaxbChunkDataMarshaller.marshal(chunk, chunkFile);
            }
        }

        for (c1c.v8fs.File fl : cont.getFiles().values()) {
            Container cnt = fl.getChild();
            if (cnt != null) {
                for (Chain chain : cnt.getChains()) {
                    for (Chunk chunk : chain.getChunks()) {
                        File chunkFile = new File(dir, chunk.getDataFileName());
                        jaxbChunkDataMarshaller.marshal(chunk, chunkFile);
                    }
                }
            }
        }

    }

    protected void serializeMain(Container cont, File dir, String name) throws JAXBException {
        File xFile = new File(dir, name + ".xml");
        jaxbMarshaller.marshal(cont, xFile);
    }

    public void serialize(Container cont, File baseDir) throws JAXBException {

        String name = (String) cont.getContainerContext().getOrDefault(
                "FileName", UUID.randomUUID().toString());
        File dir = prepareDir(baseDir, name + ".v8fs/");

        serializeMain(cont, dir, name);
        serializeData(cont, dir, name);

    }

    public void serialize(Container cont, String baseDir) throws JAXBException {
        serialize(cont, new File(baseDir));
    }

}
