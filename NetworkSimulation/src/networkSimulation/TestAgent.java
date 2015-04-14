package networkSimulation;

import repast.simphony.engine.schedule.ScheduledMethod;

public class TestAgent {
	private int id;
	
	public TestAgent(int id) {
		this.id = id;
	}
	
	@ScheduledMethod(start = 1, interval = 1)
	public void doSomethingStupid() {
		System.out.println("["+ Integer.toString(this.id) +"] Tick");
	}

}
