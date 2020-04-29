package com.wh.tools;

public class NumberHelper {
	public static Object convertNumber(Number number, Class<?> c) {
		if (c.equals(Integer.class)) {
			return number.intValue();
		}else if (c.equals(Float.class)) {
			return number.floatValue();
		}else if (c.equals(Double.class)) {
			return number.doubleValue();
		}else if (c.equals(Short.class)) {
			return number.shortValue();
		}else if (c.equals(Byte.class)) {
			return number.byteValue();
		}else if (c.equals(Long.class)) {
			return number.longValue();
		}else {
			throw new NumberFormatException();
		}
	}
	
	public static Object stringToNumber(String data, Class<?> c) {
		if (c.equals(Integer.class)) {
			return Integer.parseInt(data);
		}else if (c.equals(Float.class)) {
			return Float.parseFloat(data);
		}else if (c.equals(Double.class)) {
			return Double.parseDouble(data);
		}else if (c.equals(Short.class)) {
			return Short.parseShort(data);
		}else if (c.equals(Byte.class)) {
			return Byte.parseByte(data);
		}else if (c.equals(Long.class)) {
			return Long.parseLong(data);
		}else {
			throw new NumberFormatException();
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T convertToNumber(Object value, Class<T> c) {
		if (value == null)
			return (T)convertNumber(0, c);

		if (value instanceof Number)
			return (T)convertNumber((Number)value, c);
					
		String tmp = value.toString().trim();
		if (tmp.isEmpty())
			return (T)convertNumber(0, c);
		else {
			return (T)stringToNumber((String)tmp, c);
		}
	} 
}
