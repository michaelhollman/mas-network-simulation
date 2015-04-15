package networkSimulation;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.graph.Network;

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
		knownFiles = new Vector<Integer>(config.StartingFiles);
		workQueue = new Vector<AbstractRequest>();
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
		}
		
		
		// create and add new requests to the work queue
		if (config.RequestDistribution.getDecision()) {
			int newFile = RandomUtil.getRandom(0, GlobalContext.FileCount - 1, knownFiles);
			QueryRequest newQuery = new QueryRequest(this, newFile);
			workQueue.add(newQuery);
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
		Integer fileNum = new Integer(fileNumber);
		if (!knownFiles.contains(fileNum)) {
			knownFiles.add(fileNum);
		}
	}

	public void ping(PingRequest ping) {
		if (ping.targetIP != config.NodeIp) {
			throw new RuntimeException("Ping sent to wrong node. Target=" + ping.targetIP + " Receiver=" + config.NodeIp);
		}
		workQueue.add(ping);
	}
	
	public void query(QueryRequest query) {
		workQueue.add(query);
		query.addIntermediate(this);
		System.out.println("foo");
	}
	
	public void giveFileInfo(int fileNumber, int fileOwnerIP) {
		Integer fileNum = new Integer(fileNumber);
		Integer fileOwn = new Integer(fileOwnerIP);
		
		if (!knownFileOwners.containsKey(fileNumber)) {
			knownFileOwners.put(fileNum, new ArrayList<Integer>());
		}
		
		ArrayList<Integer> owners = knownFileOwners.get(fileNum);
		if (!owners.contains(fileOwn)) {
			owners.add(fileOwn);
		}
		
		knownFileOwners.put(fileNum, owners);
	}
	
	public void giveResponseTimeInfo(int destinationIP, double ticks) {
		
	}
	
	//// PRIVATE ---------------------------------------
	
	private boolean hasFile(int fileNumber) {
		Integer fileNum = new Integer(fileNumber);
		return knownFiles.contains(fileNum);
	}
	
	// returns true if request has been finished and should be removed
	private boolean processRequest(AbstractRequest req) {		
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
			
			Integer fileNum = new Integer(query.fileNumber);
			ArrayList<Integer> knownOwners = knownFileOwners.get(fileNum);
			
			if (knownOwners == null) {
				nextNode = GlobalContext.IpLookup.get(RandomHelper.nextIntFromTo(0, GlobalContext.NodeCount - 1));
			} else {
				nextNode = GlobalContext.IpLookup.get(knownOwners.get(RandomHelper.nextIntFromTo(0, knownOwners.size() - 1)).intValue());
			}
			
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
	
	
}