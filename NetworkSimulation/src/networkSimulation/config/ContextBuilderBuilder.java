package networkSimulation.config;

import java.util.Random;

import networkSimulation.FileSharingNode;
import repast.simphony.context.Context;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.graph.Network;

/**
 * A builder for ContextBuilders, this class operates on a ContextBuilder and configures it appropriately.
 */
public abstract class ContextBuilderBuilder {
    /** The Random instance for this builder. */
    private Random rand;

    /**
     * Instantiates a new ContextBuilder builder.
     */
    public ContextBuilderBuilder() {
        this.rand = new Random();
    }

    /**
     * Configures the given context, filling it with agents and projections
     * 
     * @param context
     *            the Repast Context
     * @param params
     *            the simulation parameters
     * @param knownConnections
     *            the known connections
     * @param currentConnections
     *            the current connections
     * @param space
     *            the continuous space
     * @return the original context, filled with agents and projections
     */
    public abstract Context<FileSharingNode> build(Context<FileSharingNode> context, Parameters params, Network<FileSharingNode> knownConnections, Network<FileSharingNode> currentConnections, ContinuousSpace<FileSharingNode> space);

    /**
     * Helper method to get a random number between min and max, inclusive
     * 
     * @param min
     *            the inclusive lower bound
     * @param max
     *            the inclusive upper bound
     * @return a random number between min and max, inclusive
     */
    protected int randInt(int min, int max) {
        int randomNum = rand.nextInt((max - min) + 1) + min;
        return randomNum;
    }
}
