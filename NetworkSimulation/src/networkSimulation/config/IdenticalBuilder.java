package networkSimulation.config;

import java.util.ArrayList;

import networkSimulation.FileSharingNode;
import networkSimulation.NodeConfiguration;
import repast.simphony.context.Context;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.graph.Network;

public class IdenticalBuilder extends ContextBuilderBuilder {

	@Override
	public Context<FileSharingNode> build(Context<FileSharingNode> context, Parameters params,
			Network<FileSharingNode> knownConnections,
			Network<FileSharingNode> currentConnections,
			ArrayList<FileSharingNode> allNodes) {
		
//		int nodeCount = params.getInteger("node_count");
//		
//		// Create nodes
//		int randomSkipAmount = 3;
//		int currentId = randInt(0, randomSkipAmount);
//		while (allNodes.size() < nodeCount) {
//			NodeConfiguration config = new NodeConfiguration();
//			config.NodeIp = currentId;
//			config.PreviouslyKnownNodes = new ArrayList<Integer>();
//			config.StartingFiles = new ArrayList<Integer>();
//			config.SimultaneousConnectionLimit = randInt(5, 10);
//			config.ChurnDistribution = new UniformDistribution(0, 1);
//			config.RequestDistribution = new UniformDistribution(0, 1);
//			
//			FileSharingNode node = new FileSharingNode(knownConnections, currentConnections, config);
//			allNodes.add(node);
//			
//			currentId += randInt(1, randomSkipAmount);			
//		}
//		
//		// TODO: Setup known connections, previously known
//		
//		// Add to context
//		context.addAll(allNodes);		
		
		return context;
	}
	
}
