package org.indiana.edu;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import com.opensymphony.xwork2.ActionSupport;

public class List_Collections_Action extends ActionSupport 
	{
	/**
	 *
	 */
		private static final long serialVersionUID = 1L;

	 /**
		 *
		 */
		 ArrayList<String> collections=new ArrayList<String>();
		 ArrayList<String> algorithms=new ArrayList<String>();
          String username,password;
          String algo_str = "http://pagodatree.cs.indiana.edu:9000/agent/debug-agent/algorithm/list";
          String collections_str = "http://pagodatree.cs.indiana.edu:9000/agent/debug-agent/collection/list";
          String algo_root_tag = "availibleAlgorithms";
          String algo_child_tag ="algorithm";
          String collections_root_tag = "collections";
          String collections_child_tag = "collection";
    
     public String execute() throws Exception
		{
			setCollections(getCollections());
			setAlgorithms(getAlgorithms());
			return SUCCESS;
		}

     public String getUsername() 
     	{
 			return username;
     	}

 	public void setUsername(String username) 
 		{
 			this.username = username;
 		}

 	public String getPassword() 
 		{
 			return password;
 		}

 	public void setPassword(String password) 
 		{
 			this.password = password;
 		}
       
     public ArrayList<String> getCollections() throws IOException, ParserConfigurationException, SAXException 
     	{
			 URL collections_url=new URL(collections_str);
			 URLConnection collection_conn = collections_url.openConnection();
			 collections= ParseXML(collections_url,collection_conn,collections_root_tag,collections_child_tag);
			 return collections;
		}

     public void setCollections(ArrayList<String> collections) 
     	{
			this.collections = collections;
		}
		
     public ArrayList<String> getAlgorithms() throws IOException, ParserConfigurationException, SAXException 
     	{
			 URL algo_url=new URL(algo_str);
			 URLConnection algo_conn = algo_url.openConnection();
			 algorithms= ParseXML(algo_url,algo_conn,algo_root_tag,algo_child_tag);
			 return algorithms;
     	}

     public void setAlgorithms(ArrayList<String> algorithms) 
     	{
			this.algorithms = algorithms;
		}
	
     public static ArrayList<String> ParseXML(URL url_str,URLConnection conn_str,String root_tag,String child_tag) throws ParserConfigurationException, SAXException, IOException 
     	{ 
    	 	String s = null;
            ArrayList <String> List_value=new ArrayList<String>();    	 
    	 	DocumentBuilderFactory dbF = DocumentBuilderFactory.newInstance();
    	 	DocumentBuilder dBuilder = dbF.newDocumentBuilder();
    	 	Document doc = dBuilder.parse(conn_str.getInputStream());
    	 	doc.getDocumentElement().normalize();
    	 	System.out.println("Root : "+doc.getDocumentElement());
    	 	System.out.println("****************");
    	 	NodeList nList= doc.getElementsByTagName(root_tag);
    	 	System.out.println("****************");
    	 	
    	 	
    	 	for (int i = 0; i < nList.getLength(); i++) {
    	 	     Node node = nList.item(i);
    	 	     if (node.getNodeType() == Node.ELEMENT_NODE) {
    	 	         Element element = (Element) node;
    	 	         NodeList nodelist1 = element.getElementsByTagName(child_tag);
    	 	         for (int i1 = 0; i1 < nodelist1.getLength(); i1++) 
    	 	         {
    	 	         Element element1 = (Element) nodelist1.item(i1);
    	 	         NodeList fstNm = element1.getChildNodes();
    	 	         s=fstNm.item(0).getNodeValue();
    	 	        
    	 	        List_value.add(s);
    	 	      }
    	 	         for(int c=0;c<List_value.size();c++)
    	 	         	{
    	 	        	 	System.out.println(List_value.get(c));
    	 	         	}
    	 	         
    	 	        }
    	 	
    	  }
			return List_value;

     	}
}