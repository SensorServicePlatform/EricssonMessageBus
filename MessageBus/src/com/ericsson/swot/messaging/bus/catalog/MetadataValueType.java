package com.ericsson.swot.messaging.bus.catalog;

import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.JexlException;
import org.apache.commons.jexl2.MapContext;

public class MetadataValueType {
	private Class<?> cls;
	private String range;
	
	private Expression exp = null;
	
	public MetadataValueType(Class<?> cls, String rangeStr) throws IllegalArgumentException, JexlException {
		if (cls != Boolean.class && cls != Integer.class && cls != Float.class && cls != String.class)
			throw new IllegalArgumentException();
		
		this.cls = cls;
		this.range = rangeStr;
		
		if (this.range != null) {
			if (range.contains("x") == false)
				throw new JexlException(null, "no 'x' found");
		
			JexlEngine jexl = new JexlEngine();				//create or retrieve a JexlEngine
			this.exp = jexl.createExpression(range);		//create an expression object
		}
	}
	
	public MetadataValueType(String clsName, String rangeStr) throws JexlException, ClassNotFoundException {
		this(Class.forName("java.lang." + clsName), rangeStr);
	}
	
	public Class<?> getCls() {
		return this.cls;
	}
	
	public String getRange() {
		return this.range;
	} 
	
	public boolean checkValidity(String valueStr) {
		Object value = null;
		
		
		if (cls.equals(Boolean.class)) { 			//Boolean
			if("true".equalsIgnoreCase(valueStr) || "false".equalsIgnoreCase(valueStr))
				value = Boolean.getBoolean(valueStr);
			else
				return false;
		} else if (cls.equals(Integer.class)) {		//Integer
			 try {
				value = Integer.parseInt(valueStr);
			 } catch (NumberFormatException e) {
				 return false;
			 }			
		} else if (cls.equals(Float.class)) {		//Float
			 try {
				value = Float.parseFloat(valueStr);
			 } catch (NumberFormatException e) {
				 return false;
			 }			
		} 
		
		if (this.exp != null) {
			JexlContext jexlContext = new MapContext();			//create a context and add data
			jexlContext.set("x", value);		
			Object result = this.exp.evaluate(jexlContext);		//now evaluate the expression, getting the result
			if (result instanceof Boolean)
				return (Boolean)result;
			else
				return false;
		}
		
		return true;
	}
}
