package smartphone_manufacturing.supply_chain_ontology.concepts;

import java.util.ArrayList;

import jade.content.Concept;
import jade.content.onto.annotations.Slot;
import jade.core.AID;

public class ComponentsOrder implements Concept{
	
	private static final long serialVersionUID = 1L;
	private String orderID;
	private int quantity;
	private int delivery;
	private ArrayList<PhoneComponent> components;
	private AID buyer;
	
	public String getOrderID() {
		return orderID;
	}
	
	public void setOrderID(String orderID) {
		this.orderID = orderID;
	}
	
	@Slot(mandatory = true)
	public int getQuantity() {
		return quantity;
	}
	
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	
	@Slot(mandatory = true)
	public int getDelivery() {
		return delivery;
	}
	
	public void setDelivery(int delivery) {
		this.delivery = delivery;
	}
	
	@Slot(mandatory = true)
	public ArrayList<PhoneComponent> getComponents() {
		return components;
	}
	
	public void setComponents(ArrayList<PhoneComponent> components) {
		this.components = components;
	}
	
	@Slot(mandatory = true)
	public AID getBuyer() {
		return buyer;
	}
	public void setBuyer(AID buyer) {
		this.buyer = buyer;
	}

}
