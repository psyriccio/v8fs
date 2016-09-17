/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package c1c.v8fs;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

/**
 * Main container class, representing a root or slave metadata container
 *
 * @author psyriccio
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Container implements Bufferable {

    private ContainerHeader header;
    private @Singular List<Chain> chains;
    private Index index;
    private HashMap<String, File> files;
    private HashMap<Long, Chain> chainIndex;
    private HashMap<String, Object> containerContext = new HashMap<>();

    public Container(HashMap<String, Object> parentContext, String suffix) {
        containerContext = (HashMap<String, Object>) parentContext.clone();
        String newSuffix = (String) containerContext.getOrDefault("Suffix", "");
        if (!newSuffix.isEmpty()) {
            newSuffix += ".";
        }
        newSuffix += suffix;
        containerContext.put("Suffix", newSuffix);
    }

    @Override
    public void writeToBuffer(ByteBuffer buffer) {
        header.writeToBuffer(buffer);
        chains.stream().forEach((chain) -> {
            chain.writeToBuffer(buffer);
        });
    }

    @Override
    public void readFromBuffer(ByteBuffer buffer) {
        header = new ContainerHeader();
        header.setContainerContext(containerContext);
        header.readFromBuffer(buffer);
        chains = new ArrayList<>();
        index = null;
        chainIndex = new HashMap<>();
        while (buffer.hasRemaining()) {
            Chain chain = new Chain();
            chain.setContainerContext(containerContext);
            chain.readFromBuffer(buffer);
            chains.add(chain);
            chainIndex.put(chain.getAddress(), chain);
            if (index == null) {
                index = new Index();
                index.readFromBuffer(ByteBuffer.wrap(chain.getData()));
            }
        }
        files = new HashMap<>();
        index.getEntries().stream().map((idx) -> {
            Attributes attr = new Attributes();
            attr.setContainerContext(containerContext);
            Chain attrChain = chainIndex.get(
                    idx.getAttributesAddress()
            );
            if (attrChain != null) {
                attr.readFromBuffer(
                        ByteBuffer.wrap(attrChain.getData())
                );
                attr.setChain(attrChain);
            }
            Chain content = chainIndex.get(idx.getContentAddress());
            File file = new File();
            file.setContainerContext(containerContext);
            file.setAttributes(attr);
            file.setContent(content);
            return file;
        }).map((file) -> {
            file.inspectContent();
            return file;
        }).forEach((file) -> {
            files.put(file.getAttributes().getName(), file);
        });
    }

}
