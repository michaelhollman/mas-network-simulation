package networkSimulation;

import java.util.ArrayList;

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


public class NetworkContextBuilder extends DefaultContext<FileSharingNode> implements ContextBuilder<FileSharingNode> {
	public Context<FileSharingNode> build(Context<FileSharingNode> context) {
		context.setId("NetworkSimulation");
		
		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);
		ContinuousSpace<FileSharingNode> space = spaceFactory.createContinuousSpace("space", context, 
				new RandomCartesianAdder<FileSharingNode>(), new WrapAroundBorders(), 100, 100);
						
		NetworkBuilder<FileSharingNode> knownConnectionsNetworkBuilder = new NetworkBuilder<FileSharingNode>("knownConnections", context, false);
		Network<FileSharingNode> knownConnections = knownConnectionsNetworkBuilder.buildNetwork();
		
		NetworkBuilder<FileSharingNode> currentConnectionsNetworkBuilder = new NetworkBuilder<FileSharingNode>("currentConnections", context, false);
		Network<FileSharingNode> currentConnections = currentConnectionsNetworkBuilder.buildNetwork();
		
		Parameters params = RunEnvironment.getInstance().getParameters();
		String experimentSetting = params.getString("experimentSetting");
		

		ContextBuilderBuilder builder = ContextBuilderBuilderFactory.getBuilder(experimentSetting);
		context = builder.build(context, params, knownConnections, currentConnections, space);
				
		return context;
	}
}
