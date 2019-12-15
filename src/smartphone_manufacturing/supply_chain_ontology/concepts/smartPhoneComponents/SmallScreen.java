package smartphone_manufacturing.supply_chain_ontology.concepts.smartPhoneComponents;

import java.util.Objects;

/*
 * Small screen component for small phone: 5" screen mandatory
 * */

public class SmallScreen extends Screen {
	private static final long serialVersionUID =1L;
	
	@Override
	public String toString() {
		return "Small phone screen: 5\"";
	}
	
	@Override
	  public int hashCode() {
	    return Objects.hash(this.toString());
	  }

}
