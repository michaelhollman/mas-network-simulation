package networkSimulation;

import java.util.HashMap;
import java.util.Stack;

import repast.simphony.engine.environment.RunEnvironment;

public class QueryRequest extends AbstractRequest {

	public int fileNumber;
	public Stack<FileSharingNode> nodes;
	private Stack<Double> nodeAddTimes;
	
	public QueryRequest(FileSharingNode source, int fileNumber) {
		super(source);
		this.fileNumber = fileNumber;
		this.nodes = new Stack<FileSharingNode>();
		this.nodes.push(source);
		this.nodeAddTimes = new Stack<Double>();
		this.nodeAddTimes.push(new Double(startTick));
	}
	
	public void fulfill(FileSharingNode fulfiller) {
		super.fulfill(fulfiller);
		sourceNode.giveFile(fileNumber);
		while (!nodes.empty()) {
			FileSharingNode node = nodes.pop();
			double tick = nodeAddTimes.pop().doubleValue();
			node.giveResponseTimeInfo(fulfiller.ip, fulfilledTick - tick);
		}
	}
	
	public void addIntermediate(FileSharingNode intermediate) {
		nodes.push(intermediate);
		nodeAddTimes.push(new Double(RunEnvironment.getInstance().getCurrentSchedule().getTickCount()));
	}
}
