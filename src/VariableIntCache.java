public interface VariableIntCache {
    public void put(int index, int value);

    /*
     * Should return Int.MIN_VALUE if value not found.
     */
    public int get(int index);

    public VariableIntCache getCompatibleInstance();
}
