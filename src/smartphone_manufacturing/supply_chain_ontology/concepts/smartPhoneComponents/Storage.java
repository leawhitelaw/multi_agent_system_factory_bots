package smartphone_manufacturing.supply_chain_ontology.concepts.smartPhoneComponents;

import java.util.Objects;

import jade.content.onto.annotations.Slot;
import smartphone_manufacturing.supply_chain_ontology.concepts.PhoneComponent;

/*
 * Storage component for phone: interchangeable between 64GB or 256GB
 * */

public class Storage extends PhoneComponent {
	private static final long serialVersionUID = 1L;
	
	private int gb;
	
	public Storage() {}
	
	public Storage(int gb) {
		setStorage(gb);
	}
	
	@Slot(mandatory = true)
	public int getStorage() {
		return gb;
	}
	
	public void setStorage(int gb) {
		this.gb = gb;
	}
	
	@Override
	public String toString() {
		return "Storage: " + this.gb;
	}
	
	@Override
	  public int hashCode() {
	    return Objects.hash(this.gb);
	  }
	

}
