/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package c1c.v8fs.assemble;

import c1c.v8fs.Container;
import c1c.v8fs.ContainerHeader;
import c1c.v8fs.Index;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

/**
 *
 * @author psyriccio
 */
public class ContainerAssembler {

    public static void disassemble(Container cont, File dir, String name) throws IOException {
        File contDir = new File(dir, name + ".v8/");
        contDir.mkdir();
        contDir.mkdirs();
        for (c1c.v8fs.File fl : cont.getFiles().values()) {
            Container child = fl.getChild();
            if (child != null) {
                disassemble(child, contDir, fl.getName());
            } else {
                byte[] data = null;
                try {
                    data = fl.getContent().getDataInflate();
                } catch (Exception ex) {
                    data = null;
                }
                if (data == null) {
                    data = fl.getContent().getData();
                }
                File cntFile = new File(contDir, fl.getName());
                try (FileOutputStream fout = new FileOutputStream(cntFile)) {
                    fout.write(data);
                    fout.flush();
                }
            }
        }
    }
    
    public static Container assemble(File dir, boolean deflate) throws IOException {
        Container cont = new Container();
        cont.setIndex(new Index());
        cont.getIndex().setEntries(new ArrayList<>());
        cont.setHeader(new ContainerHeader(32, 512, 0, 0, cont.getContainerContext()));
        for(File fl : dir.listFiles()) {
            if(fl.isDirectory()) {
                cont.addFile(fl.getName().replaceAll("\\.v8$", ""), assemble(fl, false), deflate);
            } else {
                cont.addFile(fl.getName(), Files.readAllBytes(fl.toPath()), deflate);
            }
        }
        cont.recalcOffsetsAndRebuildIndex();
        return cont;
    }
    

}
