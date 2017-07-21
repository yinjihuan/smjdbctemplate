package com.cxytiandi.jdbc.util;

import java.util.Date;


@SuppressWarnings("unchecked")
public class ValueUtils {
	/**
	 * 
	 * @CreateDate 2012-6-12 上午08:54:43
	 * @param <T>
	 * @param value
	 * @param defaultValue
	 * @return
	 */
	public static <T> T null2Default(T value, T defaultValue){
		if(value != null){
			return value;
		}
		return defaultValue;
	}

	public static String number2Str(Number obj){
		return number2Str(obj, "");
	}
	
	public static String number2Str(Number obj, String nullValue){
		if(obj != null){
			return obj.toString();
		}
		return nullValue;
	}
	
	/**
	 * <h3>给定值，返回期望的类型</h3>
	 * <p>注意：期望类型不能为Java的8个简单类型</p>
	 * @param <T>
	 * @param value
	 * @param type
	 * @return
	 */
	public static <T> T convert(Object value, Class<T> type){
		return (T) convert(value, type, null);
	}
	/**
	 * <h3>给定值，返回期望的类型</h3>
	 * <p>注意：期望类型不能为Java的8个简单类型</p>
	 * @param <T>
	 * @param value
	 * @param type
	 * @param datePattern
	 * @return
	 */
	public static <T> T convert(Object value, Class<T> type, String datePattern){
		if(value == null){
			return null;
		}
		try{
			if(type.isInstance(value)){
				return (T) value;
			}
			if(String.class.equals(type)){
				if(value instanceof Date){
					if(datePattern != null){
						return (T) DateUtils.date2Str((Date) value, datePattern);
					}
					return (T) DateUtils.date2Str((Date) value);
				}
				return (T) value.toString();
			}
			if(Number.class.isAssignableFrom(type)){
				if(value instanceof Date){
					return (T) type.getConstructor(String.class).newInstance(((Date) value).getTime()+"");
				}
				if(value instanceof Boolean){
					return (T) type.getConstructor(String.class).newInstance(((Boolean)value).booleanValue() ? 1+"" : 0+"");
				}
				if(value instanceof Number){
					Number n = (Number) value;
					if(Integer.class.equals(type)){
						return (T) new Integer(n.intValue());
					}
					if(Long.class.equals(type)){
						return (T) new Long(n.longValue());
					}
					if(Short.class.equals(type)){
						return (T) new Short(n.shortValue());
					}
					if(Byte.class.equals(type)){
						return (T) new Byte(n.byteValue());
					}
					if(Float.class.equals(type)){
						return (T) new Float(n.floatValue());
					}
				}
				return (T) type.getConstructor(String.class).newInstance(value.toString());
			}
			if(Boolean.class.equals(type)){
				return (T) Boolean.valueOf(value.toString());
			}
			if(Character.class.equals(type)){
				return (T) new Character(value.toString().charAt(0));
			}
			if(Date.class.equals(type)){
				if(value instanceof Number){
					return (T) new Date(((Number)value).longValue());
				}
				if(value instanceof String){
					if(datePattern != null){
						return (T) DateUtils.str2Date(value.toString(), datePattern);
					}
					return (T)DateUtils.str2Date(value.toString());
				}
			}
			if(type.isPrimitive()){
				throw new IllegalArgumentException("不支持简单类型的转换，请转换为简单类型的封装类型");
			}
		}catch(Exception e){
			e.printStackTrace();
			throw new ClassCastException(value.getClass().getName()+" cannot be cast to "+type.getName());
		}
		throw new ClassCastException(value.getClass().getName()+" cannot be cast to "+type.getName());
	}
	
}
