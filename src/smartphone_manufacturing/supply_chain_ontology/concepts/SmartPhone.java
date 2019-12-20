package smartphone_manufacturing.supply_chain_ontology.concepts;

import java.util.ArrayList;

import jade.content.onto.annotations.Slot;
import smartphone_manufacturing.supply_chain_ontology.concepts.smartPhoneComponents.*;

public class SmartPhone {
	
	private RAM ram;
	private Storage storage;
	private Battery battery;
	private Screen screen;
	
	@Slot(mandatory = true)
	public RAM getRAM() {
		return ram;
	}
	
	public void setRAM(RAM ram) {
		this.ram = ram;
	}
	
	@Slot(mandatory = true)
	public Storage getStorage() {
		return storage;
	}
	
	public void setStorage(Storage storage) {
		this.storage = storage;
	}
	
	@Slot(mandatory = true)
	public Battery getBattery() {
		return battery;
	}
	
	public void setBattery(Battery battery) {
		this.battery = battery;
	}
	
	@Slot(mandatory = true)
	public Screen getScreen() {
		return screen;
	}
	
	public void setScreen(Screen screen) {
		this.screen = screen;
	}
	
	@Slot(mandatory = true)
	public ArrayList<PhoneComponent> getPhoneComponents(){
		ArrayList<PhoneComponent> components = new ArrayList<>();
		components.add(battery);
		components.add(screen);
		components.add(storage);
		components.add(ram);
		return components;
	}
	

}
