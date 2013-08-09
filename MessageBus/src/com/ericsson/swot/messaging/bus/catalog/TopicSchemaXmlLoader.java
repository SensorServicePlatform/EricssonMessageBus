package com.ericsson.swot.messaging.bus.catalog;

import org.apache.commons.jexl2.JexlException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class TopicSchemaXmlLoader extends DefaultHandler {
	private SchemaCatalog catalog = null;
	boolean topic = false;
	boolean field = false;
	boolean type = false;
	boolean range = false;
	
	String curTopic = null;
	String curField = null;
	String curType = null;
	String curRange = null;
	
	public TopicSchemaXmlLoader(final SchemaCatalog cat) {
		if (cat == null)
			throw new IllegalArgumentException();
		catalog = cat;
	}
 
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (qName.equals("topic"))
			topic = true;
		else if (qName.equalsIgnoreCase("field"))
			field = true;
		else if (qName.equalsIgnoreCase("type"))
			type = true;
		else if (qName.equalsIgnoreCase("range"))
			range = true;
	}
 
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (qName.equals("metadata")) {
			try {
				catalog.addMetadataField(curTopic, curField, new MetadataValueType(curType, curRange));
				curField = null;
				curType = null;
				curRange = null;
			} catch (JexlException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		} else if (qName.equals("entry"))
			curTopic = null;
		else if (qName.equals("topic"))
			topic = false;
		else if (qName.equals("field"))
			field = false;
		else if (qName.equals("type"))
			type = false;
		else if (qName.equals("range"))
			range = false;
	}
 
	public void characters(char ch[], int start, int length) throws SAXException {
		String chars = new String(ch, start, length);
		if (topic) {
			if (curTopic == null)
				curTopic = chars;
			else
				curTopic = curTopic.concat(chars);
		} else if (field) {
			if (curField == null)
				curField = chars;
			else
				curField = curField.concat(chars);
		} else if (type) {
			if (curType == null)
				curType = chars;
			else
				curType = curType.concat(chars);
		} else if (range) {
			if (curRange == null)
				curRange = chars;
			else
				curRange = curRange.concat(chars);
		}
	}
}
