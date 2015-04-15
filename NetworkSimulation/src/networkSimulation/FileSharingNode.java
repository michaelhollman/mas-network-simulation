package networkSimulation;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;

public class FileSharingNode {	
	
	private Network<FileSharingNode> knownConnections;
	private Network<FileSharingNode> currentConnections;
	
	private HashMap<Integer, ArrayList<Integer>> knownFileOwners;
	
	public NodeConfiguration config;
	public Vector<Integer> knownFiles;
	public Vector<AbstractRequest> workQueue;
	

	public FileSharingNode(Network<FileSharingNode> knownConnections, Network<FileSharingNode> currentConnections, NodeConfiguration configuration) {
		this.knownConnections = knownConnections;
		this.currentConnections = currentConnections;
		config = configuration;
		knownFileOwners = new HashMap<Integer, ArrayList<Integer>>(); 
		workQueue = new Vector<AbstractRequest>();
		knownFiles = new Vector<Integer>(config.StartingFiles);
	}

	
	@ScheduledMethod(start = 1, interval = 1)
	public void tick() {

		// handle race condition: remove any already fulfilled requests
		// remove timed out ones too
		LinkedList<AbstractRequest> removes = new LinkedList<AbstractRequest>();
		for (AbstractRequest req : workQueue) {	
			if (req.fulfilled || req.checkTimeOut()) {
				removes.add(req);
			}
		}
		for (AbstractRequest req : removes) {
			workQueue.remove(req);
			req.removeFromNetwork(currentConnections);
		}
		
		
		// create and add new requests to the work queue
		if (workQueue.size() < config.SimultaneousConnectionLimit && config.RequestDistribution.getDecision()) {
			Integer newFile = RandomUtil.getRandom(0, GlobalContext.FileCount - 1, knownFiles);
			if (newFile != null) {
				QueryRequest newQuery = new QueryRequest(this, newFile);
				workQueue.add(newQuery);
			}
		}
		
		if (workQueue.size() < config.SimultaneousConnectionLimit && config.RequestDistribution.getDecision()) {
			Vector<Integer> excludes = new Vector<Integer>();
			excludes.add(config.NodeIp);
			Integer targetNode = RandomUtil.getRandom(0, GlobalContext.NodeCount - 1, excludes);
			if (targetNode != null) {
				PingRequest ping = new PingRequest(this, targetNode);
				workQueue.add(ping);
				ping.send();
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
	
	public void giveFile(int fileNumber) {
		if (!knownFiles.contains(fileNumber)) {
			knownFiles.add(fileNumber);
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
		System.out.println("Querying from " + lastNode.config.NodeIp + " to " + config.NodeIp);
	}
	
	public void giveFileInfo(int fileNumber, int fileOwnerIp) {
		if (!knownFileOwners.containsKey(fileNumber)) {
			knownFileOwners.put(fileNumber, new ArrayList<Integer>());
		}
		
		ArrayList<Integer> owners = knownFileOwners.get(fileNumber);
		if (!owners.contains(fileOwnerIp)) {
			owners.add(fileOwnerIp);
		}
		
		knownFileOwners.put(fileNumber, owners);
	}
	
	public void giveResponseTimeInfo(int destinationIP, double ticks, boolean didTimeout) {
		addWeightToKnownConnection(destinationIP, ticks, didTimeout);
	}
	
	//// PRIVATE ---------------------------------------
	
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
			
			// TODO: search for suitable node(s) to send query to
			// TODO: send query to new nodes
			FileSharingNode nextNode = null;
			
			Integer fileNum = query.fileNumber;
			ArrayList<Integer> knownOwners = knownFileOwners.get(fileNum);
			
			ArrayList<Integer> ignoreNodes = new ArrayList<Integer>();
			// prevent cycles
			for (FileSharingNode node : query.nodes) {
				ignoreNodes.add(node.config.NodeIp);
			}
			
			Integer nextNodeIp;
			if (knownOwners == null) {
				nextNodeIp = RandomUtil.getRandom(0, GlobalContext.NodeCount - 1, ignoreNodes);
			} else {
				nextNodeIp = knownOwners.get(RandomUtil.getRandom(0, knownOwners.size() - 1, ignoreNodes)).intValue();
			}
	
			if (nextNodeIp == null) {
				return false;
			}
			
			nextNode = GlobalContext.IpLookup.get(nextNodeIp);
			
			if (nextNode != null) {
				nextNode.query(query);
			} else {
				throw new RuntimeException("processRequest didn't know what to do!");
			}
			
			return false;
		} else if (query.nodes.contains(this)) {
			return false;
		}
		
		throw new RuntimeException("processRequest didn't know what to do!");
	}
	
	private void addWeightToKnownConnection(int ip, double weight, boolean timeout) {
		FileSharingNode dest = GlobalContext.IpLookup.get(ip);
		RepastEdge<FileSharingNode> edge = knownConnections.getEdge(this, dest);
		if (edge != null) {
			weight = (edge.getWeight() + weight) / 2.0; 
			knownConnections.removeEdge(edge);
			
			if (knownConnections.getEdge(this, dest) != null) {
				throw new RuntimeException("Fooky");
			}
			
			knownConnections.addEdge(this, dest, weight);
		} else {
			knownConnections.addEdge(this, dest, weight);
		}
	}
	
}