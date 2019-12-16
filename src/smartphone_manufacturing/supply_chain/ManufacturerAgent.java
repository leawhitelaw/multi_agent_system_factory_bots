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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import smartphone_manufacturing.supply_chain_ontology.concepts.smartPhoneComponents.PhabletBattery;
import smartphone_manufacturing.supply_chain_ontology.concepts.smartPhoneComponents.PhabletScreen;
import smartphone_manufacturing.supply_chain_ontology.concepts.smartPhoneComponents.RAM;
import smartphone_manufacturing.supply_chain_ontology.concepts.smartPhoneComponents.SmallBattery;
import smartphone_manufacturing.supply_chain_ontology.concepts.smartPhoneComponents.SmallScreen;
import smartphone_manufacturing.supply_chain_ontology.concepts.smartPhoneComponents.Storage;
import smartphone_manufacturing.supply_chain_ontology.predicates.RequestManufacture;
import smartphone_manufacturing.supply_chain_ontology.predicates.ComponentsSent;
import smartphone_manufacturing.supply_chain_ontology.predicates.ComponentsInStock;
import smartphone_manufacturing.supply_chain_ontology.predicates.OrderShipped;
import smartphone_manufacturing.supply_chain_ontology.predicates.PaymentSent;
import smartphone_manufacturing.supply_chain_ontology.predicates.SentSupplierDetails;

public class ManufacturerAgent extends Agent {

	private Codec codec = new SLCodec();
	private AID tickerAgent;

	private int day = 1;
	//private ArrayList<CustomerOrder> requestedOrders = new ArrayList<>(); //accepted orders
	private ArrayList<AID> customers = new ArrayList<>();
	private HashMap<Integer, Integer> warehouse = new HashMap<>(); // components and their qty in warehouse
	private HashMap<AID, SupplierType> suppliers = new HashMap<>();
	private ArrayList<CustomerOrderStatus> orderList = new ArrayList<>();
	private ArrayList<CustomerOrderStatus> approvedOrders = new ArrayList<>();
	private ArrayList<CustomerOrderStatus> confirmedOrders = new ArrayList<>();
	private  ArrayList<CustomerOrderStatus> gotComponents = new ArrayList<>();
	//private List<CustomerOrderStatus> toReceive = new ArrayList<>();


	//keep track of daily variable outcomes
	private int todaysProfit = 0;
	private int latePenalty = 0;
	private int storageCost = 0;
	private int costOfSupplies = 0;
	private int totalProfit = 0;
	private int todaysPhoneQuantity = 0;
	private int approvedOrdersNum = 0;
	private int accepted = 0;

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
					//find agents on yellow pages
					dailyActivity.addSubBehaviour(new GetCustomers(myAgent));
					dailyActivity.addSubBehaviour(new GetSuppliers(myAgent));
					//get supplier info
					dailyActivity.addSubBehaviour(new GetSupplierDetails(myAgent));
					//manufacturing activities
					dailyActivity.addSubBehaviour(new SelectCustomerOrders(myAgent));
					dailyActivity.addSubBehaviour(new ReceiveCustomerOrders(myAgent));
					dailyActivity.addSubBehaviour(new OrderComponents(myAgent));

					dailyActivity.addSubBehaviour(new MakeOrder(myAgent));
					//terminate day
					dailyActivity.addSubBehaviour(new CalculateDailyTotals(myAgent));
					dailyActivity.addSubBehaviour(new EndOfDay(myAgent));
					myAgent.addBehaviour(new ReceiveSupplies(myAgent));
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

	@SuppressWarnings("serial")
	public class GetSuppliers extends OneShotBehaviour{

		public GetSuppliers(Agent a) {
			super(a);
		}

		@Override
		public void action() {
			DFAgentDescription supplyTemplate = new DFAgentDescription();
			ServiceDescription supplySd = new ServiceDescription();
			supplySd.setType("supply-agent");
			supplyTemplate.addServices(supplySd);

			try {
				suppliers.clear();
				DFAgentDescription[] supplyAgents = DFService.search(myAgent, supplyTemplate);
				if(supplyAgents.length > 0) {
					for(int i=0; i<supplyAgents.length; i++) {
						suppliers.put(supplyAgents[i].getName(), new SupplierType(supplyAgents[i].getName())); //this is the supply agents AID
						//System.out.println("supplier " + i + " " + supplyAgents[i].getName());
					}
				}else {
					System.out.println("*** Cannot find suppliers ***");
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
				supplierMsg.setConversationId("request-supplier-details");

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
				mt = MessageTemplate.and((MessageTemplate.MatchPerformative(ACLMessage.INFORM)), (MessageTemplate.MatchConversationId("request-supplier-details")));
				ACLMessage suppMsg = receive(mt);

				if(suppMsg != null) {
					try {
						ContentElement ce = null;
						ce = getContentManager().extractContent(suppMsg);

						if(ce instanceof SentSupplierDetails) {
							SentSupplierDetails supplierDetails = (SentSupplierDetails) ce;
							HashMap<PhoneComponent, Integer> prices = new HashMap<>();
							ArrayList<PhoneComponent> Components = supplierDetails.getPhoneComponents();
							ArrayList<Long> ComponentPrices = supplierDetails.getComponentPrices();

							for(int i=0; i<Components.size(); i++) {
								PhoneComponent component = Components.get(i);
								int price = ((Long)ComponentPrices.get(i)).intValue();
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

					if(ce instanceof RequestManufacture) {
						RequestManufacture manufactureRequest = (RequestManufacture) ce;
						CustomerOrder order = manufactureRequest.getOrder();
						orderStatus = new CustomerOrderStatus(order);
						orderStatus.setCustomer(msg.getSender());

						AID quickestSupplier = null;
						SupplierType currentSupplier = null;
						int profit = 0;
						int cost = 0; //cost of order by calculating component prices from quickest supplier
						int lowestDelivery = 10; //this is higher than both suppliers delivery days

						// use quickest supplier
						for (SupplierType supplier : suppliers.values()) {
							int deliveryDays = supplier.getDelivery();
							if(deliveryDays < lowestDelivery) {
								quickestSupplier = supplier.getSupplier();
								currentSupplier = supplier;
								lowestDelivery = deliveryDays;
							}

						}

						//search supplier price list to get cost of order
						for(Map.Entry<PhoneComponent, Integer> entry : currentSupplier.getPrices().entrySet()) {
							PhoneComponent comp = entry.getKey();
							for(PhoneComponent component : orderStatus.getOrder().getSmartPhone().getPhoneComponents()) {
								if(comp.toString().contentEquals(component.toString())) {
									int price = entry.getValue();
									cost = cost + price;
								}
							}
						}
						cost = cost*(orderStatus.getOrder().getQuantity());
						profit = (int) ((orderStatus.getOrder().getPrice()) - cost);


						ACLMessage reply = msg.createReply();
						if(profit > 0 && accepted < 1) {
							approvedOrders.add(orderStatus);
							orderStatus.setSupplier(quickestSupplier);
							orderStatus.setComponentDeliveryDate(day + lowestDelivery);
							orderStatus.setPrice(cost);
							orderStatus.setDayOrdered(day);
							orderStatus.setOrderCompleted(false);
							orderList.add(orderStatus);
							accepted++;
							reply.setPerformative(ACLMessage.CONFIRM);
						}else {
							reply.setPerformative(ACLMessage.DISCONFIRM);
						}
						reply.setConversationId("customer-order-response");
						myAgent.send(reply);
						approvedOrdersNum = approvedOrders.size();
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
			if(replies == customers.size()) {
				System.out.println("CUSTOMER ORDERS ACCEPTED TODAY:" + accepted);
			}
			return replies == customers.size();
		}	
	}

	public class ReceiveCustomerOrders extends Behaviour{
		private int received = 0;
		private int approvedToConfirmed = 0;
		//private int numApproved = approvedOrders.size();
		public ReceiveCustomerOrders(Agent a) {
			super(a);
			System.out.println("ReceiveCustomerOrders STARTED");
		}

		@Override
		public void action() {
			MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST), MessageTemplate.MatchConversationId("customer-order-sent"));
			ACLMessage customerMsg = receive(mt);

			if(customerMsg != null) {
				//System.out.println("do u get here???");
				try {
					ContentElement ce = null;
					ce = getContentManager().extractContent(customerMsg);

					if(ce instanceof Action) {
						received ++;
						Concept action = ((Action)ce).getAction();
						if(action instanceof ManufactureOrder) {
							ManufactureOrder manufactureOrder = (ManufactureOrder)action;
							if(approvedOrders.size() > 0) {
								for(CustomerOrderStatus approvedOrder : approvedOrders) {
									if(manufactureOrder.getOrder().getOrderID().contentEquals(approvedOrder.getOrder().getOrderID())) {
										confirmedOrders.add(approvedOrder);
										approvedToConfirmed++;
									}
								}
								if(approvedToConfirmed == approvedOrders.size()) {
									approvedOrders.clear();

								}
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
		}

		@Override
		public boolean done() {
			if (received == approvedOrdersNum) {
				System.out.println("ReceiveCustomerOrders DONE");
			}
			return received == approvedOrdersNum;
		}	
	}

	//requesting and ordering components in one behaviour as they apply to same order
	public class OrderComponents extends Behaviour {

		public OrderComponents(Agent a) {
			super(a);
			System.out.println("OrderComponents STARTED");
		}

		private int step = 0;
		private CustomerOrderStatus orderStatus;
		private AID supplier;
		private int orderedComponents;

		@Override
		public void action() {
			try {
				Thread.sleep(30);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			//System.out.println("CONFIRMED ORDERS: " + confirmedOrders);
			switch(step) {
			case 0:
				if (accepted == 0) {
					System.out.println("No components to order");
					break;
				}
				orderStatus = confirmedOrders.get(0);
				System.out.println("requesting components");
				supplier = orderStatus.getSupplier();
				ComponentsInStock componentsInStock = new ComponentsInStock(); //new request for components

				ACLMessage askSupplierMsg = new ACLMessage(ACLMessage.QUERY_IF);
				askSupplierMsg.setLanguage(codec.getName());
				askSupplierMsg.setOntology(ontology.getName());
				askSupplierMsg.setConversationId("sell-components-request");
				askSupplierMsg.addReceiver(supplier);
				componentsInStock.setSupplier(supplier);
				componentsInStock.setQuantity(orderStatus.getOrder().getQuantity());
				try {
					componentsInStock.setComponents(orderStatus.getOrder().getSmartPhone().getPhoneComponents());
					getContentManager().fillContent(askSupplierMsg, componentsInStock);
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

			case 1:
				MessageTemplate responseMt = MessageTemplate.and(
						MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.CONFIRM), MessageTemplate.MatchPerformative(ACLMessage.DISCONFIRM)), MessageTemplate.MatchConversationId("sell-components-response"));
				ACLMessage response = receive(responseMt);
				if(response != null) {
					System.out.println("supplier responded ");
					//if confirmed then do following
					if (response.getPerformative() == ACLMessage.CONFIRM) {
						ACLMessage orderReq = new ACLMessage(ACLMessage.REQUEST);
						orderReq.setOntology(ontology.getName());
						orderReq.setLanguage(codec.getName());
						orderReq.setConversationId("buy-components");
						orderReq.addReceiver(supplier);
						try {
							SellComponents sellComps = new SellComponents();
							sellComps.setManufacturer(myAgent.getAID());
							sellComps.setComponents(orderStatus.getOrder().getSmartPhone().getPhoneComponents());
							sellComps.setQuantity(orderStatus.getOrder().getQuantity());
							sellComps.setOrderId(orderStatus.getOrder().getOrderID());
							Action request = new Action();
							request.setAction(sellComps);
							request.setActor(response.getSender());

							getContentManager().fillContent(orderReq, request);
							send(orderReq);
							System.out.println(confirmedOrders.get(0).toString());
							confirmedOrders.remove(0);
							step++;

						}catch(CodecException ce) {
							ce.printStackTrace();
						}catch(OntologyException oe) {
							oe.printStackTrace();
						}catch(Exception e) {
							e.printStackTrace();
						}
					}else {
						// if not confirmed then remove order
						confirmedOrders.remove(orderStatus);
						step = 0;
					}
				}else {
					block();
				}


				break;

			case 2:
				//System.out.println("sending order for comps");
				//order has been  sent for components 
				int supplyCost = orderStatus.getPrice();
				String orderID = orderStatus.getOrder().getOrderID();
				ACLMessage sendPayment = new ACLMessage(ACLMessage.INFORM);
				sendPayment.setOntology(ontology.getName());
				sendPayment.setLanguage(codec.getName());
				sendPayment.setConversationId("pay-components");
				sendPayment.addReceiver(supplier);
				PaymentSent payment = new PaymentSent();
				payment.setOrderID(orderID);
				payment.setBuyer(myAgent.getAID());
				payment.setPrice(supplyCost);
				try {
					getContentManager().fillContent(sendPayment, payment);
					send(sendPayment);
					costOfSupplies = costOfSupplies + supplyCost;
					step = 0; 
				}catch(CodecException ce) {
					ce.printStackTrace();
				}catch(OntologyException oe) {
					oe.printStackTrace();
				}catch(Exception e) {
					e.printStackTrace();
				}
				step = 0;
				break;
			}
		}

		@Override
		public boolean done() {
			System.out.println("OrderComponents DONE CHECK: " + confirmedOrders.size() + " " + step);
			if (confirmedOrders.size() == 0 && step == 0) {
				System.out.println("OrderComponents DONE");
			}
			return accepted == 0 || (confirmedOrders.size() == 0 && step == 0); 
		}	
	} // end of order components behaviour

	public class ReceiveSupplies extends Behaviour {

		private static final long serialVersionUID = 1L;
		private int suppliesReceived = 0;
		private ArrayList<CustomerOrderStatus> toReceive = new ArrayList<>();
		public ReceiveSupplies(Agent a) {
			super(a);
			for(CustomerOrderStatus status : orderList) {	
				if(status.getComponentDeliveryDate() == day) {
					//System.out.println("DUE TO ARRIVE TODAY: " + status);
					//System.out.println(status.getOrder().toString());
					//System.out.println("adding to list " + status);
					toReceive.add(status);
				}
			}
			System.out.println("before receiving supplies - received size: " + toReceive.size());
		}


		@Override 
		public void action() {
			
			if (toReceive.size() > 0) {
				MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM), MessageTemplate.MatchConversationId("sell-components-response"));
				ACLMessage receiveMsg = receive(mt);
				if(receiveMsg != null) {
					int wHquantity;
					String orderID;

					try {
						ContentElement ce = null; 
						ce = getContentManager().extractContent(receiveMsg);
						if (ce instanceof ComponentsSent) {
							ComponentsSent componentsSent = (ComponentsSent) ce;
							ArrayList<PhoneComponent> phoneComponents = componentsSent.getPhoneComponents();
							wHquantity = componentsSent.getQty();
							orderID = componentsSent.getOrderID();
							for(PhoneComponent component : phoneComponents) {
								if(warehouse.containsKey(component.hashCode())) {
									int currentQuantity = warehouse.get(component.hashCode());
									warehouse.put(component.hashCode(), wHquantity + currentQuantity);
								}else { 
									warehouse.put(component.hashCode(), wHquantity);
								}

							}

							for(CustomerOrderStatus status : toReceive) {
								if(status.getOrder().getOrderID().equals(orderID)) {
									gotComponents.add(status);
								}
							}
							//System.out.println("Got components: " + gotComponents.size() + gotComponents);
							//System.out.println("toReceive " + toReceive.size());
							suppliesReceived ++;
							//System.out.println("supps received " + suppliesReceived);

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
					//System.out.println("blockinnng");
					block();
				}
			}
			
		}

		@Override
		public boolean done() {
			if(suppliesReceived == toReceive.size()) {
				System.out.println("ReceiveSupplies DONE");
			}
			return suppliesReceived == toReceive.size();
		}
	} // end of receive supplies behaviour

	public class MakeOrder extends Behaviour {

		public MakeOrder(Agent a) {
			super(a);
			System.out.println("MakeOrder ACTION STARTED");
		}

		private int step = 0;
		private int awaitingPayment = 0;

		@Override
		public void action() {
			
			switch(step) {
			case 0:
				System.out.println(warehouse);
				for(CustomerOrderStatus status: gotComponents) {
					if(todaysPhoneQuantity + status.getOrder().getQuantity() > 50) {
						continue;
					}
					ArrayList<PhoneComponent> phoneComponents = status.getOrder().getSmartPhone().getPhoneComponents();
					int quantity = status.getOrder().getQuantity();
					boolean warehouseHasComponents = true;
					for(PhoneComponent component : phoneComponents) {
						if(!warehouse.containsKey(component.hashCode()) || (warehouse.containsKey(component.hashCode()) 
								&& warehouse.get(component.hashCode()) < quantity)){
							warehouseHasComponents = false;
							break; //stop above being overwritten
						}
					}

					if(warehouseHasComponents) {
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
								int currQty = warehouse.get(component.hashCode());
								warehouse.put(component.hashCode(), (currQty - quantity));
							}
							todaysPhoneQuantity += quantity;
							System.out.println("Phones Built: " + todaysPhoneQuantity);
							
							//ship order
							getContentManager().fillContent(sendMsg, sendOrder);
							send(sendMsg);
							awaitingPayment++;
							
						}catch(CodecException ce) {
							ce.printStackTrace();
						}catch(OntologyException oe) {
							oe.printStackTrace();
						}catch(Exception e) {
							e.printStackTrace();
						}
					}

				}

				step++;
				break;

				//receive payment from customer
			case 1:
				if(awaitingPayment > 0) {
					MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM), MessageTemplate.MatchConversationId("order-payment"));
					ACLMessage payMsg = receive(mt);
					if(payMsg != null) {
						try {
							ContentElement ce = null;
							ce = getContentManager().extractContent(payMsg);

							if (ce instanceof PaymentSent) {
								PaymentSent payment = (PaymentSent) ce;
								//System.out.println("PAYMENT ID: " + payment.getOrderID());
								for(CustomerOrderStatus status : orderList) {
									//System.out.println("- ORDER ID: " + status.getOrder().getOrderID());
									if(status.getOrder().getOrderID().equals(payment.getOrderID())) {
										status.setOrderCompleted(true);
										gotComponents.remove(status);
									}
								}
								todaysProfit += payment.getPrice();
								System.out.println("ADDING TO PROFIT: " + payment.getPrice() + " from customer " + payment.getBuyer().getLocalName());
								awaitingPayment --;
							}else {
								System.out.println("Agent: " + myAgent.getAID().getLocalName() + "Received wrong msg from customer");
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
				break;
			}
				
		}

		@Override 
		public boolean done() {
			System.out.println("Awaiting payment: " + awaitingPayment);
			if(awaitingPayment == 0) {
				System.out.println("MakeOrder DONE");
			}
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
			for(Integer component : warehouse.keySet()) {
				int cost = warehouse.get(component) * 5;
				storageCost += cost;
			}

			//calculate late fees !!!!!! check
			for(CustomerOrderStatus status : orderList) {
				int deadlineDay = status.getDayOrdered() + status.getOrder().getDaysToDeadline();
				if(day > deadlineDay) {
					int cost = status.getOrder().getPerDayPenalty();
					latePenalty +=cost;
				}

			}
			//calculateTotalProfit
			totalProfit = totalProfit + todaysProfit - storageCost - latePenalty - costOfSupplies;

			//remove finished order from current order list
			for(CustomerOrderStatus status : orderList) {
				if(status.getOrderCompleted()==true) {
					orderList.remove(status);
					confirmedOrders.remove(status);
				}
			}

			System.out.printf("\n Day %d, \nTodays profit = £%d, \nTotal profit = £%d\n", day,todaysProfit, totalProfit );
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
			todaysPhoneQuantity = 0;
			approvedOrdersNum = 0;
			accepted = 0;
			ACLMessage doneMsg = new ACLMessage(ACLMessage.INFORM);
			doneMsg.setContent("done");
			doneMsg.addReceiver(tickerAgent);
			for(AID supplier : suppliers.keySet()) {
				doneMsg.addReceiver(supplier);
			}
			myAgent.send(doneMsg);
			System.out.println("MANU DONE!");
			day++;
		}
	}
}
