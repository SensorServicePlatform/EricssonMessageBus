package com.ericsson.swot.messaging.bus;

import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.JexlException;
import org.apache.commons.jexl2.MapContext;

public class Utilities {
	private static Logger log = Logger.getLogger("Utilities");
	
	public static void main(String[] args) {
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put("type", "car");
		properties.put("eco-friendly", (Boolean)true);
		properties.put("year", 2010);
		
		String predicate = "(type=='car'&&(eco-friendly==true&&year<=2011))";

		log.info(Utilities.parsePredicateKeys(predicate).toString());		
		log.info(Boolean.toString(Utilities.evaluate(predicate, properties)));
	}
	
	public static boolean isNumeric(String str)
	{
		NumberFormat formatter = NumberFormat.getInstance();
		ParsePosition pos = new ParsePosition(0);
		formatter.parse(str, pos);
		return str.length() == pos.getIndex();
	}

	/**
	 * Evaluate the properties against the predicate, to see whether it holds true or not
	 * 
	 * @param predicate
	 * @param properties
	 * @return true if it is valid, false if it is invalid
	 */
	public static boolean evaluate(String predicate, Map<String, Object> properties) {
		if (predicate == null)
			return true;
		
		if (properties == null)
			return false;
		
		predicate = predicate.replace("true", "0");
		predicate = predicate.replace("false", "1");
		
		JexlEngine jexl = new JexlEngine();					//create or retrieve a JexlEngine
		
		Expression exp = null;
		try {
			exp = jexl.createExpression(predicate);	//create an expression object
		} catch(JexlException e) {
			log.info("Invalid predicate: " + predicate);
			return false;
		}
		
		JexlContext jexlContext = new MapContext();			//create a context and add data
		for (Entry<String, Object> entry : properties.entrySet()) 
			jexlContext.set(entry.getKey(), entry.getValue());		
	
		Object result = exp.evaluate(jexlContext);				//now evaluate the expression, getting the result
		log.info("result: " + result);
		
		if (result instanceof Boolean)
			return (Boolean)result;
		else
			return false;
	}
	
	/**
	 * parse the property names in the predicate
	 * 
	 * @param predicate
	 * @return	null if the predicate syntax is incorrect, or the list of property names
	 */
	public static List<String> parsePredicateKeys(String predicate) {
		List<String> keys = new ArrayList<String>();
		
		String temp = predicate.replace("(", "");
		temp = temp.replace(")", "");
		String[] subPredicates = temp.split("&&|\\|\\|");
		for (int i = 0; i < subPredicates.length; i++) {
			String[] elements = subPredicates[i].split("==|<=|>=");
			if (elements.length != 2)	//invalid syntax
				return null;
			
			keys.add(elements[0]);			
		}
		
		return keys;
	} 

}
