package networkSimulation.config;

import java.util.ArrayList;

import networkSimulation.FileSharingNode;
import repast.simphony.context.Context;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.graph.Network;

public interface ContextBuilderBuilder {
	void build(Context<FileSharingNode> context, Parameters params, Network<FileSharingNode> knownConnections, Network<FileSharingNode> currentConnections, ArrayList<FileSharingNode> allNodes);
}
