package networkSimulation.config;

import java.util.Random;

import networkSimulation.Distribution;

/**
 * Represents a Uniform Distribution from which random numebrs can be drawn
 */
public class UniformDistribution implements Distribution {
	
	/** The min value this distribution can return. */
	private double min;
	
	/** The max value this distribution can return. */
	private double max;
	
	/** The pvalue threshold for this instance.  A number greater than this
	 * p-value will cause a decision to resolve as true. */
	private double pvalue;
	
	/** The Random instance for this distribution. */
	private Random rand;

	/**
	 * Instantiates a new uniform distribution.
	 *
	 * @param min the min
	 * @param max the max
	 * @param pvalue the pvalue
	 */
	public UniformDistribution(double min, double max, double pvalue) {
		this.min = min;
		this.max = max;
		this.pvalue = pvalue;
		this.rand = new Random();
	}
	
	/* (non-Javadoc)
	 * @see networkSimulation.Distribution#getValue()
	 */
	@Override
	public double getValue() {
		double u = rand.nextDouble();
		return min + u * (max-min);
	}

	/* (non-Javadoc)
	 * @see networkSimulation.Distribution#getDecision()
	 */
	@Override
	public boolean getDecision() {
		double u = getValue();
		return pvalue < u;
	}
}
