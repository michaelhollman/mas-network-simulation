package networkSimulation.config;

import java.util.ArrayList;
import java.util.Random;

import networkSimulation.FileSharingNode;
import repast.simphony.context.Context;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.graph.Network;

public abstract class ContextBuilderBuilder {
	private Random rand;
	
	public ContextBuilderBuilder() {
		this.rand = new Random();	
	}
	
	public abstract Context<FileSharingNode> build(Context<FileSharingNode> context, Parameters params, Network<FileSharingNode> knownConnections, Network<FileSharingNode> currentConnections, ArrayList<FileSharingNode> allNodes);
	
	protected int randInt(int min, int max) {
	    int randomNum = rand.nextInt((max - min) + 1) + min;
	    return randomNum;
	}
}
