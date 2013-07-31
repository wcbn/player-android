/*
 **
 **  Jul. 20, 2009
 **
 **  The author disclaims copyright to this source code.
 **  In place of a legal notice, here is a blessing:
 **
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 **
 **                                         Stolen from SQLite :-)
 **  Any feedback is welcome.
 **  Kohei TAKETA <k-tak@void.in>
 **
 */
package net.moraleboost.streamscraper.util;

import java.util.LinkedList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;

public class JerichoHtmlUtils
{
    public static Element findFirstElement(
            Element segment, String name, String attrname, String attrvalue)
    {
        List<Element> elements = segment.select(name);
        if (elements != null) {
            for (Element element: elements) {
                String value = element.attr(attrname);
                if (value != null && value.equals(attrvalue)) {
                    return element;
                }
            }
        }
        
        return null;
    }
    
    public static List<org.jsoup.nodes.Element> findAllElements(
            String segment, String name, String attrname, String attrvalue)
    {
        List<org.jsoup.nodes.Element> ret = new LinkedList<org.jsoup.nodes.Element>();
        
    	Document doc = Jsoup.parse(segment);
    	Elements elements = doc.select(name);
    	for (int i = 0; i < elements.size(); i++) {
    		String value = elements.get(i).attr(attrname);
    		if (value != null) {
    			if (value.equals(attrvalue)) {
    				ret.add(elements.get(i));
    			}
    		}
    	}
    	
        return ret;
    }
   
    /*public static Element findFirstChildElementAlt(Element element, String name)
    {
    	List<Element> children = element.select(name);
    	if (children.size() > 0) {
    		return children.get(0);
    	}
    	
        return null;
    }*/
    
    public static Element findFirstChildElement(Element element, String name)
    {
    	List<Element> children = element.children();
    	for (Element child: children) {
    		if (child.nodeName().equalsIgnoreCase(name)) {
    			return child;
            }
        }
    	
        return null;
    }
    
    public static List<org.jsoup.nodes.Element> findAllChildElement(org.jsoup.nodes.Element element, String name)
    {
        return element.select(name);
    }
}
