package com.cxytiandi.jdbc;


public class MySQLHelper implements IDBHelper {

	public String wrapToPageSql(String sql, int start, int limit) {
		return sql + " limit "+start+","+limit;
	}

	public String wrapToOrderBySql(Orders[] orders) {
		if (orders != null && orders.length > 0) {
			StringBuilder orderStr = new StringBuilder(" order by ");
			for (Orders order : orders) {
				orderStr.append(order.getName());
				orderStr.append(" ");
				orderStr.append(order.getType().getType());
				orderStr.append(",");
			}
			String value = orderStr.delete(orderStr.length()-1, orderStr.length()).toString();
			return value;
		}
		return "";
	}

}
