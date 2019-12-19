package smartphone_manufacturing.supply_chain_ontology.concepts.smartPhoneComponents;

import java.util.Objects;

import jade.content.onto.annotations.Slot;

/*
 * Small battery component for small phone: 2000mAh mandatory
 * */

public class SmallBattery extends Battery {
	private static final long serialVersionUID =1L;
	
private int capacity = 2000;
	
	@Slot(mandatory = true)
	public int getBatterySize() {
		return capacity;
	}
	
	@Override
	public String toString() {
		return "Small phone battery: 2000mAh";
	}
	
	@Override
	  public int hashCode() {
	    return Objects.hash(this.toString());
	  }

}
