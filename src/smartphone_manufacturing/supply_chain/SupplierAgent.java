package smartphone_manufacturing.supply_chain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
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
import smartphone_manufacturing.supply_chain_ontology.ManufacturingOntology;
import smartphone_manufacturing.supply_chain_ontology.actions.SellComponents;
import smartphone_manufacturing.supply_chain_ontology.actions.SendDetails;
import smartphone_manufacturing.supply_chain_ontology.concepts.ComponentsOrder;
import smartphone_manufacturing.supply_chain_ontology.concepts.PhoneComponent;
import smartphone_manufacturing.supply_chain_ontology.predicates.ComponentsInStock;
import smartphone_manufacturing.supply_chain_ontology.predicates.ComponentsSent;
import smartphone_manufacturing.supply_chain_ontology.predicates.SentSupplierDetails;

public class SupplierAgent extends Agent{
	private int day = 1;
	private AID tickerAgent;
	private AID manufacturerAgent;
	private Codec codec = new SLCodec();
	private Ontology ontology = ManufacturingOntology.getInstance();
	
	private ArrayList<ComponentsOrder> orders = new ArrayList<>();
	
	HashMap<PhoneComponent, Integer> phoneComponents;
	private int deliveryDays;
	private int receivedMoney;
	
	@Override
	protected void setup() {
		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(ontology);

		//add agent to yp
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("supply-agent");
		sd.setName(getLocalName() + "-supplier-agent");
		dfd.addServices(sd);
		
		try {
			DFService.register(this, dfd);
		}catch(FIPAException e) {
			e.printStackTrace();
		}
		Object[] args = getArguments();
		if (args != null && args.length > 0) {
			if((int)args[0] == 1) {
				System.out.println("SUP ARGS = 1!");
				phoneComponents = SupplierDetails.getSupplierOneComponents();
				deliveryDays = SupplierDetails.getSupplierOneDelivery();
			}else if((int)args[0] == 2) {
				System.out.println("SUP ARGS = 2!");
				phoneComponents = SupplierDetails.getSupplierTwoComponents();
				deliveryDays = SupplierDetails.getSupplierTwoDelivery();
			}
		}else {
			System.out.println("Add arguments to supplier!");
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
				if(msg.getContent().equals("new-day")) {
					 CyclicBehaviour sendDetails = new SendSupplierDetails(myAgent);
					 CyclicBehaviour respond = new RespondToRequests(myAgent);
					 CyclicBehaviour receive = new ReceiveOrders(myAgent);
					myAgent.addBehaviour(new FindManufacturer(myAgent));
					myAgent.addBehaviour(sendDetails);
					myAgent.addBehaviour(respond);
					myAgent.addBehaviour(receive);
					ArrayList<Behaviour> removeBehaviours = new ArrayList<>();
					removeBehaviours.add(sendDetails);
					removeBehaviours.add(respond);
					removeBehaviours.add(receive);
					myAgent.addBehaviour(new SendComponents(myAgent));
					myAgent.addBehaviour(new EndOfDay(myAgent, removeBehaviours));
				}
				else {
					//termination message to end simulation
					myAgent.doDelete();
					}
				}
			else {
				block(); //suspend this behaviour till a message is received
			}
		}
	}
	
	public class FindManufacturer extends OneShotBehaviour {
		
		public FindManufacturer(Agent a) {
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
		}
	}
	
	public class SendSupplierDetails extends CyclicBehaviour {
		
		public SendSupplierDetails(Agent a) {
			super(a);
		}
		
		@Override
		public void action() {
			ArrayList<PhoneComponent> components = new ArrayList<>();
			ArrayList<Long> prices = new ArrayList<>();
			MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST),MessageTemplate.MatchConversationId("request-supplier-details"));
			ACLMessage msg = myAgent.receive(mt);
			if(msg!= null) {
				try {
					ContentElement ce = null;
					ce = getContentManager().extractContent(msg);
					if (ce instanceof Action) {
						Concept action = ((Action)ce).getAction();
						if (action instanceof SendDetails) { 
							ACLMessage reply = msg.createReply();
							reply.setPerformative(ACLMessage.INFORM);
							//split hashmap into separate arrays
							for(Map.Entry<PhoneComponent, Integer> entry : phoneComponents.entrySet()) {
								PhoneComponent component = entry.getKey();
								int price = entry.getValue();
								components.add(component);
								prices.add((long) price);
							}
							SentSupplierDetails sendDetails = new SentSupplierDetails();
							sendDetails.setComponentPrices(prices);
							sendDetails.setPhoneComponents(components);
							sendDetails.setDevlieryDays(deliveryDays);
							sendDetails.setSupplier(myAgent.getAID());
							getContentManager().fillContent(reply, sendDetails);
							send(reply);
							
						}else {
							System.out.println("wrong type received by manufacture to " + myAgent.getAID());
						}
					}
					
				}catch(CodecException ce) {
					ce.printStackTrace();
				}catch(OntologyException oe) {
					oe.printStackTrace();
				}catch(Exception e) {
					e.printStackTrace();
				}
			}else {
				block();
			}
			
		}
		
	}
	
	public class RespondToRequests extends CyclicBehaviour {
		
		public RespondToRequests(Agent a) {
			super(a);
		}
		
		@Override
		public void action() {
			MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.QUERY_IF),MessageTemplate.MatchConversationId("sell-components-request"));
			ACLMessage msg = myAgent.receive(mt);
			if(msg!=null) {
				try {
					ContentElement ce = null;
					ce = getContentManager().extractContent(msg);
					if (ce instanceof ComponentsInStock) {
						ComponentsInStock request = (ComponentsInStock) ce;
						int quantity = request.getQuantity();
						ArrayList<PhoneComponent> components = request.getComponents();
						ACLMessage reply = msg.createReply();
						reply.setPerformative(ACLMessage.CONFIRM);
						reply.setConversationId("sell-components-response");
						myAgent.send(reply);
					}else {
						System.out.println("Wrong predicate type from manufacturer agent");
					}
				}catch(CodecException ce) {
					ce.printStackTrace();
				}catch (OntologyException oe) {
					oe.printStackTrace();
				}catch(Exception e) {
					e.printStackTrace();
				}
			}else {
				block();
			}
		}
	}
	
	public class ReceiveOrders extends CyclicBehaviour {
		
		public ReceiveOrders(Agent a) {
			super(a);
		}
		
		@Override
		public void action() {
			MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST),MessageTemplate.MatchConversationId("buy-components"));
			ACLMessage msg = receive(mt);
			if(msg != null) {
				try {
					ContentElement ce = null;
					ce = getContentManager().extractContent(msg);
					if (ce instanceof Action) {
						Concept action = ((Action) ce).getAction();
						if (action instanceof SellComponents) {
							SellComponents componentsOrder = (SellComponents) action;
							ComponentsOrder order = new ComponentsOrder();
							ArrayList<PhoneComponent> components = componentsOrder.getComponents();
							int quantity = componentsOrder.getQuantity();
							String orderID = componentsOrder.getOrderId();
							order.setBuyer(msg.getSender());
							order.setDelivery(day + deliveryDays);
							order.setComponents(components);
							order.setOrderID(orderID);
							order.setQuantity(quantity);
							orders.add(order);
							
						}
					}else {
						System.out.println("incorrect predicate recieved from manufacturer agent");
					}
					
				}catch(CodecException ce) {
					ce.printStackTrace();
				}catch (OntologyException oe) {
					oe.printStackTrace();
				}catch(Exception e) {
					e.printStackTrace();
				}
			}else {
				block();
			}
		}
	}
	
	public class SendComponents extends OneShotBehaviour{
		
		public SendComponents(Agent a) {
			super(a);
		}
		

		@Override
		public void action() {
			for(ComponentsOrder order: orders) {
				if (order.getDelivery() == day) {
					ComponentsSent sendComponents= new ComponentsSent();
					ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			        msg.setLanguage(codec.getName());
			        msg.setOntology(ontology.getName()); 
			        msg.addReceiver(order.getBuyer());
			        msg.setConversationId("sell-components-response");
			        sendComponents.setOrderID(order.getOrderID());
			        sendComponents.setPhoneComponents(order.getComponents());
			        sendComponents.setQty(order.getQuantity());
			        sendComponents.setSeller(myAgent.getAID());
			        try {
			        	 getContentManager().fillContent(msg, sendComponents);
			        	 send(msg);
			        }catch(CodecException ce) {
						ce.printStackTrace();
					}catch (OntologyException oe) {
						oe.printStackTrace();
					}catch(Exception e) {
						e.printStackTrace();
					}
			        
				}else {
					continue;
				}
			}
		}
	}
	
	public class EndOfDay extends CyclicBehaviour{
		
		private ArrayList<Behaviour> behaviours;
		public EndOfDay(Agent a, ArrayList<Behaviour> behaviours) {
			super(a);
			this.behaviours = behaviours;
		}
		
		@Override
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchContent("done");
			 ACLMessage msg = myAgent.receive(mt);
			 if(msg!=null) {
				 ACLMessage doneMsg = new ACLMessage(ACLMessage.INFORM);
				 doneMsg.setContent("done");
				 doneMsg.addReceiver(tickerAgent);
				 myAgent.send(doneMsg);
				 day++;
				 
				 for(Behaviour behaviour : behaviours) {
					 myAgent.removeBehaviour(behaviour);
				 }
				 myAgent.removeBehaviour(this);
			 }else {
				 block();
			 }
		}
	}
	
	

}
