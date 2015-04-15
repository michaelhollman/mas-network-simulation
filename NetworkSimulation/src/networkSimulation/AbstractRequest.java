package networkSimulation;

import repast.simphony.engine.environment.RunEnvironment;

public abstract class AbstractRequest {

	public FileSharingNode sourceNode;
	public FileSharingNode fulfiller;
	public double startTick;
	public boolean fulfilled;
	public double fulfilledTick;
	private boolean timedOut;
	public double timedOutTick;

	public AbstractRequest(FileSharingNode source) {
		sourceNode = source;
		startTick = RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		fulfilled = false;
		timedOut = false;
	}
	
	public boolean checkTimeOut() {
		if (timedOut) 
			return timedOut;
		
		double currentTick = RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		timedOut = (currentTick - startTick) > 100; // TODO config 100
		if (timedOut) {
			timedOutTick = currentTick;
		}
		return timedOut;
	}
	
	protected void fulfill(FileSharingNode fulfiller) {
		this.fulfilled = true;
		this.fulfiller = fulfiller;
		this.fulfilledTick = RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		
		System.out.println("Fulfilled request by node " + sourceNode.config.NodeIp + " from node " + fulfiller.config.NodeIp + " in ticks " + (fulfilledTick-startTick));
	}
}
