package networkSimulation;

import java.util.Stack;
import java.util.Vector;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.space.graph.RepastEdge;

/**
 * A request for a File
 */
public class QueryRequest extends AbstractRequest implements Cloneable {

    /** The file number we're requesting. */
    public int fileNumber;

    /**
     * Instantiates a new request for a file in the network
     *
     * @param source the node initiating this request
     * @param fileNumber the file number we're requesting
     */
    public QueryRequest(FileSharingNode source, int fileNumber) {
        super(source);
        this.fileNumber = fileNumber;
    }

    public QueryRequest() {}

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
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

    /**
     * Checks if is the same as another request, used to prevent 
     * duplicate requests from happening (no cycles)
     *
     * @param req the req
     * @return true, if is equivalent to
     */
    public boolean isEquivalentTo(QueryRequest req) {
        return fileNumber == req.fileNumber && startTick == req.startTick && sourceNode.equals(req.sourceNode);
    }

    /**
     * Adds a filesharing node as an intermediate node in this request
     *
     * @param intermediate the intermediate
     */
    public void addIntermediate(FileSharingNode intermediate) {
        nodes.push(intermediate);
        double currentTick = RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
        nodeAddTimes.push(currentTick);
        lastInteractionTick = currentTick;
    }
    
    /* (non-Javadoc)
     * @see networkSimulation.AbstractRequest#bubbleUpTickInfo(boolean)
     */
    protected void bubbleUpTickInfo(boolean didTimeout) {
    	super.bubbleUpTickInfo(didTimeout);
    	
    	double timeoutTime = timedOutTick - startTick;
    	sourceNode.markUnfulfilled(timeoutTime);
    }

    /* (non-Javadoc)
     * @see networkSimulation.AbstractRequest#fulfill(networkSimulation.FileSharingNode)
     */
    public void fulfill(FileSharingNode fulfiller) {

        @SuppressWarnings("unchecked")
        Stack<FileSharingNode> tempNodes = (Stack<FileSharingNode>) nodes.clone();

        while (!tempNodes.isEmpty()) {
            FileSharingNode last = tempNodes.pop();
            if (tempNodes.isEmpty())
                break;
            FileSharingNode next = tempNodes.peek();
            next.giveFileInfo(fileNumber, last.config.NodeIp);
        }

        super.fulfill(fulfiller);
        
        double fulfillmentTime = fulfilledTick - startTick;
        sourceNode.giveFile(fileNumber, fulfillmentTime);
    }
}
