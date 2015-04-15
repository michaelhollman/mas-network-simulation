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
}
