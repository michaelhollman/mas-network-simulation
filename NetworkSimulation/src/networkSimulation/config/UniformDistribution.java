package networkSimulation.config;

import java.util.Random;

import networkSimulation.Distribution;

public class UniformDistribution implements Distribution {
	private double min;
	private double max;
	private Random rand;

	public UniformDistribution(double min, double max) {
		this.min = min;
		this.max = max;
		this.rand = new Random();
	}
	
	@Override
	public double getValue() {
		double u = rand.nextDouble();
		return min + u * (max-min);
	}

	@Override
	public boolean getDecision(double pvalue) {
		double u = getValue();
		return pvalue > u;
	}
}
