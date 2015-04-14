package networkSimulation.config;

import java.util.ArrayList;

import networkSimulation.FileSharingNode;
import networkSimulation.NodeConfiguration;
import repast.simphony.context.Context;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.graph.Network;

public class TestBuilder implements ContextBuilderBuilder {

	@Override
	public void build(Context<FileSharingNode> context, Parameters params,
			Network<FileSharingNode> knownConnections,
			Network<FileSharingNode> currentConnections,
			ArrayList<FileSharingNode> allNodes) {
		// TODO Auto-generated method stub

		int nodeCount = params.getInteger("node_count");
		
		FileSharingNode last = null;
		for (int i = 0; i < nodeCount; i++) {
			NodeConfiguration config = new NodeConfiguration();
			config.NodeId = i;

			FileSharingNode n = new FileSharingNode(knownConnections,
					currentConnections, config);
			if (last != null)
				knownConnections.addEdge(last, n);

			last = n;
			context.add(n);
		}

	}
}
