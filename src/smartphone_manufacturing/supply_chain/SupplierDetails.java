package smartphone_manufacturing.supply_chain;
import java.util.HashMap;
import smartphone_manufacturing.supply_chain_ontology.concepts.smartPhoneComponents.*;
import smartphone_manufacturing.supply_chain_ontology.concepts.PhoneComponent;

public class SupplierDetails {
	
	private static int SupplierOneDelivery = 1;
	private static int SupplierTwoDelivery = 2;
	
	private static HashMap<PhoneComponent, Integer> supplierOneComponents =
			new HashMap<PhoneComponent, Integer>() {
			{
			put(new SmallScreen(), 100);
			put(new PhabletScreen(), 150);
			put(new Storage(64), 25);
			put(new Storage(256), 50);
			put(new RAM(4), 30);
			put(new RAM(8), 60);
			put(new SmallBattery(), 70);
			put(new PhabletBattery(), 100);
			}};
	
	private static HashMap<PhoneComponent, Integer> supplierTwoComponents =
			new HashMap<PhoneComponent, Integer>() {
			{
			put(new Storage(64), 15);
			put(new Storage(256), 40);
			put(new RAM(4), 20);
			put(new RAM(8), 35);
			}};
	
	  public static HashMap<PhoneComponent, Integer> getSupplierOneComponents() {
	    return supplierOneComponents;
	  }
	  
	  public static HashMap<PhoneComponent, Integer> getSupplierTwoComponents() {
	    return supplierTwoComponents;
	  }
	  
	  public static int getSupplierOneDelivery() {
			return SupplierOneDelivery;
		}

	  public static int getSupplierTwoDelivery() {
			return SupplierTwoDelivery;
		}

}
