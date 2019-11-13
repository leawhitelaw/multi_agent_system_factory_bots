package multi_agent_systems_coursework.supply_chain;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Random;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class CustomerAgent extends Agent {
	private AID tickerAgent;
	private int numQueriesSent;
	
	@Override
	protected void setup() {
		//add this agent to yellow pages
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("customer-agent");
		sd.setName(getLocalName() + "-supply-agent");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		}
		catch(FIPAException e) {
			e.printStackTrace();
		}
		//add books to buy
		addBehaviour(new TickerWaitBehaviour(this));
	}
	
	@Override
	protected void takeDown() {
		//deregister from the yellow pages
		try {
			DFService.deregister(this);
		}
		catch(FIPAException e) {
			e.printStackTrace();
		}
	}
	
	public class TickerWaitBehaviour extends CyclicBehaviour{
		//behaviour to wait for new day
		public TickerWaitBehaviour(Agent a) {
			super(a);
		}
		
		@Override
		public void action() {
			MessageTemplate mt = MessageTemplate.or(MessageTemplate.MatchContent("new-day"),
					MessageTemplate.MatchContent("terminate"));
			ACLMessage msg = myAgent.receive(mt);
			if(msg != null) {
				if(tickerAgent == null) {
					tickerAgent = msg.getSender();
				}
				if(msg.getContent().equals("new-day")) {
					//spawn new sequential behaviour for new day
					SequentialBehaviour dailyActivity = new SequentialBehaviour();
					//sub behaviours execute in the order that they are added
					//dailyActivity.addSubBehaviour(new FindSellers(myAgent)); etc.
					//myAgent.addBehaviour(dailyActivity);
				}
				else {
					//termination message
					myAgent.doDelete();
				}
			}
			else {
				block();
			}
		}
	}

}
