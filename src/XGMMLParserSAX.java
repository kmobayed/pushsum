import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class XGMMLParserSAX extends DefaultHandler{

    
    private String tempVal;
    private ArrayList<String> newNodes = new ArrayList<String>();
    private HashMap<String, ArrayList<String>> newEdges = new HashMap<String, ArrayList<String>>();

    public ArrayList<String> getNodes()
    {
        return newNodes;
    }

    public ArrayList<String> getEdges(String node)
    {
        return newEdges.get(node);
    }

    public HashMap<String, ArrayList<String>> getAllEdges()
    {
        return newEdges;
    }
    
    public void parseDocument(String filename) throws ParserConfigurationException
    {
		//get a factory
		SAXParserFactory spf = SAXParserFactory.newInstance();
		try {

			//get a new instance of parser
			SAXParser sp = spf.newSAXParser();

			//parse the file and also register this class for call backs
			sp.parse(filename, this);
			
		}catch(SAXException se) {
		}catch(ParserConfigurationException pce) {
		}catch (IOException ie) {
		}
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
        {

            //System.out.println("Start Element :" + qName);

            if (qName.equalsIgnoreCase("node")) {
               String id = null;
               id=attributes.getValue("id");
               newNodes.add(id);
            }

            if (qName.equalsIgnoreCase("edge")) {
               String sId = null;
               String tId = null;
               sId = attributes.getValue("source");
               tId = attributes.getValue("target");
               ArrayList<String> tmpTarget1=newEdges.get(sId);
               if (tmpTarget1==null)
               {
                 tmpTarget1=new ArrayList<String>();
                 tmpTarget1.add(tId);
               }
               else
               {
                    tmpTarget1.add(tId);
               }

               ArrayList<String> tmpTarget2=newEdges.get(tId);
               if (tmpTarget2==null)
               {
                 tmpTarget2=new ArrayList<String>();
                 tmpTarget2.add(sId);
               }
               else
               {
                    tmpTarget2.add(sId);
               }
               newEdges.put(sId, tmpTarget1);
               newEdges.put(tId, tmpTarget2);
               //System.out.println("S"+sId + "\t->\t" + "S"+tId);
            }
        }

    @Override
        public void endElement(String uri, String localName, String qName) throws SAXException
        {
          //System.out.println("End Element :" + qName);
        }

    @Override
        public void characters(char ch[], int start, int length) throws SAXException
        {
            tempVal = new String(ch,start,length);
        }
}
