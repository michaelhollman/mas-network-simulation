package networkSimulation;

import repast.simphony.context.Context;
import repast.simphony.context.DefaultContext;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.graph.Network;

public class NetworkContextBuilder extends DefaultContext<FileSharingNode> implements ContextBuilder<FileSharingNode> {

	public Context<FileSharingNode> build(Context<FileSharingNode> context) {
		context.setId("rak_network");
		
		NetworkBuilder<FileSharingNode> knownConnectionsNetworkBuilder = new NetworkBuilder<FileSharingNode>("known connections", context, false);
		Network<FileSharingNode> knownConnections = knownConnectionsNetworkBuilder.buildNetwork();
		
		Parameters params = RunEnvironment.getInstance().getParameters();
		int nodeCount = (Integer) params.getValue("node_count");
		
		for (int i = 0; i < nodeCount; i++)
		{
			NodeConfiguration config = new NodeConfiguration();
			config.Id = i;
			
			context.add(new FileSharingNode(knownConnections, config));
		}
		
		return context;
	}
}
