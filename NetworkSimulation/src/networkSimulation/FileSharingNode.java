package networkSimulation;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;

public class FileSharingNode {

    private Network<FileSharingNode> knownConnections;
    private Network<FileSharingNode> currentConnections;

    private ConcurrentHashMap<Integer, ArrayList<Integer>> knownFileOwners;
    // <NodeIp>, <WeightedReponseTime ,NumberOfTimesChosen>
    private ConcurrentHashMap<Integer, Tuple<Double, Integer>> nodeMap;
    
    // Metrics
    private int numTimeouts;
    private int numUnfulfilled;
    private int numFulfilled;
    private double avgFulfillmentTime;

    public NodeConfiguration config;
    public Vector<Integer> knownFiles;
    public Vector<AbstractRequest> workQueue;

    public FileSharingNode(Network<FileSharingNode> knownConnections, Network<FileSharingNode> currentConnections, NodeConfiguration configuration) {
        this.knownConnections = knownConnections;
        this.currentConnections = currentConnections;
        config = configuration;
        knownFileOwners = new ConcurrentHashMap<Integer, ArrayList<Integer>>();
        workQueue = new Vector<AbstractRequest>();
        knownFiles = new Vector<Integer>(config.StartingFiles);
        nodeMap = new ConcurrentHashMap<Integer, Tuple<Double, Integer>>();
    }

    @ScheduledMethod(start = 1, interval = 1)
    public void tick() {

        if (config.ChurnDistribution.getDecision()) {
            config.NodeState = config.NodeState == NodeState.DEAD ? NodeState.ALIVE : NodeState.DEAD;

            if (config.NodeState == NodeState.DEAD) {
                knownFileOwners = new ConcurrentHashMap<Integer, ArrayList<Integer>>();
                knownFiles = new Vector<Integer>(config.StartingFiles);
                nodeMap = new ConcurrentHashMap<Integer, Tuple<Double, Integer>>();
            }
        }

        // handle race condition: remove any already fulfilled requests
        // remove timed out ones too
        LinkedList<AbstractRequest> removes = new LinkedList<AbstractRequest>();
        for (AbstractRequest req : workQueue) {
            if (req.fulfilled || req.checkTimeOut()) {
                removes.add(req);
                if (!(req instanceof QueryRequest))
                    continue;
                // yes, I know this is disgusting
                for (AbstractRequest innerreq : workQueue) {
                    if (!(innerreq instanceof QueryRequest))
                        continue;
                    if (!innerreq.equals(req) && ((QueryRequest) innerreq).isEquivalentTo((QueryRequest) req)) {
                        removes.add(req);
                    }
                }
            }
        }
        for (AbstractRequest req : removes) {
            workQueue.remove(req);
            req.removeFromNetwork(currentConnections);
        }

        if (config.NodeState == NodeState.DEAD)
            return;

        // create and add new requests to the work queue
        if (workQueue.size() < config.SimultaneousConnectionLimit && config.RequestDistribution.getDecision()) {
            Integer newFile = RandomUtil.getRandom(0, GlobalContext.FileCount - 1, knownFiles);
            if (newFile != null) {
                QueryRequest newQuery = new QueryRequest(this, newFile);
                workQueue.add(newQuery);
            }
        }

        // process requests
        LinkedList<AbstractRequest> finishedRequests = new LinkedList<AbstractRequest>();
        int numberOfRequestsToProcess = Math.min(config.SimultaneousConnectionLimit, workQueue.size());
        for (int i = 0; i < numberOfRequestsToProcess; i++) {
            AbstractRequest req = workQueue.get(i);
            if (processRequest(req)) {
                finishedRequests.add(req);
            }
        }
        for (AbstractRequest finreq : finishedRequests) {
            workQueue.remove(finreq);
        }
    }

    public void ping(PingRequest ping) {
        if (ping.targetIP != config.NodeIp) {
            throw new RuntimeException("Ping sent to wrong node. Target=" + ping.targetIP + " Receiver=" + config.NodeIp);
        }
        workQueue.add(ping);
        ping.addEdge(currentConnections.addEdge(ping.sourceNode, this, AbstractRequest.CONNECTION_PING));
    }

    public void query(QueryRequest query) {
        workQueue.add(query);
        FileSharingNode lastNode = query.nodes.peek();
        query.addEdge(currentConnections.addEdge(lastNode, this, AbstractRequest.CONNECTION_QUERY));
        query.addIntermediate(this);
    }

    public void giveFile(int fileNumber, double fulfillmentTime) {
        if (!knownFiles.contains(fileNumber)) {
            knownFiles.add(fileNumber);
        }
        
        // Update average and number of fulfilled
        avgFulfillmentTime = (numFulfilled * avgFulfillmentTime + 1 * fulfillmentTime) / (1 + numFulfilled); 
        numFulfilled++;
    }

    public void giveFileInfo(int fileNumber, int fileOwnerIp) {
        if (fileOwnerIp == config.NodeIp)
            return;

        if (!knownFileOwners.containsKey(fileNumber)) {
            knownFileOwners.put(fileNumber, new ArrayList<Integer>());
        }

        ArrayList<Integer> owners = knownFileOwners.get(fileNumber);
        if (!owners.contains(fileOwnerIp)) {
            owners.add(fileOwnerIp);
        }

        knownFileOwners.put(fileNumber, owners);
    }

    public void giveResponseTimeInfo(int destinationIp, double ticks, boolean didTimeout) {
        if (destinationIp == config.NodeIp)
            return;
        addWeightToKnownConnection(destinationIp, ticks, didTimeout);
    }
    

	public void markUnfulfilled(double timeoutTime) {
		numUnfulfilled++;
		numTimeouts++;		
	}
    
    // // PUBLIC Data Collection Properties
    
    public int getNumTimeouts() {
    	return numTimeouts;
    }
    
    public int getNumUnfulfilled() {
    	return numUnfulfilled;
    }
    
    public int getNumFulfilled() {
    	return numFulfilled;
    }
    
    public double getPercentFulfilled() {
    	int denom = (numFulfilled + numUnfulfilled);
    	if (denom == 0) return 0;    	
    	return (1.0 * numFulfilled) / denom;
    }
    
    public double getAvgFulfillmentTime() {
    	return avgFulfillmentTime;
    }
    
    public int getWorkQueueSize() {
    	return workQueue.size();
    }
    
    public int getNumKnownFiles() {
    	return knownFiles.size();
    }
    
    public int getNumKnownNodes() {
    	return nodeMap.size();
    }
    
    public boolean isUltraNode() {
    	return config.NodeType == NodeType.ULTRA_PEER;
    }
    
    public boolean isDead() {
    	return config.NodeState == NodeState.DEAD;
    }
    

    // PRIVATE ---------------------------------------

    private boolean hasFile(int fileNumber) {
        return knownFiles.contains(fileNumber);
    }

    // returns true if request has been finished and should be removed
    private boolean processRequest(AbstractRequest req) {
        // handle race condition, force at least 1 tick wait
        if (req.sourceNode != this && req.needsToWaitOneTick()) {
            return false;
        }

        if (req instanceof PingRequest) {
            return processPing((PingRequest) req);
        }
        if (req instanceof QueryRequest) {
            return processQuery((QueryRequest) req);
        }

        throw new RuntimeException("processRequest didn't know what to do!");
    }

    private boolean processPing(PingRequest ping) {
        if (ping.sourceNode == this && !ping.fulfilled) {
            // wait
            return false;
        } else if (config.NodeIp == ping.targetIP && !ping.fulfilled) {
            ping.fulfill(this);
            return true;
        }

        throw new RuntimeException("processPing didn't know what to do!");
    }

    private boolean processQuery(QueryRequest query) {
        if (query.fulfilled) {
            return true;
        } else if (hasFile(query.fileNumber)) {
            query.fulfill(this);
            return true;
        } else if (query.nodes.peek() == this) {
            HashMap<QueryRequest, Integer> queriesAndDestinations = new HashMap<>();
            Integer nextNode = getIpToPassQueryTo(query, null);
            if (nextNode != null) {
                queriesAndDestinations.put(query, nextNode);
            }

            int desiredDupCount = RandomHelper.nextIntFromTo(0, config.RequestDuplicationLimit);
            int numberAdded = 0;
            while (numberAdded < desiredDupCount && workQueue.size() < config.SimultaneousConnectionLimit) {
                numberAdded++;
                QueryRequest newQuery = (QueryRequest) query.clone();
                ArrayList<Integer> ignores = new ArrayList<>(queriesAndDestinations.values());
                nextNode = getIpToPassQueryTo(newQuery, ignores);
                if (nextNode != null) {
                    workQueue.add(newQuery);
                    queriesAndDestinations.put(newQuery, nextNode);
                } else {
                    break;
                }
            }

            for (Entry<QueryRequest, Integer> queryAndDestination : queriesAndDestinations.entrySet()) {
                QueryRequest req = queryAndDestination.getKey();
                int ip = queryAndDestination.getValue();

                FileSharingNode node = GlobalContext.IpLookup.get(ip);
                node.query(req);
                incrementNodeChoiceCount(ip);
            }

            if (nextNode == null || nodeMap == null || nodeMap.size() == 0) {
                startPingingThingsIfNeeded();
            }

            return false;
        } else if (query.nodes.contains(this)) {
            return false;
        }

        throw new RuntimeException("processRequest didn't know what to do!");
    }

    private Integer getIpToPassQueryTo(QueryRequest query, AbstractList<Integer> ignoreIps) {
        Integer fileNum = query.fileNumber;
        ArrayList<Integer> knownOwners = knownFileOwners.get(fileNum);

        // prevent cycles
        if (ignoreIps == null) {
            ignoreIps = new ArrayList<>();
        }
        for (FileSharingNode node : query.nodes) {
            ignoreIps.add(node.config.NodeIp);
        }

        Integer nextNodeIp = null;

        if (knownOwners != null && knownOwners.size() > 0) {
            // we have known owners. pick the fastest one.
            Double minWeight = Double.MAX_VALUE;
            Integer minWeightIp = null;
            for (int knownOwner : knownOwners) {
                if (ignoreIps.contains(knownOwner) || nodeMap == null || !nodeMap.containsKey(knownOwner))
                    continue;
                Tuple<Double, Integer> nodeInfo = nodeMap.get(knownOwner);
                if (nodeInfo == null)
                    continue;
                Double weight = nodeInfo.x;
                if (weight == null)
                    continue;
                if (weight < minWeight) {
                    minWeight = weight;
                    minWeightIp = knownOwner;
                }
            }

            if (minWeightIp == null) {
                // for some reason, we don't have any weights for known owners
                // pick a random owner
                ArrayList<Integer> tempList = new ArrayList<>(knownOwners);
                tempList.removeAll(ignoreIps);
                if (tempList.size() != 0) {
                    nextNodeIp = tempList.get(RandomHelper.nextIntFromTo(0, tempList.size() - 1));
                }
            } else {
                nextNodeIp = minWeightIp;
            }
        } else if (nodeMap != null && nodeMap.size() > 0) {
            // we have no known owners, so pick the fastest known node;
            Double minWeight = Double.MAX_VALUE;
            Integer minWeightIp = null;
            for (Entry<Integer, Tuple<Double, Integer>> entry : nodeMap.entrySet()) {
                Integer ip = entry.getKey();
                Tuple<Double, Integer> nodeInfo = entry.getValue();
                Double weight = nodeInfo.x;

                if (ip == null || weight == null || ignoreIps.contains(ip))
                    continue;

                if (weight < minWeight) {
                    minWeight = weight;
                    minWeightIp = ip;
                }
            }

            if (minWeightIp == null) {
                // for some reason, we don't have any weights for known nodes
                // pick a random one we know about
                ArrayList<Integer> tempList = new ArrayList<>(nodeMap.keySet());
                tempList.removeAll(ignoreIps);
                if (tempList.size() > 0) {
                    nextNodeIp = tempList.get(RandomHelper.nextIntFromTo(0, tempList.size() - 1));
                }
            } else {
                nextNodeIp = minWeightIp;
            }
        }
        // else, we know nothing. sit and wait while pings finish

        return nextNodeIp;
    }

    private void addWeightToKnownConnection(int ip, double weight, boolean timeout) {
        Tuple<Double, Integer> nodeInfo = nodeMap.get(ip);
        if (nodeInfo == null) {
            nodeInfo = new Tuple<Double, Integer>(weight, 0);
        } else if (nodeInfo.x == null) {
            nodeInfo.x = weight;
        } else {
            double alpha = config.AlphaLearningRate;
            if (timeout) {
                alpha = alpha * (weight / (double) GlobalContext.Timeout);
            }
            nodeInfo.x = (1 - alpha) * nodeInfo.x + alpha * weight;
        }
        nodeMap.put(ip, nodeInfo);

        FileSharingNode dest = GlobalContext.IpLookup.get(ip);
        RepastEdge<FileSharingNode> edge = knownConnections.getEdge(this, dest);
        if (edge != null) {
            knownConnections.removeEdge(edge);
            knownConnections.addEdge(this, dest, nodeInfo.x);
        } else {
            knownConnections.addEdge(this, dest, nodeInfo.x);
        }
    }

    private void incrementNodeChoiceCount(int ip) {
        Tuple<Double, Integer> nodeInfo = nodeMap.get(ip);
        if (nodeInfo == null) {
            nodeInfo = new Tuple<Double, Integer>(null, 1);
            nodeMap.put(ip, nodeInfo);
        } else {
            nodeInfo.y++;
        }
    }

    private void startPingingThingsIfNeeded() {
        boolean shouldPing = nodeMap == null || nodeMap.size() < config.PingThreshold;
        if (shouldPing && workQueue.size() < config.SimultaneousConnectionLimit) {
            Vector<Integer> excludes = new Vector<Integer>();
            excludes.add(config.NodeIp);
            int pingCount = 0;
            while (pingCount < config.PingThreshold && workQueue.size() < config.SimultaneousConnectionLimit) {
                pingCount++;
                Integer targetNode = RandomUtil.getRandom(0, GlobalContext.NodeCount - 1, excludes);
                if (targetNode != null) {
                    PingRequest ping = new PingRequest(this, targetNode);
                    workQueue.add(ping);
                    ping.send();
                    excludes.add(targetNode);
                } else {
                    break;
                }
            }
        }
    }


}