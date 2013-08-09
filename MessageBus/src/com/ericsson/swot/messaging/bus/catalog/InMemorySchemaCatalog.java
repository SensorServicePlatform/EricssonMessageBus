package com.ericsson.swot.messaging.bus.catalog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.ericsson.swot.messaging.bus.Config;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

@SuppressWarnings("restriction")
public class InMemorySchemaCatalog implements SchemaCatalog {
	private Logger log = Logger.getLogger(this.getClass().getSimpleName());
	
	private Map<String, Map<String, MetadataValueType>> msgTopicsAndMetadata;
	private Set<String> propertyNames;	
		
	public InMemorySchemaCatalog() {
		msgTopicsAndMetadata = new HashMap<String, Map<String, MetadataValueType>>();
		propertyNames = new HashSet<String>();
	}
	
	public boolean addTopic(String topic) {
		if (msgTopicsAndMetadata.containsKey(topic) == false) {
			msgTopicsAndMetadata.put(topic, new HashMap<String, MetadataValueType>());
			return true;
		}
		
		return false;
	}
	
	public boolean addMetadataField(String topic, String field, MetadataValueType valueType) {
		addTopic(topic);
		
		MetadataValueType preValueType = msgTopicsAndMetadata.get(topic).put(field, valueType);
		if (preValueType == null || preValueType != valueType) {	//the field did not exist or is not the same
			return true;
		}
		
		return false;
	}
	
	public boolean addPropertyName(String property) {
		return propertyNames.add(property);
	}
	
	public Map<String, Map<String, MetadataValueType>> getMsgTopicsAndMetadata() {
		return msgTopicsAndMetadata;
	}

	public Set<String> getPropertyNames() {
		return propertyNames;
	}	
	
	public boolean checkTopicAndMetadata(String topic, Map<String, String> metadata) {
		if (topic == null)
			return false;
		
		log.info("topic is not null");
		
		if (msgTopicsAndMetadata.containsKey(topic) == false) {
			log.warning("topic is invalid");
			return false;
		}
		
		log.info("topic is valid");
		
		if (metadata != null && msgTopicsAndMetadata.get(topic).keySet().containsAll(metadata.keySet()) == false) {
			log.warning("invalid metadata field(s)");
			return false;
		}
		
		log.info("metadata field names are valid");
		
		if (metadata != null) {
			for (Entry<String, String> entry : metadata.entrySet()) {
				if (msgTopicsAndMetadata.get(topic).get(entry.getKey()).checkValidity(entry.getValue()) == false) {
					log.warning("metadata type/range is not valid");
					return false;
				}
			}
		}
		
		log.info("metadata is valid");
		
		return true;
	}
	
	public boolean checkProperty(String property) {
		return propertyNames.contains(property);
	}
	
	public void writeTopicSchemaToFileThread() {
		
		Thread persistThread = new Thread() {
			@Override
			public void run() {
				synchronized(this) {	//thread-safe
					try {
						try {
							writeTopicSchemaToFile(Config.getTopicSchemaXmlFilePath());
						} catch (URISyntaxException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}						
					} catch (IOException e) {
						e.printStackTrace();
					} catch (SAXException e) {
						e.printStackTrace();
					}
				}
			}
		};
		
		persistThread.start();	
	}
	
	public void writePropertySchemaToFileThread() {
		
		Thread persistThread = new Thread() {
			@Override
			public void run() {
				synchronized(this) {	//thread-safe
					try {
						try {
							writePropertySchemaToFile(Config.getPropertySchemaXmlFilePath());
						} catch (URISyntaxException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}						
					} catch (IOException e) {
						e.printStackTrace();
					} catch (SAXException e) {
						e.printStackTrace();
					}
				}
			}
		};
		
		persistThread.start();	
	}
	
	
	/**
	 * 
	 * XML file Persistence
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws URISyntaxException 
	 * 
	 */
	private void writeTopicSchemaToFile(String path) throws IOException, SAXException, URISyntaxException {
		URL resourceUrl = this.getClass().getClassLoader().getResource(path);
		if (resourceUrl == null)
			throw new IOException();
		log.info("config file found. Loading...");
		File resourceFile = new File(resourceUrl.toURI());
		FileOutputStream fos = new FileOutputStream(resourceFile);
		
		
		OutputFormat of = new OutputFormat("XML","ISO-8859-1",true);
		of.setIndent(1);
		of.setIndenting(true);
		XMLSerializer serializer = new XMLSerializer(fos,of);
		
		ContentHandler hd = serializer.asContentHandler();
		hd.startDocument();
		AttributesImpl atts = new AttributesImpl();
		hd.startElement("", "", "entries", atts);
				
		for (Entry<String, Map<String,MetadataValueType>> entry : msgTopicsAndMetadata.entrySet()) {
			String topic = entry.getKey();
			hd.startElement("", "", "entry", atts);
			
			hd.startElement("", "", "topic", atts);
			hd.characters(topic.toCharArray(), 0, topic.length());
			hd.endElement("", "", "topic");
			
			Map<String, MetadataValueType> metadata = entry.getValue();
			for (Entry<String,MetadataValueType> entry1 : metadata.entrySet()) {
				String field = entry1.getKey();
				
				hd.startElement("", "", "metadata", atts);
				
				hd.startElement("", "", "field", atts);
				hd.characters(field.toCharArray(), 0, field.length());
				hd.endElement("", "", "field");	
				
				String type = entry1.getValue().getCls().getSimpleName();
				String range = entry1.getValue().getRange();
				
				hd.startElement("", "", "type", atts);
				hd.characters(type.toCharArray(), 0, type.length());
				hd.endElement("", "", "type");
				
				if (range != null) {
					hd.startElement("", "", "range", atts);
					hd.characters(range.toCharArray(), 0, range.length());
					hd.endElement("", "", "range");
				}	
				
				hd.endElement("", "", "metadata");				
			}
			
			hd.endElement("", "", "entry");
		}
		
		hd.endElement("", "", "entries");
		hd.endDocument();
		fos.close();
	}
	
	private void writePropertySchemaToFile(String path) throws IOException, SAXException, URISyntaxException {
		URL resourceUrl = this.getClass().getClassLoader().getResource(path);
		if (resourceUrl == null)
			throw new IOException();
		log.info("config file found. Loading...");
		File resourceFile = new File(resourceUrl.toURI());
		FileOutputStream fos = new FileOutputStream(resourceFile);
		
		OutputFormat of = new OutputFormat("XML","ISO-8859-1",true);
		of.setIndent(1);
		of.setIndenting(true);
		XMLSerializer serializer = new XMLSerializer(fos,of);
		
		ContentHandler hd = serializer.asContentHandler();
		hd.startDocument();
		AttributesImpl atts = new AttributesImpl();
		hd.startElement("", "", "properties", atts);
				
		for (String property : propertyNames) {
			hd.startElement("", "", "property", atts);
			hd.characters(property.toCharArray(), 0, property.length());
			hd.endElement("", "", "property");
		}
		
		hd.endElement("", "", "properties");
		hd.endDocument();
		fos.close();
	}
	
	public void loadTopicSchemaFromFile(String path) throws Exception {
		URL resourceUrl = this.getClass().getClassLoader().getResource(path);
		if (resourceUrl == null)
			throw new Exception();
		log.info("topic schema file found. Loading...");
		File resourceFile = new File(resourceUrl.toURI());
		InputStream is = new FileInputStream(resourceFile);
		SAXParserFactory factory = SAXParserFactory.newInstance();
		TopicSchemaXmlLoader loader = new TopicSchemaXmlLoader(this);
		SAXParser saxParser;
		try {
			saxParser = factory.newSAXParser();
			saxParser.parse(is, loader);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();			
		}
	}
	
	public void loadPropertySchemaFromFile(String path) throws Exception {
		URL resourceUrl = this.getClass().getClassLoader().getResource(path);
		if (resourceUrl == null)
			throw new Exception();
		log.info("property schema file found. Loading...");
		File resourceFile = new File(resourceUrl.toURI());
		InputStream is = new FileInputStream(resourceFile);
		SAXParserFactory factory = SAXParserFactory.newInstance();
		PropertySchemaXmlLoader loader = new PropertySchemaXmlLoader(this);
		SAXParser saxParser;
		try {
			saxParser = factory.newSAXParser();
			saxParser.parse(is, loader);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
