package compactcollections.tests;
import compactcollections.SparseBitSet;
import org.junit.Assert;
import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SparseBitSetTest {
    @Test
    public void testSetGet() {
        SparseBitSet set = new SparseBitSet();

        for(int i = 0; i < 10000; i++) {
            set.setBit(i);
        }

        for(int i = 0; i < 10000; i++) {
            Assert.assertTrue(set.getBit(i));
        }
    }

    @Test
    public void testSetGetRandom() {
        Random random = new Random(59);
        SparseBitSet set = new SparseBitSet();
        List<Integer> bits = new ArrayList<Integer>();
        int maxIndex = -1;

        for(int i = 0; i < 10000; i++) {
            int index = random.nextInt(32768) + 32768;
            set.setBit(index);
            bits.add(index);
            maxIndex = Math.max(index, maxIndex);
        }

        for(int i = 0; i < 10000; i++) {
            int index = bits.get(i);
            Assert.assertTrue(set.getBit(index));
        }

        for(int i = 0; i < maxIndex; i++) {
            if(!bits.contains(i)) {
                Assert.assertFalse(set.getBit(i));
            }
            else break;
        }
    }
}
