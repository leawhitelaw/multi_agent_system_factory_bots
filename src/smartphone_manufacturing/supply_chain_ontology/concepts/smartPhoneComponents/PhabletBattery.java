package smartphone_manufacturing.supply_chain_ontology.concepts.smartPhoneComponents;

import java.util.Objects;

/*
 * Phablet battery component for phablet phone: 3000mAh mandatory
 * */

public class PhabletBattery extends Battery {
private static final long serialVersionUID =1L;
	
	@Override
	public String toString() {
		return "Phablet phone battery: 3000mAh";
	}
	
	@Override
	  public int hashCode() {
	    return Objects.hash(this.toString());
	  }

}
