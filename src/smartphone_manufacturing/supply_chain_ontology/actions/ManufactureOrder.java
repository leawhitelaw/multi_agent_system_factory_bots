package smartphone_manufacturing.supply_chain_ontology.actions;
import jade.content.onto.annotations.Slot;
import jade.core.AID;
import smartphone_manufacturing.supply_chain_ontology.concepts.Order;

public class ManufactureOrder {

	public ManufactureOrder() {}
	
	private AID buyer;
	private Order order;
	
	@Slot(mandatory = true)
	public AID getBuyer() {
		return buyer;
	}
	
	public void setBuyer(AID buyer) {
		this.buyer = buyer;
	}
	
	@Slot(mandatory = true)
	public Order getOrder() {
		return order;
	}
	
	public void setOrder(Order order) {
		this.order = order;
	}

}
