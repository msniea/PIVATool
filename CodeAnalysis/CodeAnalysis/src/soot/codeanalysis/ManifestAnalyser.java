package soot.codeanalysis;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.R;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class ManifestAnalyser {
	public static List<String> ComponentList=new ArrayList<String>();
	public static String PackageName=null;
	public ManifestAnalyser(String path) {
		File file=new File(path);
      try {
    	  
         InputStream is=new FileInputStream(file);
         DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
         DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
         Document doc = dBuilder.parse(is);

         Element element=doc.getDocumentElement();
         element.normalize();
         NodeList ManifestNodeList = doc.getElementsByTagName("manifest");
         getPackageName(ManifestNodeList);
         NodeList ActivityNodeList = doc.getElementsByTagName("activity");
         getActivities(ActivityNodeList);	
         NodeList ReceiverNodeList = doc.getElementsByTagName("receiver");
         getReceivers(ReceiverNodeList);	
         NodeList ServiceNodeList = doc.getElementsByTagName("service");
         getServices(ServiceNodeList);       
      } catch (Exception e) {e.printStackTrace();}

   }
	
  /* private static String getValue(String tag, Element element) {
      NodeList nodeList = element.getElementsByTagName(tag).item(0).getChildNodes();
      NodeList nodeLis=(NodeList) element.getAttributeNode("activity");
      Node node = nodeList.item(0);
      return node.getNodeValue();
   }*/
	public static List<String> getActivities(NodeList nodelist) {
        List<String> ActivityList=new ArrayList<String>();
		for (int i=0; i<nodelist.getLength(); i++) {
            Node node = nodelist.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
               Element element2 = (Element) node;
               ActivityList.add(element2.getAttribute("android:name"));
               ComponentList.add(element2.getAttribute("android:name"));
            }
         }
		return ActivityList;
	}
	public static List<String> getReceivers(NodeList nodelist) {
        List<String> ReceiverList=new ArrayList<String>();
		for (int i=0; i<nodelist.getLength(); i++) {
            Node node = nodelist.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
               Element element2 = (Element) node;
               ReceiverList.add(element2.getAttribute("android:name"));
               ComponentList.add(element2.getAttribute("android:name"));
            }
         }
		return ReceiverList;
	}
	public static List<String> getServices(NodeList nodelist) {
        List<String> ServiceList=new ArrayList<String>();
		for (int i=0; i<nodelist.getLength(); i++) {
            Node node = nodelist.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
               Element element2 = (Element) node;
               ServiceList.add(element2.getAttribute("android:name"));
               ComponentList.add(element2.getAttribute("android:name"));
            }
         }
		return ServiceList;
	}
	public static String getPackageName(NodeList nodelist) {
       // List<String> ServiceList=new ArrayList<String>();
            Node node = nodelist.item(0);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
               Element element2 = (Element) node;
               PackageName=element2.getAttribute("package");
            }         
		return PackageName;
	}
	/*public static List<String> getProvider(NodeList nodelist) {
        List<String> ProviderList=new ArrayList<String>();
		for (int i=0; i<nodelist.getLength(); i++) {
            Node node = nodelist.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
               Element element2 = (Element) node;
               ProviderList.add(element2.getAttribute("android:name"));
               ComponentList.add(element2.getAttribute("android:name"));
            }
         }
		return ProviderList;
	}*/
	public static List<String> getComponents() {		
		return ComponentList ;
	}
	public static String getPackage() {		
		return PackageName ;
	} 

};
