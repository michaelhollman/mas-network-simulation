package networkSimulation;

import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.graph.Network;

public class FileSharingNode {
	private Network<FileSharingNode> knownConnections;
	private Network<FileSharingNode> currentConnections;
	
	private int id;
	private int count; // Test crap so we can generate graphs
	
	public FileSharingNode(Network<FileSharingNode> knownConnections, Network<FileSharingNode> currentConnections, NodeConfiguration configuration) {
		this.knownConnections = knownConnections;
		this.currentConnections = currentConnections;
		this.id = configuration.NodeId;
		count = 0;
	}

	@ScheduledMethod(start = 1, interval = 1)
	public void doSomethingStupid() {
		System.out.println("["+ Integer.toString(this.id) +"] Tick");
		count++;
	}
}