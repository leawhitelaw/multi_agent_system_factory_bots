package smartphone_manufacturing.supply_chain;

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

import java.util.HashMap;
import java.util.ArrayList;

import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import smartphone_manufacturing.supply_chain_ontology.ManufacturingOntology;
import smartphone_manufacturing.supply_chain_ontology.actions.ManufactureOrder;
import smartphone_manufacturing.supply_chain_ontology.actions.SendDetails;
import smartphone_manufacturing.supply_chain_ontology.concepts.SmartPhone;
import smartphone_manufacturing.supply_chain_ontology.concepts.SmallPhone;
import smartphone_manufacturing.supply_chain_ontology.concepts.Phablet;
import smartphone_manufacturing.supply_chain_ontology.concepts.CustomerOrder;
import smartphone_manufacturing.supply_chain_ontology.concepts.PhoneComponent;
import smartphone_manufacturing.supply_chain_ontology.concepts.smartPhoneComponents.RAM;
import smartphone_manufacturing.supply_chain_ontology.concepts.smartPhoneComponents.Storage;
import smartphone_manufacturing.supply_chain_ontology.predicates.CanManufacture;
import smartphone_manufacturing.supply_chain_ontology.predicates.SentSupplierDetails;

public class ManufacturerAgent extends Agent {
	
	private Codec codec = new SLCodec();
	private AID tickerAgent;
	//private int numQueriesSent;
	private AID customerAgent;
	private int day = 1;
	//private ArrayList<CustomerOrder> requestedOrders = new ArrayList<>(); //accepted orders
	private ArrayList<AID> customers = new ArrayList<>();
	private HashMap<PhoneComponent, Integer> warehouse = new HashMap<>(); // components and their qty in warehouse
	private HashMap<AID, SupplierType> suppliers = new HashMap<>();
	
	
	//keep track of daily variable outcomes
	private int ordersShipped = 0;
	private int latePenalty = 0;
	private int storageCost = 0;
	private int costOfSupplier = 0;
	private int totalProfit = 0;
	
	//get ontology
	private Ontology ontology = ManufacturingOntology.getInstance();
	
	@Override protected void setup() {
		//set up ontology
		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(ontology);
		
		//add agent to yp
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("manufacturer-agent");
		sd.setName(getLocalName() + "-manufacturer-agent");
		dfd.addServices(sd);
		
		try {
			DFService.register(this, dfd);
		}catch(FIPAException e) {
			e.printStackTrace();
		}
		
		//add behaviour to sync agent with ticker agent and global day timing
		addBehaviour(new TickerWaitBehaviour(this));
	}
	
	@Override
	protected void takeDown() {
		//deregisted from yp
		try {
			DFService.deregister(this);
		}catch(FIPAException e) {
			e.printStackTrace();
		}
	}
	
	public class TickerWaitBehaviour extends CyclicBehaviour {
		
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
					SequentialBehaviour dailyActivity = new SequentialBehaviour();
					
					myAgent.addBehaviour(dailyActivity);
				} else {
					//termination msg 
					myAgent.doDelete();
				}
			}else {
				block();
			}
		}
	}
	
	public class GetSuppliers extends OneShotBehaviour{
		
		public GetSuppliers(Agent a) {
			super(a);
		}
		
		@Override
		public void action() {
			DFAgentDescription supplierTemplate = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setType("supplier-agent");
			supplierTemplate.addServices(sd);
			
			try {
				suppliers.clear();
				DFAgentDescription[] supplierAgents = DFService.search(myAgent, supplierTemplate);
				
				for(int i=0; i<supplierAgents.length; i++) {
					AID supplier = supplierAgents[i].getName();
					suppliers.put(supplier, new SupplierType(supplier));
				}
			}catch(FIPAException e) {
				e.printStackTrace();
			}
		}
	}
	
	//get prices from supplier pricing claass on day 1
	public class GetSupplierDetails extends Behaviour {
		
		public GetSupplierDetails(Agent a) {
			super(a);
		}
		
		MessageTemplate mt;
		private int step = 0;
		private int received = 0;
		
		@Override
		public void action() {
			switch (step) {
			case 0:
			ACLMessage supplierMsg = new ACLMessage(ACLMessage.REQUEST);
			supplierMsg.setLanguage(codec.getName());
			supplierMsg.setOntology(ontology.getName()); 
			supplierMsg.setConversationId("supplier-details");
			
			SendDetails sendSupplierDetails = new SendDetails();
			sendSupplierDetails.setBuyer(myAgent.getAID());
			
			Action request = new Action();
			request.setAction(sendSupplierDetails);
			
			try {
				for(AID supplier : suppliers.keySet()) {
					supplierMsg.addReceiver(supplier);
					request.setActor(supplier);
					getContentManager().fillContent(supplierMsg, request);
					send(supplierMsg);
					supplierMsg.removeReceiver(supplier);
				}
				step ++;
			}catch(CodecException ce) {
				ce.printStackTrace();
			}catch(OntologyException oe) {
				oe.printStackTrace();
			}
			break;
			
			case 1:
				mt = MessageTemplate.and((MessageTemplate.MatchPerformative(ACLMessage.INFORM)), (MessageTemplate.MatchConversationId("supplier-details")));
				ACLMessage suppMsg = receive(mt);
				
				if(suppMsg != null) {
					try {
						ContentElement ce = null;
						ce = getContentManager().extractContent(suppMsg);
						
						if(ce instanceof SentSupplierDetails) {
							SentSupplierDetails supplierDetails = (SentSupplierDetails) ce;
							
							HashMap<PhoneComponent, Integer> prices = new HashMap<>();
							ArrayList<PhoneComponent> Components = supplierDetails.getPhoneComponents();
							ArrayList<Integer> ComponentPrices = supplierDetails.getComponentPrices();
							
							for(int i=0; i<Components.size(); i++) {
								PhoneComponent component = Components.get(i);
								Integer price = ComponentPrices.get(i);
								prices.put(component, price);
							}
							
							AID supplier = supplierDetails.getSupplier();
							int deilveryDays = supplierDetails.getDevlieryDays();
							suppliers.get(supplier).setPrices(prices);
							suppliers.get(supplier).setDelivery(deilveryDays);
							received ++;
							
						}else {
							System.out.println("Agent: " + myAgent.getAID() + "Received wrong msg from supplier");
						}
					}catch(CodecException ce) {
						ce.printStackTrace();
					}catch(OntologyException oe) {
						oe.printStackTrace();
					}
						
				}else {
					block();
				}
			}
		}
	
		@Override
		public boolean done() {
			return received == suppliers.size();
		}
	}//end of behaviour
	
	public class GetCustomers extends OneShotBehaviour{
		
		public GetCustomers(Agent a) {
			super(a);
		}
		
		@Override
		public void action() {
			DFAgentDescription customerTemplate = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setType("customer-agent");
			customerTemplate.addServices(sd);
			
			try {
				customers.clear();
				DFAgentDescription[] customerAgents = DFService.search(myAgent, customerTemplate);
				
				for(int i=0; i<customerAgents.length; i++) {
					customers.add(customerAgents[i].getName());
				}
			}catch(FIPAException e) {
				e.printStackTrace();
			}
		}	
	}
	
	public class SelectCustomerOrders extends OneShotBehaviour{
		
		public SelectCustomerOrders(Agent a) {
			super(a);
		}
		private CustomerOrderStatus orderStatus;
		private int replies = 0;
		
		@Override action(){
			MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.QUERY_IF), MessageTemplate.MatchConversationId("customer-order-request"));
			ACLMessage msg = receive(mt);
			
			if(msg != null) {
				try {
					ContentElement ce = null;
					ce = getContentManager().extractContent(msg);
					
					if(ce instanceof CanManufacture) {
						CanManufacture manufactureRequest = (CanManufacture) ce;
						CustomerOrder order = manufactureRequest.getOrder();
						orderStatus = new CustomerOrderStatus(order);
						orderStatus.setCustomer(msg.getSender());
						
						orderStatus.setA
						
					}
				}
			}
		}
		
		
	}
}
