package com.timepath.steam.io.storage.gcf;

import com.timepath.DataUtils;
import com.timepath.io.RandomAccessFileWrapper;
import java.io.IOException;

/**
 *
 * @author Timepath
 */
class FileAllocationTableHeader {

    /**
     * 4 * 4
     */
    static final long SIZE = 16;

    /**
     * Header checksum.
     */
    final int checksum;

    /**
     * Number of data blocks.
     */
    final int clusterCount;

    /**
     * Index of 1st unused GCFFRAGMAP entry.
     */
    final int firstUnusedEntry;

    /**
     * Defines the end of block chain terminator.
     * If the value is 0, then the terminator is 0x0000FFFF; if the value is 1, then the
     * terminator is 0xFFFFFFFF
     */
    final int isLongTerminator;

    final long pos;

    FileAllocationTableHeader(GCF g) throws IOException {
        RandomAccessFileWrapper raf = g.raf;
        pos = raf.getFilePointer();
        clusterCount = raf.readULEInt();
        firstUnusedEntry = raf.readULEInt();
        isLongTerminator = raf.readULEInt();
        checksum = raf.readULEInt();
        g.fragMapEntries = new FileAllocationTableEntry[clusterCount];
        raf.skipBytes(g.fragMapEntries.length * FileAllocationTableEntry.SIZE);
    }

    @Override
    public String toString() {
        int checked = check();
        String checkState = (checksum == checked) ? "OK" : checksum + "vs" + checked;
        return "blockCount:" + clusterCount + ", firstUnusedEntry:" + firstUnusedEntry + ", isLongTerminator:"
               + isLongTerminator + ", checksum:" + checkState;
    }

    int check() {
        int checked = 0;
        checked += DataUtils.updateChecksumAdd(clusterCount);
        checked += DataUtils.updateChecksumAdd(firstUnusedEntry);
        checked += DataUtils.updateChecksumAdd(isLongTerminator);
        return checked;
    }

}
