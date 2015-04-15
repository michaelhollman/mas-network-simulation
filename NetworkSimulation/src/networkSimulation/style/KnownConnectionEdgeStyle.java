package networkSimulation.style;

import java.awt.Color;

import networkSimulation.GlobalContext;
import repast.simphony.space.graph.RepastEdge;
import repast.simphony.visualizationOGL2D.DefaultEdgeStyleOGL2D;

public class KnownConnectionEdgeStyle extends DefaultEdgeStyleOGL2D {

	@Override
	public Color getColor(RepastEdge<?> edge) {
		return Color.LIGHT_GRAY;
	}

	@Override
	public int getLineWidth(RepastEdge<?> edge) {
		double wt = edge.getWeight();
		int max = GlobalContext.Timeout;
		int floor = 1;
		int ceil = 4;
		
		return (int) ((wt / max) * (ceil - floor) + floor);
	}

}
