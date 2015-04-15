package networkSimulation.config;

import java.util.ArrayList;

import networkSimulation.FileSharingNode;
import networkSimulation.GlobalContext;
import networkSimulation.NodeConfiguration;
import networkSimulation.NodeState;
import networkSimulation.NodeType;
import repast.simphony.context.Context;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.graph.Network;

public class TestBuilder extends ContextBuilderBuilder {

	@Override
	public Context<FileSharingNode> build(Context<FileSharingNode> context, Parameters params,
			Network<FileSharingNode> knownConnections,
			Network<FileSharingNode> currentConnections,
			ArrayList<FileSharingNode> allNodes) {

		// Parse parameters
		int nodeCount = params.getInteger("node_count");
		int initialActive = params.getInteger("initialActive");
		int fileCount = params.getInteger("file_count");
		int genericConnectionLimit = params.getInteger("genericConnectionLimit");
		double churnRate = params.getDouble("churnRate");
		double requestRate = params.getDouble("requestRate");
		
		// Initialize nodes
		ArrayList<FileSharingNode> nodes = new ArrayList<FileSharingNode>();
		int initialDead = nodeCount - initialActive;
		int deadModulo = nodeCount / initialDead;
		
		for (int ip = 0; ip < nodeCount; ip++) {
			boolean isDead = ip % deadModulo == 0;
			
			NodeConfiguration configuration = new NodeConfiguration();
			configuration.NodeIp = ip;
			configuration.NodeState = isDead ? NodeState.DEAD : NodeState.ALIVE;
			configuration.NodeType = NodeType.GENERIC;
			configuration.SimultaneousConnectionLimit = genericConnectionLimit;
			configuration.ChurnDistribution = new UniformDistribution(0, 1, 1-churnRate);
			configuration.RequestDistribution = new UniformDistribution(0, 1, 1-requestRate);
			
			// TODO: Allocate files
			
			// TODO: Place in space
			
			FileSharingNode node = new FileSharingNode(knownConnections, currentConnections, configuration);
			nodes.add(node);
		}
		
		// Set shared global state
		GlobalContext.NodeCount = nodeCount;
		GlobalContext.FileCount = fileCount;	
		GlobalContext.IpLookup = nodes;
		
		// Debug information
		System.out.println("Created " + nodeCount + " nodes.");
		System.out.println("Created " + fileCount + " files.");
		
		// Set and return context
		context.addAll(nodes);				
		return context;
	}
}
