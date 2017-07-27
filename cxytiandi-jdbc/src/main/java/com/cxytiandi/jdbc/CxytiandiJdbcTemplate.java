package com.cxytiandi.jdbc;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.util.Assert;

import com.cxytiandi.jdbc.annotation.AutoId;
import com.cxytiandi.jdbc.annotation.TableName;
import com.cxytiandi.jdbc.keygen.DefaultKeyGenerator;
import com.cxytiandi.jdbc.keygen.KeyGenerator;
import com.cxytiandi.jdbc.keygen.KeyGeneratorFactory;
import com.cxytiandi.jdbc.util.ArrayUtils;
import com.cxytiandi.jdbc.util.BeanUtils;
import com.cxytiandi.jdbc.util.ClassUtils;
import com.cxytiandi.jdbc.util.ClasspathPackageScannerUtils;
import com.cxytiandi.jdbc.util.ReflectUtils;

/**
 * 增强版JdbcTemplate类<br>
 * 
 * @author yinjihuan
 *
 */
public class CxytiandiJdbcTemplate extends JdbcTemplate {
	private IDBHelper dbHelper = new MySQLHelper();
	private static final Logger logger = LoggerFactory.getLogger(CxytiandiJdbcTemplate.class);
	private KeyGenerator keyGenerator = KeyGeneratorFactory.createKeyGenerator(DefaultKeyGenerator.class);
	
	public CxytiandiJdbcTemplate(String poPackage) {
		try {
			List<String> classList = new ClasspathPackageScannerUtils(poPackage).getFullyQualifiedClassNameList();
			for (String className : classList) {
				Class<?> clazz = Class.forName(className);
				if(!clazz.isAnnotationPresent(TableName.class)){
					throw new NullPointerException("数据表对应的实体类必须加TableName注解:" + className);
				}
				Field[] fields = clazz.getDeclaredFields();
				for (Field field : fields) {
					if (Modifier.isStatic(field.getModifiers())) {
						continue;
					}
					if (!field.isAnnotationPresent(com.cxytiandi.jdbc.annotation.Field.class)) {
						throw new NullPointerException("数据表对应的实体类的属性必须加Field注解：" + className + "." + field.getName());
					}
					com.cxytiandi.jdbc.annotation.Field cf = field.getAnnotation(com.cxytiandi.jdbc.annotation.Field.class);
					CacheData.put(className + "." + cf.value() , field.getName());
				}
			}
		} catch (Exception e) {
			logger.error("扫描" + poPackage + "中的PO异常", e);
			throw new RuntimeException(e);
		}
	}
	
	public long count(Class<?> entityClass) {
		return doQueryCount(entityClass, new String[] {}, new Object[] {});
	}
	
	public long count(Class<?> entityClass, String field, Object value) {
		Assert.notNull(field);
		Assert.notNull(value);
		return doQueryCount(entityClass, new String[] { field }, new Object[] { value });
	}
	
	public long count(String sql, Object[] values) {
		return doQueryCountResult(sql, values);
	}
	
	public long count(Class<?> entityClass, String[] params, Object[] values) {
		return doQueryCount(entityClass, params, values);
	}
	
	private long doQueryCount(Class<?> entityClass, String[] params, Object[] values) {
		Assert.notNull(entityClass);
		String tableName = getTableName(entityClass);
		StringBuilder sql = new StringBuilder(Constants.COUNT_SQL)
				.append(tableName)
				.append(Constants.ONE_EQ_ONE_SQL);
		for (int i = 0; i < params.length; i++) {
			sql.append(" and "+params[i]+"= ?");
		}
		return doQueryCountResult(sql.toString(), values);
	}
	
	private long doQueryCountResult(String sql, Object[] values) {
		Assert.notNull(sql);
		values = formatValues(values);
		printSqlLog(sql.toString(), values);
		return super.queryForObject(sql, Long.class, values);
	}
	
	public boolean exists(String sql, Object[] values) {
		return doExists(sql, values);
	}

	public boolean exists(Class<?> entityClass, String[] params, Object[] values) {
		String[] fieldNames = BeanUtils.getFieldNames(entityClass);
		StringBuilder sql = getParamSql(entityClass, fieldNames, params, values, new Orders[]{});
		return doExists(sql.toString(), values);
	}
	
	public boolean exists(Class<?> entityClass, String field, Object value) {
		Assert.notNull(field);
		Assert.notNull(value);
		String[] fieldNames = BeanUtils.getFieldNames(entityClass);
		StringBuilder sql = getParamSql(entityClass, fieldNames, new String[] {field}, new Object[]{value}, new Orders[]{});
		return doExists(sql.toString(), new Object[] { value });
	}
	
	private boolean doExists(String sql, Object[] values) {
		values = formatValues(values);
		List<Map<String, Object>> list = this.queryForPage(0, 1, sql, values);
		if (list != null && list.size() > 0) {
			return true;
		}
		return false;
	}
	
	public <T> List<T> in(Class<T> entityClass, String field, Object[] values) {
		String[] fieldNames = BeanUtils.getFieldNames(entityClass);
		return doQuery(entityClass, getInSql(entityClass, fieldNames, field, values, new Orders[]{}), values);
	}
	
	public <T> List<T> in(Class<T> entityClass, String field, Object[] values, Orders[] orders) {
		String[] fieldNames = BeanUtils.getFieldNames(entityClass);
		return doQuery(entityClass, getInSql(entityClass, fieldNames, field, values, orders), values);
	}
	
	public <T> List<T> in(Class<T> entityClass, String[] fieldNames, String field, Object[] values) {
		return doQuery(entityClass, getInSql(entityClass, fieldNames, field, values, new Orders[]{}), values);
	}
	
	public <T> List<T> in(Class<T> entityClass, String[] fieldNames, String field, Object[] values, Orders[] orders) {
		return doQuery(entityClass, getInSql(entityClass, fieldNames, field, values, orders), values);
	}
	
	private String getInSql(Class<?> entityClass, String[] fieldNames, String field, Object[] values, Orders[] orders) {
		StringBuilder sql = getParamSql(entityClass, fieldNames, new String[]{}, values, new Orders[]{});
		sql.append(" and ");
		sql.append(field);
		sql.append(" in (");
		for (int i = 0; i < values.length; i++) {
			sql.append(" ?,");
		}
		sql.delete(sql.length()-1, sql.length());
		sql.append(" )");
		sql.append(dbHelper.wrapToOrderBySql(orders));
		return sql.toString();
	}
	
	public int[] batchSave(Class<?> entityClass, List<?> entitys, String... excludeFields) {
		Assert.notNull(entitys);
		List<Object[]> allValues = new ArrayList<Object[]>();
		String[] fieldNames = BeanUtils.getFieldNames(entityClass, excludeFields);
		return doBatchSave(entityClass, entitys, allValues, fieldNames);
	}
	
	public int[] batchSaveByContainsFields(Class<?> entityClass, List<?> entitys, String... containsFields) {
		Assert.notNull(entitys);
		List<Object[]> allValues = new ArrayList<Object[]>();
		String[] fieldNames = BeanUtils.getContainsFieldNames(entityClass, "", containsFields);
		return doBatchSave(entityClass, entitys, allValues, fieldNames);
	}

	/**
	 * <h3>保存一个实体对象</h3>
	 * <p>此方法用反射机制动态生成insert语句，类名即是表名</p>
	 * <p>通常一个自增主键的表，插入记录的时候，需要指定排除主键</p>
	 * @param entityClass
	 * @param entity
	 * @param excludeFields 要排除的字段
	 * @return 主键，如果数据库不支持java.sql.PreparedStatement.RETURN_GENERATED_KEYS，则返回null
	 */
	public <T> Serializable save(Class<T> entityClass, Object entity, String... excludeFields) {
		String[] fieldNames = BeanUtils.getFieldNames(entityClass, excludeFields);
		return doSave(entityClass, entity, fieldNames);
	}
	
	public <T> Serializable saveByContainsFields(Class<T> entityClass, Object entity, String... containsFields) {
		String[] fieldNames = BeanUtils.getContainsFieldNames(entityClass, "", containsFields);
		return doSave(entityClass, entity, fieldNames);
	}
	
	private int[] doBatchSave(Class<?> entityClass, List<?> entitys, List<Object[]> allValues, String[] fieldNames) {
		String[] paramPlaceHolders = new String[fieldNames.length];
		Arrays.fill(paramPlaceHolders, 0, paramPlaceHolders.length, "?");
		StringBuilder sql = getSaveSql(entityClass, fieldNames, paramPlaceHolders);
		@SuppressWarnings("unchecked")
		List<Object> list = (List<Object>) entitys;
		for (Object entity : list) {
			final Object[] values = getSaveValues(entityClass, entity, fieldNames);
			printSqlLog(sql.toString(), values);
			allValues.add(values);
		}
		return batchUpdate(sql.toString(), allValues);
	}
	
	private <T> Serializable doSave(Class<T> entityClass, Object entity, String[] fieldNames) {
		setAutoId(entityClass, entity, fieldNames);
		String[] paramPlaceHolders = new String[fieldNames.length];
		Arrays.fill(paramPlaceHolders, 0, paramPlaceHolders.length, "?");
		final StringBuilder sql = getSaveSql(entityClass, fieldNames, paramPlaceHolders);
		
		final Object[] values = getSaveValues(entityClass, entity, fieldNames);
		printSqlLog(sql.toString(), values);
		if(supportsGeneratedKey()){
			return super.execute(new ConnectionCallback<Serializable>() {
				public Serializable doInConnection(Connection conn)
						throws SQLException, DataAccessException {
					java.sql.PreparedStatement stmt = null;
					java.sql.ResultSet rs = null;
					Serializable pk = null;
					try{
						stmt = conn.prepareStatement(sql.toString(), java.sql.PreparedStatement.RETURN_GENERATED_KEYS);
						if(values != null){
							for(int i=0,len=values.length; i<len; i++) {
								stmt.setObject(i+1, values[i]);
							}
							stmt.execute();
							rs = stmt.getGeneratedKeys();
							if(rs.next()){
								pk = (Serializable) rs.getObject(1);
							}
						}
						return pk;
					}catch(Exception e){
						throw new RuntimeException(e.getMessage(), e);
					}finally{
						try{
							if(rs != null){rs.close();}
							if(stmt != null){stmt.close();}
						}catch(Exception e){}
					}
				}
			});
		} else {
			execute(sql.toString(), values);
			return null;
		}
	}

	private <T> void setAutoId(Class<T> entityClass, Object entity, String[] fieldNames) {
		try {
			Field field = ReflectUtils.getField(entityClass, fieldNames, AutoId.class);
			if (field != null) {
				if (field.getType().getSimpleName().equals("String")) {
					ReflectUtils.callSetMethod(field.getName(), String.class, entity, keyGenerator.generateKey().toString());
				} else {
					ReflectUtils.callSetMethod(field.getName(), Long.class, entity, keyGenerator.generateKey());
				}
			}
		} catch (NoSuchFieldException e) {
			logger.error("设置分布式主键ID异常", e);
		}
	}

	private <T> Object[] getSaveValues(Class<T> entityClass, Object entity, String[] fieldNames) {
		setAutoId(entityClass, entity, fieldNames);
		final Object[] values = new Object[fieldNames.length];
		for(int i=0; i<fieldNames.length; i++) {
			String fieldName = fieldNames[i];
			String cache = CacheData.get(entityClass.getName() + "." + fieldName);
			values[i] = ReflectUtils.callGetMethod(cache, entity);
		}
		return values;
	}

	private <T> StringBuilder getSaveSql(Class<T> entityClass, String[] fieldNames, String[] paramPlaceHolders) {
		String tableName = getTableName(entityClass);
		final StringBuilder sql = new StringBuilder("insert into ").append(tableName)
				.append("(").append(ArrayUtils.join(fieldNames, ", "))
				.append(") values(").append(ArrayUtils.join(paramPlaceHolders, ", ")).append(")");
		return sql;
	}
	
	public int[] batchUpdate(Class<?> entityClass, List<?> entitys, String pkField, String... excludeFields) {
		Assert.notNull(entitys);
		List<Object[]> allValues = new ArrayList<Object[]>();
		Set<String> set = new HashSet<String>(excludeFields.length + 1);
		set.add(pkField);
		set.addAll(Arrays.asList(excludeFields));
		String[] fieldNames = BeanUtils.getFieldNames(entityClass, set.toArray(new String[set.size()]));
		return doBatchUpdate(entityClass, entitys, pkField, allValues, fieldNames);
		
	}

	public int[] batchUpdateByContainsFields(Class<?> entityClass, List<?> entitys, String pkField, String... containsFields) {
		Assert.notNull(entitys);
		List<Object[]> allValues = new ArrayList<Object[]>();
		Set<String> set = new HashSet<String>(containsFields.length + 1);
		set.addAll(Arrays.asList(containsFields));
		String[] fieldNames = BeanUtils.getContainsFieldNames(entityClass, "", set.toArray(new String[set.size()]));
		return doBatchUpdate(entityClass, entitys, pkField, allValues, fieldNames);
		
	}
	
	private int[] doBatchUpdate(Class<?> entityClass, List<?> entitys, String pkField, List<Object[]> allValues,
			String[] fieldNames) {
		if(fieldNames.length == 0){
			throw new IllegalArgumentException("请设置要修改的字段，不能全部排除掉");
		}
		String tableName = getTableName(entityClass);
		StringBuilder sql = new StringBuilder("update ").append(tableName);
	
		for(int i=0; i<fieldNames.length; i++) {
			String fieldName = fieldNames[i];
			if(i==0)sql.append(" set "); else sql.append(", ");
			sql.append(fieldName+" = ?");
		}
		sql.append(" where "+pkField+" = ?");
		
		@SuppressWarnings("unchecked")
		List<Object> list = (List<Object>) entitys;
		for (Object en : list) {
			Object[] values = new Object[fieldNames.length + 1];
			for(int i=0; i<fieldNames.length; i++) {
				String fieldName = fieldNames[i];
				String cache = CacheData.get(entityClass.getName() + "." + fieldName);
				values[i] = ReflectUtils.callGetMethod(cache, en);
			}
			values[values.length-1] = ReflectUtils.callGetMethod(pkField, en);
			printSqlLog(sql.toString(), values);
			allValues.add(values);
		}
		
		return batchUpdate(sql.toString(), allValues);
	}
	
	
	
	/**
	 * <h3>更新一个实体对象</h3>
	 * <p>此方法用反射机制动态生成update语句，类名即是表名，只用于根据主键定位记录的更新</p>
	 * <p>主键会自动排除，不用再指定排除主键，当你这么做时，也不会有错误</p>
	 * @param entityClass
	 * @param entity
	 * @param pkField 主键
	 * @param excludeFields 要排除的字段
	 */
	public <T> void update(Class<T> entityClass, Object entity, String pkField, String... excludeFields) {
		Set<String> set = new HashSet<String>(excludeFields.length + 1);
		set.add(pkField);
		set.addAll(Arrays.asList(excludeFields));
		String[] fieldNames = BeanUtils.getFieldNames(entityClass, set.toArray(new String[set.size()]));
		doUpdateByContainsFields(entityClass, entity, pkField, fieldNames);
	}
	
	public <T> void updateByContainsFields(Class<T> entityClass, Object entity, String pkField, String... containsFields) {
		Set<String> set = new HashSet<String>(containsFields.length + 1);
		set.addAll(Arrays.asList(containsFields));
		String[] fieldNames = BeanUtils.getContainsFieldNames(entityClass, "", set.toArray(new String[set.size()]));
		doUpdateByContainsFields(entityClass, entity, pkField, fieldNames);
	}
	
	private <T> void doUpdateByContainsFields(Class<T> entityClass, Object entity, String pkField, String[] fieldNames) {
		if(fieldNames.length == 0){
			throw new IllegalArgumentException("请设置要修改的字段，不能全部排除掉");
		}
		String tableName = getTableName(entityClass);
		StringBuilder sql = new StringBuilder("update ").append(tableName);
	
		Object[] values = new Object[fieldNames.length + 1];
		for(int i=0; i<fieldNames.length; i++) {
			String fieldName = fieldNames[i];
			if(i==0)sql.append(" set "); else sql.append(", ");
			sql.append(fieldName+" = ?");
			String cache = CacheData.get(entityClass.getName() + "." + fieldName);
			values[i] = ReflectUtils.callGetMethod(cache, entity);
		}
		
		sql.append(" where "+pkField+" = ?");
		String cache = CacheData.get(entityClass.getName() + "." + pkField);
		values[values.length-1] = ReflectUtils.callGetMethod(cache, entity);
		execute(sql.toString(), values);
	}
	
	/**
	 * <h3>根据主键删除一个实体对象</h3>
	 * <p>此方法用反射机制动态生成delete语句，类名即是表名</p>
	 * @param entityClass
	 * @param pkField 主键名
	 * @param pk 主键值
	 */
	public <T> void deleteById(Class<T> entityClass, String pkField, Object pk) {
		String tableName = getTableName(entityClass);
		StringBuilder sql = new StringBuilder(Constants.DELETE_SQL).append(tableName)
				.append(" where "+pkField+" = ?");
		execute(sql.toString(), pk);
	}
	
	public <T> void deleteById(String sql, Object...values) {
		execute(sql, values);
	}
	
	public <T> void deleteByParams(Class<T> entityClass, String[] params, Object[] values) {
		String tableName = getTableName(entityClass);
		StringBuilder sql = new StringBuilder(Constants.DELETE_SQL).append(tableName)
				.append(Constants.ONE_EQ_ONE_SQL);
		for (int i = 0; i < params.length; i++) {
			sql.append(" and ").append(params[i]).append(" = ?");
		}
		execute(sql.toString(), values);
	}
	
	/**
	 * <h3>根据主键得到一个实体对象</h3>
	 * @param entityClass
	 * @param pkField
	 * @param pk
	 * @return
	 */
	public <T> T getById(Class<T> entityClass, String pkField, Object pk) {
		String[] fieldNames = BeanUtils.getFieldNames(entityClass);
		StringBuilder sql = getParamSql(entityClass, fieldNames, new String[] {pkField}, new Object[]{pk}, new Orders[]{});
		return get(entityClass, sql.toString(), pk);
	}
	
	public <T> T get(Class<T> entityClass, String sql, Object... values) {
		values = formatValues(values);
		sql = dbHelper.wrapToPageSql(sql, 0, 1);
		printSqlLog(sql, values);
		List<T> list = super.query(sql, values, RowMapperFactory.getRowMapper(entityClass));
		if(list != null && list.size() > 0){
			return list.get(0);
		}
		return null;
	}
	
	public <T> T getByParams(Class<T> entityClass, String[] params, Object[] values) {
		String[] fieldNames = BeanUtils.getFieldNames(entityClass);
		return doGetByParams(entityClass, fieldNames, params, values, new Orders[] {});
	}
	
	public <T> T getByParams(Class<T> entityClass, String[] params, Object[] values, Orders[] orders) {
		String[] fieldNames = BeanUtils.getFieldNames(entityClass);
		return doGetByParams(entityClass, fieldNames, params, values, orders);
	}
	
	public <T> T getByParams(Class<T> entityClass, String[] fieldNames, String[] params, Object[] values) {
		return doGetByParams(entityClass, fieldNames, params, values, new Orders[] {});
	}
	
	public <T> T getByParams(Class<T> entityClass, String[] fieldNames, String[] params, Object[] values, Orders[] orders) {
		return doGetByParams(entityClass, fieldNames, params, values, orders);
	}
	
	private <T> T doGetByParams(Class<T> entityClass, String[] fieldNames, String[] params, Object[] values, Orders[] orders) {
		StringBuilder sql = getParamSql(entityClass, fieldNames, params, values, orders);
		return get(entityClass, sql.toString(), values);
	}
	
	public <T> List<T> findByParams(Class<T> entityClass, String[] params, Object[] values) {
		String[] fieldNames = BeanUtils.getFieldNames(entityClass);
		return doFindByParams(entityClass, fieldNames, params, values, new Orders[]{});
	}
	
	public <T> List<T> findByParams(Class<T> entityClass, String[] fieldNames, String[] params, Object[] values) {
		return doFindByParams(entityClass, fieldNames, params, values, new Orders[]{});
	}
	
	private <T> List<T> doFindByParams(Class<T> entityClass, String[] fieldNames, String[] params, Object[] values, Orders[] orders) {
		StringBuilder sql = getParamSql(entityClass, fieldNames, params, values, orders);
		values = formatValues(values);
		return doExecuteListSql(entityClass, values, sql.toString());
	}

	private <T> List<T> doExecuteListSql(Class<T> entityClass, Object[] values, String sql) {
		printSqlLog(sql.toString(), values);
		return super.query(sql.toString(), values, RowMapperFactory.getRowMapper(entityClass));
	}

	private <T> StringBuilder getParamSql(Class<T> entityClass, String[] fieldNames, String[] params, Object[] values, Orders[] orders) {
		String tableName = getTableName(entityClass);
		StringBuilder sql = new StringBuilder("select ").append(ArrayUtils.join(fieldNames, ", "))
				.append(" from ").append(tableName)
				.append(Constants.ONE_EQ_ONE_SQL);
		if (params != null) {
			for (int i = 0; i < params.length; i++) {
				if (values[i] !=null) {
					sql.append(" and "+params[i]+"= ?");
				}
			}
		}
		sql.append(dbHelper.wrapToOrderBySql(orders));
		return sql;
	}
	
	public <T> List<T> list(Class<T> entityClass){
		String[] fieldNames = BeanUtils.getFieldNames(entityClass);
		return doFindByParams(entityClass, fieldNames, new String[] {}, new Object[] {}, new Orders[]{});
	}
	
	public <T> List<T> list(Class<T> entityClass, Orders[] orders){
		String[] fieldNames = BeanUtils.getFieldNames(entityClass);
		return doFindByParams(entityClass, fieldNames, new String[] {}, new Object[] {}, orders);
	}
	
	public <T> List<T> list(Class<T> entityClass, String sql){
		return doExecuteListSql(entityClass, new Object[] {}, sql);
		
	}
	
	public <T> List<T> list(Class<T> entityClass, String sql, Object[] values){
		return doExecuteListSql(entityClass, values, sql);
	}
	
	public <T> List<T> list(Class<T> entityClass, String[] fieldNames){
		return doFindByParams(entityClass, fieldNames, new String[] {}, new Object[] {}, new Orders[]{});
	}
	
	public <T> List<T> list(Class<T> entityClass, String[] fieldNames, Orders[] orders){
		return doFindByParams(entityClass, fieldNames, new String[] {}, new Object[] {}, orders);
	}
	
	public <T> List<T> list(Class<T> entityClass, String param, Object value){
		String[] fieldNames = BeanUtils.getFieldNames(entityClass);
		return doFindByParams(entityClass, fieldNames, new String[] {param}, new Object[] { value }, new Orders[]{});
	}
	
	public <T> List<T> list(Class<T> entityClass, String param, Object value, Orders[] orders){
		String[] fieldNames = BeanUtils.getFieldNames(entityClass);
		return doFindByParams(entityClass, fieldNames, new String[] {param}, new Object[] { value }, orders);
	}
	
	public <T> List<T> list(Class<T> entityClass, String[] fieldNames, String param, Object value){
		return doFindByParams(entityClass, fieldNames, new String[] {param}, new Object[] { value }, new Orders[]{});
	}
	
	public <T> List<T> list(Class<T> entityClass, String[] fieldNames, String param, Object value, Orders[] orders){
		return doFindByParams(entityClass, fieldNames, new String[] {param}, new Object[] { value }, orders);
	}
	
	public <T> List<T> list(Class<T> entityClass, String[] params, Object[] values){
		String[] fieldNames = BeanUtils.getFieldNames(entityClass);
		return doFindByParams(entityClass, fieldNames, params, values, new Orders[]{});
	}
	
	public <T> List<T> list(Class<T> entityClass, String[] params, Object[] values, Orders[] orders){
		String[] fieldNames = BeanUtils.getFieldNames(entityClass);
		return doFindByParams(entityClass, fieldNames, params, values, orders);
	}
	
	public <T> List<T> list(Class<T> entityClass, String[] fieldNames, String[] params, Object[] values){
		return doFindByParams(entityClass, fieldNames, params, values, new Orders[]{});
	}
	
	public <T> List<T> list(Class<T> entityClass, String[] fieldNames, String[] params, Object[] values, Orders[] orders){
		return doFindByParams(entityClass, fieldNames, params, values, orders);
	}
	
	public <T> List<T> listForPage(Class<T> entityClass, int start, int limit, String sql, Object[] values) {
		String new_sql = dbHelper.wrapToPageSql(sql, start, limit);
		return doQuery(entityClass, new_sql, values);
	}
	
	public <T> List<T> listForPage(Class<T> entityClass, int start, int limit) {
		String[] fieldNames = BeanUtils.getFieldNames(entityClass);
		StringBuilder sql = getParamSql(entityClass, fieldNames, new String[] {}, new Object[]{}, new Orders[] {});
		String new_sql = dbHelper.wrapToPageSql(sql.toString(), start, limit);
		return doQuery(entityClass, new_sql, new Object[] {});
	}
	
	public <T> List<T> listForPage(Class<T> entityClass, String param, Object value, int start, int limit) {
		String[] fieldNames = BeanUtils.getFieldNames(entityClass);
		StringBuilder sql = getParamSql(entityClass, fieldNames, new String[] {param}, new Object[]{value}, new Orders[] {});
		String new_sql = dbHelper.wrapToPageSql(sql.toString(), start, limit);
		return doQuery(entityClass, new_sql, new Object[] {value});
	}
	
	public <T> List<T> listForPage(Class<T> entityClass, String[] fieldNames, String param, Object value, int start, int limit) {
		StringBuilder sql = getParamSql(entityClass, fieldNames, new String[] {param}, new Object[]{value}, new Orders[] {});
		String new_sql = dbHelper.wrapToPageSql(sql.toString(), start, limit);
		return doQuery(entityClass, new_sql, new Object[] {value});
	}
	
	public <T> List<T> listForPage(Class<T> entityClass, String param, Object value, int start, int limit, Orders[] orders) {
		String[] fieldNames = BeanUtils.getFieldNames(entityClass);
		StringBuilder sql = getParamSql(entityClass, fieldNames, new String[] {param}, new Object[]{value}, orders);
		String new_sql = dbHelper.wrapToPageSql(sql.toString(), start, limit);
		return doQuery(entityClass, new_sql, new Object[] {value});
	}
	
	public <T> List<T> listForPage(Class<T> entityClass, String[] fieldNames, String param, Object value, int start, int limit, Orders[] orders) {
		StringBuilder sql = getParamSql(entityClass, fieldNames, new String[] {param}, new Object[]{value}, orders);
		String new_sql = dbHelper.wrapToPageSql(sql.toString(), start, limit);
		return doQuery(entityClass, new_sql, new Object[] {value});
	}
	
	public <T> List<T> listForPage(Class<T> entityClass, String[] params, Object[] values, int start, int limit) {
		String[] fieldNames = BeanUtils.getFieldNames(entityClass);
		StringBuilder sql = getParamSql(entityClass, fieldNames, params, values, new Orders[] {});
		String new_sql = dbHelper.wrapToPageSql(sql.toString(), start, limit);
		return doQuery(entityClass, new_sql, values);
	}
	
	public <T> List<T> listForPage(Class<T> entityClass, String[] params, Object[] values, int start, int limit, Orders[] orders) {
		String[] fieldNames = BeanUtils.getFieldNames(entityClass);
		StringBuilder sql = getParamSql(entityClass, fieldNames, params, values, orders);
		String new_sql = dbHelper.wrapToPageSql(sql.toString(), start, limit);
		return doQuery(entityClass, new_sql, values);
	}
	
	public <T> List<T> listForPage(Class<T> entityClass, String[] fieldNames, String[] params, Object[] values, int start, int limit) {
		StringBuilder sql = getParamSql(entityClass, fieldNames, params, values, new Orders[] {});
		String new_sql = dbHelper.wrapToPageSql(sql.toString(), start, limit);
		return doQuery(entityClass, new_sql, values);
	}
	
	public <T> List<T> listForPage(Class<T> entityClass, String[] fieldNames, String[] params, Object[] values, int start, int limit, Orders[] orders) {
		StringBuilder sql = getParamSql(entityClass, fieldNames, params, values, orders);
		String new_sql = dbHelper.wrapToPageSql(sql.toString(), start, limit);
		return doQuery(entityClass, new_sql, values);
	}
	
	public <T> List<T> listForPage(Class<T> entityClass, String[] fieldNames, int start, int limit) {
		StringBuilder sql = getParamSql(entityClass, fieldNames, new String[]{}, new Object[]{}, new Orders[]{});
		String new_sql = dbHelper.wrapToPageSql(sql.toString(), start, limit);
		return doQuery(entityClass, new_sql, new Object[] {});
	}
	
	public <T> List<T> listForPage(Class<T> entityClass, String[] fieldNames, int start, int limit, Orders[] orders) {
		StringBuilder sql = getParamSql(entityClass, fieldNames, new String[]{}, new Object[]{}, orders);
		String new_sql = dbHelper.wrapToPageSql(sql.toString(), start, limit);
		return doQuery(entityClass, new_sql, new Object[] {});
	}
	
	public <T> List<T> listForPage(Class<T> entityClass, int start, int limit, Orders[] orders) {
		String[] fieldNames = BeanUtils.getFieldNames(entityClass);
		StringBuilder sql = getParamSql(entityClass, fieldNames, new String[] {}, new Object[]{}, orders);
		String new_sql = dbHelper.wrapToPageSql(sql.toString(), start, limit);
		return doQuery(entityClass, new_sql, new Object[] {});
	}
	
	public List<Map<String,Object>> queryForPage(int start, int limit, String sql, Object[] values){
		sql = dbHelper.wrapToPageSql(sql, start, limit);
		printSqlLog(sql, values);
		return super.queryForList(sql, values);
	}
	
	private <T> List<T> doQuery(Class<T> entityClass, String sql, Object[] values) {
		values = formatValues(values);
		printSqlLog(sql, values);
		return super.query(sql, values, RowMapperFactory.getRowMapper(entityClass));
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void execute(final String sql, final Object... values) {
		printSqlLog(sql, values);
		if(values == null){
			super.execute(sql);
		}
		super.execute(sql, new PreparedStatementCallback(){
			public Object doInPreparedStatement(PreparedStatement ps)
					throws SQLException, DataAccessException {
				for(int i=0; i<values.length; i++){
					setValue(ps, values[i], i+1);
				}
				return ps.execute();
			}
		});
	}
	
	public int[] batchUpdate(String sql,final Object[] values){
		return super.batchUpdate(sql, new BatchPreparedStatementSetter(){
			public int getBatchSize() {
				return values.length;
			}
			public void setValues(PreparedStatement ps, int index)
					throws SQLException {
				Object value = values[index];
				if(value.getClass().isArray()){
					Object[] subValues = (Object[]) value;
					for(int i=0; i<subValues.length; i++){
						setValue(ps, subValues[i], i+1);
					}
				}else{
					setValue(ps, value, 1);
				}
			}
		});
	}
	
	protected boolean supportsGeneratedKey() {
		return true;
	}
	
	protected String escape(){
		return " escape '/'";
	}
	
	/**
	 * 根据值的定义类型调用PreparedStatement相应的set方法，将值加入到SQL参数中
	 * @param ps PreparedStatement实例
	 * @param value 准备加入到SQL参数中的值
	 * @param index 当前参数索引
	 * @throws SQLException
	 */
	protected void setValue(PreparedStatement ps , Object value, int index) throws SQLException {
		if(value == null){
			ps.setNull(index, java.sql.Types.NULL);
			return;
		}
		Class<?> type = value.getClass();
		if(ClassUtils.isPrimitiveWrapper(type)){
			type = ClassUtils.resolvePrimitiveClassName(type);
		}
		if(int.class.isAssignableFrom(type)){
			ps.setInt(index, (Integer)value);
		}else if(String.class.isAssignableFrom(type)){
			ps.setString(index, (String)value);
		}else if(long.class.isAssignableFrom(type)){
			ps.setLong(index, (Long)value);
		}else if(boolean.class.isAssignableFrom(type)){
			ps.setBoolean(index, (Boolean)value);
		}else if(double.class.isAssignableFrom(type)){
			ps.setDouble(index, (Double)value);
		}else if(char.class.isAssignableFrom(type)){
			ps.setString(index, (Character)value+"");
		}else if(short.class.isAssignableFrom(type)){
			ps.setShort(index, (Short)value);
		}else if(float.class.isAssignableFrom(type)){
			ps.setFloat(index, (Float)value);
		}else if(byte.class.isAssignableFrom(type)){
			ps.setByte(index, (Byte)value);
		}else if(type.isArray()){
			ps.setArray(index, (Array)value);
		}else if(value instanceof java.sql.Timestamp){
			ps.setTimestamp(index, (java.sql.Timestamp) value);
		}else if(value instanceof java.sql.Date){
			ps.setDate(index, (java.sql.Date)value );
		}else if(value instanceof java.util.Date){
			ps.setTimestamp(index, new java.sql.Timestamp( ((java.util.Date)value).getTime() ));
		}else if(value instanceof java.sql.Time){
			ps.setTime(index, (java.sql.Time)value );
		}
	}
	
	private Object[] formatValues(Object[] values) {
		if (values == null) {
			return new Object[]{};
		}
		List<Object> list = new ArrayList<Object>(Arrays.asList(values));
		for (int i = 0; i < values.length; i++) {
			if (values[i] == null) {
				list.remove(i);
			}
		}
		return list.toArray();
	}
	
	private String getTableName(Class<?> entityClass) {
		Assert.notNull(entityClass);
		return entityClass.getAnnotation(TableName.class).value();
	}
	
	private void printSqlLog(String sql, Object... values) {
		logger.debug("execute sql:" + sql);
		StringBuffer p = new StringBuffer();
		if (values != null) {
			p.append(ArrayUtils.join(values, "   "));
		}
		logger.debug("execute sql params:" + p.toString());
	}
}
