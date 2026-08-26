package Exe_Z.lib;

import java.util.HashMap;
import java.util.List;
import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

public class RandomCollection<E> {

    private final NavigableMap<Double, E> map = new TreeMap<Double, E>();
    private final Random random;
    private double total = 0;
    private List<E> elements;

    public void add(E element) {//MỞ JSON
        elements.add(element);
    }

    public boolean isEmpty() {//MỞ JSON
        return elements.isEmpty();
    }

    public E next() {//MỞ JSON
        if (map.isEmpty()) {
            return null;
        }

        double value = random.nextDouble() * map.lastKey();
        return map.higherEntry(value).getValue();
    }

    public RandomCollection() {
        this(new Random());
    }

    public RandomCollection(Random random) {
        this.random = random;
    }

    public RandomCollection<E> add(double weight, E result) {
        if (weight <= 0) {
            return this;
        }
        total += weight;
        map.put(total, result);
        return this;
    }
    public HashMap<E, Integer> test(int times) {
        HashMap<E, Integer> hashmap = new HashMap<>();
        for (int i = 0; i < times; i++) {
            E value = next();
            if (hashmap.containsKey(value)) {
                int quantity = hashmap.get(value);
                hashmap.put(value, quantity + 1);
            } else {
                hashmap.put(value, 1);
            }
        }
        return hashmap;
    }

    public void clearMap() {
        if (this.map != null) {
            this.map.clear();
        }
    }
}
