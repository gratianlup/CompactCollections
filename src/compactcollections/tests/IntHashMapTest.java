package compactcollections.tests;
import compactcollections.IntHashMap;
import org.junit.Assert;
import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class IntHashMapTest {
    @Test
    public void testPutGet() {
        IntHashMap map = new IntHashMap();

        for(int i = 0; i < 10000; i++) {
            map.put(i, i + 1);
        }

        for(int i = 0; i < 10000; i++) {
            Assert.assertEquals(map.get(i), i + 1);
        }
    }

    @Test
    public void testPutGetRandom() {
        Random random = new Random(59);
        IntHashMap map = new IntHashMap();
        List<Integer> keys = new ArrayList<Integer>();
        List<Integer> values = new ArrayList<Integer>();

        for(int i = 0; i < 10000; i++) {
            int key = random.nextInt();
            int value = random.nextInt();
            int index = keys.indexOf(key);

            if(index != -1) {
                values.set(index, value);
            }
            else {
                keys.add(key);
                values.add(value);
            }

            map.put(key, value);
        }

        for(int i = 0; i < keys.size(); i++) {
            int key = keys.get(i);
            Assert.assertEquals(map.get(key), (int)values.get(i));
        }
    }
}
