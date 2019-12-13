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

import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import smartphone_manufacturing.supply_chain_ontology.ManufacturingOntology;
import smartphone_manufacturing.supply_chain_ontology.actions.ManufactureOrder;
import smartphone_manufacturing.supply_chain_ontology.actions.SellComponents;
import smartphone_manufacturing.supply_chain_ontology.actions.SendDetails;
import smartphone_manufacturing.supply_chain_ontology.concepts.SmartPhone;
import smartphone_manufacturing.supply_chain_ontology.concepts.SmallPhone;
import smartphone_manufacturing.supply_chain_ontology.concepts.Phablet;
import smartphone_manufacturing.supply_chain_ontology.concepts.CustomerOrder;
import smartphone_manufacturing.supply_chain_ontology.concepts.PhoneComponent;
import smartphone_manufacturing.supply_chain_ontology.concepts.smartPhoneComponents.RAM;
import smartphone_manufacturing.supply_chain_ontology.concepts.smartPhoneComponents.Storage;
import smartphone_manufacturing.supply_chain_ontology.predicates.CanManufacture;
import smartphone_manufacturing.supply_chain_ontology.predicates.ComponentsSent;
import smartphone_manufacturing.supply_chain_ontology.predicates.HasComponents;
import smartphone_manufacturing.supply_chain_ontology.predicates.OrderShipped;
import smartphone_manufacturing.supply_chain_ontology.predicates.PaymentSent;
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
	private ArrayList<CustomerOrderStatus> orderList = new ArrayList<>();
	
	
	//keep track of daily variable outcomes
	private int orderID = 0;
	private int todaysProfit = 0;
	private int latePenalty = 0;
	private int storageCost = 0;
	private int costOfSupplies = 0;
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
	
	//decide which orders to accept or reject
	public class SelectCustomerOrders extends Behaviour{
		
		public SelectCustomerOrders(Agent a) {
			super(a);
		}
		private CustomerOrderStatus orderStatus;
		private int replies = 0;
		
		@Override 
		public void action(){
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
						
						AID quickestSupplier = null;
						SupplierType currentSupplier = null;
						int profit = 0;
						int cost = 0; //cost of order by calculating component prices from quickest supplier
						int lowestDelivery = 10; //this is higher than both suppliers delivery days
						
						//part of strategy - use quickest supplier
						for (SupplierType supplier : suppliers.values()) {
							int deliveryDays = supplier.getDelivery();
							if(deliveryDays < lowestDelivery) {
								quickestSupplier = supplier.getSupplier();
								currentSupplier = supplier;
								lowestDelivery = deliveryDays;
							}
							
						}
						for(PhoneComponent component : orderStatus.getOrder().getSmartPhone().getPhoneComponents()) {
							cost = cost + currentSupplier.getPrices().get(component);
						}
						cost = cost*(orderStatus.getOrder().getQuantity());
						profit = (int) ((orderStatus.getOrder().getPrice()) - cost);
						
						//set Max profit and decide if profit is close to that
						
						ACLMessage reply = msg.createReply();
						if(profit > 0) {
							orderID ++;
							orderStatus.getOrder().setOrderID(orderID);
							orderStatus.setOrderStatus("APPROVED");
							orderStatus.setSupplier(quickestSupplier);
							orderStatus.setComponentDeliveryDate(lowestDelivery);
							orderStatus.setPrice(cost);
							orderStatus.setDayOrdered(day);
							orderList.add(orderStatus);
							reply.setPerformative(ACLMessage.CONFIRM);
						}else {
							reply.setPerformative(ACLMessage.DISCONFIRM);
						}
						reply.setConversationId("customer-order-response");
						myAgent.send(reply);
						replies++;
					}else {
						System.out.println("Agent: " + myAgent.getAID() + "Received wrong msg from customer");
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
		
		@Override
		public boolean done() {
			return replies == customers.size();
		}	
	}
	
	//requesting and ordering components in one behaviour as they apply to same order
	public class OrderComponents extends Behaviour {
		
		public OrderComponents(Agent a) {
			super(a);
		}
		
		private int step = 0;
		private CustomerOrderStatus orderStatus;
		private AID supplier;
		
		@Override
		public void action() {
			switch(step) {
			case 0:
				MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST), MessageTemplate.MatchConversationId("customer-order-sent"));
				ACLMessage customerMsg = receive(mt);
				
				if(customerMsg != null) {
					try {
						ContentElement ce = null;
						ce = getContentManager().extractContent(customerMsg);
						
						if(ce instanceof Action) {
							Concept action = ((Action)ce).getAction();
							if(action instanceof ManufactureOrder) {
								ManufactureOrder manufactureOrder = (ManufactureOrder)action;
								ArrayList<CustomerOrderStatus> approved = new ArrayList<>();
								for(CustomerOrderStatus status : orderList) {
									if(status.getOrder() == manufactureOrder.getOrder() && status.getOrderStatus() == "APPROVED") {
										approved.add(status);
									}
								}
								orderStatus = approved.get(0);
								
								if(orderStatus != null) {
									orderStatus.setOrderStatus("CONFIRMED");
									step ++;
								}else {
									step = 0;
								}
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
				break;
				
				//if step = 1 then components are to be ordered otherwise there are no approved orders
			case 1:
				supplier = orderStatus.getSupplier();
				HasComponents hasComponents = new HasComponents(); //new request for components
				
				ACLMessage askSupplierMsg = new ACLMessage(ACLMessage.QUERY_IF);
				askSupplierMsg.setLanguage(codec.getName());
				askSupplierMsg.setOntology(ontology.getName());
				askSupplierMsg.setConversationId("sell-components-request");
				askSupplierMsg.addReceiver(supplier);
				hasComponents.setSupplier(supplier);
				hasComponents.setQuantity(orderStatus.getOrder().getQuantity());
				try {
					hasComponents.setComponents(orderStatus.getOrder().getSmartPhone().getPhoneComponents());
					getContentManager().fillContent(askSupplierMsg, hasComponents);
					send(askSupplierMsg);
					step++;
				}catch(CodecException ce) {
					ce.printStackTrace();
				}catch(OntologyException oe) {
					oe.printStackTrace();
				}catch(Exception e) {
					e.printStackTrace();
				}
			break;
			
			case 2:
				MessageTemplate responseMt = MessageTemplate.and(
						MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.CONFIRM), MessageTemplate.MatchPerformative(ACLMessage.DISCONFIRM)), MessageTemplate.MatchConversationId("sell-components-response"));
				ACLMessage response = receive(responseMt);
				if(response != null) {
					//if confirmed then do following
					if (response.getPerformative() == ACLMessage.CONFIRM) {
						ACLMessage orderReq = new ACLMessage(ACLMessage.REQUEST);
						orderReq.setOntology(ontology.getName());
						orderReq.setLanguage(codec.getName());
						orderReq.setConversationId("buy-components");
						orderReq.addReceiver(supplier);
						try {
							SellComponents sellComps = new SellComponents();
							sellComps.setBuyer(myAgent.getAID());
							sellComps.setComponents(orderStatus.getOrder().getSmartPhone().getPhoneComponents());
							sellComps.setQuantity(orderStatus.getOrder().getQuantity());
							sellComps.setOrderId(orderID);
							Action request = new Action();
							request.setAction(sellComps);
							request.setActor(response.getSender());
							
							getContentManager().fillContent(orderReq, request);
							send(orderReq);
							orderStatus.setComponentDeliveryDate(day + suppliers.get(supplier).getDelivery());
							step++;
							
						}catch(CodecException ce) {
							ce.printStackTrace();
						}catch(OntologyException oe) {
							oe.printStackTrace();
						}catch(Exception e) {
							e.printStackTrace();
						}
					}else {
						// if not confirmed then set order status to delete
						orderStatus.setOrderStatus("DELETE");
						step = 0;
					}
				}else {
					block();
				}
			
				
			break;
			
			case 3:
				//order has been  send for components 
				int supplyCost = orderStatus.getPrice();
				ACLMessage sendPayment = new ACLMessage(ACLMessage.INFORM);
				sendPayment.setOntology(ontology.getName());
				sendPayment.setLanguage(codec.getName());
				sendPayment.setConversationId("pay-components");
				sendPayment.addReceiver(supplier);
				PaymentSent payment = new PaymentSent();
				payment.setBuyer(myAgent.getAID());
				payment.setPrice(supplyCost);
				try {
					getContentManager().fillContent(sendPayment, payment);
					send(sendPayment);
					costOfSupplies = costOfSupplies + supplyCost;
				}catch(CodecException ce) {
					ce.printStackTrace();
				}catch(OntologyException oe) {
					oe.printStackTrace();
				}catch(Exception e) {
					e.printStackTrace();
				}
				break;
			}	
				
		}
		
		@Override
		public boolean done() {
			ArrayList<CustomerOrderStatus> approvedList = new ArrayList<>();
			//check to see that all 'approved' orders have been tended to
			for(CustomerOrderStatus status : orderList) {
				if(status.getOrderStatus() == "APPROVED") {
					approvedList.add(status);
				}
			}
			return approvedList.size() == 0 && step == 0; 
		}	
	} // end of order components behaviour
	
	public class ReceiveSupplies extends Behaviour {
		
		public ReceiveSupplies(Agent a) {
			super(a);
			//put inside here as it cannot be in behaviour outside of action
			for(CustomerOrderStatus status : orderList) {
				if(status.getComponentDeliveryDate() == day) {
					toReceive.add(status);
				}
			}
		}
		private int suppliesReceived = 0;
		private ArrayList<CustomerOrderStatus> toReceive;
		
		@Override 
		public void action() {
			 MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM), MessageTemplate.MatchConversationId("sell-components-response"));
			 ACLMessage receiveMsg = receive(mt);
			 if(receiveMsg != null) {
				 int quantity;
				 int orderID;
				 
				 try {
					 ContentElement ce = null; 
					 ce = getContentManager().extractContent(receiveMsg);
					 if (ce instanceof ComponentsSent) {
						 ComponentsSent componentsSent = (ComponentsSent) ce;
						 ArrayList<PhoneComponent> phoneComponents = componentsSent.getPhoneComponents();
						 quantity = componentsSent.getQty();
						 orderID = componentsSent.getOrderID();
						 
						 //add components to warehouse components
						 for(PhoneComponent component : phoneComponents) {
							 if(warehouse.get(component) == null) {
								 warehouse.put(component, quantity);
							 }else {
								 int currentQuantity = warehouse.get(component);
								 warehouse.put(component, currentQuantity + quantity);
							 }
						 }
						 for(CustomerOrderStatus status : orderList) {
							 if(status.getOrder().getOrderID() == orderID) {
								 status.setOrderStatus("READY");
							 }
						 }
						 suppliesReceived ++;
						
					 }else {
						 System.out.println("Agent: " + myAgent.getAID() + "Received wrong msg from supplier");
					 }
				 }catch(CodecException ce) {
						ce.printStackTrace();
				}catch(OntologyException oe) {
					oe.printStackTrace();
				}catch(Exception e) {
					e.printStackTrace();
				}
			 } else if (toReceive.size() > 0) {
				 block();
			 }
		}
		
		@Override
		public boolean done() {
			return suppliesReceived == toReceive.size();
		}
	} // end of receive supplies bejaviour
	
	public class MakeOrder extends Behaviour {
		
		public MakeOrder(Agent a) {
			super(a);
		}
		
		private int step = 0;
		private int awaitingPayment = 0;
		
		@Override
		public void action() {
			switch(step) {
			case 0:
				for(CustomerOrderStatus status: orderList) {
					ArrayList<PhoneComponent> phoneComponents = status.getOrder().getSmartPhone().getPhoneComponents();
					int quantity = status.getOrder().getQuantity();
					if(status.getOrderStatus() == "READY") {
						boolean warehouseHasComponents = true;
						for(PhoneComponent component : phoneComponents) {
							if(!warehouse.containsKey(component) || (warehouse.containsKey(component) 
																	&& warehouse.get(component) < quantity)){
								warehouseHasComponents = false;
								break; //stop above being overwritten
							}
						}
						
						if(warehouseHasComponents == false) {
							continue;
						}
						else {
							//assemble and send order to customer
							OrderShipped sendOrder = new OrderShipped();
							ACLMessage sendMsg = new ACLMessage(ACLMessage.INFORM);
							sendMsg.setOntology(ontology.getName());
							sendMsg.setLanguage(codec.getName());
							sendMsg.addReceiver(status.getCustomer());
							sendMsg.setConversationId("send-phones-to-customer");
							sendOrder.setManufacturer(myAgent.getAID());
							sendOrder.setOrder(status.getOrder());
							try {
								//remove components from warehouse (assemble)
								for(PhoneComponent component : phoneComponents) {
									int currQty = warehouse.get(component);
									warehouse.put(component, (currQty - quantity));
								}
								//ship order
								getContentManager().fillContent(sendMsg, sendOrder);
								send(sendMsg);
								status.setOrderStatus("SENT_ORDER");
								awaitingPayment++;
							}catch(CodecException ce) {
								ce.printStackTrace();
							}catch(OntologyException oe) {
								oe.printStackTrace();
							}catch(Exception e) {
								e.printStackTrace();
							}
						}
					}else {
						continue; //order not ready
					}
				}
				
				step++;
				break;
			
				//receive payment from customer
			case 1:
				MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM), MessageTemplate.MatchConversationId("order-payment"));
				ACLMessage payMsg = receive(mt);
				if(payMsg != null) {
					try {
						ContentElement ce = null;
						ce = getContentManager().extractContent(payMsg);
						
						if (ce instanceof PaymentSent) {
							PaymentSent payment = (PaymentSent) ce;
							for(CustomerOrderStatus status : orderList) {
								if(status.getOrder().getOrderID() == payment.getOrderID()) {
									status.setOrderStatus("COMPLETED");
								}
							}
							todaysProfit += payment.getPrice();
							awaitingPayment --;
						}else {
							System.out.println("Agent: " + myAgent.getAID() + "Received wrong msg from customer");
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
				break;
			}
		}
		
		@Override 
		public boolean done() {
			return awaitingPayment == 0;
		}
	}
	
	public class CalculateDailyTotals extends OneShotBehaviour{
		public CalculateDailyTotals(Agent a) {
			super(a);
		}
		@Override
		public void action() {
			//calculate warehouse totals
			for(PhoneComponent component : warehouse.keySet()) {
				int cost = warehouse.get(component)*5;
				storageCost += cost;
			}
			
			//calculate late fees !!!!!! check
			for(CustomerOrderStatus status : orderList) {
				int deadlineDay = status.getDayOrdered() + status.getOrder().getDaysToDeadline();
				if(day >= deadlineDay) {
					int daysToPayFor = day - deadlineDay;
					int cost = daysToPayFor * status.getOrder().getPerDayPenalty();
					latePenalty +=cost;
				}
				
			}
			//calculateTotalProfit
			totalProfit = totalProfit + todaysProfit - storageCost - latePenalty - costOfSupplies;
			
			//remove finished order from current order list
			for(CustomerOrderStatus status : orderList) {
				if(status.getOrderStatus()=="COMPLETED") {
					orderList.remove(status);
				}
			}
			
			System.out.printf("Day %d, days profit = £%d, total profit = £%d", day,todaysProfit, totalProfit);
		}
	}
	
	public class EndOfDay extends OneShotBehaviour {
		public EndOfDay(Agent a){
			super(a);
		}
		@Override
		public void action() {
			todaysProfit = 0;
			latePenalty = 0;
			storageCost = 0;
			costOfSupplies = 0;
			ACLMessage doneMsg = new ACLMessage(ACLMessage.INFORM);
			doneMsg.setContent("done");
			doneMsg.addReceiver(tickerAgent);
			for(AID supplier : suppliers.keySet()) {
				doneMsg.addReceiver(supplier);
			}
			myAgent.send(doneMsg);
			day++;
		}
		//
		
	}
}
