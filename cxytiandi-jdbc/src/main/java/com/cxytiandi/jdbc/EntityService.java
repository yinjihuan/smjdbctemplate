package com.cxytiandi.jdbc;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import com.cxytiandi.jdbc.util.ReflectUtils;

public abstract class EntityService<T> {

	protected Class<T> entityClass;

	@Autowired
	private CxytiandiJdbcTemplate jdbcTemplate;
	
	public CxytiandiJdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}
	
	@SuppressWarnings("unchecked")
	public EntityService(){
		entityClass = ReflectUtils.getSuperClassGenericType(getClass(), 0);
	}
	
	public long count() {
		return jdbcTemplate.count(entityClass);
	}
	
	public long count(String field, Object value) {
		return jdbcTemplate.count(entityClass, field, value);
	}
	
	public long count(String sql, Object[] values) {
		return jdbcTemplate.count(sql, values);
	}
	
	public long count(String[] params, Object[] values) {
		return jdbcTemplate.count(entityClass, params, values);
	}
	
	public boolean exists(String sql, Object[] values) {
		return jdbcTemplate.exists(sql, values);
	}

	public boolean exists(String[] params, Object[] values) {
		return jdbcTemplate.exists(entityClass, params, values);
	}
	
	public boolean exists(String field, Object value) {
		return jdbcTemplate.exists(entityClass, field, value);
	}
	
	public List<T> in(String field, Object[] values) {
		return jdbcTemplate.in(entityClass, field, values);
	}
	
	public List<T> in(String field, Object[] values, Orders[] orders) {
		return jdbcTemplate.in(entityClass, field, values, orders);
	}
	
	public List<T> in(String[] fieldNames, String field, Object[] values) {
		return jdbcTemplate.in(entityClass, fieldNames, field, values);
	}
	
	public List<T> in(String[] fieldNames, String field, Object[] values, Orders[] orders) {
		return jdbcTemplate.in(entityClass, fieldNames, field, values, orders);
	}
	
	public int[] batchSave(List<?> entitys, String... excludeFields) {
		return jdbcTemplate.batchSave(entityClass, entitys, excludeFields);
	}
	
	public int[] batchSaveByContainsFields(List<?> entitys, String... containsFields) {
		return jdbcTemplate.batchSaveByContainsFields(entityClass, entitys, containsFields);
	}

	public Serializable save(Object entity, String... excludeFields) {
		return jdbcTemplate.save(entityClass, entity, excludeFields);
	}
	
	public Serializable saveByContainsFields(Object entity, String... containsFields) {
		return jdbcTemplate.saveByContainsFields(entityClass, entity, containsFields);
	}
	
	public int[] batchUpdate( List<?> entitys, String pkField, String... excludeFields) {
		return jdbcTemplate.batchUpdate(entityClass, entitys, pkField, excludeFields);
	}

	public int[] batchUpdateByContainsFields(List<?> entitys, String pkField, String... containsFields) {
		return jdbcTemplate.batchUpdateByContainsFields(entityClass, entitys, pkField, containsFields);
	}
	
	public void update(Object entity, String pkField, String... excludeFields) {
		jdbcTemplate.update(entityClass, entity, pkField, excludeFields);
	}
	
	public void updateByContainsFields(Object entity, String pkField, String... containsFields) {
		jdbcTemplate.updateByContainsFields(entityClass, entity, pkField, containsFields);
	}
	
	public void deleteById(String pkField, Object pk) {
		jdbcTemplate.deleteById(entityClass, pkField, pk);
	}
	
	public void deleteById(String sql, Object...values) {
		jdbcTemplate.deleteById(sql, values);
	}
	
	public void deleteByParams(String[] params, Object[] values) {
		jdbcTemplate.deleteByParams(entityClass, params, values);
	}
	
	public T getById(String pkField, Object pk) {
		return jdbcTemplate.getById(entityClass, pkField, pk);
	}
	
	public T get(String sql, Object... values) {
		return jdbcTemplate.get(entityClass, sql, values);
	}
	
	public T getByParams(String[] params, Object[] values) {
		return jdbcTemplate.getByParams(entityClass, params, values);
	}
	
	public T getByParams(String[] params, Object[] values, Orders[] orders) {
		return jdbcTemplate.getByParams(entityClass, params, values, orders);
	}
	
	public T getByParams(String[] fieldNames, String[] params, Object[] values) {
		return jdbcTemplate.getByParams(entityClass, fieldNames, params, values);
	}
	
	public T getByParams(String[] fieldNames, String[] params, Object[] values, Orders[] orders) {
		return jdbcTemplate.getByParams(entityClass, fieldNames, params, values, orders);
	}
	
	public List<T> findByParams(String[] params, Object[] values) {
		return jdbcTemplate.findByParams(entityClass, params, values);
	}
	
	public List<T> findByParams(String[] fieldNames, String[] params, Object[] values) {
		return jdbcTemplate.findByParams(entityClass, fieldNames, params, values);
	}
	
	public List<T> list(){
		return jdbcTemplate.list(entityClass);
	}
	
	public List<T> list(Orders[] orders){
		return jdbcTemplate.list(entityClass, orders);
	}
	
	public List<T> list(String sql){
		return jdbcTemplate.list(entityClass, sql);
	}
	
	public List<T> list(String sql, Object[] values){
		return jdbcTemplate.list(entityClass, sql, values);
	}
	
	public List<T> list(String[] fieldNames){
		return jdbcTemplate.list(entityClass, fieldNames);
	}
	
	public List<T> list(String[] fieldNames, Orders[] orders){
		return jdbcTemplate.list(entityClass, fieldNames, orders);
	}
	
	public List<T> list(String param, Object value){
		return jdbcTemplate.list(entityClass, param, value);
	}
	
	public List<T> list(String param, Object value, Orders[] orders){
		return jdbcTemplate.list(entityClass, param, value, orders);
	}
	
	public List<T> list(String[] fieldNames, String param, Object value){
		return jdbcTemplate.list(entityClass, fieldNames, param, value);
	}
	
	public List<T> list(String[] fieldNames, String param, Object value, Orders[] orders){
		return jdbcTemplate.list(entityClass, param, value, orders);
	}
	
	public List<T> list(String[] params, Object[] values){
		return jdbcTemplate.list(entityClass, params, values);
	}
	
	public List<T> list(String[] params, Object[] values, Orders[] orders){
		return jdbcTemplate.list(entityClass, params, values, orders);
	}
	
	public List<T> list(String[] fieldNames, String[] params, Object[] values){
		return jdbcTemplate.list(entityClass, fieldNames, params, values);
	}
	
	public List<T> list(String[] fieldNames, String[] params, Object[] values, Orders[] orders){
		return jdbcTemplate.list(entityClass, fieldNames, params, values, orders);
	}
	
	public List<T> listForPage(int start, int limit, String sql, Object[] values) {
		return jdbcTemplate.listForPage(entityClass, start, limit, sql, values);
	}
	
	public List<T> listForPage(int start, int limit) {
		return jdbcTemplate.listForPage(entityClass, start, limit);
	}
	
	public List<T> listForPage(String param, Object value, int start, int limit) {
		return jdbcTemplate.listForPage(entityClass, param, value, start, limit);
	}
	
	public List<T> listForPage(String[] fieldNames, String param, Object value, int start, int limit) {
		return jdbcTemplate.listForPage(entityClass, fieldNames, param, value, start, limit);
	}
	
	public List<T> listForPage(String param, Object value, int start, int limit, Orders[] orders) {
		return jdbcTemplate.listForPage(entityClass, param, value, start, limit, orders);
	}
	
	public List<T> listForPage(String[] fieldNames, String param, Object value, int start, int limit, Orders[] orders) {
		return jdbcTemplate.listForPage(entityClass, fieldNames, param, value, start, limit, orders);
	}
	
	public List<T> listForPage(String[] params, Object[] values, int start, int limit) {
		return jdbcTemplate.listForPage(entityClass, params, values, start, limit);
	}
	
	public List<T> listForPage(String[] params, Object[] values, int start, int limit, Orders[] orders) {
		return jdbcTemplate.listForPage(entityClass, params, values, start, limit, orders);
	}
	
	public List<T> listForPage(String[] fieldNames, String[] params, Object[] values, int start, int limit) {
		return jdbcTemplate.listForPage(entityClass, fieldNames, params, values, start, limit);
	}
	
	public List<T> listForPage(String[] fieldNames, String[] params, Object[] values, int start, int limit, Orders[] orders) {
		return jdbcTemplate.listForPage(entityClass, fieldNames, params, values, start, limit, orders);
	}
	
	public List<T> listForPage(String[] fieldNames, int start, int limit) {
		return jdbcTemplate.listForPage(entityClass, fieldNames, start, limit);
	}
	
	public List<T> listForPage(String[] fieldNames, int start, int limit, Orders[] orders) {
		return jdbcTemplate.listForPage(entityClass, fieldNames, start, limit, orders);
	}
	
	public List<T> listForPage(int start, int limit, Orders[] orders) {
		return jdbcTemplate.listForPage(entityClass, start, limit, orders);
	}
	
	public List<Map<String,Object>> queryForPage(int start, int limit, String sql, Object[] values){
		return jdbcTemplate.queryForPage(start, limit, sql, values);
	}
	
}
