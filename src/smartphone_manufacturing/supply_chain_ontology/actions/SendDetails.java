package smartphone_manufacturing.supply_chain_ontology.actions;

import jade.content.AgentAction;
import jade.content.onto.annotations.Slot;
import jade.core.AID;

/**
 * Agent action to request a list of prices and delivery times 
 * from the supplier. These are returned with the 'SentSupplierDetails'
 * predicate from the supplier
 * */

public class SendDetails implements AgentAction {
	
	private static final long serialVersionUID = 1L;
	
	private AID buyer;
	
	@Slot(mandatory = true)
	public AID getBuyer() {
		return buyer;
	}
	
	public void setBuyer(AID buyer) {
		this.buyer = buyer;
	}
	

}
