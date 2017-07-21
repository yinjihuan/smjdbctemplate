package com.cxytiandi.jdbc;


public interface IDBHelper {

	String wrapToPageSql(String sql, int start, int limit);
	
	String wrapToOrderBySql(Orders[] orders);
	
}