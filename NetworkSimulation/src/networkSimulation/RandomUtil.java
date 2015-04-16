package networkSimulation;

import java.util.AbstractList;
import java.util.ArrayList;

import org.apache.poi.ss.formula.functions.T;

import repast.simphony.random.RandomHelper;

public class RandomUtil {

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
