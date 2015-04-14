package networkSimulation;

import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.graph.Network;
import repast.simphony.space.grid.Grid;

public class FileSharingNode {
	private Network<Object> knownConnections;
	private Network<Object> currentConnections;
	
	private int id;
	
	public FileSharingNode(Network<Object> knownConnections, Network<Object> currentConnections, NodeConfiguration configuration) {
		this.knownConnections = knownConnections;
		this.currentConnections = currentConnections;
		
		this.id = configuration.NodeId;
	}

	@ScheduledMethod(start = 1, interval = 1)
	public void doSomethingStupid() {
		System.out.println("["+ Integer.toString(this.id) +"] Tick");
	}
}