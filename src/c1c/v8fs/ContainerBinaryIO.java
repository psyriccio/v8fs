/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package c1c.v8fs;

import com.google.common.io.Files;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 *
 * @author psyriccio
 */
public class ContainerBinaryIO {

    public static Container readContainer(java.io.File file) throws IOException {
        Container container = null;
        if (file.exists()) {
            byte[] content = Files.toByteArray(file);
            ByteBuffer buf = ByteBuffer.wrap(content);
            container = new Container();
            container.getContainerContext().put("FileName", file.getName());
            container.readFromBuffer(buf);
        }
        return container;
    }
}
