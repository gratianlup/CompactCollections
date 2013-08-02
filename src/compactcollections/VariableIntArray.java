package compactcollections;
import java.util.AbstractList;
import java.util.List;

public class VariableIntArray extends AbstractList<Integer> {
    // The number of values stored inside a group.
    // A group consists of a 1-byte header that describes
    // the size of the values, followed by the values.
    private static final int GROUP_SIZE = 8;

    // The number of values stored inside a section.
    // A section represents SECTION_SIZE / GROUPSIZE groups.
    // Sections are created to allow fast access to random positions.
    private static final int SECTION_SIZE = 64;

    // The default size of the array storing the values.
    private static final int DEFAULT_DATA_CAPACITY = 4096;

    // The number of modified values stored into a cache
    // before they are written (committed) to the array.
    private static final int MAX_PENDING_EDITED_VALUES = 32 * 1024;

    // Contains the precomputed size of a group associated
    // with a particular value of a group header in range 0-255.
    // Used when skipping over entire groups of values.
    private static int[] groupSize;

    // Contains the precomputed size of each value in a group.
    // Used when accessing a value at a known index in a group.
    private static int[][] groupValueSize;

    // Contains the precomputed offset of each value in a group.
    // The offset is relative to the group start (includes the 1-byte header).
    // Used when accessing a value at a known index in a group.
    private static int[][] groupValueOffset;

    // Set if the table with the precomputed values
    // has already been created (it is only created once).
    private static boolean tablesInitialized;

    // Contains the offset at which the first group in the section starts.
    private int[] sectionOffset;

    // Contains the section start value (representative value).
    // It is the value of the first location in the first group.
    // All other values in the section are stored as the delta
    // between the actual value and this representative value.
    // This allows using 1-byte values in most cases.
    private int[] sectionValue;

    // Stores the actual data: 1 or 4 byte integer values.
    // This also includes the 1-byte group header.
    private byte[] data;

    // The current position into the data array.
    private int dataOffset;

    // The total number of values stored in the array.
    private int valueCount;

    // A group is written to the data array only when
    // all values have been added to it. Until all values
    // are available they are stored in this array.
    private int[] pendingValues;

    // The number of values that still need to be written
    // as a new group to the data array.
    private int pendingValueCount;

    // Modified values that can not be updated directly
    // are stored into a cache and written together once it is full.
    // This greatly reduces the overhead of creating a new array each time
    // a value that previously required 1 byte and now requires 4 bytes.
    private IntHashMap pendingEditedValues;

    // An optional cache that maps a value index
    // to the associated value. Can speed up the query in many cases.
    private VariableIntCache cache;

    // The index of the first location from the last accessed group.
    // In many used case the index of the accessed location
    // is close to the index of the previously accessed location;
    // in this case the associated group remains the same.
    private int lastGroupIndex;

    // The offset where the last accessed group starts.
    private int lastGroupOffset;

    public VariableIntArray(int capacity, VariableIntCache valueCache) {
        if(!tablesInitialized) {
            initializeLookupTables();
            tablesInitialized = true;
        }

        capacity = Math.max(capacity, DEFAULT_DATA_CAPACITY);
        data = new byte[capacity];
        sectionOffset = new int[capacity / SECTION_SIZE];
        sectionValue = new int[capacity / SECTION_SIZE];
        pendingValues = new int[GROUP_SIZE];
        cache = valueCache;
        lastGroupIndex = -1;
        lastGroupOffset = -1;
    }

    public VariableIntArray(int capacity) {
        this(capacity, null);
    }

    public VariableIntArray(VariableIntCache valueCache) {
        this(DEFAULT_DATA_CAPACITY, valueCache);
    }

    public VariableIntArray() {
        this(DEFAULT_DATA_CAPACITY, null);
    }

    public VariableIntArray(List<Integer> values, VariableIntCache valueCache) {
        this(values.size() + (values.size() / GROUP_SIZE), valueCache);
        addValues(values);
    }

    public VariableIntArray(int[] values, VariableIntCache valueCache) {
        this(values.length + (values.length / GROUP_SIZE), valueCache);
        addValues(values);
    }

    public VariableIntArray(List<Integer> values) {
        this(values.size() + (values.size() / GROUP_SIZE), null);
        addValues(values);
    }

    public VariableIntArray(int[] values) {
        this(values.length + (values.length / GROUP_SIZE), null);
        addValues(values);
    }

    private void initializeLookupTables() {
        // Create the tables containing the mapping between one
        // of the group header values and the configuration of the values
        // (size and offset of each value). The header encodes, for each value,
        // it's type - 1 byte or 4 byte value. For example, if 8 values are stored
        // into a group, then the header 0x1F means that the first 5 values
        // have each 4 bytes, while the remaining 3 have only 1 byte.
        int groupCount = 1 << GROUP_SIZE;
        groupSize = new int[groupCount];
        groupValueSize = new int[groupCount][GROUP_SIZE];
        groupValueOffset = new int[groupCount][GROUP_SIZE];

        for(int i = 0; i < groupCount; i++) {
            int valueOffset = 1;

            for(int k = 0; k < GROUP_SIZE; k++) {
                int valueSize = (i & (1 << k)) != 0 ? 4 : 1;
                groupValueOffset[i][k] = valueOffset;
                groupValueSize[i][k] = valueSize;
                valueOffset += valueSize;
            }

            groupSize[i] = valueOffset;
        }
    }

    private int[] resizeArray(int[] values) {
        int[] newValues = new int[values.length * 2];
        System.arraycopy(values, 0, newValues, 0, values.length);
        return newValues;
    }

    private byte[] resizeArray(byte[] values) {
        byte[] newValues = new byte[values.length * 2];
        System.arraycopy(values, 0, newValues, 0, values.length);
        return newValues;
    }

    private int[] compactArray(int[] values, int usedCapacity) {
        if(usedCapacity < values.length) {
            int[] newValues = new int[usedCapacity];
            System.arraycopy(values, 0, newValues, 0, usedCapacity);
            return newValues;
        }

        return values;
    }

    private byte[] compactArray(byte[] values, int usedCapacity) {
        if(usedCapacity < values.length) {
            byte[] newValues = new byte[usedCapacity];
            System.arraycopy(values, 0, newValues, 0, usedCapacity);
            return newValues;
        }

        return values;
    }

    private void writeData(int value) {
        if(dataOffset == data.length) {
            // Maximum capacity reached, try to resize.
            data = resizeArray(data);
        }

        data[dataOffset] = (byte)value;
        dataOffset++;
    }

    private int getSectionIndex(int valueIndex) {
        return valueIndex / SECTION_SIZE;
    }

    private int getSectionValueIndex(int valueIndex) {
        return valueIndex % SECTION_SIZE;
    }

    private int getSectionValue() {
        int sectionIndex = getSectionIndex(valueCount);

        if((valueCount % SECTION_SIZE) == 0) {
            // A new section begins and the offset and start values must be setValue.
            // Make sure the used arrays are large enough.
            if(sectionIndex == sectionOffset.length) {
                sectionOffset = resizeArray(sectionOffset);
                sectionValue = resizeArray(sectionValue);
            }

            sectionOffset[sectionIndex] = dataOffset;
            sectionValue[sectionIndex] = pendingValues[0];
        }

        return sectionValue[sectionIndex];
    }

    private boolean requiresFourBytes(int value) {
        return (value < -128) || (value > 127);
    }

    private void writePendingValuesHeader(int sectionValue) {
        int header = 0;

        for(int i = 0; i < pendingValueCount; i++) {
            // Values are represented as the delta between
            // the actual value and the value starting the current section.
            // The variable-sized integers have either 1 or 4 bytes.
            int delta = pendingValues[i] - sectionValue;

            if(requiresFourBytes(delta)) {
                header |= 1 << i;
            }
        }

        writeData(header);
    }

    private void writeDeltaValue(int delta) {
        if(requiresFourBytes(delta)) {
            writeData(delta & 0xFF);
            writeData((delta >>> 8) & 0xFF);
            writeData((delta >>> 16) & 0xFF);
            writeData((delta >>> 24) & 0xFF);
        }
        else writeData(delta);
    }

    private void writeDeltaValueAtOffset(int delta, int locationOffset,
                                         int locationSize) {
        // This function is used when modifying an already written value.
        if(requiresFourBytes(delta)) {
            data[locationOffset] = (byte)(delta & 0xFF);
            data[locationOffset + 1] = (byte)((delta >>> 8) & 0xFF);
            data[locationOffset + 2] = (byte)((delta >>> 16) & 0xFF);
            data[locationOffset + 3] = (byte)((delta >>> 24) & 0xFF);
        }
        else {
            data[locationOffset] = (byte)delta;

            if(locationSize == 4) {
                // Previously there was a 4-byte value in this location,
                // make sure the upper bytes are initialized properly:
                // two-complement's arithmetic requires filling the unused space
                // with bits setValue to 1 in case of negative numbers.
                int signValue = delta >= 0 ? 0 : -1;
                data[locationOffset + 1] = (byte)signValue;
                data[locationOffset + 2] = (byte)signValue;
                data[locationOffset + 3] = (byte)signValue;
            }
        }
    }

    private void writePendingValues(int sectionValue) {
        for(int i = 0; i < pendingValueCount; i++) {
            writeDeltaValue(pendingValues[i] - sectionValue);
        }
    }

    private void writePendingGroup() {
        int sectionValue = getSectionValue();
        writePendingValuesHeader(sectionValue);
        writePendingValues(sectionValue);

        pendingValueCount = 0;
        valueCount += GROUP_SIZE;
    }

    @Override
    public boolean add(Integer value) {
        if(value == null) {
            throw new NullPointerException("Value should not be null!");
        }

        addValue(value);
        return true;
    }

    public void addValue(int value) {
        // Values are written to the data array only as a complete group.
        // This is required in order to compute the group header.
        pendingValues[pendingValueCount] = value;
        pendingValueCount++;

        if(pendingValueCount == GROUP_SIZE) {
            writePendingGroup();
        }

        cacheValue(valueCount + pendingValueCount, value);
    }

    public void addValues(List<Integer> values) {
        for(int value : values) {
            addValue(value);
        }
    }

    public void addValues(int[] values) {
        for(int value : values) {
            addValue(value);
        }
    }

    public VariableIntArray flush() {
        // If pending values remain and the group is not complete
        // fill the remaining positions with zero and write the group.
        if(pendingValueCount > 0) {
            for(int i = pendingValueCount; i < GROUP_SIZE; i++) {
                pendingValues[i] = 0;
                pendingValueCount++;
            }

            writePendingGroup();
        }

        // Make sure any edited values are written to the data array.
        writePendingEditedValues();
        return this;
    }

    private int readGroupHeader(int groupOffset) {
        return data[groupOffset] & 0xFF;
    }

    private int getGroupSize(int groupOffset) {
        return groupSize[readGroupHeader(groupOffset)];
    }

    private int getGroupValueOffset(int groupOffset, int index) {
        return groupValueOffset[readGroupHeader(groupOffset)][index];
    }

    private int getGroupValueSize(int groupOffset, int index) {
        return groupValueSize[readGroupHeader(groupOffset)][index];
    }

    private int readValueAtOffset(int offset, int size) {
        if(size == 1) {
            return (int)data[offset];
        }
        else return ((int)data[offset] & 0xFF)           |
                    ((int)data[offset + 1] & 0xFF) << 8  |
                    ((int)data[offset + 2] & 0xFF) << 16 |
                    ((int)data[offset + 3] & 0xFF) << 24;
    }

    private int readGroupValue(int groupOffset, int valueIndex, int sectionIndex) {
        int valueOffset = groupOffset + getGroupValueOffset(groupOffset, valueIndex);
        int valueSize = getGroupValueSize(groupOffset, valueIndex);
        return sectionValue[sectionIndex] +
               readValueAtOffset(valueOffset, valueSize);
    }

    private void writeGroupDeltaValue(int groupOffset, int valueIndex, int delta) {
        int locationOffset = groupOffset + getGroupValueOffset(groupOffset, valueIndex);
        int locationSize = getGroupValueSize(groupOffset, valueIndex);
        writeDeltaValueAtOffset(delta, locationOffset, locationSize);
    }

    private int cacheValue(int index, int value) {
        if(cache != null) {
            cache.put(index, value);
        }

        return value;
    }

    @Override
    public Integer get(int index) {
        return getValue(index);
    }

    public int getValue(int index) {
        if(index < 0) {
            throw new ArrayIndexOutOfBoundsException("Invalid value index!");
        }

        // Try to use the cache if available.
        if(cache != null) {
            int value = cache.get(index);

            if(value != Integer.MIN_VALUE) {
                return value;
            }
        }

        // Check if the value has been modified, but has not yet been written
        // to the array. The edited value is always the last version of the value.
        if(hasPendingEditedValues()) {
            int value = pendingEditedValues.get(index);

            if(value != Integer.MIN_VALUE) {
                return cacheValue(index, value);
            }
        }

        // Check if the value is one of the values that has recently been added
        // and it has not been written yet to the data array.
        if(index >= valueCount) {
            if((pendingValueCount > 0) &&
               ((index - valueCount) < pendingValueCount)) {
                int value = pendingValues[index - valueCount];
                return cacheValue(index, value);
            }

            throw new ArrayIndexOutOfBoundsException("Invalid value index!");
        }

        // Check if the requested value is found in the same group as one
        // of the previously requested values. In this case the computation
        // of the group offset can be skipped - happens very often in practice.
        int sectionIndex = getSectionIndex(index);

        if((lastGroupOffset != -1) && (index >= lastGroupIndex)) {
            int indexOffset = index - lastGroupIndex;

            if(indexOffset < GROUP_SIZE) {
                int value = readGroupValue(lastGroupOffset, indexOffset, sectionIndex);
                return cacheValue(index, value);
            }
        }

        // Start with the current offset setValue at the beginning of the section
        // which contains the value and find the corresponding group.
        int valueIndex = getSectionValueIndex(index);
        int groupOffset = sectionOffset[sectionIndex];

        // Skip over groups of multiple values.
        while(valueIndex >= GROUP_SIZE) {
            groupOffset += getGroupSize(groupOffset);
            valueIndex -= GROUP_SIZE;
        }

        // Cache position for next lookup.
        lastGroupIndex = index - (index % GROUP_SIZE);
        lastGroupOffset = groupOffset;

        int value = readGroupValue(groupOffset, valueIndex, sectionIndex);
        return cacheValue(index, value);
    }

    private void addPendingEditedValue(int index, int value) {
        // The value could not be modified directly, write it
        // to the data array later, together with other modified values.
        if(pendingEditedValues == null) {
            pendingEditedValues = new IntHashMap();
        }

        pendingEditedValues.put(index, value);

        if(pendingEditedValues.size() >= MAX_PENDING_EDITED_VALUES) {
            writePendingEditedValues();
        }
    }

    private boolean hasPendingEditedValues() {
        return (pendingEditedValues != null) &&
               (pendingEditedValues.size() > 0);
    }

    private void writePendingEditedValues() {
        if(!hasPendingEditedValues()) {
            return;
        }

        // Read all the values currently in the array
        // and modify the values at the pending indices.
        int[] values = new int[valueCount];
        for(int i = 0; i < valueCount; i++) values[i] = getValue(i);

        for(int i = 0; i < pendingEditedValues.size(); i++) {
            int index = pendingEditedValues.getKeyAt(i);
            int value = pendingEditedValues.getValueAt(i);
            values[index] = value;
        }

        // Add the new values to a new array, then copy
        // the new data back to this array.
        VariableIntArray clonedArray = new VariableIntArray(valueCount);
        for(int i = 0; i < valueCount; i++) clonedArray.addValue(values[i]);

        clonedArray.flush();
        takeArrayData(clonedArray);
        pendingEditedValues.clear();
    }

    private void takeArrayData(VariableIntArray otherArray) {
        sectionOffset = otherArray.sectionOffset;
        sectionValue = otherArray.sectionValue;
        data = otherArray.data;
        dataOffset = otherArray.dataOffset;
        lastGroupIndex = -1;
        lastGroupOffset = -1;
    }

    private boolean deltaValueFitsInLocation(int delta, int groupOffset, int valueIndex) {
        // There are 4 cases based on the previous delta size and the new one:
        // Previous  New   Fits
        //    4       4     Y
        //    4       1     Y
        //    1       4     N  => Add to pending edited values.
        //    1       1     Y
        return (getGroupValueSize(groupOffset, valueIndex) == 4) ||
                !requiresFourBytes(delta);
    }

    private boolean isPendingEditedValue(int index) {
        return hasPendingEditedValues() &&
               pendingEditedValues.containsKey(index);
    }

    @Override
    public Integer set(int index, Integer value) {
        if(value == null) {
            throw new NullPointerException("Value should not be null!");
        }

        setValue(index, value);
        return null; // Don't return last value.
    }

    public void setValue(int index, int value) {
        cacheValue(index, value);

        if(index == (valueCount + pendingValueCount)) {
            // Just append after the last inserted value.
            addValue(value);
        }
        else if(isPendingEditedValue(index)) {
            // The value will be modified after all pending values
            // are written, because it might modify one of them.
            addPendingEditedValue(index, value);
        }
        else if((pendingValueCount > 0) && (index >= valueCount)) {
            // The value is part of the current incomplete group,
            // modify it directly, before it is written to the data array.
            int pendingIndex = index - valueCount;
            pendingValues[pendingIndex] = value;
        }
        else {
            // The value overwrites an existing value.
            // There are two cases:
            // 1. The value is small enough to fit in the existing location.
            // 2. The location must be enlarged to make room for the value.
            //    To simplify implementation a copy of the current array is made,
            //    but using the modified value. To improve performance many edit
            //    operations are accumulated and written at once.
            int sectionIndex = getSectionIndex(index);
            int sectionStartValue = sectionValue[sectionIndex];
            int delta = value - sectionStartValue;

            // Start with the current offset at the beginning
            // of the section where the value resides.
            int valueIndex = getSectionValueIndex(index);
            int groupOffset = sectionOffset[sectionIndex];

            // Skip over groups of multiple values.
            while(valueIndex >= GROUP_SIZE) {
                groupOffset += getGroupSize(groupOffset);
                valueIndex -= GROUP_SIZE;
            }

            if(deltaValueFitsInLocation(delta, groupOffset, valueIndex)) {
                // Replace the existing value.
                writeGroupDeltaValue(groupOffset, valueIndex, delta);
            }
            else addPendingEditedValue(index, value);
        }
    }

    @Override
    public int size() {
        return valueCount + pendingValueCount;
    }

    public VariableIntArray trim() {
        flush();
        data = compactArray(data, dataOffset);
        return this;
    }
}
