package smartphone_manufacturing.supply_chain_ontology.concepts;

import jade.content.onto.annotations.Slot;
import smartphone_manufacturing.supply_chain_ontology.concepts.smartPhoneComponents.PhabletBattery;
import smartphone_manufacturing.supply_chain_ontology.concepts.smartPhoneComponents.PhabletScreen;

public class Phablet extends SmartPhone {
	
	private PhabletBattery battery;
	private PhabletScreen screen;
	
	public Phablet() {
		this.battery = new PhabletBattery();
		this.screen = new PhabletScreen();
	}
	
	@Slot(mandatory = true)
	public PhabletBattery getBattery() {
		return battery;
	}
	
	public void setBattery(PhabletBattery battery) {
		this.battery = battery;
	}
	
	@Slot(mandatory = true)
	public PhabletScreen getScreen() {
		return screen;
	}
	
	public void setScreen(PhabletScreen screen) {
		this.screen = screen;
	}
	
	@Override
	public String toString() {
		String string = String.format("Battery: %s, Screen: %s", battery, screen);
		return string;
	}

}
