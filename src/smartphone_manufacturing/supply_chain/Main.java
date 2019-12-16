package smartphone_manufacturing.supply_chain;

import jade.core.*;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

public class Main {
	
	public static void main(String[] args) {
		
		Profile myProfile = new ProfileImpl();
		Runtime myRuntime = Runtime.instance();
		
		try {
			ContainerController myContainer = myRuntime.createMainContainer(myProfile);	
			AgentController rma = myContainer.createNewAgent("rma", "jade.tools.rma.rma", null);
			rma.start();
			//start customer agents
			int customers = 3;
			AgentController customerAgent;
			for(int i=0; i < customers; i++) {
				customerAgent = myContainer.createNewAgent("customer-" + i , CustomerAgent.class.getCanonicalName(), null);
				customerAgent.start();
			}
			//start supplier agents
			AgentController supplierOne = myContainer.createNewAgent("supplier-1", SupplierAgent.class.getCanonicalName(), new Object[] {1});
			supplierOne.start();
			AgentController supplierTwo = myContainer.createNewAgent("supplier-2", SupplierAgent.class.getCanonicalName(), new Object[] {2});
			supplierTwo.start();
			//start manufacturer agent
			AgentController manufacturerAgent = myContainer.createNewAgent("manufacturer", ManufacturerAgent.class.getCanonicalName(), null);
			manufacturerAgent.start();
			//start ticker agent
			AgentController tickerAgent = myContainer.createNewAgent("ticker", TickerAgent.class.getCanonicalName(), null);
			tickerAgent.start();
			
		}catch(Exception e) {
			System.out.println("Exception starting agent: " + e.toString());
		}
	
	}

}
