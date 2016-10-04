/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package c1c.v8fs;

import com.google.common.io.ByteStreams;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.Deflater;
import java.util.zip.DeflaterInputStream;
import lombok.Data;
import lombok.Singular;

/**
 * Main container class, representing a root or slave metadata container
 *
 * @author psyriccio
 */
@Data
public class Container implements Bufferable {

    private ContainerHeader header;
    private @Singular List<Chain> chains;
    private Index index;
    private HashMap<String, File> files;
    private HashMap<Long, Chain> chainIndex;
    private HashMap<String, Object> containerContext = new HashMap<>();

    public Container() {
        chains = new ArrayList<>();
        chains.add(null);
        files = new HashMap<>();
        chainIndex = new HashMap<>();
    }

    public Container(ContainerHeader header, List<Chain> chains, Index index, HashMap<String, File> files, HashMap<Long, Chain> chainIndex) {
        this.header = header;
        this.chains = chains;
        this.index = index;
        this.files = files;
        this.chainIndex = chainIndex;
    }

    public Container(HashMap<String, Object> parentContext, String suffix) {
        this.containerContext = (HashMap<String, Object>) parentContext.clone();
        String newSuffix = (String) getContainerContext().getOrDefault("Suffix", "");
        if (!newSuffix.isEmpty()) {
            newSuffix += ".";
        }
        newSuffix += suffix;
        getContainerContext().put("Suffix", newSuffix);
        chains = new ArrayList<>();
        chains.add(null);
        files = new HashMap<>();
        chainIndex = new HashMap<>();
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
        header.setContainerContext(getContainerContext());
        header.readFromBuffer(buffer);
        chains = new ArrayList<>();
        index = null;
        chainIndex = new HashMap<>();
        while (buffer.hasRemaining()) {
            Chain chain = new Chain();
            chain.setContainerContext(getContainerContext());
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
            attr.setContainerContext(getContainerContext());
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
            file.setContainerContext(getContainerContext());
            file.setAttributes(attr);
            file.setContent(content);
            return file;
        }).map((file) -> {
            file.inspectContent();
            return file;
        }).forEach((file) -> {
            files.put(file.getAttributes().getName(), file);
        });
        recalcOffsetsAndRebuildIndex();
    }

    public void recalcOffsetsAndRebuildIndex() {
        this.getIndex().getEntries().clear();
        this.getChainIndex().clear();
        int indexSize = 12 * this.getFiles().size();
        int headerSize = 32;
        int baseOffset = headerSize + indexSize;
        final AtomicInteger offset = new AtomicInteger(baseOffset);
        this.getChains().stream().skip(1).forEach((chain) -> {
            chain.setAddress(offset.get());
            final AtomicReference<Chunk> lastChunk = new AtomicReference<>();
            lastChunk.set(null);
            chain.getChunks().forEach((chunk) -> {
                chunk.setAddress(offset.getAndAdd((int) chunk.getHeader().getThisChunkSize()));
                if (lastChunk.get() != null) {
                    lastChunk.get().getHeader().setNextChunkAddress(chunk.getAddress());
                }
                lastChunk.set(chunk);
            });
        });
        this.getFiles().forEach((name, file) -> {

            this.getChainIndex()
                    .put(
                            file.getAttributes().getChain().getAddress(),
                            file.getAttributes().getChain()
                    );

            this.getChainIndex()
                    .put(
                            file.getContent().getAddress(),
                            file.getContent()
                    );
            IndexEntry indEnt = new IndexEntry(
                    file.getAttributes().getChain().getAddress(),
                    file.getContent().getAddress(),
                    Integer.MAX_VALUE,
                    getContainerContext()
            );
            indEnt.setContainerContext(getContainerContext());
            this.getIndex().getEntries().add(indEnt);
        });
        this.chains.remove(0);
        this.chains.add(0, new Chain(this.getIndex().asByteArray(), (int) this.getHeader().getDefaultChunkSize(), getContainerContext()));
    }

    public void addFile(String name, byte[] content, boolean deflate) throws IOException {

        byte[] data;
        if (deflate) {
            ByteArrayInputStream in = new ByteArrayInputStream(content);
            DeflaterInputStream def = new DeflaterInputStream(in, new Deflater(9, true));
            data = ByteStreams.toByteArray(def);
        } else {
            data = content;
        }

        Chain dataChain = new Chain(data, (int) this.getHeader().getDefaultChunkSize(), getContainerContext());
        dataChain.setContainerContext(getContainerContext());
        Date date = new Date();
        Attributes attributes = new Attributes(date, date, 0, name, null, getContainerContext());
        Chain attrChain = new Chain(attributes.asByteArray(), (int) this.getHeader().getDefaultChunkSize(), getContainerContext());
        attrChain.setContainerContext(getContainerContext());
        attributes.setChain(attrChain);

        File fl = new File(name, attributes, dataChain, this, getContainerContext());

        this.getChains().add(attrChain);
        this.getChains().add(dataChain);

        this.getFiles().put(fl.getName(), fl);
    }

    public void addFile(String name, Container contentCont, boolean deflate) throws IOException {
        ByteBuffer bbCont = ByteBuffer.allocate(1024 * 1024 * 10);
        contentCont.writeToBuffer(bbCont);
        bbCont.limit(bbCont.position());
        int size = bbCont.position();
        byte[] data = Arrays.copyOfRange(bbCont.array(), 0, size);
        addFile(name, data, deflate);
    }

}
