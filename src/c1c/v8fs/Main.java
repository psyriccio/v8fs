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

    public static void main(String[] args) throws IOException {

        if (args.length == 0) {
            args = new String[]{"test.epf"};
        }

        if (args.length == 0) {
            System.out.println("v8fs");
            System.exit(0);
        }

        File file = new File(args[0]);
        if (file.exists()) {
            byte[] content = Files.toByteArray(file);
            ByteBuffer buf = ByteBuffer.wrap(content);
            Container container = new Container();
            container.readFromBuffer(buf);
            for (Block blk : container.getBlocks()) {
                File bFile = new File(Integer.toString(container.getBlocks().indexOf(blk)) + ".blk");
                bFile.delete();
                Files.write(blk.getData(), bFile);

            }
            System.out.println("Readed " + Integer.toString(container.getBlocks().size()) + " blocks");
            System.out.println("Index:");
            container.getIndex().getEntries().stream().forEach((iEnt) -> {
                if (iEnt.getAttributesAddress() != 0) {
                    Attributes attr = new Attributes();
                    Block attBlk = null;
                    for (Block blk : container.getBlocks()) {
                        if (blk.getAddress() == iEnt.getAttributesAddress()) {
                            attBlk = blk;
                            break;
                        }
                    }
                    if (attBlk != null) {
                        ByteBuffer buf1 = ByteBuffer.wrap(attBlk.getData());
                        attr.readFromBuffer(buf1);
                    }
                    System.out.println(to8Digits(Long.toHexString(iEnt.getAttributesAddress())) + ", " + to8Digits(Long.toHexString(iEnt.getContentAddress())) + ": " + attr.getName());
                }
            });
        }
    }

}
