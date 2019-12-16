package smartphone_manufacturing.supply_chain_ontology.predicates;

import java.util.ArrayList;

import jade.content.Predicate;
import jade.content.onto.annotations.Slot;
import jade.core.AID;
import smartphone_manufacturing.supply_chain_ontology.concepts.PhoneComponent;

/*
 * Predicate to query if supplier owns required components 
 * */

public class ComponentsInStock implements Predicate {

	private static final long serialVersionUID = 1L;
	private AID supplier;
	private ArrayList<PhoneComponent> components;
	private int quantity;
	//private String orderID;
	
	@Slot(mandatory = true)
	public AID getSupplier() {
		return supplier;
		
	}
	
	public void setSupplier(AID supplier) {
		this.supplier = supplier;
	}
	
	@Slot(mandatory = true)
	public ArrayList<PhoneComponent> getComponents(){
		return components;
	}
	
	public void setComponents(ArrayList<PhoneComponent> components) {
		this.components = components;
	}
	
	@Slot(mandatory = true)
	public int getQuantity() {
		return quantity;
	}
	
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

}
