package networkSimulation.config;

import java.util.ArrayList;

import networkSimulation.FileSharingNode;
import networkSimulation.GlobalContext;
import networkSimulation.NodeConfiguration;
import networkSimulation.NodeState;
import networkSimulation.NodeType;
import repast.simphony.context.Context;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.graph.Network;

/**
 * ContetxBuilderBuilder that fills the Context with both Leaf Nodes and Ultra Nodes,
 * with the ultras occupying an inner circle in continous space, and the leaves an
 * outer circle
 */
public class UltraAndLeafBuilder extends ContextBuilderBuilder {

	/* (non-Javadoc)
	 * @see networkSimulation.config.ContextBuilderBuilder#build(repast.simphony.context.Context, repast.simphony.parameter.Parameters, repast.simphony.space.graph.Network, repast.simphony.space.graph.Network, repast.simphony.space.continuous.ContinuousSpace)
	 */
	@Override
	public Context<FileSharingNode> build(Context<FileSharingNode> context,
			Parameters params, Network<FileSharingNode> knownConnections,
			Network<FileSharingNode> currentConnections,
			ContinuousSpace<FileSharingNode> space) {
		
		// Parse parameters
		int nodeCount = params.getInteger("node_count");
		int initialActive = params.getInteger("initialActive");
		int timeout = params.getInteger("timeout");		
		int fileCount = params.getInteger("file_count");
		double percentUltra = params.getDouble("percentUltra");
		
		int ultraStartingFiles = params.getInteger("ultraStartingFiles");
		int leafStartingFiles = params.getInteger("leafStartingFiles");
		int ultraConnectionLimit = params.getInteger("ultraConnectionLimit");
		int leafConnectionLimit = params.getInteger("leafConnectionLimit");
		double ultraChurnRate = params.getDouble("ultraChurnRate");
		double leafChurnRate = params.getDouble("leafChurnRate");
		double ultraRequestRate = params.getDouble("ultraRequestRate");
		double leafRequestRate = params.getDouble("leafRequestRate");
				
		// Initialize nodes
		ArrayList<FileSharingNode> ultras = new ArrayList<FileSharingNode>();
		ArrayList<FileSharingNode> leaves = new ArrayList<FileSharingNode>();
		int initialDead = nodeCount - initialActive;
		int deadModulo = nodeCount / initialDead;
		int numUltras = (int)Math.floor(nodeCount * percentUltra);
		System.out.println("Num Ultras: " + numUltras);
		
		// Create ultras
		for (int ip = 0; ip < numUltras; ip++) {
			boolean isDead = ip % deadModulo == 0;
			
			// Generic node configuration
			NodeConfiguration configuration = new NodeConfiguration();
			configuration.NodeIp = ip;
			configuration.NodeState = isDead ? NodeState.DEAD : NodeState.ALIVE;
			configuration.NodeType = NodeType.ULTRA_PEER;
			configuration.SimultaneousConnectionLimit = ultraConnectionLimit;
			configuration.ChurnDistribution = new UniformDistribution(0, 1, 1-ultraChurnRate);
			configuration.RequestDistribution = new UniformDistribution(0, 1, 1-ultraRequestRate);
			configuration.StartingFiles = new ArrayList<Integer>();
			
			// Allocate files 
			for (int i = 0; i < ultraStartingFiles; i++) {
				int fileNum = randInt(0, fileCount - 1);
				if (!configuration.StartingFiles.contains(fileNum)) {
					configuration.StartingFiles.add(fileNum);					
				}
			}
			
			FileSharingNode node = new FileSharingNode(knownConnections, currentConnections, configuration);
			ultras.add(node);
		}
		
		// Create leaves
		for (int ip = numUltras; ip < nodeCount; ip++) {
			boolean isDead = ip % deadModulo == 0;
			
			// Generic node configuration
			NodeConfiguration configuration = new NodeConfiguration();
			configuration.NodeIp = ip;
			configuration.NodeState = isDead ? NodeState.DEAD : NodeState.ALIVE;
			configuration.NodeType = NodeType.LEAF;
			configuration.SimultaneousConnectionLimit = leafConnectionLimit;
			configuration.ChurnDistribution = new UniformDistribution(0, 1, 1-leafChurnRate);
			configuration.RequestDistribution = new UniformDistribution(0, 1, 1-leafRequestRate);
			configuration.StartingFiles = new ArrayList<Integer>();
			
			// Allocate files 
			for (int i = 0; i < leafStartingFiles; i++) {
				int fileNum = randInt(0, fileCount - 1);
				if (!configuration.StartingFiles.contains(fileNum)) {
					configuration.StartingFiles.add(fileNum);					
				}
			}
			
			FileSharingNode node = new FileSharingNode(knownConnections, currentConnections, configuration);
			leaves.add(node);
		}
		
		// Set and return context
		context.addAll(ultras);		
		context.addAll(leaves);
		
		// Place nodes in a circle in space (after adding to context)
		double spaceSize = space.getDimensions().getHeight();
		double center = spaceSize/2;
		double ultraRadius = center / 2;
		double leafRadius = center - 2;
		
		for (int i = 0; i < ultras.size(); i++) {
			double theta = 2*Math.PI*i/ultras.size();
			double x = center + ultraRadius*Math.cos(theta);
			double y = center + ultraRadius*Math.sin(theta);
			space.moveTo(ultras.get(i), x, y);
		}
		
		for (int i = 0; i < leaves.size(); i++) {
			double theta = 2*Math.PI*i/leaves.size();
			double x = center + leafRadius*Math.cos(theta);
			double y = center + leafRadius*Math.sin(theta);
			space.moveTo(leaves.get(i), x, y);
		}
		
		// Set shared global state
		GlobalContext.NodeCount = nodeCount;
		GlobalContext.FileCount = fileCount;
		GlobalContext.Timeout = timeout;
		GlobalContext.IpLookup = ultras;
		GlobalContext.IpLookup.addAll(leaves);
		
		// Debug information
		System.out.println("Created " + nodeCount + " nodes.");
		System.out.println("Created " + fileCount + " files.");
		
		return context;
		
	}
}
