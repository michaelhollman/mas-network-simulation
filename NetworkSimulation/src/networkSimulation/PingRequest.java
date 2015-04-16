package networkSimulation;

public class PingRequest extends AbstractRequest {

    public int targetIP;

    public PingRequest(FileSharingNode source, int targetIP) {
        super(source);
        this.targetIP = targetIP;
    }

    public void send() {
        GlobalContext.IpLookup.get(targetIP).ping(this);
    }

    protected void bubbleUpTickInfo(boolean didTimeout) {
        double numberOfTicks = (didTimeout ? timedOutTick : fulfilledTick) - startTick;

        if (!didTimeout && fulfiller != null) {
            numberOfTicks *= fulfiller.config.PingWeightingScalar;
        }

        sourceNode.giveResponseTimeInfo(targetIP, numberOfTicks, didTimeout);
    }
}
