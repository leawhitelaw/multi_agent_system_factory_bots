package smartphone_manufacturing.supply_chain_ontology.predicates;

import jade.content.Predicate;
import jade.content.onto.annotations.Slot;
import jade.core.AID;

public class PaymentSent implements Predicate {
	
	private static final long serialVersionUID = 1L;
	private AID buyer;
	private int price;
	private String orderID;
	
	@Slot(mandatory = true)
	public AID getBuyer() {
		return buyer;
	}
	
	public void setBuyer(AID buyer) {
		this.buyer = buyer;
	}
	
	public String getOrderID() {
		return orderID;
	}
	
	public void setOrderID(String orderid) {
		this.orderID = orderid;
	}
	
	@Slot(mandatory = true)
	public int getPrice() {
		return price;
	}
	
	public void setPrice(int price) {
		this.price = price;
	}

	@Override
	public String toString() {
		return "PaymentSent [buyer=" + buyer + ", price=" + price + ", orderID=" + orderID + "]";
	}
	
	

}
