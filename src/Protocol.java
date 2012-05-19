
import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;

public class Protocol implements Control {
	
	//***************
	//** Constants **
	//***************
	
	private final String PEER_CLASS_PAR = "peer";
	
	//****************
	//** Attributes **
	//****************

	protected String prefix = "";
	protected String peerClassName = "";

	//*****************
	//** Constructor **
	//*****************

	public Protocol(String prefix){
		this.prefix = prefix;
		this.peerClassName = Configuration.getString(prefix+"."+PEER_CLASS_PAR);
		System.err.println("- Running control CanYouHereMeProtocol");
	}

	//*********************
	//** Implementations **
	//*********************

	@Override
	public boolean execute() {
		for (Integer nodeIndex = 0 ; nodeIndex < Network.size() ; nodeIndex++){
			Long peerId = Network.get(nodeIndex).getID(); 
			Peer peer = new Peer(peerId);
                        peer.calcualteGlobalDiv();
		}
		return false;
	}

}
