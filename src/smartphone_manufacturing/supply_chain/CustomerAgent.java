package smartphone_manufacturing.supply_chain;
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
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import smartphone_manufacturing.supply_chain_ontology.ManufacturingOntology;
import smartphone_manufacturing.supply_chain_ontology.actions.ManufactureOrder;
import smartphone_manufacturing.supply_chain_ontology.concepts.SmartPhone;
import smartphone_manufacturing.supply_chain_ontology.concepts.SmallPhone;
import smartphone_manufacturing.supply_chain_ontology.concepts.Phablet;
import smartphone_manufacturing.supply_chain_ontology.concepts.CustomerOrder;
import smartphone_manufacturing.supply_chain_ontology.concepts.smartPhoneComponents.RAM;
import smartphone_manufacturing.supply_chain_ontology.concepts.smartPhoneComponents.Storage;
import smartphone_manufacturing.supply_chain_ontology.predicates.CanManufacture;

public class CustomerAgent extends Agent {
	
	private static final long serialVersionUID = 1L;
	private Codec codec = new SLCodec();
	private AID tickerAgent;
	//private int numQueriesSent;
	private AID manufacturerAgent;
	private int day = 1;
	private CustomerOrder todaysOrder;
	private ArrayList<CustomerOrder> requestedOrders = new ArrayList<>(); //accepted orders
	
	//get ontology
	private Ontology ontology = ManufacturingOntology.getInstance();
	
	@Override
	protected void setup() {
		// setup ontology
		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(ontology);
		
		//add this agent to yellow pages
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("customer-agent");
		sd.setName(getLocalName() + "-customer-agent");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		}
		catch(FIPAException e) {
			e.printStackTrace();
		}
		
		//add behaviour to sync agent with ticker agent and global day timing
		addBehaviour(new TickerWaitBehaviour(this));
		//add receive order cyclic behaviour
		//addBehaviour(new ReceiveOrder(this));
		
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
		private static final long serialVersionUID = 1L;

		//behaviour to wait for new day
		public TickerWaitBehaviour(Agent a) {
			super(a);
		}
		
		@Override
		public void action() {
			MessageTemplate mt = MessageTemplate.or(MessageTemplate.MatchContent("new-day"), MessageTemplate.MatchContent("terminate"));
			ACLMessage msg = myAgent.receive(mt);
			if(msg != null) {
				if(tickerAgent == null) {
					tickerAgent = msg.getSender();
				}
				if(msg.getContent().equals("new-day")) {
					//spawn new sequential behaviour for new day
					SequentialBehaviour dailyActivity = new SequentialBehaviour();
					//sub behaviours execute in the order that they are added
					//for example: "dailyActivity.addSubBehaviour(new FindSellers(myAgent));" etc.
					
					dailyActivity.addSubBehaviour(new generateNewOrder(myAgent));
					dailyActivity.addSubBehaviour(new requestManufacturer(myAgent));
					dailyActivity.addSubBehaviour(new sendOrderAction(myAgent));
					dailyActivity.addSubBehaviour(new EndOfDay(myAgent));
					
					myAgent.addBehaviour(dailyActivity);
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
	
	public class generateNewOrder extends OneShotBehaviour {
		
		private static final long serialVersionUID = 1L;

		public generateNewOrder(Agent a) {
			super(a);
		}
		
		@Override
		public void action() {
			
			todaysOrder = new CustomerOrder();
			SmartPhone phone;
			RAM ram;
			Storage storage;
			
			// random order specification generator
			Random rand = new Random();
			int orderQty = (int) Math.floor(1 + 50 * (rand.nextFloat()));
			int price = (int) ((int) orderQty * (Math.floor(100 + 500 * (rand.nextFloat()))));
			int dueDays = (int) Math.floor(1 + 10 * rand.nextFloat());
			int perDayFee = (int) (orderQty * (Math.floor(1 + 50 * (rand.nextFloat()))));
			
			if(rand.nextFloat() < 0.5) {
				//small phone
				phone = new SmallPhone();
			}
			else {
				//phablet
				phone = new Phablet();
			}
			if(rand.nextFloat()< 0.5) {
				ram = new RAM(4);
			}
			else {
				ram = new RAM(8);
			}
			if(rand.nextFloat()< 0.5) {
				storage = new Storage(64);
			}
			else {
				storage = new Storage(256);
			}
			
			//set phone to have generated components
			phone.setRAM(ram);
			phone.setStorage(storage);
			
			//set order to have generated phone and random fees
			todaysOrder.setSmartPhone(phone);
			todaysOrder.setPrice(price);
			todaysOrder.setDaysToDeadline(dueDays);
			todaysOrder.setQuantity(orderQty);
			todaysOrder.setPerDayPenalty(perDayFee);
			System.out.println(todaysOrder.toString());
			
		}
	}
	
	public class requestManufacturer extends OneShotBehaviour {

		private static final long serialVersionUID = 1L;

		public requestManufacturer(Agent a) {
			super(a);
		}
		
		@Override
		public void action() {
			//find manufacturer
			DFAgentDescription manufacturerTemplate = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setType("manufacturer-agent");
			manufacturerTemplate.addServices(sd);
			try {
				DFAgentDescription[] manufacturerList  = DFService.search(myAgent, manufacturerTemplate);
				if(manufacturerList.length >0) {
					manufacturerAgent = manufacturerList[0].getName(); //gets AID of manufacturer
				}
				
			}catch(FIPAException e) {
				e.printStackTrace();
			}
			//prepare query
			ACLMessage msg = new ACLMessage(ACLMessage.QUERY_IF);
			msg.setLanguage(codec.getName());
			msg.setOntology(ontology.getName());
			msg.setConversationId("customer-order-request");
			msg.addReceiver(manufacturerAgent);
			//use ontology to query if manufacturer can make the order or not
			CanManufacture canManufacture = new CanManufacture();
			canManufacture.setManufacturer(manufacturerAgent);
			canManufacture.setOrder(todaysOrder);
			
			//send query
			try {
				getContentManager().fillContent(msg, canManufacture);
				send(msg);
			}catch(CodecException ce) {
				ce.printStackTrace();
			}catch(OntologyException oe) {
				oe.printStackTrace();
			}
		}
	}
	
	public class sendOrderAction extends Behaviour {
		private static final long serialVersionUID = 1L;

		public sendOrderAction(Agent a) {
			super(a);
		}
		
		private Boolean responseReceived = false;
		
		@Override
		public void action() {
			//check conversation ID is a response to order request and is either a confirm or disconfirm 
			MessageTemplate mt = MessageTemplate.and((MessageTemplate.MatchConversationId("customer-order-response")),(MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.CONFIRM), MessageTemplate.MatchPerformative(ACLMessage.DISCONFIRM))));
			
			ACLMessage msg = myAgent.receive(mt);
			if(msg != null) {
				responseReceived = true;
				if(msg.getPerformative() == ACLMessage.CONFIRM) {
					//prepare message
					ACLMessage sendOrderMsg = new ACLMessage(ACLMessage.REQUEST);
					sendOrderMsg.setConversationId("customer-order-sent");
					sendOrderMsg.setLanguage(codec.getName());
					sendOrderMsg.setOntology(ontology.getName());
					sendOrderMsg.addReceiver(manufacturerAgent);
					
					//use ontology to request 'manufactureOrder' action
					ManufactureOrder manufactureOrder = new ManufactureOrder();
					Action action = new Action();
					manufactureOrder.setOrder(todaysOrder);
					manufactureOrder.setBuyer(myAgent.getAID());
					action.setAction(manufactureOrder);
					action.setActor(manufacturerAgent);
					
					//send action request
					try {
						requestedOrders.add(todaysOrder);
						getContentManager().fillContent(sendOrderMsg, action);
						send(sendOrderMsg);
					}catch(CodecException ce) {
						ce.printStackTrace();
					}catch(OntologyException oe) {
						oe.printStackTrace();
					}
					
				}
				//order rejected
				else {
					System.out.println("\n DENIED ORDER: Customer " + myAgent.getLocalName());
				}
			}
			//reply not received
			else {
				block();
			}
			
		}
		
		@Override
		public boolean done() {
	      return responseReceived;
	    }
	}
	
	public class EndOfDay extends OneShotBehaviour {

		private static final long serialVersionUID = 1L;

		public EndOfDay(Agent a) {
			super(a);
		}
		
		@Override
		public void action() {
			ACLMessage doneMsg = new ACLMessage(ACLMessage.INFORM);
			doneMsg.setContent("done");
			doneMsg.addReceiver(tickerAgent);
			myAgent.send(doneMsg);
			day++;
		}
	}

}
