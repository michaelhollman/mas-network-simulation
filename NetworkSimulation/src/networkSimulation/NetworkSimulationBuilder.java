package networkSimulation;

import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.graph.Network;

public class NetworkSimulationBuilder implements ContextBuilder<Object> {

	@Override
	public Context<Object> build(Context<Object> context) {
		context.setId("NetworkSimulation");
		
		NetworkBuilder<Object> builder1 = new NetworkBuilder<Object>("active connections", context, true);
		Network<Object> activeConnections = builder1.buildNetwork();
		
		NetworkBuilder<Object> builder2 = new NetworkBuilder<Object>("known connections", context, true);
		Network<Object> knownConnections = builder2.buildNetwork();
		
		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);
		ContinuousSpace<Object> space = spaceFactory.createContinuousSpace("space", context, 
				new RandomCartesianAdder<Object>(), new repast.simphony.space.continuous.WrapAroundBorders(), 50, 50);
		
		Parameters params = RunEnvironment.getInstance().getParameters();
		int nodeCount = (Integer) params.getValue("node_count");
		
		for (int i = 0; i < nodeCount; i++) {
			// TODO: Pass in configuration
			NodeConfiguration config = new NodeConfiguration();
			context.add(new P2PFileSharingNode(activeConnections, knownConnections, space, config));
		}
		
		return context;
	}

}
