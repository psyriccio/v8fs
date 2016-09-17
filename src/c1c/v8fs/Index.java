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
import lombok.Singular;

/**
 * File index from first chain in container (like a FAT-table in FAT(XX) file
 * systems)
 *
 * @author psyriccio
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Index implements Bufferable {

    private @Singular List<IndexEntry> entries;

    @Override
    public void writeToBuffer(ByteBuffer buffer) {
        entries.stream().forEach((ent) -> {
            ent.writeToBuffer(buffer);
        });
    }

    @Override
    public void readFromBuffer(ByteBuffer buffer) {
        entries = new ArrayList<>();
        while (buffer.hasRemaining()) {
            IndexEntry ent = new IndexEntry();
            ent.readFromBuffer(buffer);
            if (ent.getAttributesAddress() != 0L && ent.getContentAddress() != 0L) {
                entries.add(ent);
            }
        }
    }

}
