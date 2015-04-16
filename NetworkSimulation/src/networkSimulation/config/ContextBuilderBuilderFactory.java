package networkSimulation.config;

/**
 * A factory for creating ContextBuilderBuilder objects.
 */
public class ContextBuilderBuilderFactory {
	
	/**
	 * Gets the correct ContextBuilderBuilder, depending on the experiment stting we
	 * want to run
	 *
	 * @param experimentSetting the experiment setting
	 * @return the builder
	 */
	public static ContextBuilderBuilder getBuilder(String experimentSetting) {
		System.out.println("Experiment Setting: " + experimentSetting);
		
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
