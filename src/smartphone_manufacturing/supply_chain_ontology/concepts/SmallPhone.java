package smartphone_manufacturing.supply_chain_ontology.concepts;

import jade.content.onto.annotations.Slot;
import smartphone_manufacturing.supply_chain_ontology.concepts.smartPhoneComponents.SmallBattery;
import smartphone_manufacturing.supply_chain_ontology.concepts.smartPhoneComponents.SmallScreen;

public class SmallPhone extends SmartPhone {
	
	private SmallBattery battery;
	private SmallScreen screen;
	
	public SmallPhone() {
		this.battery = new SmallBattery();
		this.screen = new SmallScreen();
	}
	
//	@Slot(mandatory = true)
//	public SmallBattery getBattery() {
//		return battery;
//	}
//	
//	public void setBattery(SmallBattery battery) {
//		this.battery = battery;
//	}
	
	@Slot(mandatory = true)
	public SmallScreen getScreen() {
		return screen;
	}
	
	public void setScreen(SmallScreen screen) {
		this.screen = screen;
	}
	
	@Override
	public String toString() {
		String string = String.format("Battery: %s, Screen: %s", battery, screen);
		return string;
	}

}