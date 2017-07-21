package com.cxytiandi.jdbc.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
@SuppressWarnings({"unchecked"})
public abstract class BeanUtils {
	private static Log logger = LogFactory.getLog(BeanUtils.class);
	
	/**
	 * 得到给定实体类的所有字段名，以逗号分隔
	 * @param entityClass 实体类
	 * @param prefix 前缀
	 * @param excludes 要排除的字段列表
	 * @return 返回逗号分隔的entityClass的所有声明字段，排除excludes指定的字段
	 */
	public static String[] getFieldNames(Class<?> entityClass, String... excludes){
		Field[] fs = extractFieldsFromPOJO(entityClass);
		List<String> rslt = new java.util.LinkedList<String>();
		for(int i=0;i<fs.length;i++){
			Field f = fs[i];
			com.cxytiandi.jdbc.annotation.Field cf = f.getAnnotation(com.cxytiandi.jdbc.annotation.Field.class);
			String fieldName = null;
			if (cf != null) {
				fieldName = cf.value();
			} else {
				fieldName = f.getName();
			}
			if(contains(fieldName, excludes)){
				continue;
			}
			rslt.add(fieldName);
		}
		return rslt.toArray(new String[rslt.size()]);
	}
	
	public static String[] getContainsFieldNames(Class<?> entityClass, String prefix, String... contains){
		Field[] fs = extractFieldsFromPOJO(entityClass);
		String p = prefix != null ? prefix : "";
		List<String> rslt = new java.util.LinkedList<String>();
		for(int i=0;i<fs.length;i++){
			Field f = fs[i];
			if(contains(f.getName(), contains)){
				rslt.add(p + f.getName());
			}
		}
		return rslt.toArray(new String[rslt.size()]);
	}
	
	private static boolean contains( String f, String... fs){
		if(fs == null)return false;
		boolean result = false;
		for(int i=0; i<fs.length; i++){
			if(fs[i].indexOf(",") != -1){
				return contains(f, fs[i].split(","));
			}else{
				if(f.equals(fs[i])){
					result = true;
					break;
				}
			}
		}
		return result;
	}
	
	 /**
	   * 抽取pojo对象的属性(private)
	   * <p>
	   * <li>要求传入的类符合pojo规范
	   * <li>返回的结果包含父类的属性
	   * 
	   * @param pojoClazz
	   * @return
	   */
	  public static Field[] extractFieldsFromPOJO(Class pojoClazz) {
	    List<Field> list = new ArrayList();
	    do {
	      Field[] dfs = pojoClazz.getDeclaredFields();
	      if (dfs != null) {
	        for (Field f : dfs) {
	          if (Modifier.isPrivate(f.getModifiers()) && Modifier.isStatic(f.getModifiers()) == false
	              && Modifier.isFinal(f.getModifiers()) == false)
	            list.add(f);
	        }
	      }
	      pojoClazz = pojoClazz.getSuperclass();
	    } while (pojoClazz != null
	        && pojoClazz.getSimpleName().equals(Object.class.getSimpleName()) == false);
	    return list.toArray(new Field[list.size()]);
	  }
	
	/**
	 * 将Map转换为Bean,Map中Key对应Bean中FieldName
	 * @param <T>
	 * @param data
	 * @param beanClass
	 * @return Bean实例，类型与beanClass参数中对应
	 */
	public static <T> T map2Bean(Map<String, Object> data, Class<T> beanClass){
		if(data == null)return null;
		Iterator<String> ite = data.keySet().iterator();
		try {
			T obj = beanClass.newInstance();
			while(ite.hasNext()){
				String key = ite.next();
				try{
					java.lang.reflect.Field field = beanClass.getDeclaredField(key);
					field.setAccessible(true);
					Object value = data.get(key);
					setValueToField(field, obj, value);
				} catch (NoSuchFieldException e) {
					logger.warn("The Type--"+beanClass.getCanonicalName()+" don't has a field which name is "+key);
				} 
			}
			return obj;
		}catch(Exception e){
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
	/**
	 * 将Map转换为多个Bean,Map中Key应为"Alias.FieldName"
	 * 例：Map<[a.id=1],[a.name="a"],[b.id=1],[b.name="b"]>
	 * @param <T>
	 * @param data
	 * @param alias 别名
	 * @param beanClass
	 * @return 多个Bean实例，类型和顺序与beanClass参数中对应
	 */
	public static Object[] map2Bean(Map<String, Object> data,String[] alias, Class<?>[] beanClass){
		if(data == null)return null;
		Iterator<String> ite = data.keySet().iterator();
		try {
			Object[] oa = new Object[alias.length];
			Map<String, Object> amap = new java.util.HashMap<String, Object>(alias.length);
			for(int i=0; i<alias.length; i++){
				Object o =  beanClass[i].newInstance();
				amap.put(alias[i], o);
				oa[i] = o;
			}
			while(ite.hasNext()){
				String key = ite.next();
				String[] keyspt = key.split("\\.");
				Object obj = amap.get(keyspt[0]);
				try{
					java.lang.reflect.Field field = obj.getClass().getDeclaredField(keyspt[1]);
					field.setAccessible(true);
					Object value = data.get(key);
					setValueToField(field, obj, value);
				} catch (NoSuchFieldException e) {
					logger.warn("The Type--"+obj.getClass().getCanonicalName()+" don't has a field which name is "+keyspt[1]);
				} 
			}
			return oa;
		}catch(Exception e){
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
	/**
	 * <h3>从源对象中复制属性到目标对象</h3>
	 * <p>对象可以是一个Bean，也可以是一个Map，只有同名的属性（区分大小写）会被复制</p>
	 * @param source 源对象
	 * @param target 目标对象
	 * @throws Exception
	 */
	public static void copyProperties(Object source, Object target) throws Exception {
		copyProperties(source, target, null, null);
	}
	/**
	 * <h3>从源对象中复制属性到目标对象</h3>
	 * <p>对象可以是一个Bean，也可以是一个Map，只有同名的属性（区分大小写）会被复制</p>
	 * <p>此方法额外提供一个值回调接口，可供转换值的类型，此特性在当两个对象的属性类型不一致时非常有用</p>
	 * @param source
	 * @param target
	 * @param valueCallback 
	 * @throws Exception
	 */
	public static void copyProperties(Object source, Object target, ValueCallback valueCallback) throws Exception {
		copyProperties(source, target, valueCallback, null);
	}
	/**
	 * <h3>从源对象中复制属性到目标对象</h3>
	 * <p>对象可以是一个Bean，也可以是一个Map，只有同名的属性（区分大小写）会被复制</p>
	 * <p>此方法可以排除不需要复制的属性</p>
	 * @param source
	 * @param target
	 * @param ignoreProperties
	 * @throws Exception
	 */
	public static void copyProperties(Object source, Object target, String[] ignoreProperties) throws Exception {
		copyProperties(source, target, null, ignoreProperties);
	}
	
	/**
	 * <h3>从源对象中复制属性到目标对象</h3>
	 * <p>对象可以是一个Bean，也可以是一个Map，只有同名的属性（区分大小写）会被复制</p>
	 * <p>此方法可以排除不需要复制的属性</p>
	 * <p>另外提供一个值回调接口，可供转换值的类型，此特性在当两个对象的属性类型不一致时非常有用</p>
	 * @author WuBo
	 * @CreateDate 2012-7-30 下午05:26:08
	 * @param source
	 * @param target
	 * @param valueCallback
	 * @param ignoreProperties
	 * @param skipNullValue 是否跳过Null值
	 * @throws Exception
	 */
	public static void copyProperties(Object source, Object target,
			ValueCallback valueCallback, String[] ignoreProperties) throws Exception {
		Class srcClass = source.getClass();
		Class targetClass = target.getClass();
		
		boolean isSrcMap = Map.class.isAssignableFrom(srcClass);
		boolean isTargetMap = Map.class.isAssignableFrom(targetClass);
		
		if(! isSrcMap){
			Field[] fields = ReflectUtils.getDeclaredFields(srcClass, ignoreProperties, true, false);
			for(Field field : fields){
				if( Modifier.isStatic(field.getModifiers()) ){
					continue; //skip static field
				}
				String fieldName = field.getName();
				Method getter = ReflectUtils.getter(srcClass, fieldName);
				Object value = getter.invoke(source);
				if(valueCallback != null){
					value = valueCallback.onValue(fieldName, value);
					if(valueCallback.skipCopy(fieldName, value)){
						continue;
					}
				}
				if(isTargetMap){
					((Map)target).put(fieldName, value);
				}else{
					Method setter = ReflectUtils.setter(targetClass, fieldName, getter.getReturnType());
					if(setter == null && value != null){
						setter = ReflectUtils.setter(targetClass, fieldName, value.getClass());
					}
					if(setter != null){
						setter.invoke(target, value);
					}
				}
			}
		}else{
			Map map = (Map) source;
			Set<String> keys = map.keySet();
			List<String> excludeProps = Collections.EMPTY_LIST;
			if(ignoreProperties != null){
				excludeProps = Arrays.asList(ignoreProperties);
			}
			for(String key : keys){
				if(excludeProps.contains(key)){
					continue;
				}
				Object value = map.get(key);
				if(valueCallback != null){
					value = valueCallback.onValue(key, value);
					if(valueCallback.skipCopy(key, value)){
						continue;
					}
				}
				if(isTargetMap){
					((Map)target).put(key, value);
				}else{
					if(value != null){
						Method setter = ReflectUtils.setter(targetClass, key, value.getClass());
						if(setter != null){
							setter.invoke(target, value);
						}
					}
				}
			}
		}
	}
	
	/**
	 * 值回调
	 */
	public static class ValueCallback {
		protected Object onValue(String propertyName, Object value){
			return value;
		}
		/**
		 * 跳过此值的复制
		 * @param propertyName
		 * @param value
		 * @return
		 */
		protected boolean skipCopy(String propertyName, Object value){
			return false;
		}
	}
	
	private static void setValueToField(java.lang.reflect.Field field ,Object obj, Object value)
	throws Exception{
		if(value == null){
			return;
		}
		try{
			field.set(obj, value);
		}catch(IllegalArgumentException e){
			Class<?> type = field.getType();
			if(ClassUtils.isPrimitiveWrapper(type)){
				type = ClassUtils.resolvePrimitiveClassName(type);
			}
			if(int.class.isAssignableFrom(type)){
				value = new Integer(value.toString());
			}else if(long.class.isAssignableFrom(type)){
				value = new Long(value.toString());
			}else if(boolean.class.isAssignableFrom(type)){
				value = new Boolean(value.toString());
			}else if(double.class.isAssignableFrom(type)){
				value = new Double(value.toString());
			}else if(char.class.isAssignableFrom(type)){
				value = value.toString().charAt(0);
			}else if(short.class.isAssignableFrom(type)){
				value = new Short(value.toString());
			}else if(float.class.isAssignableFrom(type)){
				value = new Float(value.toString());
			}else if(byte.class.isAssignableFrom(type)){
				value = new Byte(value.toString());
			}
			field.set(obj, value);
		}
	}
}
