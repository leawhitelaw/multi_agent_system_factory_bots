package smartphone_manufacturing.supply_chain;

import java.util.ArrayList;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
/*import set10111.simulation.SellerAgent.EndDayListener;
import set10111.simulation.SellerAgent.OffersServer;
import set10111.simulation.SellerAgent.TickerWaiter.BookGenerator;
import set10111.simulation.SellerAgent.TickerWaiter.FindBuyers;*/

public class SupplierAgent extends Agent{
	private int day = 0;
	private AID tickerAgent;
	
	@Override
	protected void setup() {
		//add agent to yellow pages
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("supply-agent");
		sd.setName(getLocalName() + "-supply-agent");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		}
		catch(FIPAException e) {
			e.printStackTrace();
		}
		addBehaviour(new TickerWaitBehaviour(this));
	}
	
	@Override
	protected void takeDown() {
		//Deregister from the yellow pages
		try {
			DFService.deregister(this);
		}
		catch(FIPAException e) {
			e.printStackTrace();
		}
	}
	
	public class TickerWaitBehaviour extends CyclicBehaviour {
		public TickerWaitBehaviour(Agent a) {
			super(a);
		}
		
		@Override
		public void action() {
			//wait for new day 
			MessageTemplate mt = MessageTemplate.or(MessageTemplate.MatchContent("new-day"),
					MessageTemplate.MatchContent("terminate"));
			ACLMessage msg = myAgent.receive(mt);
			if(msg != null) {
				if(tickerAgent == null) {
					tickerAgent = msg.getSender(); //AID of ticker agent
				}
				//do computation here
				if(msg.getContent().equals("new day")) {
					//myAgent.addBehaviour(new BookGenerator());
					//myAgent.addBehaviour(new FindBuyers(myAgent));
					//CyclicBehaviour os = new OffersServer(myAgent);
					//myAgent.addBehaviour(os);
					//ArrayList<Behaviour> cyclicBehaviours = new ArrayList<>();
					//cyclicBehaviours.add(os);
					//myAgent.addBehaviour(new EndDayListener(myAgent,cyclicBehaviours));
				}
				else {
					//termination message to end simulation
					myAgent.doDelete();
					}
				day++;
				System.out.println(getLocalName() + "day: " + day);
				addBehaviour(new WakerBehaviour(myAgent,5000){
					protected void onWake() {
						//send a done message
						ACLMessage dayDone = new ACLMessage(ACLMessage.INFORM);
						dayDone.addReceiver(tickerAgent);
						dayDone.setContent("done");
						myAgent.send(dayDone);
					}
				});
				
			}
			else {
				block(); //suspend this behaviour till a message is received
			}
		}
	}

}
