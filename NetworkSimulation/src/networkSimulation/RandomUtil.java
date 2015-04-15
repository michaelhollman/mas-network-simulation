package networkSimulation;

import java.util.*;

import repast.simphony.random.RandomHelper;

public class RandomUtil {
	
	public static int getRandom(int from, int to, AbstractList<Integer> excluding) {
		ArrayList<Integer> possible = new ArrayList<Integer>();
		for (int i = from; i <= to; i ++) {
			Integer intg = new Integer(i);
			if (!excluding.contains(intg)) {
				possible.add(intg);
			}
		}
		int idx = RandomHelper.nextIntFromTo(0, possible.size() - 1);
		return possible.get(idx);
	}
	
}
