package c1c.v8fs;

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public class Main {

    private static String to8Digits(String buf) {
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

    public static void main(String[] args) throws IOException {

        if (args.length == 0) {
            args = new String[]{"user2.cf"};
        }

        if (args.length == 0) {
            System.out.println("v8fs");
            System.exit(0);
        }

        File file = new File(args[0]);
        if (file.exists()) {
            File dir = new File(file.getName() + ".v8fs/");
            dir.mkdirs();
            byte[] content = Files.toByteArray(file);
            ByteBuffer buf = ByteBuffer.wrap(content);
            Container container = new Container();
            container.readFromBuffer(buf);
            for (Chain ch : container.getChains()) {
                File bFile = new File(dir, Integer.toString(container.getChains().indexOf(ch)) + ".chain");
                bFile.delete();
                Files.write(ch.getData(), bFile);

            }
            System.out.println("Readed " + Integer.toString(container.getChains().size()) + " chains");
            System.out.println("Content:");
            printContent(container, 1);
        }
    }

}
