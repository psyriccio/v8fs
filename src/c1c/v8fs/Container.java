/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package c1c.v8fs;

import java.nio.ByteBuffer;
import java.util.ArrayList;
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
        while(buffer.hasRemaining()) {
            Block block = new Block();
            block.readFromBuffer(buffer);
            blocks.add(block);
            if(index == null) {
                index = new Index();
                index.readFromBuffer(ByteBuffer.wrap(block.getData()));
            }
        }
    }
    
}
