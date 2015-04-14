package networkSimulation;

import repast.simphony.context.Context;
import repast.simphony.context.DefaultContext;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.graph.Network;


public class NetworkContextBuilder extends DefaultContext<FileSharingNode> implements ContextBuilder<FileSharingNode> {
	public Context<FileSharingNode> build(Context<FileSharingNode> context) {
		context.setId("NetworkSimulation");
				
		NetworkBuilder<FileSharingNode> knownConnectionsNetworkBuilder = new NetworkBuilder<FileSharingNode>("knownConnections", context, false);
		Network<FileSharingNode> knownConnections = knownConnectionsNetworkBuilder.buildNetwork();
		
		NetworkBuilder<FileSharingNode> currentConnectionsNetworkBuilder = new NetworkBuilder<FileSharingNode>("currentConnections", context, false);
		Network<FileSharingNode> currentConnections = currentConnectionsNetworkBuilder.buildNetwork();
		
		Parameters params = RunEnvironment.getInstance().getParameters();
		int nodeCount = (Integer) params.getValue("node_count");
		
		FileSharingNode last = null;
		for (int i = 0; i < nodeCount; i++)
		{
			NodeConfiguration config = new NodeConfiguration();
			config.NodeId = i;
			
			FileSharingNode n = new FileSharingNode(knownConnections, currentConnections, config);
			if (last != null) knownConnections.addEdge(last, n);
			
			last = n;
			context.add(n);
		}
				
		return context;
	}
}
