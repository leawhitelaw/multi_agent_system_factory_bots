package smartphone_manufacturing.supply_chain_ontology.concepts.smartPhoneComponents;

import java.util.Objects;

import jade.content.onto.annotations.Slot;

/*
 * Phablet battery component for phablet phone: 3000mAh mandatory
 * */

public class PhabletBattery extends Battery {
private static final long serialVersionUID =1L;
	private int capacity = 3000;
	
	@Slot(mandatory = true)
	public int getBatterySize() {
		return capacity;
	}
	
	@Override
	public String toString() {
		return "Phablet phone battery: 3000mAh";
	}
	
	@Override
	  public int hashCode() {
	    return Objects.hash(this.toString());
	  }

}
