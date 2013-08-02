package compactcollections.tests;
import compactcollections.IntPairHashMap;
import org.junit.Assert;
import org.junit.Test;
import java.util.*;

public class IntPairHashMapTest {
    @Test
    public void testPutGet() {
        IntPairHashMap map = new IntPairHashMap();

        for(int i = 0; i < 1000; i++) {
            for(int j = 0; j < 1000; j++) {
                map.put(i, j, i + j);
            }
        }

        for(int i = 0; i < 1000; i++) {
            for(int j = 0; j < 1000; j++) {
                Assert.assertEquals(map.get(i, j), i + j);
            }
        }
    }

    @Test
    public void testPutGetRandom() {
        Random random = new Random(59);
        IntPairHashMap map = new IntPairHashMap();
        Map<IntPairHashMap.KeyEntry, Integer> inserted = new HashMap<IntPairHashMap.KeyEntry, Integer>();

        for(int i = 0; i < 100000; i++) {
            IntPairHashMap.KeyEntry key = new IntPairHashMap.KeyEntry(random.nextInt(),
                                                                      random.nextInt());
            int value = random.nextInt();
            map.put(key, value);
            inserted.put(key, value);
        }

        for(Map.Entry<IntPairHashMap.KeyEntry, Integer> entry : inserted.entrySet()) {
            Assert.assertEquals(map.get(entry.getKey()), entry.getValue());
        }
    }
}
