package compactcollections.tests;
import compactcollections.VariableIntArray;
import org.junit.Assert;
import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class VariableIntArrayTest {
    @Test
    public void testAddGet() {
        VariableIntArray array = new VariableIntArray();

        for(int i = 0; i < 1000000; i++) {
            array.addValue(i + 1);
        }

        for(int i = 0; i < 1000000; i++) {
            Assert.assertEquals(array.getValue(i), i + 1);
        }
    }

    @Test
    public void testRandomAddGet() {
        Random random = new Random();
        List<Integer> values = new ArrayList<Integer>();
        VariableIntArray array = new VariableIntArray();

        for(int i = 0; i < 1000000; i++) {
            int value = random.nextInt();
            array.addValue(value);
            values.add(value);
        }

        for(int i = 0; i < 1000000; i++) {
            Assert.assertEquals(array.getValue(i), (int)values.get(i));
        }
    }

    @Test
    public void testSet() {
        VariableIntArray array = new VariableIntArray();

        for(int i = 0; i < 1000000; i++) {
            array.addValue(i + 1);
        }

        for(int i = 0; i < 1000000; i++) {
            if(i % 100 == 0) {
                array.setValue(i, i + 2);
            }
        }

        for(int i = 0; i < 1000000; i++) {
            if(i % 100 == 0) {
                Assert.assertEquals(array.getValue(i), i + 2);
            }
            else Assert.assertEquals(array.getValue(i), i + 1);
        }
    }

    @Test
    public void testRandomSet() {
        Random random = new Random();
        List<Integer> values = new ArrayList<Integer>();
        VariableIntArray array = new VariableIntArray();

        for(int i = 0; i < 1000000; i++) {
            int value = random.nextInt();
            array.addValue(value);
            values.add(value);
        }

        for(int i = 0; i < 1000000; i++) {
            if(i % 10 == 0) {
                int value = random.nextInt();
                array.setValue(i, value);
                values.set(i, value);
            }
        }

        for(int i = 0; i < 1000000; i++) {
            Assert.assertEquals(array.getValue(i), (int)values.get(i));
        }
    }

    @Test
    public void testRandomSetConsecutvie() {
        VariableIntArray array = new VariableIntArray();

        for(int i = 0; i < 1000000; i++) {
            array.setValue(i, i + 1);
        }

        for(int i = 0; i < 1000000; i++) {
            Assert.assertEquals(array.getValue(i), i + 1);
        }
    }
}
