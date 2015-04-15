package networkSimulation.style;

import java.awt.Color;
import java.awt.Font;

import networkSimulation.FileSharingNode;
import repast.simphony.visualizationOGL2D.DefaultStyleOGL2D;
import saf.v3d.scene.Position;

public class FileSharingNodeStyle extends DefaultStyleOGL2D {

	public Color getColor(Object agent) {
		if (!(agent instanceof FileSharingNode))
			return super.getColor(agent);
		FileSharingNode node = (FileSharingNode) agent;

		switch (node.config.NodeType) {
		case GENERIC:
			return Color.BLACK;
		case ULTRA_PEER:
			return Color.ORANGE;
		case LEAF:
			return Color.GREEN;
		}

		return Color.BLACK;
	}

	public float getScale(Object object) {
		return 3f;
	}

}
