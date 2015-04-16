package networkSimulation;

/**
 * A simple tuple wrapper, and ode to C#
 *
 * @param <X> a generic type
 * @param <Y> a generic type
 */
public class Tuple<X, Y> {
	
	public X x;
	public Y y;

	/**
	 * Instantiates a new tuple.
	 *
	 * @param x the first value
	 * @param y the second value
	 */
	public Tuple(X x, Y y) {
		this.x = x;
		this.y = y;
	}
}
