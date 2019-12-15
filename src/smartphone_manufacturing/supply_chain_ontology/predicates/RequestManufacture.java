package smartphone_manufacturing.supply_chain_ontology.predicates;

import jade.core.AID;
import jade.content.Predicate;
import jade.content.onto.annotations.Slot;
import smartphone_manufacturing.supply_chain_ontology.concepts.CustomerOrder;

/*
 * Predicate for customer to query if manufacturer can manufacture the order
 * */

public class RequestManufacture implements Predicate {
	
	private static final long serialVersionUID = 1L;
	private AID manufacturer;
	private CustomerOrder customerOrder;
	
	@Slot(mandatory = true)
	public AID getManufacturer() {
		return manufacturer;
	}
	
	public void setManufacturer(AID manufacturer) {
		this.manufacturer = manufacturer;
	}
	
	@Slot(mandatory = true)
	public CustomerOrder getOrder() {
		return customerOrder;
	}
	public void setOrder(CustomerOrder customerOrder) {
		this.customerOrder = customerOrder;
	}

}
