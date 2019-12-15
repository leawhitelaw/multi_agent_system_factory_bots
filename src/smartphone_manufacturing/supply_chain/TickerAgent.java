package smartphone_manufacturing.supply_chain;

import java.util.ArrayList;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/*
 * Ticker agent syncs global clock, tells agents when it is a new day
 * */

public class TickerAgent extends Agent {
	public static final int num_days = 100;
	
	@Override
	protected void setup() {
		//add ticker agent to yellow pages
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("ticker-agent");
		sd.setName(getLocalName() + "-ticker-agent");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		}
		catch(FIPAException e){
			e.printStackTrace();
		}
		
		//wait for all other agents to start
		doWait(5000);
		addBehaviour(new SynchAgentsBehaviour(this));
	}
	
	@Override
	protected void takeDown() {
		//Deregister from yellow pages
		try {
			DFService.deregister(this);
		}
		catch(FIPAException e) {
			e.printStackTrace();
		}
	}
	
	public class SynchAgentsBehaviour extends Behaviour{
		
		private static final long serialVersionUID = 1L;
		private int step = 0;
		private int numReceivedMsgs = 0; //no of finished responses from other agents
		private int day = 0;
		private ArrayList<AID> simulationAgents = new ArrayList<>();
		
		public SynchAgentsBehaviour(Agent a) {
			super(a);
		}
		
		@Override
		public void action() {
			switch(step) {
			case 0:
				//find all agents (customers and suppliers
				DFAgentDescription supplyTemplate = new DFAgentDescription();
				ServiceDescription supplySd = new ServiceDescription();
				supplySd.setType("supply-agent");
				supplyTemplate.addServices(supplySd);
				DFAgentDescription customerTemplate = new DFAgentDescription();
				ServiceDescription customerSd = new ServiceDescription();
				customerSd.setType("customer-agent");
				customerTemplate.addServices(customerSd);
				DFAgentDescription manufacturerTemplate = new DFAgentDescription();
				ServiceDescription manufacturerSd = new ServiceDescription();
				manufacturerSd.setType("manufacturer-agent");
				manufacturerTemplate.addServices(manufacturerSd);
				try {
					simulationAgents.clear();
					//search for supply agents
					DFAgentDescription[] supplyAgents = DFService.search(myAgent, supplyTemplate);
					for(int i=0; i<supplyAgents.length; i++) {
						simulationAgents.add(supplyAgents[i].getName()); //this is the supply agents AID
					}
					//search for customer agents
					DFAgentDescription[] customerAgents = DFService.search(myAgent, customerTemplate);
					for(int i=0; i<customerAgents.length; i++) {
						simulationAgents.add(customerAgents[i].getName()); //this is the customer agents AID
					}
					//search for manufacturer agents
					DFAgentDescription[] manufacturerAgents = DFService.search(myAgent, manufacturerTemplate);
					for(int i=0; i<manufacturerAgents.length; i++) {
						simulationAgents.add(manufacturerAgents[i].getName()); //this is the manufacturer agents AID
					}
				}
				catch(FIPAException e) {
					e.printStackTrace();
				}
				//send new day message to each agent
				ACLMessage newDayTick = new ACLMessage(ACLMessage.INFORM);
				newDayTick.setContent("new-day");
				for(AID id: simulationAgents) {
					newDayTick.addReceiver(id);
				}
				myAgent.send(newDayTick);
				step++;
				day++;
				break;
			case 1:
				//wait to receive 'done' messages from all agents 
				MessageTemplate mt = MessageTemplate.MatchContent("done");
				ACLMessage msg = myAgent.receive(mt);
				if(msg != null) {
					numReceivedMsgs++;
					if(numReceivedMsgs >= simulationAgents.size()) {
						step++;
					}
				}
				else {
					block();
				}
			}
		}
		
		@Override
		public boolean done() {
			return step == 2;
		}
		
		@Override
		public void reset() {
			step = 0;
			//simulationAgents.clear();
			numReceivedMsgs = 0;
		}
		
		@Override
		public int onEnd() {
			System.out.println("End-of-day " + day + "!");
			//send termination to agents if end of days and terminate
			if(day == num_days) {
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.setContent("terminate");
				for(AID agent : simulationAgents) {
					msg.addReceiver(agent);
				}
				myAgent.send(msg);
				myAgent.doDelete();
			}
			else {
				reset();
				myAgent.addBehaviour(this);
			}
			return 0;
		}
	}

}
