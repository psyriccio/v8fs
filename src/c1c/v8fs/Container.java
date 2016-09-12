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

/**
 *
 * @author psyriccio
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Container implements Bufferable {

    private ContainerHeader header;
    private List<Block> blocks;
    private Index index;
    private HashMap<String, File> files;
    private HashMap<Long, Block> blockIndex;

    @Override
    public void writeToBuffer(ByteBuffer buffer) {
        header.writeToBuffer(buffer);
        blocks.stream().forEach((block) -> {
            block.writeToBuffer(buffer);
        });
    }

    @Override
    public void readFromBuffer(ByteBuffer buffer) {
        header = new ContainerHeader();
        header.readFromBuffer(buffer);
        blocks = new ArrayList<>();
        index = null;
        blockIndex = new HashMap<>();
        while (buffer.hasRemaining()) {
            Block block = new Block();
            block.readFromBuffer(buffer);
            blocks.add(block);
            blockIndex.put(block.getAddress(), block);
            if (index == null) {
                index = new Index();
                index.readFromBuffer(ByteBuffer.wrap(block.getData()));
            }
        }
        files = new HashMap<>();
        for (IndexEntry idx : index.getEntries()) {
            Attributes attr = new Attributes();
            Block attrBlock = blockIndex.get(
                    idx.getAttributesAddress()
            );
            if (attrBlock != null) {
                attr.readFromBuffer(
                        ByteBuffer.wrap(attrBlock.getData())
                );
                attr.setBlock(attrBlock);
            }
            Block content = blockIndex.get(idx.getContentAddress());
            File file = new File();
            file.setAttributes(attr);
            file.setContent(content);
            file.inspectContent();
            files.put(file.getAttributes().getName(), file);
        }
    }

}
