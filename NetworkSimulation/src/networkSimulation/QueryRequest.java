package networkSimulation;

import java.util.Stack;
import java.util.Vector;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.space.graph.RepastEdge;

public class QueryRequest extends AbstractRequest implements Cloneable {

    public int fileNumber;

    public QueryRequest(FileSharingNode source, int fileNumber) {
        super(source);
        this.fileNumber = fileNumber;
    }

    public QueryRequest() {}

    @SuppressWarnings("unchecked")
    @Override
    public Object clone() {
        QueryRequest n = new QueryRequest();
        n.sourceNode = sourceNode;
        n.fulfiller = fulfiller;
        n.nodes = (Stack<FileSharingNode>) nodes.clone();
        n.nodeAddTimes = (Stack<Double>) nodeAddTimes.clone();
        n.startTick = startTick;
        n.fulfilled = fulfilled;
        n.fulfilledTick = fulfilledTick;
        n.timedOut = timedOut;
        n.timedOutTick = timedOutTick;
        n.lastInteractionTick = lastInteractionTick;
        n.edges = (Vector<RepastEdge<FileSharingNode>>) edges.clone();
        n.fileNumber = fileNumber;
        return n;
    }

    public boolean isEquivalentTo(QueryRequest req) {
        return fileNumber == req.fileNumber && startTick == req.startTick && sourceNode.equals(req.sourceNode);
    }

    public void addIntermediate(FileSharingNode intermediate) {
        nodes.push(intermediate);
        double currentTick = RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
        nodeAddTimes.push(currentTick);
        lastInteractionTick = currentTick;
    }

    public void fulfill(FileSharingNode fulfiller) {
        super.fulfill(fulfiller);
        sourceNode.giveFile(fileNumber);
    }
}
