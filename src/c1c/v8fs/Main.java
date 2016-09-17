package c1c.v8fs;

import com.google.common.io.Files;
import com.google.common.io.Resources;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.eclipse.persistence.jaxb.JAXBContextFactory;
import org.eclipse.persistence.jaxb.JAXBContextProperties;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.util.ConfigurationBuilder;

/**
 * Testing class (unit tests is coming soon)
 */
public class Main {

    public static String _dbgFlt(String str) {
        System.out.println("_dbgFlt: " + str);
        return str;
    }

    /**
     * Utility method. Extends string hex-number values to 8 digits with leading
     * zeros
     *
     * @param buf value to extend
     */
    public static String to8Digits(String buf) {
        String res = buf;
        while (res.length() < 8) {
            res = "0" + res;
        }
        if (res.length() > 8) {
            res = res.substring(0, 7);
        }
        return res;
    }

    private static String toXXChars(String buf, int xxNum) {
        String res = buf;
        while (res.length() < xxNum) {
            res = " " + res;
        }
        if (res.length() > xxNum) {
            res = res.substring(0, xxNum - 1);
        }
        return res;
    }

    public static void printWithLevel(String msg, int level) {
        String buf = "";
        for (int k = 0; k < level; k++) {
            buf += "\t";
        }
        System.out.println(buf + msg);
    }

    public static void printContent(Container cont, int level) {
        long total = 0L;
        for (c1c.v8fs.File fl : cont.getFiles().values()) {
            try {
                printWithLevel(
                        "0x"
                        + to8Digits(Long.toHexString(fl.getAttributes().getChain().getAddress()))
                        + " / 0x"
                        + to8Digits(Long.toHexString(
                                fl.getContent().getAddress()
                        )) + " -> " + toXXChars(fl.getAttributes().getName(), 40) + "\t ("
                        + Integer.toString(
                                fl.getContent().getData().length
                        ) + " bytes)", level
                );
                total += fl.getContent().getData().length;
            } catch (Exception ex) {
                printWithLevel(fl.toString(), level);
            }
            if (fl.getChild() != null) {
                printContent(fl.getChild(), level + 1);
            }
        }
        printWithLevel("Total " + Long.toString(total) + " bytes", level);
    }

    public static void main(String[] args) throws IOException, JAXBException {

        if (args.length == 0) {
            args = new String[]{"test.epf"};
        }

        if (args.length == 0) {
            System.out.println("v8fs");
            System.exit(0);
        }

        File file = new File(args[0]);
        if (file.exists()) {
            File dir = new File(file.getName() + ".v8fs.debug/");
            dir.mkdirs();
            byte[] content = Files.toByteArray(file);
            ByteBuffer buf = ByteBuffer.wrap(content);
            Container container = new Container();
            container.readFromBuffer(buf);
            for (Chain ch : container.getChains()) {
                File bFile = new File(dir, Integer.toString(container.getChains().indexOf(ch)) + ".chain");
                File bFileInf = new File(dir, Integer.toString(container.getChains().indexOf(ch)) + ".chain.inflated ");
                bFile.delete();
                bFileInf.delete();
                Files.write(ch.getData(), bFile);
                System.out.println("0x" + to8Digits(Long.toHexString(ch.getAddress())) + " > " + bFile.getPath());
                try {
                    Files.write(ch.getDataInflate(), bFileInf);
                } catch (Exception ex) {
                    //
                }
            }
            System.out.println("Readed " + Integer.toString(container.getChains().size()) + " chains");
            System.out.println("Content:");
            printContent(container, 1);
            File xFile = new File(args[0] + ".xml");
            xFile.delete();

            List<Object> bindings = new Reflections(
                    new ConfigurationBuilder()
                    .forPackages("c1c/v8fs/jaxb/bindings")
                    .setScanners(new ResourcesScanner())
            ).getResources(
                    (nm) -> (nm != null ? nm.matches(".+-bindings\\.xml") : false)
            ).stream()
                    .map((nm) -> Resources.getResource(nm))
                    .collect(Collectors.toList());

            Map<String, Object> bindingMap = new HashMap<>();
            bindingMap.put("c1c.v8fs", bindings);

            Map<String, Object> properties = new HashMap<>();
            properties.put(JAXBContextProperties.OXM_METADATA_SOURCE, bindingMap);

//            new FileReader(bndPath + "attributes-bindings.xml");
//            new FileReader(bndPath + "chain-bindings.xml");
//            new FileReader(bndPath + "chunk-bindings.xml");
//            new FileReader(bndPath + "chunk-header-bindings.xml");
//            new FileReader(bndPath + "container-bindings.xml");
//            new FileReader(bndPath + "container-header-bindings.xml");
//            new FileReader(bndPath + "file-bindings.xml");
//            new FileReader(bndPath + "index-bindings.xml");
//            new FileReader(bndPath + "index-entry-bindings.xml");
            JAXBContext context = JAXBContextFactory.createContext(
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

            Marshaller marsh = context.createMarshaller();
            marsh.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marsh.marshal(container, xFile);
            final String arg0 = args[0];
//            context.generateSchema(new SchemaOutputResolver() {
//                @Override
//                public Result createOutput(String namespaceUri, String suggestedFileName) throws IOException {
//                    File schFile = new File(arg0 + ".xsd");
//                    StreamResult result = new StreamResult(schFile);
//                    result.setSystemId(schFile.toURI().toURL().toString());
//                    return result;
//                }
//            });
        }
    }

}
