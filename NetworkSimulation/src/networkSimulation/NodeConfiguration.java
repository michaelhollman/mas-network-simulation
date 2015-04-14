package networkSimulation;

import java.util.ArrayList;

public class NodeConfiguration {
	public int NodeId;
	public ArrayList<Integer> PreviouslyKnownNodes;
	
	public ArrayList<Integer> StartingFiles;
	
	public int SimultaneousConnectionLimit;
	
	public Distribution WantNewFileDistribution;
	public Distribution JoinLeaveDistribution;
}
