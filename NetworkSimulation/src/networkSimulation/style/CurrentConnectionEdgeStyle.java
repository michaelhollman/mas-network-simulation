package networkSimulation.style;

import java.awt.Color;

import repast.simphony.space.graph.RepastEdge;
import repast.simphony.visualizationOGL2D.DefaultEdgeStyleOGL2D;

public class CurrentConnectionEdgeStyle extends DefaultEdgeStyleOGL2D {

	@Override
	public Color getColor(RepastEdge<?> edge) {
		return Color.BLUE;
	}

	@Override
	public int getLineWidth(RepastEdge<?> edge) {
		return 2;
	}
}
