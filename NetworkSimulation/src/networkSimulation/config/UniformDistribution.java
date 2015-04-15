package networkSimulation.config;

import java.util.Random;

import networkSimulation.Distribution;

public class UniformDistribution implements Distribution {
	private double min;
	private double max;
	private double pvalue;
	private Random rand;

	public UniformDistribution(double min, double max, double pvalue) {
		this.min = min;
		this.max = max;
		this.pvalue = pvalue;
		this.rand = new Random();
	}
	
	@Override
	public double getValue() {
		double u = rand.nextDouble();
		return min + u * (max-min);
	}

	@Override
	public boolean getDecision() {
		double u = getValue();
		return pvalue > u;
	}
}
