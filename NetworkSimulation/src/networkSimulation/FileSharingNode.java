package networkSimulation;

import java.util.*;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.graph.Network;

public class FileSharingNode {	
	
	public NodeConfiguration config;
	public int numberOfConnections; // this will eventually be in  config or something
	public int ip; // this will eventually be in  config or something
	public ArrayList<Integer> knownFiles;
	public LinkedList<AbstractRequest> workQueue;
	
	public FileSharingNode(NodeConfiguration configuration) {
		this.workQueue = new LinkedList<AbstractRequest>();
		this.knownFiles = new ArrayList<Integer>();
		this.numberOfConnections = 1;
		this.config = configuration;
	}
	
	@ScheduledMethod(start = 1, interval = 1)
	public void tick() {

		// handle race condition: remove any already fulfilled requests
		// remove timed out ones too
		for (AbstractRequest req : workQueue) {
			if (req.fulfilled || req.checkTimeOut()) {
				workQueue.remove(req);
			}
		}
		
		
		// TODO:  create and add new requests to the work queue
		
		
		// process requests
		LinkedList<AbstractRequest> finishedRequests = new LinkedList<AbstractRequest>();
		int numberOfRequestsToProcess = Math.min(numberOfConnections, workQueue.size());
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
		if (ping.targetIP != ip) {
			throw new RuntimeException("Ping sent to wrong node. Target=" + ping.targetIP + " Receiver=" + ip);
		}
		workQueue.add(ping);
	}
	
	public void query(QueryRequest query) {
		workQueue.add(query);
	}
	
	public void giveFileInfo(int fileNumber, int fileOwnerIP) {
		
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
		} else if (ip == ping.targetIP && !ping.fulfilled) {
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
			
			return false;
		} else if (query.nodes.contains(this)) {
			return false;
		}
		
		throw new RuntimeException("processRequest didn't know what to do!");
	}
	
	
}