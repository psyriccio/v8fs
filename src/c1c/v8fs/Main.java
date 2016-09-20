package c1c.v8fs;

import c1c.v8fs.jaxb.JAXBSerializer;
import java.io.File;
import java.io.IOException;
import javax.xml.bind.JAXBException;

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

    public static void printInfo(Container container) {
        container.getChains().stream().forEach((ch) -> {
            System.out.println("0x" + to8Digits(Long.toHexString(ch.getAddress())));
        });
        System.out.println("Readed " + Integer.toString(container.getChains().size()) + " chains");
        System.out.println("Content:");
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
            args = new String[]{"./test-data/test.epf"};
        }

        if (args.length == 0) {
            System.out.println("v8fs");
            System.exit(0);
        }

        Container container = ContainerBinaryIO.readContainer(new File(args[0]));
        printInfo(container);
        printContent(container, 1);

        JAXBSerializer jaxbSerializer = new JAXBSerializer();

        jaxbSerializer.serialize(container, "test-data/");

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
