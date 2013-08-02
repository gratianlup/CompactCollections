package compactcollections;
import java.util.AbstractList;

public class SparseBitSet extends AbstractList<Boolean> {
    private final static int BITS_PER_RANGE = 64 * 8;
    private final static int BITS_PER_GROUP = BITS_PER_RANGE * 32;

    private static class BitRange {
        public long[] data;
        public BitRange next;
        public int startIndex;

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


    private IntObjectHashMap<BitRange> ranges;
    private BitRange lastRange;

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
