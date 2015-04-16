# MAS NetworkSimulation
A multi-agent peer to peer network simulation using Repast<br/>
CSCE475H, Spring 2015 | Dr. LeenKiat Soh

### Getting Started

Our simulation was built against Repast Simphony 2.2, and developed in Java using Eclipse.  To run the simulation:
- Ensure that Repast Simphony 2.2 is installed on your system
- Open Eclipse, and ensure that the Repast Simphony Perspective is enabled
- Open the `NetworkSimulation` project in Eclipse.  (This can be done from the File -> Import -> General -> Import Existing Projects into Workspace menu option)
- In the PackageExplorer, open the `launchers` folder and select `NetworkSimulation Model.launch`.  Then, click the green Run button in Eclipse.  This will install the appropriate run configuration, and start the Repast GUI to run our models. 

### Setting up the Model

When the simulation is started from Eclipse, the Repast GUI appears.  The parameters pane offers a wide variety of options for configuring the setup of the model.  The Experiment Setting option lets you choose between test (basic), identical (all nodes the same), and ultraAndLeaf (nodes partitioned into two configurable classes) experiment settings.  These broad experiment settings control the structure of how nodes are configured.

From there, you can can choose the configuration applied to each node.  These include: 
- The number of files per node
- The simulataneous connection limit
- The probability of wanting to request a file
- The churn rate
- And many more. See the parameters window for all of them.

For ease of testing, reasonable defaults are set.

Clicking the Start button will start the simulation.  The **KnownAndCurrentConnections** pane visualizes the current state of the network:
- Dead nodes have a red border
- Ultra nodes are orange; all others are black
- Light gray lines indicate total known connections
- Green lines indicate current ping requests
- Blue lines indicate current query requests

The other panes provide well-labeled charts of data we're collecting from each node.  Unfortunately, Repast does not allow us to visualize Ultra Peer Nodes and Leaf node data differently, so we also dump all of the collected data to a .csv file after each run so we can process it in Excel.

### About the Simulation

This is a multi-agent system that simulates a simple distributed peer-to-peer network for file sharing.  The network consists of many nodes, which either download files, host files, or serve as middlemen.  Nodes employ local value functions to maximize the speed of each query transaction (Local Decisions), such that the network provides high average query speeds across the graph, optimally distributed traffic, and low failure rates (Global Coherence).

The peer-to-peer network system contains one fundamental agent type, a node, which may exhibit any combination of three core behaviors: 1) requesting files, 2) hosting (seeding) files, and 3) serving as a middleman routing traffic.  The following actions are available to nodes:
- Ping: Discover peer i
- Pong: Reply to a ping from peer i
- Query: Request file f
- Query Hit: Reply to a query for file f
- Join: Join network N
- Leave: Leave network N (die)

The desired emergent behavior of this system is that the nodes in the network will structure themselves such that the network provides high average query speeds across the graph, optimally distributed traffic, and low failure rates.

The FileSharingNode is the only agent type in our multi-agent system. Depending on the experiement setting, it may be configured the same or very different from other nodes in the network.

FileSharingNodes value getting the files they request quickly, and gaining information about other nodes in the network. They use this knowledge to optimize their decisions made while processing requests.  At each iteration, if the node needs to pass on the request to another node (beacuse it does not have the file), it uses a local heuristic to find the best target node.

When a FileSharingNode is created, it knows nothing about the other nodes in the environment (other than the max IP address).  When it starts desiring files, it needs to ping into the files sharing network first to find a node that can serve its request.

In our experiments, we broadly consider generic nodes (where they're configured roughly the same), and ultra peer and leaf nodes (which have very different configurations). These nodes are configured differently, and lead to very different network behavior.
