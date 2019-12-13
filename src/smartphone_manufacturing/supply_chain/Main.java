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
			
			AgentController customerAgent = myContainer.createNewAgent("customer" , CustomerAgent.class.getCanonicalName(), null);
			customerAgent.start();
			
			AgentController tickerAgent = myContainer.createNewAgent("ticker", TickerAgent.class.getCanonicalName(), null);
			tickerAgent.start();
			
		}catch(Exception e) {
			System.out.println("Exception starting agent: " + e.toString());
		}
	
	}

}
