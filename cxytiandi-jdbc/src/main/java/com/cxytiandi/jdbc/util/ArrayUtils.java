package com.cxytiandi.jdbc.util;

import java.lang.reflect.Array;
public abstract class ArrayUtils {
	
	public static boolean isEmpty(Object[] objs){
		return objs == null || objs.length == 0;
	}
	
	public static <T> String join(T arrayObj, String split){
		return join(arrayObj, split, "");
	}

	public static <T> String join(T arrayObj, String split, String quoteStr){
		if(arrayObj == null)return null;
		
		if(! arrayObj.getClass().isArray()){
			throw new IllegalArgumentException("The first param must be an array");
		}
		
		StringBuffer sb = new StringBuffer();
		for(int i=0; i<Array.getLength(arrayObj); i++){
			if(i>0){
				sb.append(split);
			}
			sb.append(quoteStr);
			sb.append(Array.get(arrayObj, i));
			sb.append(quoteStr);
		}
		return sb.toString();
	}
	
}