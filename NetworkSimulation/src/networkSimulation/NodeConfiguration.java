package networkSimulation;

import java.util.ArrayList;

/**
 * Basic configuration struct passed to each node when they are created
 */
public class NodeConfiguration {
	public int NodeIp;
	public NodeState NodeState;
	public NodeType NodeType;
	
	public ArrayList<Integer> StartingFiles;	
	public int SimultaneousConnectionLimit;	
	public Distribution RequestDistribution;
	public Distribution ChurnDistribution;
}
