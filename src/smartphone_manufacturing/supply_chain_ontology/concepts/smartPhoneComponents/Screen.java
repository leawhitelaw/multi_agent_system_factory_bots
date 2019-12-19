package smartphone_manufacturing.supply_chain_ontology.concepts.smartPhoneComponents;

import jade.content.onto.annotations.Slot;
import smartphone_manufacturing.supply_chain_ontology.concepts.PhoneComponent;

public class Screen extends PhoneComponent {
	private static final long serialVersionUID = 1L;
	private int size;
	
	@Slot(mandatory = true)
	public int getBatterySize() {
		return size;
	}
}
