package smartphone_manufacturing.supply_chain;
import java.util.HashMap;

import jade.core.AID;
import smartphone_manufacturing.supply_chain_ontology.concepts.PhoneComponent;

public class SupplierType {
	
	private AID supplier;
	private int delivery;
	private HashMap<PhoneComponent, Integer> prices;
	
	public SupplierType(AID id) {
		this.supplier = id;
	}

	public AID getSupplier() {
		return supplier;
	}

	public void setSupplier(AID supplier) {
		this.supplier = supplier;
	}

	public int getDelivery() {
		return delivery;
	}

	public void setDelivery(int delivery) {
		this.delivery = delivery;
	}

	public HashMap<PhoneComponent, Integer> getPrices() {
		return prices;
	}

	public void setPrices(HashMap<PhoneComponent, Integer> prices) {
		this.prices = prices;
	}

}
