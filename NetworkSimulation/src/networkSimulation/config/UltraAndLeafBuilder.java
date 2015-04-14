package networkSimulation.config;

import java.util.ArrayList;

import networkSimulation.FileSharingNode;
import repast.simphony.context.Context;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.graph.Network;

public class UltraAndLeafBuilder extends ContextBuilderBuilder {

	@Override
	public Context<FileSharingNode> build(Context<FileSharingNode> context, Parameters params,
			Network<FileSharingNode> knownConnections,
			Network<FileSharingNode> currentConnections,
			ArrayList<FileSharingNode> allNodes) {
		// TODO Auto-generated method stub
		return context;
	}

}
