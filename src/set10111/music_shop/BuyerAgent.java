package set10111.music_shop;

import java.util.ArrayList;
import java.util.List;

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
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import set10111.music_shop_ontology.ECommerceOntology;
import set10111.music_shop_ontology.elements.*;

public class BuyerAgent extends Agent{
	private Codec codec = new SLCodec();
	private Ontology ontology = ECommerceOntology.getInstance();
	private int QueriesSent = 0;
	private int ResponsesReceived = 0;
	private AID sellerAID;
	protected void setup(){
		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(ontology);
		String[] args = (String[])this.getArguments();
		//Add tickerBehaviour that schedules request to seller agents every min
		addBehaviour(new WakerBehaviour(this, 10000) {
			protected void onWake() {
				// update the list of seller agents
				DFAgentDescription template = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				sd.setType("selling-agent");
				template.addServices(sd);
				try {
					DFAgentDescription[] result = DFService.search(myAgent, template);
					sellerAID = result[0].getName();
				}
				catch (FIPAException fe) {
					fe.printStackTrace();
				}
			} //this could be it's own 'find seller behaviour class'
			});
		addBehaviour(new WakerBehaviour(this, 10000) {
			@Override
			protected void onWake() {
				SequentialBehaviour seqB = new SequentialBehaviour();
				seqB.addSubBehaviour(new QueryBuyerBehaviour());
				addBehaviour(seqB);
			}
		});
	}
	
	private class QueryBuyerBehaviour extends OneShotBehaviour{
		//private boolean finished = false;
		
		@Override
		public void action(){
			// Prepare the Query-IF message
			ACLMessage msg = new ACLMessage(ACLMessage.QUERY_IF);
			msg.addReceiver(sellerAID); // sellerAID is the AID of the Seller agent
			msg.setLanguage(codec.getName());
			msg.setOntology(ontology.getName()); 
			// Prepare the content. 
			CD cd = new CD();
			cd.setName("Synchronicity");
			cd.setSerialNumber(123);
			ArrayList<Track> tracks = new ArrayList<Track>();
			Track t = new Track();
			t.setName("Every breath you take");
			t.setDuration(230);
			tracks.add(t);
			t = new Track();
			t.setName("King of pain");
			t.setDuration(500);
			tracks.add(t);
			cd.setTracks(tracks);
			Owns owns = new Owns();
			owns.setOwner(sellerAID);
			owns.setItem(cd);
			try {
			 // Let JADE convert from Java objects to string
			 getContentManager().fillContent(msg, owns);
			 send(msg);
			 QueriesSent ++;
			}
			catch (CodecException ce) {
			 ce.printStackTrace();
			}
			catch (OntologyException oe) {
			 oe.printStackTrace();
			} 
			//finished = true;
		}

	} //end of query behaviour
	
	private class ReceiveResponses extends Behaviour {
		public ReceiveResponses(Agent a) {
			super(a);
		}
		
		@Override
		public void action() {
			//This behaviour should only respond to CONFIRM messages
			MessageTemplate mt = MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.CONFIRM), MessageTemplate.MatchPerformative(ACLMessage.DISCONFIRM));
			ACLMessage msg = receive(mt);
			if(msg != null) {
				try {
					ContentElement ce = null;
					ce = getContentManager().extractContent(msg);
					if (ce instanceof Owns) { /// This whole below section needs changed. Consider 'itemToBuy' variable
						ACLMessage reply = msg.createReply(); //create reply to message
						Owns owns = (Owns) ce;
						Item it = owns.getItem();
						// Extract the CD name and print it to demonstrate use of the ontology
						CD cd = (CD)it;
						System.out.println("The CD name is " + cd.getName());
						
						//check if seller has it in stock
						if(itemsForSale.containsKey(cd.getSerialNumber())) {
							System.out.println("I have the CD in stock!");
							reply.setPerformative(ACLMessage.CONFIRM);
							System.out.println(sender);
							getContentManager().fillContent(reply, owns);
						}
						else {
							reply.setPerformative(ACLMessage.DISCONFIRM);
							System.out.println("CD out of stock");
						}
						send(reply);
					} /// end of section to change ! ! ! 
					
					
					
					ResponsesReceived ++;
				}
				catch(CodecException ce) {
					ce.printStackTrace();
				}
				catch (OntologyException oe) {
					oe.printStackTrace();
				}
			}
		}
		
		@Override
		public boolean done() {
			return;
		}
	}
}
