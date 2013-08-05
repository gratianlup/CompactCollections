// Copyright (c) 2013 Gratian Lup. All rights reserved.
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are
// met:
//
// * Redistributions of source code must retain the above copyright
// notice, this list of conditions and the following disclaimer.
//
// * Redistributions in binary form must reproduce the above
// copyright notice, this list of conditions and the following
// disclaimer in the documentation and/or other materials provided
// with the distribution.
//
// * The name "CompactCollections" must not be used to endorse or promote
// products derived from this software without prior written permission.
//
// * Products derived from this software may not be called "CompactCollections" nor
// may "CompactCollections" appear in their names without prior written
// permission of the author.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
// A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
// OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
// LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
// THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE
package compactcollections;
import java.util.AbstractList;

public class SparseBitSet extends AbstractList<Boolean> {
    // The number of bits stored associated with a range.
    // Having less bits in a range might save some space,
    // but it increases, ultimatelly using more memory in many cases.
    private final static int BITS_PER_RANGE = 64 * 8;

    // The number of bits stored associated with a group of ranges.
    // Groups are used to speed up random access to bit positions.
    // Having less bits in a group reduces search time, but increases
    // memory consumption by requiring more group entries in the hash table.
    private final static int BITS_PER_GROUP = BITS_PER_RANGE * 32;

    private static class BitRange {
        public long[] data;    // The bit data associated with the range.
        public BitRange next;  // The next range in the linked list.
        public int startIndex; // The index of the first represented bit.

        public BitRange(int startIndex) {
            this.data = new long[BITS_PER_RANGE / 64];
            this.startIndex = startIndex;
        }

        public BitRange(int startIndex, BitRange next) {
            this(startIndex);
            this.next = next;
        }

        public boolean getBit(int index) {
            int valueIndex = (index - startIndex) >>> 6;
            int valueOffset = (index - startIndex) & 0x3F;
            return (data[valueIndex] & (1L << valueOffset)) != 0;
        }

        public void setBit(int index) {
            int valueIndex = (index - startIndex) >>> 6;
            int valueOffset = (index - startIndex) & 0x3F;
            data[valueIndex] |= (1L << valueOffset);
        }

        public void resetBit(int index) {
            int valueIndex = (index - startIndex) >>> 6;
            int valueOffset = (index - startIndex) & 0x3F;
            data[valueIndex] &= ~(1L << valueOffset);
        }
    }


    private IntObjectHashMap<BitRange> ranges; // Maps a range to the associated bits.
    private BitRange lastRange;                // The last accessed range, used for caching.

    public SparseBitSet() {
        ranges = new IntObjectHashMap<BitRange>();
    }

    @Override
    public int size() {
        return ranges.size() * BITS_PER_RANGE;
    }

    @Override
    public Boolean get(int index) {
        return getBit(index);
    }

    @Override
    public Boolean set(int index, Boolean value) {
        boolean previousValue = getBit(index);
        if(value) setBit(index);
        else resetBit(index);
        return previousValue;
    }

    @Override
    public Boolean remove(int index) {
        boolean previousValue = getBit(index);
        resetBit(index);
        return previousValue;
    }

    private boolean indexInRange(int index, BitRange range) {
        return (index >= range.startIndex) &&
               (index < range.startIndex + BITS_PER_RANGE);
    }

    private BitRange findRange(int index) {
        // In many cases a bit from the last accessed range
        // is requested and no further search is required.
        if((lastRange != null) && indexInRange(index, lastRange)) {
            return lastRange;
        }

        int groupIndex = index / BITS_PER_GROUP;
        BitRange range = ranges.get(groupIndex);

        while((range != null) && (index >= range.startIndex)) {
            if(indexInRange(index, range)) {
                lastRange = range;
                return range;
            }

            range = range.next;
        }

        return null;
    }

    private BitRange findOrCreateRange(int index, boolean create) {
        // In many cases a bit from the last accessed range
        // is requested and no further search is required.
        if((lastRange != null) && indexInRange(index, lastRange)) {
            return lastRange;
        }

        int groupIndex = index / BITS_PER_GROUP;
        BitRange range = ranges.get(groupIndex);
        BitRange previousRange = range;

        while((range != null) && (index >= range.startIndex)) {
            if(indexInRange(index, range)) {
                // Found the range containing the index.
                lastRange = range;
                return range;
            }

            previousRange = range;
            range = range.next;
        }

        if(!create) {
            return null;
        }

        // Create a new range containing the given index.
        int startIndex = index - (index % BITS_PER_RANGE);
        BitRange newRange = new BitRange(startIndex);

        if(previousRange == null) {
            // The first range in this group.
            ranges.put(groupIndex, newRange);
        }
        else if(index > previousRange.startIndex) {
            // A new range must be created after the last one.
            newRange.next = previousRange.next;
            previousRange.next = newRange;
        }
        else {
            // A new range must be created before the last one
            // and it replaces the group representative.
            newRange.next = previousRange;
            ranges.put(groupIndex, newRange);
        }

        lastRange = newRange;
        return newRange;
    }

    public boolean getBit(int index) {
        BitRange range = findRange(index);
        return (range != null) && range.getBit(index);
    }

    public void setBit(int index) {
        BitRange range = findOrCreateRange(index, true /* create */);
        range.setBit(index);
    }

    public void resetBit(int index) {
        BitRange range = findOrCreateRange(index, false /* create */);

        if(range != null) {
            range.resetBit(index);
        }
    }

    public void clear() {
        ranges = null;
        lastRange = null;
    }
}
