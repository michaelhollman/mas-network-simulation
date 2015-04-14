package networkSimulation;

import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.graph.Network;

public class P2PFileSharingNode {

	private Network<Object> activeConnections;
	private Network<Object> knownConnections;
	private ContinuousSpace<Object> space;
	private NodeConfiguration configuration;
	
	public P2PFileSharingNode(Network<Object> activeConnections, 
							  Network<Object> knownConnections, 
							  ContinuousSpace<Object> space, 
							  NodeConfiguration configuration) {
		this.activeConnections = activeConnections;
		this.knownConnections = knownConnections;
		this.space = space;
		this.configuration = configuration;
	}

	@ScheduledMethod(start = 1, interval = 1)
	public void step() {
				
	}
}
