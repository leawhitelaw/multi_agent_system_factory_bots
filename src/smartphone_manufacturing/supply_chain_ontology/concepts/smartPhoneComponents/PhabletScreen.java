package smartphone_manufacturing.supply_chain_ontology.concepts.smartPhoneComponents;

import java.util.Objects;

import jade.content.onto.annotations.Slot;

/*
 * Phablet screen component for phablet phone: 7" screen mandatory
 * */

public class PhabletScreen extends Screen {
private static final long serialVersionUID =1L;
	
	private int size = 7;
	
	@Slot(mandatory = true)
	public int getScreenSize() {
		return size;
	}

	
	@Override
	public String toString() {
		return "Phablet phone screen: 7\"";
	}
	
	@Override
	  public int hashCode() {
	    return Objects.hash(this.toString());
	  }

}
