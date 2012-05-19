import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import peersim.core.Network;
import peersim.core.Node;
import peersim.core.Protocol;


public class Peer implements Protocol, Comparable<Peer>{

	//****************
	//** Attributes **
	//****************

	/** Unique identifier of the peer. */
	protected Long id = new Long(-1);
        //protected int div= 0;

	//*****************
	//** Constructor **
	//*****************


	public Peer(String prefix){}

	public Peer(){}

	public Peer(Long id){
		this.id = id;
	}

	//*************************
	//** Getters and setters **
	//*************************

	public Set<Long> getP2PNeighbours(){
                return Initializer.Topology.get(id);
	}
        
        public Set<Long> getSocialNeighbours(){
	        return Initializer.SocioTopology.get(id);
        }

	
	public Node getNode(){
		Node node = null;
		for (Integer i=0 ; i<Network.size() ; i++){
			if (this.id.equals(Network.get(i).getID())){
				node = Network.get(i);
			}
		}
		return node;
	}

        public Double getDiv(){
            return Initializer.LocalDiv.get(id);
        }

        public void setDiv(Double div)
        {
            Initializer.LocalDiv.put(id, div);
        }

        public Double getW(){
            return Initializer.LocalWeight.get(id);
        }

        public void setW(Double w)
        {
            Initializer.LocalWeight.put(id, w);
        }
	
	//********************
	//** Implementation **
	//********************
	
	public void printDiv(){
                System.out.print("Peer "+id+"\tdiv = "+this.getDiv());
                System.out.print("\tW="+this.getW());
                System.out.print("\tAvg = ");
                System.out.println( (this.getW()==0) ? this.getDiv():this.getDiv()/this.getW());
	}


        public Double calcualteLocalDiv()
        {
            Node node = getNode();
            int count = 0;
            String siteID=Initializer.getID(node.getID());
            String URL="http://localhost/S"+siteID+"/scho.xml";
            String queryString =
            "PREFIX scho: <http://localhost/scho.xml#> " +
            "SELECT (COUNT (DISTINCT ?op) AS ?count) WHERE {" +
            "{  " +
            "?op a scho:Operation . " +
            //"?patch a scho:Patch . " +
            "}"+
            "NOT EXISTS { ?patch scho:hasOp ?op }"+
        //    "FILTER (?site1 != ?site2)"+
            "}";
            Model data= ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM);
            data.read(URL);
            QueryExecution qe = QueryExecutionFactory.create(queryString, Syntax.syntaxARQ, data);
            for (ResultSet rs1 = qe.execSelect() ; rs1.hasNext() ; )
            {
                QuerySolution binding1 = rs1.nextSolution();
                Literal result=((Literal) binding1.get("count"));
                if (result!=null) count=result.getInt();

            }
            qe.close();
            return new Double(count);
        }
        
        public void calcualteGlobalDiv()
        {
            	List<Long> l = new ArrayList<Long>(getP2PNeighbours());
                Integer r = Initializer.rnNode.nextInt(l.size());
                Peer p = new Peer(l.get(r));
                Double curDiv=this.getDiv()/2;
                Double curW=this.getW()/2;
                this.setDiv(0.0);
                this.setW(0.0);
                p.receiveDiv(this.id, curDiv,curW);
                this.receiveDiv(this.id, curDiv,curW);
        }


        public void receiveDiv(Long id, Double div,Double w)
        {
           this.setW(this.getW()+w);
           this.setDiv(this.getDiv()+div);
        }

	//**************************
	//** Intermediate methods **
	//**************************	

	@Override
	public int compareTo(Peer peer) {
		int res;
		if (this.id.equals(peer.id)){
			res = 0;
		} else if (this.id.equals(peer.id)){
			res = -1;
		} else {
			res = 1;
		}
		return res;
	}

	public boolean equals(Object o){
		boolean res = false;
		if (o instanceof Peer){
			Peer peer = (Peer) o;
			res = this.id.equals(peer.id);
		}
		return res;
	}
	
	public Object clone(){
		return null;
	}

}