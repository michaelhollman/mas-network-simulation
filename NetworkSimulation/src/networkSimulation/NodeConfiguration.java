package networkSimulation;

import java.util.ArrayList;

public class NodeConfiguration {
    public int NodeIp;
    public NodeState NodeState;
    public NodeType NodeType;

    public ArrayList<Integer> StartingFiles;
    public int SimultaneousConnectionLimit;
    public Distribution RequestDistribution;
    public Distribution ChurnDistribution;
    public int PingThreshold;
    public double AlphaLearningRate;
    public int RequestDuplicationLimit;
    public double PingWeightingScalar;
}
