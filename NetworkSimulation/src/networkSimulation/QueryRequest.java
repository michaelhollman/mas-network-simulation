package networkSimulation;

import java.util.*;

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
			FileSharingNode lastNode = nodes.pop();
			double tick = nodeAddTimes.pop().doubleValue();
			for (FileSharingNode node : nodes) {
				node.giveResponseTimeInfo(lastNode.config.NodeIp, fulfilledTick - tick);	
			}
		}
	}
	
	public void addIntermediate(FileSharingNode intermediate) {
		nodes.push(intermediate);
		nodeAddTimes.push(new Double(RunEnvironment.getInstance().getCurrentSchedule().getTickCount()));
	}
}
