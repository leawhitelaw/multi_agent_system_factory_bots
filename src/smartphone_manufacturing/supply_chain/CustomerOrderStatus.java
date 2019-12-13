package smartphone_manufacturing.supply_chain;
import java.util.HashMap;

import jade.core.AID;
import smartphone_manufacturing.supply_chain_ontology.concepts.PhoneComponent;
import smartphone_manufacturing.supply_chain_ontology.concepts.CustomerOrder;

public class CustomerOrderStatus {
	
	private String orderStatus;
	private AID supplier;
	private AID customer;
	private CustomerOrder order;
	private int price;
	private HashMap<PhoneComponent, Integer> components;
	private int componentDeliveryDate;
	private int dayOrdered;
	
	public CustomerOrderStatus(CustomerOrder order) {
		this.setOrder(order);
		components = new HashMap<>();
	}

	public String getOrderStatus() {
		return orderStatus;
	}

	public void setOrderStatus(String orderStatus) {
		this.orderStatus = orderStatus;
	}

	public AID getSupplier() {
		return supplier;
	}

	public void setSupplier(AID supplier) {
		this.supplier = supplier;
	}

	public AID getCustomer() {
		return customer;
	}

	public void setCustomer(AID customer) {
		this.customer = customer;
	}

	public CustomerOrder getOrder() {
		return order;
	}

	public void setOrder(CustomerOrder order) {
		this.order = order;
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	public HashMap<PhoneComponent, Integer> getComponents() {
		return components;
	}

	public void setComponents(HashMap<PhoneComponent, Integer> components) {
		this.components = components;
	}

	public int getComponentDeliveryDate() {
		return componentDeliveryDate;
	}

	public void setComponentDeliveryDate(int componentDeliveryDate) {
		this.componentDeliveryDate = componentDeliveryDate;
	}

	public int getDayOrdered() {
		return dayOrdered;
	}

	public void setDayOrdered(int dayOrdered) {
		this.dayOrdered = dayOrdered;
	}
	

}
