package com.timepath.steam.io.storage.gcf;

import com.timepath.EnumFlags;
import com.timepath.io.RandomAccessFileWrapper;
import com.timepath.steam.io.storage.util.ExtendedVFile;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.EnumSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * http://wiki.singul4rity.com/steam:filestructures:gcf
 *
 * @author TimePath
 */
public class GCF extends ExtendedVFile {

    private static final Logger LOG = Logger.getLogger(GCF.class.getName());

    final BlockAllocationTableHeader blockAllocationTableHeader;

    BlockAllocationTableEntry[] blocks;

    ChecksumEntry[] checksumEntries;

    final ChecksumHeader checksumHeader;

    ChecksumMapEntry[] checksumMapEntries;

    final ChecksumMapHeader checksumMapHeader;

    /**
     * TODO
     */
    tagGCFDIRECTORYCOPYENTRY[] copyEntries;

    final DataBlockHeader dataBlockHeader;

    GCFDirectoryEntry[] directoryEntries;

    DirectoryMapEntry[] directoryMapEntries;

    final DirectoryMapHeader directoryMapHeader;

    final FileAllocationTableHeader fragMap;

    FileAllocationTableEntry[] fragMapEntries;

    final FileHeader header;

    /**
     * Name table
     */
    tagGCFDIRECTORYINFO1ENTRY[] info1Entries;

    /**
     * Hash table
     */
    tagGCFDIRECTORYINFO2ENTRY[] info2Entries;

    /**
     * TODO
     */
    tagGCFDIRECTORYLOCALENTRY[] localEntries;

    final ManifestHeader manifestHeader;

    final String name;

    final RandomAccessFileWrapper raf;

    public GCF(File file) throws IOException {
        name = file.getName();
        raf = new RandomAccessFileWrapper(file, "r");

        header = new FileHeader(raf);
        blockAllocationTableHeader = new BlockAllocationTableHeader(this);
        fragMap = new FileAllocationTableHeader(this);

        //<editor-fold defaultstate="collapsed" desc="Manifest">
        manifestHeader = new ManifestHeader(raf);
        boolean skipManifest = false;
        if(skipManifest) {
            raf.skipBytes(manifestHeader.binarySize - ManifestHeader.SIZE);
        } else {
            directoryEntries = new GCFDirectoryEntry[manifestHeader.nodeCount];
            for(int i = 0; i < manifestHeader.nodeCount; i++) {
                directoryEntries[i] = new GCFDirectoryEntry(i);
            }
            byte[] ls = raf.readBytes(manifestHeader.nameSize);
            for(GCFDirectoryEntry de : directoryEntries) {
                int off = de.nameOffset;
                ByteArrayOutputStream s = new ByteArrayOutputStream();
                while(ls[off] != 0) {
                    s.write(ls[off]);
                    off++;
                }
                de.name = new String(s.toByteArray());

                if(de.parentIndex != 0xFFFFFFFF) {
                    de.setParent(directoryEntries[de.parentIndex]);
                }
            }
            directoryEntries[0].name = this.name;

            info1Entries = new tagGCFDIRECTORYINFO1ENTRY[manifestHeader.hashTableKeyCount];
            for(int i = 0; i < manifestHeader.hashTableKeyCount; i++) {
                info1Entries[i] = new tagGCFDIRECTORYINFO1ENTRY(raf);
            }

            info2Entries = new tagGCFDIRECTORYINFO2ENTRY[manifestHeader.nodeCount];
            for(int i = 0; i < manifestHeader.nodeCount; i++) {
                info2Entries[i] = new tagGCFDIRECTORYINFO2ENTRY(raf);
            }

            copyEntries = new tagGCFDIRECTORYCOPYENTRY[manifestHeader.minimumFootprintCount];
            for(int i = 0; i < manifestHeader.minimumFootprintCount; i++) {
                tagGCFDIRECTORYCOPYENTRY f = new tagGCFDIRECTORYCOPYENTRY();
                f.DirectoryIndex = raf.readULEInt();

                copyEntries[i] = f;
            }

            localEntries = new tagGCFDIRECTORYLOCALENTRY[manifestHeader.userConfigCount];
            for(int i = 0; i < manifestHeader.userConfigCount; i++) {
                tagGCFDIRECTORYLOCALENTRY f = new tagGCFDIRECTORYLOCALENTRY();
                f.DirectoryIndex = raf.readULEInt();

                localEntries[i] = f;
            }
        }

        //</editor-fold>
        directoryMapHeader = new DirectoryMapHeader(this);

        checksumHeader = new ChecksumHeader(raf);

        checksumMapHeader = new ChecksumMapHeader(this);

        dataBlockHeader = new DataBlockHeader(raf);
    }

    @Override
    public Object getAttributes() {
        return null;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public ExtendedVFile getRoot() {
        return directoryEntries[0];
    }

    @Override
    public boolean isComplete() {
        return true;
    }

    @Override
    public boolean isDirectory() {
        return true;
    }

    @Override
    public InputStream stream() {
        return null;
    }

    @Override
    public String toString() {
        return name;
    }

    private ChecksumEntry checksumEntries(int i) throws IOException {
        ChecksumEntry ce = checksumEntries[i];
        if(ce == null) {
            raf.seek((directoryMapHeader.pos + ChecksumMapHeader.SIZE)
                         + (checksumMapEntries.length * ChecksumMapEntry.SIZE)
                         + (i * ChecksumEntry.SIZE));
            return (checksumEntries[i] = new ChecksumEntry(raf));
        }
        return ce;
    }

    private ChecksumMapEntry checksumMapEntries(int i) throws IOException {
        ChecksumMapEntry cme = checksumMapEntries[i];
        if(cme == null) {
            raf.seek((directoryMapHeader.pos + ChecksumMapHeader.SIZE)
                         + (i * ChecksumMapEntry.SIZE));
            return (checksumMapEntries[i] = new ChecksumMapEntry(raf));
        }
        return cme;
    }

    private DirectoryMapEntry directoryMapEntries(int i) {
        DirectoryMapEntry dme = directoryMapEntries[i];
        if(dme == null) {
            try {
                raf.seek((directoryMapHeader.pos + DirectoryMapHeader.SIZE)
                             + (i * DirectoryMapEntry.SIZE));
                return (directoryMapEntries[i] = new DirectoryMapEntry(raf));
            } catch(IOException ex) {
                Logger.getLogger(GCF.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return dme;
    }

    private BlockAllocationTableEntry getBlock(int i) throws IOException {
        BlockAllocationTableEntry bae = blocks[i];
        if(bae == null) {
            raf.seek((blockAllocationTableHeader.pos + BlockAllocationTableHeader.SIZE)
                         + (i * BlockAllocationTableEntry.SIZE));
            return (blocks[i] = new BlockAllocationTableEntry(raf));
        }
        return bae;
    }

    private FileAllocationTableEntry getEntry(int i) throws IOException {
        FileAllocationTableEntry fae = fragMapEntries[i];
        if(fae == null) {
            raf.seek((fragMap.pos + FileAllocationTableHeader.SIZE)
                         + (i * FileAllocationTableEntry.SIZE));
            return (fragMapEntries[i] = new FileAllocationTableEntry(raf));
        }
        return fae;
    }

    private byte[] readData(BlockAllocationTableEntry block, int dataIdx) throws IOException {
        long pos = (dataBlockHeader.firstBlockOffset + (dataIdx * dataBlockHeader.blockSize));
        raf.seek(pos);
        byte[] buf = new byte[dataBlockHeader.blockSize];
        if(block.fileDataOffset != 0) {
            LOG.log(Level.INFO, "off = {0}", block.fileDataOffset);
        }
        raf.read(buf);
        return buf;
    }

    private class GCFDirectoryEntry extends ExtendedVFile {

        final EnumSet<DirectoryEntryAttributes> attributes;

        /**
         * Checksum index / file ID. 0xFFFFFFFF == None.
         */
        final int checksumIndex;

        /**
         * Index of the first directory item. 0x00000000 == None.
         */
        final int firstChildIndex;

        final int index;

        /**
         * Size of the item. If file, file size. If folder, number of items.
         */
        final int itemSize;

        String name;

        /**
         * Offset to the directory item name from the end of the directory items
         */
        final int nameOffset;

        /**
         * Index of the next directory item. 0x00000000 == None.
         */
        final int nextIndex;

        /**
         * Index of the parent directory item. 0xFFFFFFFF == None.
         */
        final int parentIndex;

        GCFDirectoryEntry(int index) throws IOException {
            this.index = index;
            nameOffset = raf.readULEInt();
            itemSize = raf.readULEInt();
            checksumIndex = raf.readULEInt();
            attributes = EnumFlags.decode(raf.readULEInt(), DirectoryEntryAttributes.class);
            parentIndex = raf.readULEInt();
            nextIndex = raf.readULEInt();
            firstChildIndex = raf.readULEInt();
        }

        @Override
        public long calculateChecksum() {
            return -1;
        }

        public Object getAttributes() {
            return this.attributes;
        }

        @Override
        public long getChecksum() {
            return -1;
        }

        public String getName() {
            return name;
        }

        public ExtendedVFile getRoot() {
            return GCF.this;
        }

        public boolean isComplete() {
            return GCF.this.directoryMapEntries(index).firstBlockIndex < blocks.length || this.itemSize == 0;
        }

        public boolean isDirectory() {
            return this.attributes.contains(DirectoryEntryAttributes.Directory);
        }

        @Override
        public long length() {
            return this.itemSize;
        }

        public InputStream stream() {
            return new InputStream() {

                private BlockAllocationTableEntry block;

                private final ByteBuffer buf = createBuffer();

                private byte[] data;

                private int dataIdx;

                private int pointer;

                @Override
                public int available() throws IOException {
                    return GCFDirectoryEntry.this.itemSize - pointer;
                }

                @Override
                public int read() throws IOException {
                    if(data == null || pointer > data.length) {
                        return -1;
                    }
                    return data[pointer++];
                }

                private ByteBuffer createBuffer() {
                    ByteBuffer b = ByteBuffer.wrap(new byte[GCFDirectoryEntry.this.itemSize]);
                    b.order(ByteOrder.LITTLE_ENDIAN);

                    int idx = GCF.this.directoryMapEntries(GCFDirectoryEntry.this.index).firstBlockIndex;
                    if(idx >= blocks.length) {
                        LOG.log(Level.WARNING, "Block out of range for item {0}. Is the size 0?",
                                GCFDirectoryEntry.this);
                        return null;
                    }
                    try {
                        block = GCF.this.getBlock(idx);
                        dataIdx = block.firstClusterIndex;
                        LOG.log(Level.FINE, "bSize: {0}", new Object[] {block.fileDataSize});
                        data = fill(b);
                    } catch(IOException ex) {
                        Logger.getLogger(GCF.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    return b;
                }

                private byte[] fill(ByteBuffer buf) {
                    if(dataIdx == 0xFFFF || dataIdx == -1) {
                        return new byte[] {-1};
                    }
                    try {
                        byte[] b = GCF.this.readData(block, dataIdx);
                        if(buf.position() + b.length > buf.capacity()) {
                            buf.put(b, 0, block.fileDataSize % dataBlockHeader.blockSize);
                        } else {
                            buf.put(b);
                        }
                        dataIdx = GCF.this.getEntry(dataIdx).nextClusterIndex;
                        LOG.log(Level.INFO, "next dataIdx: {0}", dataIdx);
                        return buf.array();
                    } catch(IOException ex) {
                        Logger.getLogger(GCF.class.getName()).log(Level.SEVERE, null, ex);
                        return new byte[] {-1};
                    }
                }
            };
        }

        private int getIndex() {
            return index;
        }

    }

}
