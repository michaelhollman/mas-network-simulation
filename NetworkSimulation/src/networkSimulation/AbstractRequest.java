package networkSimulation;

import java.util.Stack;
import java.util.Vector;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;

/**
 * Represents a request that can be made from one node to
 * another.  
 */
public abstract class AbstractRequest {

    /** The Constant CONNECTION_PING. */
    public final static int CONNECTION_PING = 1;
    
    /** The Constant CONNECTION_QUERY. */
    public final static int CONNECTION_QUERY = 2;

    /** The source node of this request. */
    public FileSharingNode sourceNode;
    
    /** The fulfiller of this request. */
    public FileSharingNode fulfiller;
    
    /** The nodes involved in this request. */
    public Stack<FileSharingNode> nodes;
    
    /** The times nodes were added to the request.  Syncronized with nodes. */
    protected Stack<Double> nodeAddTimes;
    
    /** The tick this requested started */
    public double startTick;
    
    /** If this request is fulfilled. */
    public boolean fulfilled;
    
    /** The tick this request was fulfilled. */
    public double fulfilledTick;
    
    /** If this request has timed out. */
    protected boolean timedOut;
    
    /** The tick this request timed out. */
    public double timedOutTick;
    
    /** The tick of the last time we interacted with this request. */
    protected double lastInteractionTick;
    
    /** The currentConnections network edges built up during this request.
     * These get cleared when the request is resolved. */
    protected Vector<RepastEdge<FileSharingNode>> edges;

    /**
     * Base constructor
     */
    protected AbstractRequest() {};

    /**
     * Instantiates a new abstract request coming from a source
     *
     * @param source the FileSharingNode that initiated this request
     */
    public AbstractRequest(FileSharingNode source) {
        sourceNode = source;
        startTick = RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
        lastInteractionTick = startTick;
        fulfilled = false;
        timedOut = false;
        nodes = new Stack<FileSharingNode>();
        nodes.push(source);
        nodeAddTimes = new Stack<Double>();
        nodeAddTimes.push(startTick);
        edges = new Vector<RepastEdge<FileSharingNode>>();
    }

    /**
     * Checks if this request has timed out.
     *
     * @return true, if the request has timed out
     */
    public boolean checkTimeOut() {
        if (timedOut)
            return timedOut;

        double currentTick = RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
        timedOut = (currentTick - startTick) >= GlobalContext.Timeout;
        if (timedOut) {
            timedOutTick = currentTick;
            bubbleUpTickInfo(true);
            // System.out.println("Request timed out from " + sourceNode.config.NodeIp + " in " + (fulfilledTick - startTick) + " ticks");
        }
        return timedOut;
    }

    /**
     * Bubbles up information learned during the request to all involved nodes
     *
     * @param didTimeout if the request timed out
     */
    protected void bubbleUpTickInfo(boolean didTimeout) {
        while (!nodes.empty()) {
            FileSharingNode lastNode = nodes.pop();
            double tick = nodeAddTimes.pop().doubleValue();
            for (FileSharingNode node : nodes) {
                double numberOfTicks = didTimeout ? (timedOutTick - startTick) : (fulfilledTick - tick);
                node.giveResponseTimeInfo(lastNode.config.NodeIp, numberOfTicks, didTimeout);
            }
        }
    }

    /**
     * Fulfills this request.  All information is then bubbled up back to
     * involved nodes in the request stack.
     *
     * @param fulfiller the FileSharingNode that fulfilled this request
     */
    protected void fulfill(FileSharingNode fulfiller) {
        this.fulfilled = true;
        this.fulfiller = fulfiller;
        this.fulfilledTick = RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
        bubbleUpTickInfo(false);

        // System.out.println("Request fulfilled from " + sourceNode.config.NodeIp + " to " + fulfiller.config.NodeIp + " in " + (fulfilledTick - startTick) + " ticks");
    }

    /**
     * Checks if we need to wait a tick before processing this request,
     * to ensure that Repast doesn't process this request on the wrong tick
     *
     * @return true, if we need to wait a tick
     */
    public boolean needsToWaitOneTick() {
        double currentTick = RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
        return currentTick == lastInteractionTick;
    }

    /**
     * Iterates over all of the connections involved in this request,
     * and removes them from the currentConnections network passed int
     *
     * @param network the current connections request
     */
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

    /**
     * Adds an edge representing this part of the request to the current connections network
     *
     * @param edge the current connections request
     */
    public void addEdge(RepastEdge<FileSharingNode> edge) {
        if (edge == null)
            return;
        edges.add(edge);
    }
}
