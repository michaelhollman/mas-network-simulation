package networkSimulation;

import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.graph.Network;
import repast.simphony.space.grid.Grid;

public class FileSharingNode {
	private Network<Object> knownConnections;
	
	private int id;
	
	public FileSharingNode(Network<Object> knownConnections, NodeConfiguration configuration) {
		this.knownConnections = knownConnections;
		
		this.id = configuration.Id;
	}

	@ScheduledMethod(start = 1, interval = 1)
	public void doSomethingStupid() {
		System.out.println("["+ Integer.toString(this.id) +"] Tick");
	}
}