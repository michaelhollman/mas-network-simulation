package networkSimulation.style;

import java.awt.Color;

import networkSimulation.FileSharingNode;
import networkSimulation.NodeState;
import networkSimulation.NodeType;
import repast.simphony.visualizationOGL2D.DefaultStyleOGL2D;

public class FileSharingNodeStyle extends DefaultStyleOGL2D {

    public Color getColor(Object agent) {
        if (!(agent instanceof FileSharingNode))
            return super.getColor(agent);
        FileSharingNode node = (FileSharingNode) agent;

        if (node.config.NodeType == NodeType.ULTRA_PEER) {
            return Color.ORANGE;
        }

        return Color.BLACK;
    }

    public int getBorderSize(Object object) {
        if (!(object instanceof FileSharingNode))
            return super.getBorderSize(object);
        FileSharingNode node = (FileSharingNode) object;

        if (node.config.NodeState == NodeState.DEAD) {
            return 5;
        }
        return 0;
    }

    public Color getBorderColor(Object object) {
        if (!(object instanceof FileSharingNode))
            return super.getBorderColor(object);
        // FileSharingNode node = (FileSharingNode) object;

        return Color.RED;
    }

    public float getScale(Object object) {
        return 3f;
    }

}
