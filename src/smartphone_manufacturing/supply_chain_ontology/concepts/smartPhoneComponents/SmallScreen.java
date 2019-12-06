package smartphone_manufacturing.supply_chain_ontology.concepts.smartPhoneComponents;

/*
 * Small screen component for small phone: 5" screen mandatory
 * */

public class SmallScreen extends Screen {
	private static final long serialVersionUID =1L;
	
	@Override
	public String toString() {
		return "Small phone screen: 5\"";
	}
	
//	//compare if small screen is the same
//	@Override
//	public boolean equals(Object comparison) {
//		if(!(comparison instanceof SmallScreen)) {
//			return false;
//		}
//		else {
//			SmallScreen comparsionScreen = (SmallScreen) comparison;
//			
//			
//		}
//	
//	@Override
//	public int hashCode() {
//		return Objects.hash(this);
//	}

}
