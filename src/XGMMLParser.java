import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class XGMMLParser{


	private Element docEle = null;

	public XGMMLParser (String filename) {
                InputStream xgmml = null;
                try {
                    xgmml = new BufferedInputStream(new FileInputStream(filename));
                    		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    Document dom = null;
                    try {
                            DocumentBuilder db = dbf.newDocumentBuilder();
                            dom = db.parse(xgmml);
                            docEle = dom.getDocumentElement();
                    } catch (Exception e) {
                            System.out.print( "An error occured while reading the XGMML-data:\n\n" + e.getMessage());
                    }
                } catch (FileNotFoundException ex) {
                } finally {
                    try {
                        xgmml.close();
                    } catch (IOException ex) {
                    }
                }
	}

	String getAttValue (Element ele, String attributeName) {
		NodeList nl = ele.getElementsByTagName("att");
		if (nl != null && nl.getLength() > 0)
			for (int i = 0; i < nl.getLength(); i++) {
				Element nEl = (Element) nl.item(i);
				if (nEl.getAttribute("name").equals(attributeName))
					return nEl.getAttribute("value");
			}

		return null;
	}

	String getGraphicsFill (Element ele) {
		NodeList nl = ele.getElementsByTagName("graphics");
		if (nl != null && nl.getLength() > 0) {
			String c = ((Element) nl.item(0)).getAttribute("fill");
			if (c.equals(""))
				return null;
			else
				return c;
		}

		return null;
	}

	String getGraphicsType (Element ele) {
		NodeList nl = ele.getElementsByTagName("graphics");
		if (nl != null && nl.getLength() > 0) {
			String t = ((Element) nl.item(0)).getAttribute("type");
			if (t.equals(""))
				return null;
			else
				return t;
		}

		return null;
	}

	void parseEdges () {
		Element el = null;
		String sId = null;
		String tId = null;

		NodeList edgesList = docEle.getElementsByTagName("edge");
		if (edgesList != null) {
			for (int i = 0; i < edgesList.getLength(); i++) {
				el = (Element) edgesList.item(i);
				sId = el.getAttribute("source");
				tId = el.getAttribute("target");
			
                                System.out.println("S"+sId + "\t->\t" + "S"+tId);
				
			}
		}
	}

        ArrayList<String> parseNodeEdges (String nodeID) {
		Element el = null;
		String sId = null;
		String tId = null;

                ArrayList<String> newNodes = new ArrayList<String>();
		NodeList edgesList = docEle.getElementsByTagName("edge");
		if (edgesList != null) {
			for (int i = 0; i < edgesList.getLength(); i++) {
				el = (Element) edgesList.item(i);
				sId = el.getAttribute("source");
				tId = el.getAttribute("target");
                                if (sId.equals(nodeID)) 
                                {    
                                    newNodes.add(tId);
                                }
			}
		}
                return newNodes;
	}

	ArrayList<String> parseNodes () {
		Element el = null;
		String id = null;

		ArrayList<String> newNodes = new ArrayList<String>();

		NodeList nodesList = docEle.getElementsByTagName("node");
		if (nodesList != null) {
			for (int i = 0; i < nodesList.getLength(); i++) {
				el = (Element) nodesList.item(i);

				id = el.getAttribute("id");
				newNodes.add(id);
			}
		}
                return newNodes;
	}

}
