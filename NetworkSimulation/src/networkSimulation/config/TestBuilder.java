package networkSimulation.config;

import java.util.ArrayList;

import networkSimulation.FileSharingNode;
import networkSimulation.GlobalContext;
import networkSimulation.NodeConfiguration;
import networkSimulation.NodeState;
import networkSimulation.NodeType;
import repast.simphony.context.Context;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.graph.Network;

/**
 * A simple ContextBuilderBuilder for testing purposes; uses some simple defaults.
 */
public class TestBuilder extends ContextBuilderBuilder {

    /*
     * (non-Javadoc)
     * 
     * @see networkSimulation.config.ContextBuilderBuilder#build(repast.simphony.context.Context, repast.simphony.parameter.Parameters, repast.simphony.space.graph.Network,
     * repast.simphony.space.graph.Network, repast.simphony.space.continuous.ContinuousSpace)
     */
    public Context<FileSharingNode> build(Context<FileSharingNode> context, Parameters params, Network<FileSharingNode> knownConnections, Network<FileSharingNode> currentConnections, ContinuousSpace<FileSharingNode> space) {

        // Parse parameters
        int nodeCount = params.getInteger("node_count");
        double percentInitialActive = params.getDouble("initialActivePercentage");
        int fileCount = params.getInteger("file_count");
        int genericConnectionLimit = params.getInteger("genericConnectionLimit");
        int timeout = params.getInteger("timeout");
        int pingThreshold = params.getInteger("pingThreshold");
        int requestDuplicationLimit = params.getInteger("requestDuplication");
        double churnRate = params.getDouble("churnRate");
        double requestRate = params.getDouble("requestRate");
        double alphaLearningRate = params.getDouble("alphaRate");
        double pingWeightingScalar = params.getDouble("pingWeightingScalar");
        boolean nodesLearnUltimateFileOwner = params.getBoolean("nodesLearnFileOwner");
        boolean nodesLearnPathToFileOwner = params.getBoolean("nodesLearnNextInChain");
        boolean allowSelfOverscheduling = params.getBoolean("allowSelfOverscheduling");

        // Initialize nodes
        ArrayList<FileSharingNode> nodes = new ArrayList<FileSharingNode>();
        int initialDead = (int) Math.floor(nodeCount * (1 - percentInitialActive));
        int deadModulo = initialDead != 0 ? nodeCount / initialDead : Integer.MAX_VALUE;
        int filesPerNode = fileCount / nodeCount;

        for (int ip = 0; ip < nodeCount; ip++) {
            boolean isDead = ip % deadModulo == 0;

            // Generic node configuration
            NodeConfiguration configuration = new NodeConfiguration();
            configuration.NodeIp = ip;
            configuration.NodeState = isDead ? NodeState.DEAD : NodeState.ALIVE;
            configuration.NodeType = NodeType.GENERIC;
            configuration.SimultaneousConnectionLimit = genericConnectionLimit;
            configuration.ChurnDistribution = new UniformDistribution(0, 1, 1 - churnRate);
            configuration.RequestDistribution = new UniformDistribution(0, 1, 1 - requestRate);
            configuration.StartingFiles = new ArrayList<Integer>();
            configuration.PingThreshold = pingThreshold;
            configuration.AlphaLearningRate = alphaLearningRate;
            configuration.RequestDuplicationLimit = requestDuplicationLimit;
            configuration.PingWeightingScalar = pingWeightingScalar;

            // Allocate files evenly
            int startingFile = ip * filesPerNode;
            for (int fileNum = startingFile; fileNum < startingFile + filesPerNode && fileNum < fileCount; fileNum++) {
                configuration.StartingFiles.add(fileNum);
            }

            FileSharingNode node = new FileSharingNode(knownConnections, currentConnections, configuration);
            nodes.add(node);
        }

        // Set shared global state
        GlobalContext.NodeCount = nodeCount;
        GlobalContext.FileCount = fileCount;
        GlobalContext.Timeout = timeout;
        GlobalContext.IpLookup = nodes;
        GlobalContext.NodesLearnUltimateFileOwner = nodesLearnUltimateFileOwner;
        GlobalContext.NodesLearnPathToFileOwner = nodesLearnPathToFileOwner;
        GlobalContext.AllowSelfOverScheduling = allowSelfOverscheduling;

        // Debug information
        System.out.println("Created " + nodeCount + " nodes.");
        System.out.println("Created " + fileCount + " files.");

        // Set and return context
        context.addAll(nodes);

        // Place nodes in a circle in space (after adding to context)
        int center = 50;
        int radius = 48;
        for (int i = 0; i < nodeCount; i++) {
            double theta = 2 * Math.PI * i / nodeCount;
            double x = center + radius * Math.cos(theta);
            double y = center + radius * Math.sin(theta);
            space.moveTo(nodes.get(i), x, y);
        }

        return context;
    }
}
