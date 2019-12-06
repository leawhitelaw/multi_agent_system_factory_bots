package smartphone_manufacturing.supply_chain_ontology.concepts.smartPhoneComponents;
import jade.content.onto.annotations.Slot;
import smartphone_manufacturing.supply_chain_ontology.concepts.PhoneComponent;

/*
 * RAM memory component for phone: interchangeable between 4GB or 8GB
 * */

public class RAM extends PhoneComponent {
	private static final long serialVersionUID = 1L;
	
	private int gb;
	
	public RAM() {}
	
	public RAM(int gb) {
		setGb(gb);
	}
	
	@Slot(mandatory = true)
	public int getGb() {
		return gb;
	}
	
	public void setGb(int gb) {
		this.gb = gb;
	}
	
	@Override
	public String toString() {
		return "RAM: " + this.gb;
	}

}
