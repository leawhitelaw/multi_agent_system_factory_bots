package smartphone_manufacturing.supply_chain;
import java.util.HashMap;

import jade.core.AID;
import smartphone_manufacturing.supply_chain_ontology.concepts.PhoneComponent;
import smartphone_manufacturing.supply_chain_ontology.concepts.CustomerOrder;

public class CustomerOrderStatus {
	
	private boolean orderCompleted; //know when order has been completed
	private AID supplier;
	private AID customer;
	private CustomerOrder order;
	private int price;
	private HashMap<PhoneComponent, Integer> components;
	private int componentDeliveryDate;
	private int dayOrdered;
	private int phonesBuilt;
	
	public CustomerOrderStatus(CustomerOrder order) {
		this.setOrder(order);
		components = new HashMap<>();
	}

	public boolean getOrderCompleted() {
		return orderCompleted;
	}

	public void setOrderCompleted(boolean bool) {
		this.orderCompleted = bool;
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

	public int getPhonesBuilt() {
		return phonesBuilt;
	}

	public void setPhonesBuilt(int phonesBuilt) {
		this.phonesBuilt = phonesBuilt;
	}

	@Override
	public String toString() {
		return "CustomerOrderStatus [orderCompleted=" + orderCompleted + ", supplier=" + supplier.getLocalName() + ", customer="
				+ customer.getLocalName() + ", order=" + order + ", price=" + price + ", components=" + components
				+ ", componentDeliveryDate=" + componentDeliveryDate + ", dayOrdered=" + dayOrdered + ", phonesBuilt="
				+ phonesBuilt + "]";
	}

	
	
	

}
