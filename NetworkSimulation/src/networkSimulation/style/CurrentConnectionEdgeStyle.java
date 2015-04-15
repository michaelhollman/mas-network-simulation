package networkSimulation.style;

import java.awt.Color;

import networkSimulation.AbstractRequest;
import repast.simphony.space.graph.RepastEdge;
import repast.simphony.visualizationOGL2D.DefaultEdgeStyleOGL2D;

public class CurrentConnectionEdgeStyle extends DefaultEdgeStyleOGL2D {

	@Override
	public Color getColor(RepastEdge<?> edge) {
		if (edge.getWeight() == AbstractRequest.CONNECTION_PING) {
			return new Color(0f, 1f, 0f, 0.2f);
		} else {
			return new Color(0f, 0f, 1f, 0.2f);
		}
	}

	@Override
	public int getLineWidth(RepastEdge<?> edge) {
		return 1;
	}
}
