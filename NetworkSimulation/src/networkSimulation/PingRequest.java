package networkSimulation;

public class PingRequest extends AbstractRequest {
		
	public int targetIP;
	
	public PingRequest(FileSharingNode source, int targetIP) {
		super(source);
		this.targetIP = targetIP;
	}
	
	public void fulfull(FileSharingNode ponger) {
		super.fulfill(ponger);
		sourceNode.giveResponseTimeInfo(targetIP, fulfilledTick - startTick);
	}
}
