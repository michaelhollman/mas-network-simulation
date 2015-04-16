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

/**
 * The FileSharingNode is the only agent type in our multi-agent system. Depending on the experiement setting, it may be configured the same or very different from other nodes in the network.
 * 
 * FileSharingNodes value getting the files they request quickly, and gaining information about other nodes in the network. They use this knowledge to optimize their decisions made while processing
 * requests.
 * 
 * When a FileSharingNode is created, it knows nothing about the other nodes in the environment (other than the max IP address). When it starts desiring files, it needs to ping into the files sharing
 * network first to find a node that can serve its request.
 * 
 * In our experiments, we broadly consider generic nodes (where they're configured roughly the same), and ultra peer and leaf nodes (which have very different configurations). These nodes are
 * configured differently, and lead to very different network behavior.
 */
public class FileSharingNode {

    /**
     * The global network modeling known connections. We keep this up to date with our weighted knowledge of other nodes in the system. (better nodes have a highger weight)
     */
    private Network<FileSharingNode> knownConnections;

    /**
     * The global network modeling current connections. We keep this up to date with request originating from this instance, and clear them when the request is complete. This allows us to visualize
     * how requests propagate through the network
     */
    private Network<FileSharingNode> currentConnections;

    /** The configuration this node was given at creation. */
    public NodeConfiguration config;

    /**
     * The first element of this nodes base knowledge, a map of FileNumber to a List of Nodes who are known to have that file
     */
    private ConcurrentHashMap<Integer, ArrayList<Integer>> knownFileOwners;

    // <NodeIp>, <WeightedReponseTime ,NumberOfTimesChosen>
    /**
     * The second element of this node's base knowledge, a map of Node IP Address to their Weighted Time to Query Hit and Number of Times Chosen as the destination
     */
    private ConcurrentHashMap<Integer, Tuple<Double, Integer>> nodeMap;

    /** The files this node knows about */
    public Vector<Integer> knownFiles;

    /** The current work queue. */
    public Vector<AbstractRequest> workQueue;

    // Fields used as node metrics

    /** The number of timeouts. */
    private int numTimeouts;

    /** The number of unfulfilled requests. */
    private int numUnfulfilled;

    /** The number of fulfilled requests. */
    private int numFulfilled;

    /** The average fulfillment time. */
    private double avgFulfillmentTime;

    /**
     * Instantiates a new file sharing node.
     * 
     * @param knownConnections
     *            the known connections
     * @param currentConnections
     *            the current connections
     * @param configuration
     *            the node configuration
     */
    public FileSharingNode(Network<FileSharingNode> knownConnections, Network<FileSharingNode> currentConnections, NodeConfiguration configuration) {
        this.knownConnections = knownConnections;
        this.currentConnections = currentConnections;
        config = configuration;
        knownFileOwners = new ConcurrentHashMap<Integer, ArrayList<Integer>>();
        workQueue = new Vector<AbstractRequest>();
        knownFiles = new Vector<Integer>(config.StartingFiles);
        nodeMap = new ConcurrentHashMap<Integer, Tuple<Double, Integer>>();
    }

    /**
     * Method that runs on each Repast tick and performs the basic logic for each node.
     * 
     * Each tick, a file sharing node follows the following basic logic: - If we should churn, change our state from Dead to alive (or vice versa) - Remove any finished or timedout work from our
     * current work queue - If we're dead, finish - If we're alive, see if we should request a file this tick - If we do want to request a file, initiate a request and add it to our queue - Process as
     * many items in our work queue as we can, given our connection limit
     */
    @ScheduledMethod(start = 1, interval = 1)
    public void tick() {

        // Randomly switch from dead to alive, with a probability configured via a parameter
        if (config.ChurnDistribution.getDecision()) {
            config.NodeState = config.NodeState == NodeState.DEAD ? NodeState.ALIVE : NodeState.DEAD;

            // If we're dying, we need to wipe our knowledge
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

        // Dead nodes can't do anything more
        if (config.NodeState == NodeState.DEAD)
            return;

        // create and add new requests to the work queue (governed by a probability)
        if ((workQueue.size() < config.SimultaneousConnectionLimit || GlobalContext.AllowSelfOverScheduling) && config.RequestDistribution.getDecision()) {
            Integer newFile = RandomUtil.getRandom(0, GlobalContext.FileCount - 1, knownFiles);
            if (newFile != null) {
                QueryRequest newQuery = new QueryRequest(this, newFile);
                workQueue.add(newQuery);
            }
        }

        // Process as many requests as we can given our connection limit
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

    /**
     * Receives a ping from another node. The ping request is added to our work queue, and processed as soon as we have time
     * 
     * @param ping
     *            the ping request
     */
    public void ping(PingRequest ping) {
        if (ping.targetIP != config.NodeIp) {
            throw new RuntimeException("Ping sent to wrong node. Target=" + ping.targetIP + " Receiver=" + config.NodeIp);
        }
        workQueue.add(ping);
        ping.addEdge(currentConnections.addEdge(ping.sourceNode, this, AbstractRequest.CONNECTION_PING));
    }

    /**
     * Receives a query from another node for a file. The ping request is added to our work queue, and processed as soon as we have time
     * 
     * @param query
     *            the file query
     */
    public void query(QueryRequest query) {
        workQueue.add(query);
        FileSharingNode lastNode = query.nodes.peek();
        query.addEdge(currentConnections.addEdge(lastNode, this, AbstractRequest.CONNECTION_QUERY));
        query.addIntermediate(this);
    }

    /**
     * Receives a file from a request. This is called by the request on this node when a QueryRequest is fulfilled. We update our known files and metrics about fulfillment
     * 
     * @param fileNumber
     *            the file number found
     * @param fulfillmentTime
     *            the ticks to fulfillment
     */
    public void giveFile(int fileNumber, double fulfillmentTime) {
        if (!knownFiles.contains(fileNumber)) {
            knownFiles.add(fileNumber);
        }

        // Update average and number of fulfilled
        avgFulfillmentTime = (numFulfilled * avgFulfillmentTime + 1 * fulfillmentTime) / (1 + numFulfilled);
        numFulfilled++;
    }

    /**
     * Receives information about a file from a request. This is called by the request on us when a QueryRequest is fulfilled. We use it to update our internal knowledge about who has files.
     * 
     * @param fileNumber
     *            the file number found
     * @param fileOwnerIp
     *            the IP address of the node capable of getting this file
     */
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

    /**
     * Receives information about a file request from a request. This is called by the request on us when a QueryRequest is fulfilled. We use it to update our internal knowledge about who is able to
     * respond to QueryRequests quickly.
     * 
     * @param destinationIp
     *            the destination ip
     * @param ticks
     *            the forward counted ticks (number of ticks it took from this node forward)
     * @param didTimeout
     *            if the request timed out
     */
    public void giveResponseTimeInfo(int destinationIp, double ticks, boolean didTimeout) {
        if (destinationIp == config.NodeIp)
            return;
        addWeightToKnownConnection(destinationIp, ticks, didTimeout);
    }

    /**
     * Called on us when a request we initiated went unfulfilled. For now, this can only happen when the request times out.
     * 
     * @param timeoutTime
     *            the timeout time
     */
    public void markUnfulfilled(double timeoutTime) {
        numUnfulfilled++;
        numTimeouts++;
    }

    // // PUBLIC Data Collection Properties

    /**
     * Gets the number of timeouts.
     * 
     * @return the number of timeouts
     */
    public int getNumTimeouts() {
        return numTimeouts;
    }

    /**
     * Gets the number of unfulfilled requests.
     * 
     * @return the number of unfulfilled requests
     */
    public int getNumUnfulfilled() {
        return numUnfulfilled;
    }

    /**
     * Gets the number of fulfilled requests.
     * 
     * @return the number of fulfilled requests
     */
    public int getNumFulfilled() {
        return numFulfilled;
    }

    /**
     * Gets the percent of requests that have been fulfilled.
     * 
     * @return the percent of fulfilled requests
     */
    public double getPercentFulfilled() {
        int denom = (numFulfilled + numUnfulfilled);
        if (denom == 0)
            return 0;
        return (1.0 * numFulfilled) / denom;
    }

    /**
     * Gets the average fulfillment time.
     * 
     * @return the avgerage fulfillment time
     */
    public double getAvgFulfillmentTime() {
        return avgFulfillmentTime;
    }

    /**
     * Gets the work queue size.
     * 
     * @return the work queue size
     */
    public int getWorkQueueSize() {
        return workQueue.size();
    }

    /**
     * Gets the number of known files.
     * 
     * @return the number of known files
     */
    public int getNumKnownFiles() {
        return knownFiles.size();
    }

    /**
     * Gets the number of known nodes.
     * 
     * @return the number known nodes
     */
    public int getNumKnownNodes() {
        return nodeMap.size();
    }

    /**
     * Checks if this is an ultra node.
     * 
     * @return true, if this is an ultra node
     */
    public boolean isUltraNode() {
        return config.NodeType == NodeType.ULTRA_PEER;
    }

    /**
     * Checks if this node is dead.
     * 
     * @return true, if we are dead
     */
    public boolean isDead() {
        return config.NodeState == NodeState.DEAD;
    }

    // PRIVATE ---------------------------------------

    /**
     * Checks if this node has a particular file
     * 
     * @param fileNumber
     *            the file number to check
     * @return true, if we have the file indicated by fileNumber
     */
    private boolean hasFile(int fileNumber) {
        return knownFiles.contains(fileNumber);
    }

    /**
     * Processes a request. Applies different logic depending on the type of request we're processing (Ping or Query)
     * 
     * @param req
     *            the request to process
     * @return true, if the request has been finished and should be removed
     */
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

    /**
     * Processes a ping request. We simply fulfill the request immediately to indicate that we are alive in the network
     * 
     * @param ping
     *            the ping
     * @return true, if the request has been finished and should be removed
     */
    private boolean processPing(PingRequest ping) {
        if (ping.sourceNode == this && !ping.fulfilled) {
            // wait.
            return false;
        } else if (config.NodeIp == ping.targetIP && !ping.fulfilled) {
            // No logic necessary; simply fulfill the request and return true
            ping.fulfill(this);
            return true;
        }

        throw new RuntimeException("processPing didn't know what to do!");
    }

    /**
     * Processes a query for a file.
     * 
     * @param query
     *            the file query
     * @return true, if the request has been finished and should be removed
     */
    private boolean processQuery(QueryRequest query) {
        if (query.fulfilled) {
            // Query has already been fulfilled; ok to remove from queue
            return true;
        } else if (hasFile(query.fileNumber)) {
            // We have this file; fulfill directly
            query.fulfill(this);
            return true;
        } else if (query.nodes.peek() == this) {
            // We're the last node on the query stack. Need to pass it along to another node

            // Use our internal heuristics to find a node to pass the query on to. Update our knowledge
            // indicating that we took this path
            HashMap<QueryRequest, Integer> queriesAndDestinations = new HashMap<>();
            Integer nextNode = getIpToPassQueryTo(query, null);
            if (nextNode != null) {
                queriesAndDestinations.put(query, nextNode);
            }

            // create and add new requests to the work queue (governed by a probability)
            int desiredDupCount = RandomHelper.nextIntFromTo(0, config.RequestDuplicationLimit);
            int numberAdded = 0;
            while (numberAdded < desiredDupCount && workQueue.size() < config.SimultaneousConnectionLimit) {
                numberAdded++;

                // Clone the query, and get a node to pass the request on to
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

            startPingingThingsIfNeeded();

            return false;
        } else if (query.nodes.contains(this)) {
            return false;
        }

        throw new RuntimeException("processRequest didn't know what to do!");
    }

    /**
     * Gets the ip to pass query to, using our heuristics
     * 
     * @param query
     *            the query
     * @param ignoreIps
     *            the ips to ignore
     * @return the ip to pass query to
     */
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

    /**
     * Adds the weight to known connection map
     * 
     * @param ip
     *            the ip
     * @param weight
     *            the weight
     * @param timeout
     *            the timeout
     */
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

    /**
     * Increment node choice count.
     * 
     * @param ip
     *            the ip
     */
    private void incrementNodeChoiceCount(int ip) {
        Tuple<Double, Integer> nodeInfo = nodeMap.get(ip);
        if (nodeInfo == null) {
            nodeInfo = new Tuple<Double, Integer>(null, 1);
            nodeMap.put(ip, nodeInfo);
        } else {
            nodeInfo.y++;
        }
    }

    /**
     * Start pinging things once we've requested files
     */
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