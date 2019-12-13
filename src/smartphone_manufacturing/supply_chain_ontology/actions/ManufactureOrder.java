package smartphone_manufacturing.supply_chain_ontology.actions;
import jade.content.AgentAction;
import jade.content.onto.annotations.Slot;
import jade.core.AID;
import smartphone_manufacturing.supply_chain_ontology.concepts.CustomerOrder;

public class ManufactureOrder implements AgentAction {

	private static final long serialVersionUID = 1L;

	public ManufactureOrder() {}
	
	private AID buyer;
	private CustomerOrder customerOrder;
	
	@Slot(mandatory = true)
	public AID getBuyer() {
		return buyer;
	}
	
	public void setBuyer(AID buyer) {
		this.buyer = buyer;
	}
	
	@Slot(mandatory = true)
	public CustomerOrder getOrder() {
		return customerOrder;
	}
	
	public void setOrder(CustomerOrder customerOrder) {
		this.customerOrder = customerOrder;
	}

}
