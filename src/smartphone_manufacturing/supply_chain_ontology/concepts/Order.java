package smartphone_manufacturing.supply_chain_ontology.concepts;

import jade.content.Concept;
import jade.content.onto.annotations.Slot;

/**
 * Concept that defines an order from a customer to a manufacturer
 * */

public class Order implements Concept{
	public static final long serialVersionUID = 1L;
	
	private int orderID;
	private SmartPhone phone;
	private int quantity;
	private double price;
	private int daysToDeadline;
	
	public int getOrderID() {
		return orderID;
	}
	
	public void setOrderID(int ID) {
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
	public double getPrice() {
		return price;
	}
	
	public void setPrice(double price) {
		this.price = price;
	}
	
	@Slot(mandatory = true)
	public int getDaysToDeadline() {
		return daysToDeadline;
	}
	
	public void setDaysToDeadline(int days) {
		this.daysToDeadline = days;
	}
	
	@Override
	  public String toString() {
	    String phoneString = phone.toString();
	    return String.format("(\n phone: %s, \n quantity: %s, \n price: %s, \n due: %s)", phoneString, quantity, price, daysToDeadline);
	  }

}
