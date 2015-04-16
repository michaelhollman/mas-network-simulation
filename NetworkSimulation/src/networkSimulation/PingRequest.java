package networkSimulation;

/**
 * A ping to another node to see if it is online
 */
public class PingRequest extends AbstractRequest {

    /** The target ip address. */
    public int targetIP;

    /**
     * Instantiates a new ping request to another node
     *
     * @param source the node initiating the ping
     * @param targetIP the target ip we're pinging
     */
    public PingRequest(FileSharingNode source, int targetIP) {
        super(source);
        this.targetIP = targetIP;
    }

    /**
     * Sends the ping to the target node
     */
    public void send() {
        GlobalContext.IpLookup.get(targetIP).ping(this);
    }

    /* (non-Javadoc)
     * @see networkSimulation.AbstractRequest#bubbleUpTickInfo(boolean)
     */
    protected void bubbleUpTickInfo(boolean didTimeout) {
        double numberOfTicks = (didTimeout ? timedOutTick : fulfilledTick) - startTick;

        if (!didTimeout && fulfiller != null && fulfiller.config.NodeType != NodeType.ULTRA_PEER) {
            numberOfTicks *= GlobalContext.Timeout;
        }

        sourceNode.giveResponseTimeInfo(targetIP, numberOfTicks, didTimeout);
    }
}
