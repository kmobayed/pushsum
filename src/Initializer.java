
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;
import javax.xml.parsers.ParserConfigurationException;
import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Linkable;
import peersim.core.Network;
import peersim.core.Node;
import peersim.dynamics.WireKOut;
import peersim.util.ExtendedRandom;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Resource;
import de.fuberlin.wiwiss.ng4j.semwebclient.SemanticWebClient;
import de.fuberlin.wiwiss.ng4j.semwebclient.SemanticWebClientConfig;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.FileHandler;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;


public final class Initializer implements Control {

	//***************
	//** Constants **
	//***************

	private static final String PAR_PROT = "protocol";
        //private Log log = LogFactory.getLog(getClass());

	//**********************
	//** Class Attributes **
	//**********************

	protected static int pid;

        public static ExtendedRandom rnOperation = new ExtendedRandom(Configuration.getLong("random.seed"));
        public static Long operationsAVG = Configuration.getLong("operations.average");
        public static ExtendedRandom rnNode = new ExtendedRandom(Configuration.getLong("random.seed"));
        public static HashMap<Long, Double> LocalDiv=new HashMap<Long, Double>();
        public static HashMap<Long, Double> LocalWeight=new HashMap<Long, Double>();
        public static HashMap<Long, Set<Long>> Topology = new HashMap<Long, Set<Long>>();
        public static HashMap<Long, Set<Long>> SocioTopology = new HashMap<Long, Set<Long>>();
        public static HashMap<Long, String> TopologyMap = new HashMap<Long, String>();
        public static Double pushsumError = Configuration.getDouble("pushsum.error");
        public static Long cycleTimeMS = Configuration.getLong("cycle.time");
        public static Long LinkTraversalTime =new Long(0) ;
        public static Long LinkTraversalValue = new Long (0);
        public static String XGGMLfile= Configuration.getString("network.xgmmlfile");


	//****************
	//** Attributes **
	//****************

	public String prefix;
	
	//*****************
	//** Constructor **
	//*****************

	/** @{inherit} */
	public Initializer(String prefix) {
                BasicConfigurator.configure();
		pid = Configuration.getPid(prefix+"."+PAR_PROT);
                WireKOut wire = new WireKOut(prefix);
                wire.execute();
                this.netTopo();
                GenerateRDF GRDF = new GenerateRDF(XGGMLfile);
                try {
                    GRDF.readXGMML(true);
                } catch (IOException ex) {
                    //Logger.getLogger(Initializer.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ParserConfigurationException ex) {
                    //Logger.getLogger(Initializer.class.getName()).log(Level.SEVERE, null, ex);
                }
                this.socioTopo();
	}

	//*********************
	//** Implementations **
	//*********************

	/** @{inherit} */
        public void netTopo()
        {
            for (Integer i=0 ; i<Network.size() ; i++){

                Set<Long> neighbours = new TreeSet<Long>();
                Node n = Network.get(i);
                Linkable l = (Linkable) n.getProtocol(Initializer.pid);
                for (Integer j = 0  ; j<l.degree() ; j++){
                    neighbours.add(l.getNeighbor(j).getID());
                }
                Topology.put(n.getID(), neighbours);
            }

        }

        public static Long getID(String id)
        {
            for(Long idx : TopologyMap.keySet())
            {
              if (TopologyMap.get(idx).equals(id)) return idx;
            }
            return null;
        }

        public static String getID(Long id)
        {
            return TopologyMap.get(id);
        }

        public final void socioTopo()
        {
            GenerateRDF GRDF = new GenerateRDF(XGGMLfile);
            try {
                ArrayList<String> nodes = GRDF.getNodes();
                int i=0;
                for(Iterator<String> iter1 =nodes.iterator();iter1.hasNext();)
                {
                    String nodeID=iter1.next();
                    Node n = Network.get(i);
                    TopologyMap.put(n.getID(),nodeID);
                    i++;
                }
            } catch (ParserConfigurationException ex) {
                //Logger.getLogger(Initializer.class.getName()).log(Level.SEVERE, null, ex);
            }

            for (Integer i=0 ; i<Network.size() ; i++){
                Set<Long> neighbours = new TreeSet<Long>();
                Node n = Network.get(i);
                try {
                        String nodeID=getID(n.getID());
                        ArrayList<String> edges = GRDF.getEdges(nodeID);
                        if (edges!=null)
                        {
                            for (Iterator<String> iter2=edges.iterator();iter2.hasNext();)
                            {
                                String neighbourID=iter2.next();
                                neighbours.add(getID(neighbourID));
                            }
                        }
                    }
                catch (ParserConfigurationException ex) {
                    //Logger.getLogger(Initializer.class.getName()).log(Level.SEVERE, null, ex);
                }
                SocioTopology.put(n.getID(), neighbours);
                }
        }

        public boolean execute() {
                for (Integer nodeIndex = 0 ; nodeIndex<Network.size() ; nodeIndex++){
			Long peerId = Network.get(nodeIndex).getID();
			Peer peer = new Peer(peerId);
                        LocalDiv.put(peerId, peer.calcualteLocalDiv());
                        if (nodeIndex ==0)LocalWeight.put(peerId, new Double(1));
                        else LocalWeight.put(peerId, new Double(0));
			System.out.println("Peer "+peerId+" knows "+peer.getSocialNeighbours().size()+" other collaborator.");
                        System.out.println("Peer "+peerId+" conntected to "+peer.getP2PNeighbours().size()+" other peers.");
                }
                try {
                    Initializer.globalDivergence(Configuration.getString("reference.site"));
                } catch (IOException ex) {
                    //Logger.getLogger(Initializer.class.getName()).log(Level.SEVERE, null, ex);
                }
                System.out.println("Global divergence (Link Traversal)= "+LinkTraversalValue);
                System.out.println("Global divergence (Link Traversal) time (milli seconds): "+LinkTraversalTime);
		return true;
	}

        public static void globalDivergence(String site) throws IOException
        {
            //FileHandler hand = new FileHandler("out.log");
            //hand.setFormatter(new LoggingSimpleFormatter());
            Logger.getLogger("de.fuberlin.wiwiss.ng4j.semwebclient").setLevel(Level.ALL);
            //log.addHandler(hand);
          
            SemanticWebClient semweb = new SemanticWebClient();
            semweb.getConfig().setValue( SemanticWebClientConfig.MAXSTEPS, "100" );
            semweb.getConfig().setValue( SemanticWebClientConfig.TIMEOUT, "100000" );
            //this.log.debug("Ignored (maxsteps reached): " + uri);
            int count=0;
            String queryString =
            "PREFIX scho: <http://localhost/scho.xml#> " +
            "SELECT DISTINCT ?op WHERE {" +
            "{ " +
            "<http://localhost/"+site+"/scho.xml#"+site+"> a scho:Site . " +
            "?project a scho:Project . " +
            "?op a scho:Operation . " +
            "}"+
 //           "NOT EXISTS { ?patch scho:hasOp ?op }"+
            "}";
            long startTime = System.currentTimeMillis();
            Query query = QueryFactory.create(queryString);

            QueryExecution qe = QueryExecutionFactory.create(query, semweb.asJenaModel("default"));

            for (ResultSet rs1 = qe.execSelect() ; rs1.hasNext() ; )
            {
                QuerySolution binding1 = rs1.nextSolution();
                Resource result=((Resource) binding1.get("op"));
                if (result!=null) count++;
            }
            qe.close();
            long endTime = System.currentTimeMillis();
            String URI="";
            int cc=0;

            System.out.println(semweb.successfullyDereferencedURIs().size());
//            System.out.println(semweb.redirectedURIs().size());
//            System.out.println(semweb.successfullySearchedURIs().size());
//            System.out.println(semweb.unsuccessfullySearchedURIs().size());

            for (Iterator<String> iter3=semweb.successfullyDereferencedURIs().iterator();iter3.hasNext();)
                            {
                                cc++;
                                URI=iter3.next();
                                System.out.println(cc+"\t"+URI);
                            }
            LinkTraversalValue=new Long(count);
            LinkTraversalTime=endTime-startTime;
        }
}