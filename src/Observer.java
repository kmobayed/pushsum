

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;

public class Observer implements Control {
	
	protected String prefix = "";
        private boolean stop;
        private int cycleNo =0;
        private FileWriter outFile;
        private PrintWriter out;
        private String outputFilename=Configuration.getString("output.file");
	//*****************
	//** Constructor **
	//*****************

	public Observer(String prefix) throws IOException {
		this.prefix = prefix;
                outFile = new FileWriter(outputFilename);
                outFile.flush();
                
	}

	//*********************
	//** Implementations **
	//*********************



        public Double RealDiv()
        {
            Double sum=new Double(0);
            //Double count=new Double(0);
//            for (Integer i=0 ; i<Network.size() ; i++){
//                Node n = Network.get(i);
//                n.getID();
//
//
//            }

            for (Object key: Initializer.LocalDiv.keySet()) {
                    sum += Initializer.LocalDiv.get(key);
                   // count += Initializer.LocalWeight.get(key);
                }
            return sum;
        }

        @Override
	public boolean execute() {

		System.out.println("We are observing the system:");
                try {
                    outFile = new FileWriter(outputFilename,true);                   
                } catch (IOException ex) {
                    Logger.getLogger(Observer.class.getName()).log(Level.SEVERE, null, ex);
                }
                out = new PrintWriter(outFile);
                cycleNo++;

                Double sum=RealDiv();
                
                stop=true;
                Long executionTime=new Long(0);
                System.out.println("Real Div = "+sum);
                for (Integer nodeIndex = 0 ; nodeIndex < Network.size() ; nodeIndex++){
			Long peerId = Network.get(nodeIndex).getID();
			Peer peer = new Peer(peerId);
                        executionTime = Configuration.getLong("cycle.time")*cycleNo;
			peer.printDiv();
                        if (nodeIndex==0)
                        {
                            out.append(executionTime+"\t"+peer.getDiv()/peer.getW());
                            if (executionTime<Initializer.LinkTraversalTime) out.append("\t 0");
                            else out.append("\t "+Initializer.LinkTraversalValue );
                            out.append("\t"+sum.toString()+"\n");
                        }
                        if (Math.abs(peer.getDiv()/peer.getW()-sum)>Initializer.pushsumError)
                            stop=false;
		}
                out.close();
                if (stop) 
                {
                    System.out.println("============= STOP ============");
                    System.out.println("Execution Time = "+executionTime);
                }
		return false;
	}



}
