package smartphone_manufacturing.supply_chain_ontology.concepts;

import java.util.Objects;

import jade.content.Concept;
import jade.content.onto.annotations.Slot;

/**
 * Concept that defines an order from a customer to a manufacturer
 * */

public class CustomerOrder implements Concept{
	public static final long serialVersionUID = 1L;
	
	private String orderID;
	private SmartPhone phone;
	private int quantity;
	private int price;
	private int daysToDeadline;
	private int perDayPenalty;
	
	@Slot(mandatory = true)
	public String getOrderID() {
		return orderID;
	}
	
	public void setOrderID(String ID) {
		this.orderID = ID;
	}
	
	@Slot(mandatory = true)
	public SmartPhone getSmartPhone() {
		return phone;
	}
	
	public void setSmartPhone(SmartPhone smartPhone) {
		this.phone = smartPhone;
	}
	
	@Slot(mandatory = true)
	public int getQuantity() {
		return quantity;
	}
  
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	
	@Slot(mandatory = true)
	public int getPrice() {
		return price;
	}
	
	public void setPrice(int price) {
		this.price = price;
	}
	
	@Slot(mandatory = true)
	public int getDaysToDeadline() {
		return daysToDeadline;
	}
	
	public void setDaysToDeadline(int days) {
		this.daysToDeadline = days;
	}
	
	@Slot(mandatory = true)
	public int getPerDayPenalty() {
		return perDayPenalty;
	}

	public void setPerDayPenalty(int perDayPenalty) {
		this.perDayPenalty = perDayPenalty;
	}

	@Override
	  public String toString() {
	    String phoneString = phone.toString();
	    return String.format("(\n phone: %s, \n quantity: %s, \n price: %s, \n due: %s)", phoneString, quantity, price, daysToDeadline);
	  }

}
