package networkSimulation.config;

public class ContextBuilderBuilderFactory {
	public static ContextBuilderBuilder getBuilder(String experimentSetting) {
		switch (experimentSetting) {
			case "test":
				return new TestBuilder();		
			case "identical":
				return new IdenticalBuilder();
			case "ultraAndLeaf":
				return new UltraAndLeafBuilder();
		}
		
		return null;
	}
}
