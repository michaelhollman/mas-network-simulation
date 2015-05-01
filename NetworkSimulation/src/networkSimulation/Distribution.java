package networkSimulation;

/**
 * Represents a distribution from which random numbers can be drawn
 */
public interface Distribution {
	
	/**
	 * Gets a random number from this distribution.
	 *
	 * @return the random number
	 */
	double getValue();
	
	/**
	 * Conveninience method that gets a yes/no decision.  Typically,
	 * this will come from a p-value set when the distribution was created.
	 *
	 * @return the decision
	 */
	boolean getDecision();
}
