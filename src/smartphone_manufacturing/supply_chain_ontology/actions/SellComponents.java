package smartphone_manufacturing.supply_chain_ontology.actions;
import smartphone_manufacturing.supply_chain_ontology.concepts.PhoneComponent;

import java.util.ArrayList;
import jade.content.AgentAction;
import jade.content.onto.annotations.Slot;
import jade.core.AID;

/*
 * Action for manufacturer to buy components from supplier
 * */

public class SellComponents implements AgentAction {
	private static final long serialVersionUID = 1L;
	
	private ArrayList<PhoneComponent> phoneComponents;
	private String orderID;
	private int quantity;
	private AID manufacturer;
	
	@Slot(mandatory = true)
	  public ArrayList<PhoneComponent> getComponents() {
	    return phoneComponents;
	  }
	
	public void setComponents(ArrayList<PhoneComponent> phoneComponents) {
	    this.phoneComponents = phoneComponents;
	  }
	
	@Slot(mandatory = true)
	  public String getOrderId() {
	    return orderID;
	  }
	  public void setOrderId(String orderID) {
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
	public AID getManufacturer() {
		return manufacturer;
	}
	
	public void setManufacturer(AID manufacturer) {
		this.manufacturer = manufacturer;
	}

}
