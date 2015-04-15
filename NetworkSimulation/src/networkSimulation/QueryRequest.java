package networkSimulation;

import repast.simphony.engine.environment.RunEnvironment;

public class QueryRequest extends AbstractRequest {

	public int fileNumber;
	
	public QueryRequest(FileSharingNode source, int fileNumber) {
		super(source);
		this.fileNumber = fileNumber;
	}
	
	public void fulfill(FileSharingNode fulfiller) {
		super.fulfill(fulfiller);
		sourceNode.giveFile(fileNumber);
	}
	
	public void addIntermediate(FileSharingNode intermediate) {
		nodes.push(intermediate);
		double currentTick = RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		nodeAddTimes.push(currentTick);
		lastInteractionTick = currentTick;
	}
}
