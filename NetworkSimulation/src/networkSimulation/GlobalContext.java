package networkSimulation;

import java.util.ArrayList;

public class GlobalContext {
    public static int NodeCount;
    public static int FileCount;
    public static int Timeout;
    public static ArrayList<FileSharingNode> IpLookup;
    public static boolean NodesLearnUltimateFileOwner;
    public static boolean NodesLearnPathToFileOwner;
    public static boolean AllowSelfOverScheduling;
}
