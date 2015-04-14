package networkSimulation;

import java.util.ArrayList;

import networkSimulation.config.ContextBuilderBuilder;
import networkSimulation.config.ContextBuilderBuilderFactory;
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
		
		ArrayList<FileSharingNode> allNodes = new ArrayList<FileSharingNode>();
		
		Parameters params = RunEnvironment.getInstance().getParameters();
		String experiment = params.getString("experiment");
		
		ContextBuilderBuilder builder = ContextBuilderBuilderFactory.getBuilder(experiment);
		builder.build(context, params, knownConnections, currentConnections, allNodes);
				
		return context;
	}
}
