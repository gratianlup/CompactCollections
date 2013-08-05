package compactcollections;
import java.util.*;

public class Driver {
    static void shuffleArray(int[] ar)
    {
        Random rnd = new Random(59);
        for (int i = ar.length - 1; i >= 0; i--)
        {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            int a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
    }

    public static void main(String [] args) throws Exception {
        final int VALUE_COUNT = 2000;

        int[] order = new int[VALUE_COUNT];

        for(int i = 0; i < VALUE_COUNT;i++) order[i] = i;
        shuffleArray(order);
        System.out.println("Shuffeld ");

        Random random = new Random(59);
        long startTime = System.nanoTime();
        int sum = 0;

        if(false) {
            Map<Integer, Integer> map;

            if(false) {
                map = new HashMap<Integer, Integer>();

                for(int i = 0; i < VALUE_COUNT; i++) {
                    map.put(i, random.nextInt());
                }
            }
            else {
                map = new IntHashMap();

                for(int i = 0; i < VALUE_COUNT; i++) {
                    map.put(i, random.nextInt());
                }
            }

            double construction = System.nanoTime();
            Runtime runtime = Runtime.getRuntime();

            System.out.println("Mem = " + (runtime.totalMemory() - runtime.freeMemory()));
            System.in.read();


            for(int i = 0; i < VALUE_COUNT; i++) {
                sum += map.get(i);
            }

            double query = System.nanoTime();
            System.out.println("Construction duration = " + (double)(construction - startTime) / 1.0e9);
            System.out.println("Query duration = " + (double)(query - construction) / 1.0e9);
        }
        else if(1 > 2) {
            List<Integer> list;

            if(false) {
                list = new ArrayList<Integer>();

                for(int i = 0; i < VALUE_COUNT; i++) {
                    list.add(random.nextInt());
                }
            }
            else {
                list = new VariableIntArray();

                for(int i = 0; i < VALUE_COUNT; i++) {
                    list.add(random.nextInt());
                }
            }

            double construction = System.nanoTime();
            Runtime runtime = Runtime.getRuntime();

            System.out.println("Mem = " + (runtime.totalMemory() - runtime.freeMemory()));
            //System.in.read();

            for(int i = 0; i < VALUE_COUNT; i++) {
                sum += list.get(order[i]);
            }

            double query = System.nanoTime();
            System.out.println("Construction duration = " + (double)(construction - startTime) / 1.0e9);
            System.out.println("Query duration = " + (double)(query - construction) / 1.0e9);
        }
        else {
            Map<Map.Entry<Integer, Integer>, Integer> map;

            if(false) {
                map = new HashMap<Map.Entry<Integer, Integer>, Integer>();

                for(int i = 0; i < VALUE_COUNT; i++) {
                    for(int j = 0; j < VALUE_COUNT; j++) {
                        map.put(new IntPairHashMap.KeyEntry(i, j), random.nextInt());
                    }
                }
            }
            else {
                map = new IntPairHashMap();

                for(int i = 0; i < VALUE_COUNT; i++) {
                    for(int j = 0; j < VALUE_COUNT; j++) {
                        map.put(new IntPairHashMap.KeyEntry(i, j), random.nextInt());
                    }
                }
            }

            double construction = System.nanoTime();
            Runtime runtime = Runtime.getRuntime();

            System.out.println("Mem = " + (runtime.totalMemory() - runtime.freeMemory()));
            System.in.read();


            for(int i = 0; i < VALUE_COUNT; i++) {
                for(int j = 0; j < VALUE_COUNT; j++) {
                    sum += map.get(new IntPairHashMap.KeyEntry(i, j));
                }
            }

            double query = System.nanoTime();
            System.out.println("Construction duration = " + (double)(construction - startTime) / 1.0e9);
            System.out.println("Query duration = " + (double)(query - construction) / 1.0e9);
        }

        System.out.println("Sum = " + sum);
    }
}
