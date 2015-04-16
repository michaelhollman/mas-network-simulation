package networkSimulation;

import networkSimulation.config.ContextBuilderBuilder;
import networkSimulation.config.ContextBuilderBuilderFactory;
import repast.simphony.context.Context;
import repast.simphony.context.DefaultContext;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.continuous.WrapAroundBorders;
import repast.simphony.space.graph.Network;

/**
 * Our custom Repast ContextBuilder, which builds up the context with our projections and fills
 * it with agents.  Parameters are parsed from the user, and then we use a ContextBuilderBuilder
 * to apply experiment-specific configuration.
 */
public class NetworkContextBuilder extends DefaultContext<FileSharingNode> implements ContextBuilder<FileSharingNode> {
	
	/* (non-Javadoc)
	 * @see repast.simphony.dataLoader.ContextBuilder#build(repast.simphony.context.Context)
	 */
	public Context<FileSharingNode> build(Context<FileSharingNode> context) {
		context.setId("NetworkSimulation");
		
		// Create our projections
		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);
		ContinuousSpace<FileSharingNode> space = spaceFactory.createContinuousSpace("space", context, 
				new RandomCartesianAdder<FileSharingNode>(), new WrapAroundBorders(), 100, 100);
		
		// Models the knowledge each node has about its peers
		NetworkBuilder<FileSharingNode> knownConnectionsNetworkBuilder = new NetworkBuilder<FileSharingNode>("knownConnections", context, true);
		Network<FileSharingNode> knownConnections = knownConnectionsNetworkBuilder.buildNetwork();
		
		// Models the currently active connections in the network
		NetworkBuilder<FileSharingNode> currentConnectionsNetworkBuilder = new NetworkBuilder<FileSharingNode>("currentConnections", context, true);
		Network<FileSharingNode> currentConnections = currentConnectionsNetworkBuilder.buildNetwork();
		
		Parameters params = RunEnvironment.getInstance().getParameters();
		String experimentSetting = params.getString("experimentSetting");
		
		// Apply experiment specific configuration
		ContextBuilderBuilder builder = ContextBuilderBuilderFactory.getBuilder(experimentSetting);
		context = builder.build(context, params, knownConnections, currentConnections, space);
				
		return context;
	}
}
