package smartphone_manufacturing.supply_chain_ontology;

import jade.content.onto.BeanOntology;
import jade.content.onto.BeanOntologyException;
import jade.content.onto.Ontology;

public class ManufacturingOntology extends BeanOntology {

	private static final long serialVersionUID = 1L;
	private static Ontology instance = new ManufacturingOntology("my_ontology");
	  
	public static Ontology getInstance(){
	    return instance;
	}
	
	private ManufacturingOntology(String name) {
	    super(name);  
	    try {
	      add("smartphone_manufacturing.supply_chain_ontology.concepts");
	      add("smartphone_manufacturing.supply_chain_ontology.concepts.smartPhoneComponents");
	      add("smartphone_manufacturing.supply_chain_ontology.actions");
	      add("smartphone_manufacturing.supply_chain_ontology.predicates");
	      System.out.println("ontology added");
	    } catch (BeanOntologyException e) {
	      e.printStackTrace();
	    }
	}

}
