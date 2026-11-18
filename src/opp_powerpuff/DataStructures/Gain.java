package opp_powerpuff.DataStructures;

public class Gain {
    private static int capacity;
    private static int[] values;
    private static int index = 0;
    private static int count = 0;
    private static int sum = 0;

    public static void init(int cap) {
        capacity = cap;
        values = new int[capacity];
    }

    /**
     * Add a new value to the running window.
     */
    public static void add(int value) {
        // If buffer is full, remove the value being overwritten
        if (count == capacity) {
            sum -= values[index];
        } else {
            count++;
        }

        values[index] = value;
        sum += value;

        index = (index + 1) % capacity;
    }

    /**
     * Get the current running average.
     */
    public static float getAverage() {
        if (count == 0) return 0;
        return (float) sum / count;
    }

    /**
     * Get a copy of the stored values (oldest → newest).
     */
    public static int[] getValues() {
        int[] result = new int[count];
        for (int i = 0; i < count; i++) {
            int pos = (index - count + i + capacity) % capacity;
            result[i] = values[pos];
        }
        return result;
    }

    /**
     * Reset all stored data.
     */
    public static void reset() {
        values = new int[capacity];
        index = 0;
        count = 0;
        sum = 0;
    }
}