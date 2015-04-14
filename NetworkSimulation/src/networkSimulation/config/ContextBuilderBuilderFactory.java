package networkSimulation.config;

public class ContextBuilderBuilderFactory {
	public static ContextBuilderBuilder getBuilder(String experiment) {
		switch (experiment) {
			case "test":
				return new TestBuilder();		
		}
		
		return null;
	}
}
