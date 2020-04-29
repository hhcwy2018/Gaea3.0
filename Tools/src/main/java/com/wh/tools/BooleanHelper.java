package com.wh.tools;

public class BooleanHelper {
	public static boolean stringToBoolean(String data) {
		if (data == null || data.trim().isEmpty())
			return false;
		
		return data.trim().equalsIgnoreCase("true");
	}
	
	public static boolean convertToBoolean(Object value) {
		if (value == null)
			return false;
		
		if (value instanceof Number)
			return ((Number)value).intValue() != 0;
					
		return stringToBoolean(value.toString());
	} 

}
