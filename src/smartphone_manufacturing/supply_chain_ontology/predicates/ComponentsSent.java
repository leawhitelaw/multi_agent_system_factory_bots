package smartphone_manufacturing.supply_chain_ontology.predicates;

import java.util.ArrayList;

import jade.content.Predicate;
import jade.content.onto.annotations.Slot;
import jade.core.AID;
import smartphone_manufacturing.supply_chain_ontology.concepts.PhoneComponent;

public class ComponentsSent implements Predicate {
	
	private AID seller;
	private String orderID;
	private int qty;
	private ArrayList<PhoneComponent> phoneComponents;
	
	@Slot(mandatory = true)
	public AID getSeller() {
		return seller;
	}
	
	public void setSeller(AID seller) {
		this.seller = seller;
	}
	
	//@Slot(mandatory = true)
	public String getOrderID() {
		return orderID;
	}
	
	public void setOrderID(String orderID) {
		this.orderID = orderID;
	}
	
	@Slot(mandatory = true)
	public int getQty() {
		return qty;
	}
	
	public void setQty(int qty) {
		this.qty = qty;
	}
	
	@Slot(mandatory = true)
	public ArrayList<PhoneComponent> getPhoneComponents() {
		return phoneComponents;
	}
	
	public void setPhoneComponents(ArrayList<PhoneComponent> phoneComponents) {
		this.phoneComponents = phoneComponents;
	}

}
