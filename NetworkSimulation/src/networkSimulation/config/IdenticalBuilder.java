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

public class IdenticalBuilder extends ContextBuilderBuilder {

    @Override
    public Context<FileSharingNode> build(Context<FileSharingNode> context, Parameters params, Network<FileSharingNode> knownConnections, Network<FileSharingNode> currentConnections, ContinuousSpace<FileSharingNode> space) {

        // Parse parameters
        int nodeCount = params.getInteger("node_count");
        int initialActive = params.getInteger("initialActive");
        int fileCount = params.getInteger("file_count");
        int startingFiles = params.getInteger("startingFiles");
        int connectionLimit = params.getInteger("connectionLimit");
        int timeout = params.getInteger("timeout");
        int pingThreshold = params.getInteger("pingThreshold");
        int requestDuplicationLimit = params.getInteger("requestDuplication");
        double churnRate = params.getDouble("churnRate");
        double requestRate = params.getDouble("requestRate");
        double alphaLearningRate = params.getDouble("alphaRate");
        double pingWeightingScalar = params.getDouble("pingWeightingScalar");
        boolean nodesLearnUltimateFileOwner = params.getBoolean("nodesLearnFileOwner");
        boolean nodesLearnPathToFileOwner = params.getBoolean("nodesLearnNextInChain");

        // Initialize nodes
        ArrayList<FileSharingNode> nodes = new ArrayList<FileSharingNode>();
        int initialDead = nodeCount - initialActive;
        int deadModulo = nodeCount / initialDead;

        for (int ip = 0; ip < nodeCount; ip++) {
            boolean isDead = ip % deadModulo == 0;

            // Generic node configuration
            NodeConfiguration configuration = new NodeConfiguration();
            configuration.NodeIp = ip;
            configuration.NodeState = isDead ? NodeState.DEAD : NodeState.ALIVE;
            configuration.NodeType = NodeType.GENERIC;
            configuration.SimultaneousConnectionLimit = connectionLimit;
            configuration.ChurnDistribution = new UniformDistribution(0, 1, 1 - churnRate);
            configuration.RequestDistribution = new UniformDistribution(0, 1, 1 - requestRate);
            configuration.StartingFiles = new ArrayList<Integer>();
            configuration.PingThreshold = pingThreshold;
            configuration.AlphaLearningRate = alphaLearningRate;
            configuration.RequestDuplicationLimit = requestDuplicationLimit;
            configuration.PingWeightingScalar = pingWeightingScalar;

            // Allocate files
            for (int i = 0; i < startingFiles; i++) {
                int fileNum = randInt(0, fileCount - 1);
                if (!configuration.StartingFiles.contains(fileNum)) {
                    configuration.StartingFiles.add(fileNum);
                }
            }

            FileSharingNode node = new FileSharingNode(knownConnections, currentConnections, configuration);
            nodes.add(node);
        }

        // Set and return context
        context.addAll(nodes);

        // Place nodes in a circle in space (after adding to context)
        double spaceSize = space.getDimensions().getHeight();
        double center = spaceSize / 2;
        double radius = center - 2;
        for (int i = 0; i < nodeCount; i++) {
            double theta = 2 * Math.PI * i / nodeCount;
            double x = center + radius * Math.cos(theta);
            double y = center + radius * Math.sin(theta);
            space.moveTo(nodes.get(i), x, y);
        }

        // Set shared global state
        GlobalContext.NodeCount = nodeCount;
        GlobalContext.FileCount = fileCount;
        GlobalContext.Timeout = timeout;
        GlobalContext.IpLookup = nodes;
        GlobalContext.NodesLearnUltimateFileOwner = nodesLearnUltimateFileOwner;
        GlobalContext.NodesLearnPathToFileOwner = nodesLearnPathToFileOwner;

        // Debug information
        System.out.println("Created " + nodeCount + " nodes.");
        System.out.println("Created " + fileCount + " files.");

        return context;
    }

}
