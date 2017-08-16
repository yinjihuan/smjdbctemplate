package com.cxytiandi.jdbc;

import java.lang.reflect.Field;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.RowMapper;

import com.cxytiandi.jdbc.util.Assert;
import com.cxytiandi.jdbc.util.ClassUtils;
import com.cxytiandi.jdbc.util.ReflectUtils;


/**
 * RowMapper工厂类
 * 根据传入的参数类型返回一个RowMapper的实例，此实例将自动注入除了
 * 关联实体类以外的所以类中声明属性的值
 */
@SuppressWarnings("unchecked")
public class RowMapperFactory {
	
	private final static Log logger = LogFactory.getLog(RowMapperFactory.class);
	
	public static <T> RowMapper<T> getRowMapper(Class<T> entityClass){
		
		return new RowMapperImpl<T>(entityClass, null);
		
	}
	
	@Deprecated
	public static <T> RowMapper<T> getRowMapper(Class<T> entityClass, String[] excludeFields){
		
		return new RowMapperImpl<T>(entityClass, excludeFields);
		
	}
	
	private static class RowMapperImpl<T> implements RowMapper<T> {
		private Class<T> entityClass;
		private String[] excludeFields;
		
		private Map<String, Object[]> namemap = new HashMap<String, Object[]>();
		
		public RowMapperImpl(Class<T> entityClass, String[] excludeFields){
			this.entityClass = entityClass;
			this.excludeFields = excludeFields;
		}

		public T mapRow(ResultSet rs, int index) throws SQLException {
			return byColumns(rs, index);
		}
		
		private T byColumns(ResultSet rs, int index) throws SQLException {
			Assert.notNull(this.entityClass);
			Object instance = null;
			try{
				 instance = entityClass.newInstance();
				 if(namemap.size()<1){
					ResultSetMetaData rsmd = rs.getMetaData();
					int columnCount = rsmd.getColumnCount();
					
					for(int i=0; i<columnCount; i++){
						String name = rsmd.getColumnName(i+1);
						logger.debug("DBColumnName:"+name+"\tDBType:"+rsmd.getColumnClassName(i+1));
						
						Object[] nameAndType = getFieldNameByColumnName(name);
						Assert.notNull(nameAndType, "No such field : " + name);
						
						namemap.put(name, nameAndType);
					}
					
				}
				for(Iterator<String> ite = namemap.keySet().iterator(); ite.hasNext();){
					String name = ite.next();
					Object[] nameAndType = namemap.get(name);
					String fieldName = (String) nameAndType[0];
					Class<?> type = (Class<?>) nameAndType[1];
					
					Object value = getValue(rs, name, type);
					
					if(logger.isDebugEnabled()){
						if(value != null){
							logger.debug("CurrentName:"+fieldName+"\tCurrentType:"+value.getClass().getCanonicalName());
						}else{
							logger.debug("CurrentName:"+fieldName+" Value is null !");
						}
					}
					ReflectUtils.callSetMethod(fieldName, type, instance, value);
				}
			}catch(Exception e){
				throw new RuntimeException(e.getMessage(), e);
			}
			return (T)instance;
		}
		
		private Object[] getFieldNameByColumnName(String columnName) throws NoSuchFieldException{
			String cache = CacheData.get(entityClass.getName() + "." + columnName);
			if (cache != null) {
				columnName = cache;
			}
			Field f = ReflectUtils.getField(entityClass, columnName, false);
			if(f != null){
				Object[] result = new Object[2];
				result[0] = f.getName();
				result[1] = f.getType();
				return result;
			}
			return null;
		}
		
		/*
		 * 得到正确的值
		 * @param rs
		 * @param name 数据库返回结果集的列名，与实体字段名对应
		 * @param type 定义的数据类型
		 * @return
		 * @throws SQLException
		 */
		private Object getValue(ResultSet rs, String name, Class<?> type) throws SQLException {
			if(ClassUtils.isPrimitiveWrapper(type)){
				type = ClassUtils.resolvePrimitiveClassName(type);
			}
			if(int.class.isAssignableFrom(type)){
				return rs.getInt(name);
			}else if(String.class.isAssignableFrom(type)){
				return rs.getString(name);
			}else if(long.class.isAssignableFrom(type)){
				return rs.getLong(name);
			}else if(boolean.class.isAssignableFrom(type)){
				return rs.getBoolean(name);
			}else if(double.class.isAssignableFrom(type)){
				return rs.getDouble(name);
			}else if(char.class.isAssignableFrom(type)){
				return rs.getString(name);
			}else if(short.class.isAssignableFrom(type)){
				return rs.getShort(name);
			}else if(float.class.isAssignableFrom(type)){
				return rs.getFloat(name);
			}else if(byte.class.isAssignableFrom(type)){
				return rs.getByte(name);
			}else if(Blob.class.isAssignableFrom(type)) {
				return rs.getBlob(name);
			}else if(type.isArray()){
				return rs.getArray(name);
			}else if(java.sql.Timestamp.class.isAssignableFrom(type)){
				return rs.getTimestamp(name);
			}else if(java.sql.Date.class.isAssignableFrom(type)){
				return rs.getDate(name);
			}else if(java.util.Date.class.isAssignableFrom(type)){
				return rs.getTimestamp(name);
			}else if(java.sql.Time.class.isAssignableFrom(type)){
				return rs.getTime(name);
			}
			return null;
		}
		
		@SuppressWarnings("unused")
		private boolean isExcludeFields(String fieldName){
			if(null == excludeFields || excludeFields.length < 1)
				return false;
			boolean result = false;
			for(int i=0; i<excludeFields.length; i++){
				if(excludeFields[i].equals(fieldName)){
					result = true;
					break;
				}
			}
			return result;
		}
	}
}