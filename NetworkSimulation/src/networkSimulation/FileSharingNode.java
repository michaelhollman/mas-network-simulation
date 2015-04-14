package networkSimulation;

import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.graph.Network;

public class FileSharingNode {
	private Network<FileSharingNode> knownConnections;
	
	private int id;
	private int count; // Test crap so we can generate graphs
	
	public FileSharingNode(Network<FileSharingNode> knownConnections, NodeConfiguration configuration) {
		this.knownConnections = knownConnections;
		
		this.id = configuration.Id;
		
		count = 0;
	}

	@ScheduledMethod(start = 1, interval = 1)
	public void doSomethingStupid() {
		System.out.println("["+ Integer.toString(this.id) +"] Tick");
		
		count++;
	}
}