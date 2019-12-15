package smartphone_manufacturing.supply_chain_ontology.predicates;

import java.util.ArrayList;

import jade.content.Predicate;
import jade.content.onto.annotations.Slot;
import jade.core.AID;
import smartphone_manufacturing.supply_chain_ontology.concepts.PhoneComponent;

public class SentSupplierDetails implements Predicate {
	private static final long serialVersionUID = 1L;
	
	private AID supplier;
	private ArrayList<Long> componentPrices; //this will be used as the value
	private ArrayList<PhoneComponent> phoneComponents; //this will be used as key (hashmaps not supported)
	private int devlieryDays;
	
	@Slot(mandatory = true)
	public AID getSupplier() {
		return supplier;
	}
	
	public void setSupplier(AID supplier) {
		this.supplier = supplier;
	}
	
	@Slot(mandatory = true)
	public ArrayList<Long> getComponentPrices() {
		return componentPrices;
	}
	
	public void setComponentPrices(ArrayList<Long> componentPrices) {
		this.componentPrices = componentPrices;
	}
	
	@Slot(mandatory = true)
	public ArrayList<PhoneComponent> getPhoneComponents() {
		return phoneComponents;
	}
	
	public void setPhoneComponents(ArrayList<PhoneComponent> phoneComponents) {
		this.phoneComponents = phoneComponents;
	}
	
	@Slot(mandatory = true)
	public int getDevlieryDays() {
		return devlieryDays;
	}
	public void setDevlieryDays(int devlieryDays) {
		this.devlieryDays = devlieryDays;
	}
	
	

}
