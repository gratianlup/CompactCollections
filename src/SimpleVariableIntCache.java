public class SimpleVariableIntCache implements VariableIntCache {
    private int[] fixedCache;
    private boolean[] fixedCacheState;

    public SimpleVariableIntCache(int fixedCacheSize) {
        assert fixedCacheSize > 0;
        this.fixedCache = new int[fixedCacheSize];
        this.fixedCacheState = new boolean[fixedCacheSize];
    }

    @Override
    public void put(int index, int value) {
        if(index < fixedCache.length) {
            fixedCache[index] = value;
            fixedCacheState[index] = true;
        }
    }

    @Override
    public int get(int index) {
        if(index < fixedCache.length) {
            if(fixedCacheState[index]) {
                return fixedCache[index];
            }
        }

        return Integer.MIN_VALUE;
    }

    @Override
    public VariableIntCache getCompatibleInstance() {
        return new SimpleVariableIntCache(fixedCache.length);
    }
}
