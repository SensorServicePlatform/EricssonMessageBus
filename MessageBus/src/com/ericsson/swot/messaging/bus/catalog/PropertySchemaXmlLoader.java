package com.ericsson.swot.messaging.bus.catalog;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class PropertySchemaXmlLoader extends DefaultHandler {
	private SchemaCatalog catalog = null;

	boolean property = false;
	String curProperty = null;
	
	public PropertySchemaXmlLoader(final SchemaCatalog cat) {
		if (cat == null)
			throw new IllegalArgumentException();
		catalog = cat;
	}
	
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (qName.equals("property"))
			property = true;
	}
 
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (qName.equals("property")) {
			catalog.addPropertyName(curProperty);
			curProperty = null;
			property = false;
		}
	}
	
	public void characters(char ch[], int start, int length) throws SAXException {
		String chars = new String(ch, start, length);
		if (property) {
			if (curProperty == null)
				curProperty = chars;
			else
				curProperty = curProperty.concat(chars);
		}
	}
}
