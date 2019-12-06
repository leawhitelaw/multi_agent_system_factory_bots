package smartphone_manufacturing.supply_chain_ontology.predicates;

import jade.content.Predicate;
import jade.content.onto.annotations.Slot;
import jade.core.AID;

public class PaymentSent implements Predicate {
	
	private static final long serialVersionUID = 1L;
	private AID buyer;
	private double price;
	private int orderID;
	
	@Slot(mandatory = true)
	public AID getBuyer() {
		return buyer;
	}
	
	public void setBuyer(AID buyer) {
		this.buyer = buyer;
	}
	
	public int getOrderID() {
		return orderID;
	}
	
	public void setOrderID(int orderid) {
		this.orderID = orderid;
	}
	
	@Slot(mandatory = true)
	public double getPrice() {
		return price;
	}
	
	public void setPrice(double price) {
		this.price = price;
	}

}
