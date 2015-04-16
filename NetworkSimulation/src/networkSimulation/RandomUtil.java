package networkSimulation;

import java.util.AbstractList;
import java.util.ArrayList;

import org.apache.poi.ss.formula.functions.T;

import repast.simphony.random.RandomHelper;

/**
 * Utility class for getting random numbers under various circumstances
 */
public class RandomUtil {

    /**
     * Gets a random number between two numbers (upper-exclusive), excluding all of the numbers
     * passed in to the excluding list.
     *
     * @param from the inclusive lower bound
     * @param to the exclusive upper bound
     * @param excluding exclude all numbers from this
     * @return a random number in [from, to) that is not in excluding
     */
    public static Integer getRandom(int from, int to, AbstractList<Integer> excluding) {
        ArrayList<Integer> possible = new ArrayList<Integer>();
        for (int i = from; i <= to; i++) {
            Integer intg = new Integer(i);
            if (excluding == null || !excluding.contains(intg)) {
                possible.add(intg);
            }
        }

        if (possible.size() == 0)
            return null;

        int idx = RandomHelper.nextIntFromTo(0, possible.size() - 1);
        return possible.get(idx);
    }

    public static T getRandom(AbstractList<T> list, AbstractList<T> excluding) {
        return null;

    }

}
