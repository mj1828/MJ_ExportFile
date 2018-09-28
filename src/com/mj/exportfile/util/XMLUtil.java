package com.mj.exportfile.util;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @addtime 2015-07-03 13:27:30
 * @author gl
 */
public class XMLUtil {

	/**
	 * 解析当前项目的eclipse配置，并将配置参数放入map中
	 * @param workspace
	 * @param project
	 * @return
	 */
	public static Map<String,String> xml(String workspace, String project) {
		
		Map<String,String> xmlMap = new HashMap<String,String>();
		
		try {
			FileInputStream in = new FileInputStream(new File(workspace + "/"
					+ project + "/.classpath"));
	
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			DocumentBuilder db = dbf.newDocumentBuilder();
			// 该处返回的Document是JAVA库中内带的 org.w3c.dom.Document
			Document doc = db.parse(in);
			
			NodeList nodeList = doc.getElementsByTagName("classpath");
			if (nodeList != null && nodeList.getLength() > 0) {
				for (int count = 0; count < nodeList.getLength(); count++) {
					Node n = nodeList.item(count);
					NodeList sonList = n.getChildNodes();
					if (sonList != null && sonList.getLength() > 0) {
						for (int i = 0; i < sonList.getLength(); i++) {
							Node son = sonList.item(i);
							if ("classpathentry".equals(son.getNodeName())) {
								NamedNodeMap map = son.getAttributes();
								if (map != null && map.getLength() == 2) {
									Node k = map.item(0);
									Node v = map.item(1);
									if("kind".equals(k.getNodeName()) && "path".equals(v.getNodeName())){
										xmlMap.put(k.getNodeValue(),v.getNodeValue());
									}
								}
							}
						}
	
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return xmlMap;
	}

}
