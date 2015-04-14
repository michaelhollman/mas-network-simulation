package networkSimulation;

import repast.simphony.context.Context;
import repast.simphony.context.DefaultContext;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.random.RandomHelper;

public class NetworkContextBuilder extends DefaultContext<TestAgent> implements ContextBuilder<TestAgent> {

	public Context<TestAgent> build(Context<TestAgent> context) {
		context.setId("rak_network");
		
		for (int i = 0; i < 5; i++)
		{
		context.add(new TestAgent(i));
		}
		
		System.out.print("FUCK");
		return context;
	}

}
