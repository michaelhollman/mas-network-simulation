package networkSimulation;

import java.util.ArrayList;

/**
 * Globally shared data in this MAS. Note that this data doesn't provide any information for coordination or decision making; it's all done locally.
 */
public class GlobalContext {
    /**
     * The number of nodes configured in this MAS. The last agent IP address will be NodeCount - 1
     */
    public static int NodeCount;

    /**
     * The number of total files in the universe. The last file number will be FileCount - 1
     */
    public static int FileCount;

    /** The global timeout. */
    public static int Timeout;

    /** The IpLookup table, which allows us to address a node by their IP Address. */
    public static ArrayList<FileSharingNode> IpLookup;

    public static boolean NodesLearnUltimateFileOwner;
    public static boolean NodesLearnPathToFileOwner;
    public static boolean AllowSelfOverScheduling;
}
