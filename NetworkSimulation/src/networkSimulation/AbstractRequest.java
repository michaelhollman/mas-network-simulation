package networkSimulation;

import java.util.*;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;

public abstract class AbstractRequest {

	public final static int CONNECTION_PING = 1;
	public final static int CONNECTION_QUERY = 2;
	
	public FileSharingNode sourceNode;
	public FileSharingNode fulfiller;
	public Stack<FileSharingNode> nodes;
	protected Stack<Double> nodeAddTimes;
	public double startTick;
	public boolean fulfilled;
	public double fulfilledTick;
	private boolean timedOut;
	public double timedOutTick;
	protected double lastInteractionTick;
	private Vector<RepastEdge<FileSharingNode>> edges;

	public AbstractRequest(FileSharingNode source) {
		sourceNode = source;
		startTick = RunEnvironment.getInstance().getCurrentSchedule()
				.getTickCount();
		lastInteractionTick = startTick;
		fulfilled = false;
		timedOut = false;
		nodes = new Stack<FileSharingNode>();
		nodes.push(source);
		nodeAddTimes = new Stack<Double>();
		nodeAddTimes.push(startTick);
		edges = new Vector<RepastEdge<FileSharingNode>>();
	}

	public boolean checkTimeOut() {
		if (timedOut)
			return timedOut;

		double currentTick = RunEnvironment.getInstance().getCurrentSchedule()
				.getTickCount();
		timedOut = (currentTick - startTick) >= GlobalContext.Timeout;
		if (timedOut) {
			timedOutTick = currentTick;
			bubbleUpTickInfo(true);
		}
		return timedOut;
	}

	protected void bubbleUpTickInfo(boolean didTimeout) {
		while (!nodes.empty()) {
			FileSharingNode lastNode = nodes.pop();
			double tick = nodeAddTimes.pop().doubleValue();
			for (FileSharingNode node : nodes) {
				double numberOfTicks = didTimeout ? (timedOutTick - startTick)
						: (fulfilledTick - tick);
				node.giveResponseTimeInfo(lastNode.config.NodeIp,
						numberOfTicks, didTimeout);
			}
		}
	}

	protected void fulfill(FileSharingNode fulfiller) {
		this.fulfilled = true;
		this.fulfiller = fulfiller;
		this.fulfilledTick = RunEnvironment.getInstance().getCurrentSchedule()
				.getTickCount();
		bubbleUpTickInfo(false);
		
		System.out.println("Request fulfilled from " + sourceNode.config.NodeIp + " to " + fulfiller.config.NodeIp +" in " + (fulfilledTick - startTick) + " ticks");
	}

	public boolean needsToWaitOneTick() {
		double currentTick = RunEnvironment.getInstance().getCurrentSchedule()
				.getTickCount();
		return currentTick == lastInteractionTick;
	}

	public void removeFromNetwork(Network<FileSharingNode> network) {
		if (edges == null)
			return;
		for (RepastEdge<FileSharingNode> edge : edges) {
			if (network.containsEdge(edge)) {
				network.removeEdge(edge);
			}
		}
		edges = null;
	}

	public void addEdge(RepastEdge<FileSharingNode> edge) {
		if (edge == null)
			return;
		edges.add(edge);
	}
}
