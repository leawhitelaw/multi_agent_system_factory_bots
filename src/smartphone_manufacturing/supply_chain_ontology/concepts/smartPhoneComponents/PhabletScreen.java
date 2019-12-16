package smartphone_manufacturing.supply_chain_ontology.concepts.smartPhoneComponents;

import java.util.Objects;

/*
 * Phablet screen component for phablet phone: 7" screen mandatory
 * */

public class PhabletScreen extends Screen {
private static final long serialVersionUID =1L;

	
	@Override
	public String toString() {
		return "Phablet phone screen: 7\"";
	}
	
	@Override
	  public int hashCode() {
	    return Objects.hash(this.toString());
	  }

}
