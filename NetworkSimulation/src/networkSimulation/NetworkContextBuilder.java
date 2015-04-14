package networkSimulation;

import repast.simphony.context.Context;
import repast.simphony.context.DefaultContext;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.graph.Network;

import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.space.grid.WrapAroundBorders;


public class NetworkContextBuilder extends DefaultContext<Object> implements ContextBuilder<Object> {

	public Context<Object> build(Context<Object> context) {
		context.setId("NetworkSimulation");
				
		NetworkBuilder<Object> knownConnectionsNetworkBuilder = new NetworkBuilder<Object>("knownConnections", context, false);
		Network<Object> knownConnections = knownConnectionsNetworkBuilder.buildNetwork();
		
		NetworkBuilder<Object> currentConnectionsNetworkBuilder = new NetworkBuilder<Object>("currentConnections", context, false);
		Network<Object> currentConnections = currentConnectionsNetworkBuilder.buildNetwork();
		
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
