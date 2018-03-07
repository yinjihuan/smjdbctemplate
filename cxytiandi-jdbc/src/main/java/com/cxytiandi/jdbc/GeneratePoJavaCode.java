package com.cxytiandi.jdbc;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import com.cxytiandi.jdbc.util.StringUtils;

/**
 * 根据表结构生成对应的PO
 * 
 * @author yinjihuan
 *
 */
public class GeneratePoJavaCode {

	public void generate(Connection conn, String pack, String author, String savePath) {
		DatabaseMetaData metaData = null;
		ResultSet rs = null;
		ResultSet crs = null;
		try {
			metaData = conn.getMetaData();
			File dirFile = new File(savePath);
			if (!dirFile.exists()) {
				dirFile.mkdirs();
			}

			// 获取表信息
			rs = metaData.getTables(null, "%", "%", new String[] { "TABLE" });
			while (rs.next()) {
				String tableName = rs.getString("TABLE_NAME");
				String tableComment = rs.getString("REMARKS");
				if (tableComment == null) {
					tableComment = "";
				}
				String classname = getClassName(tableName);
				StringBuffer sb = new StringBuffer();
				StringBuffer gets = new StringBuffer();
				StringBuffer sbpackage = new StringBuffer();
				sbpackage.append("package " + pack + ";\r\n\r\n");
				sbpackage.append("import java.io.Serializable;\r\n");
				sbpackage.append("import com.cxytiandi.jdbc.annotation.Field;\r\n");
				sbpackage.append("import com.cxytiandi.jdbc.annotation.TableName;\r\n");
				sb.append("\r\n@TableName(value = \"" + tableName + "\", author = \"" + author + "\", desc = \"" + tableComment
						+ "\")\r\n");
				sb.append("public class " + classname + " implements Serializable {\r\n");
				// 获取当前表的列
				crs = metaData.getColumns(null, "%", tableName, "%");
				while (crs.next()) {
					String oldCLoumnName = crs.getString("COLUMN_NAME");
					String columnname = getFieldName(crs.getString("COLUMN_NAME"));
					String columntype = crs.getString("TYPE_NAME");
					String comment = crs.getString("REMARKS");
					if (comment == null) {
						comment = "";
					}
					String ftype = getFieldType(columntype, sbpackage);
					if (columnname.equals("id")) {
						sbpackage.append("import com.cxytiandi.jdbc.annotation.AutoId;\r\n");
						sb.append("\t\t@AutoId\r\n");
						sb.append("\t\t@Field(value=\""+oldCLoumnName+"\", desc=\""+comment+"\")\r\n");
						sb.append("\t\tprivate " + ftype + " " + columnname + ";\r\n\r\n");
					} else {
						sb.append("\t\t@Field(value=\""+oldCLoumnName+"\", desc=\""+comment+"\")\r\n");
						sb.append("\t\tprivate " + ftype + " " + columnname + ";\r\n\r\n");
					}
					String mg = "get"+StringUtils.capitalize(columnname);
					String ms = "set"+StringUtils.capitalize(columnname);
					gets.append("\t\tpublic " + ftype + " " + mg + "() {\r\n");
					gets.append("\t\t\t\treturn "+ columnname + ";\r\n");
					gets.append("\t\t}\r\n");
					gets.append("\t\tpublic void " + ms + "("+ftype+" "+columnname+") {\r\n");
					gets.append("\t\t\t\tthis."+ columnname + " = " + columnname + ";\r\n");
					gets.append("\t\t}\r\n");
				}
				gets.append("}");
				File file = new File(dirFile, classname + ".java");
				if (file.exists()) {
					file.delete();
				}
				FileOutputStream outputStream = new FileOutputStream(file);
				outputStream.write(sbpackage.toString().getBytes());
				outputStream.write(sb.toString().getBytes());
				outputStream.write(gets.toString().getBytes());
				outputStream.close();
				System.out.println(classname + " create success ... ");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (null != rs) {
					rs.close();
				}
				if (null != conn) {
					conn.close();
				}
			} catch (Exception e2) {
			}
		}
	}


	/**
	 * 根据表名获取类名<br>
	 * 表名：user  类名:User<br>
	 * 表名：user_info  类名:UserInfo<br>
	 * @param tableName 表名
	 * @return
	 */
	public static String getClassName(String tableName) {
		String name = tableName.toLowerCase();
		if (name.contains("_")) {
			StringBuilder sb = new StringBuilder();
			String[] tbs = name.split("_");
			for (String tb : tbs) {
				sb.append(tb.substring(0, 1).toUpperCase() + tb.substring(1));
			}
			return sb.toString();
		} else {
			return name.substring(0, 1).toUpperCase() + name.substring(1);
		}
	}

	/**
	 * 根据字段名获取属性名<br>
	 * 字段名：user_name 属性名:userName<br>
	 * 字段名：name 属性名:name
	 * @param fieldName
	 * @return
	 */
	public static String getFieldName(String fieldName) {
		String name = fieldName.toLowerCase();
		if (name.contains("_")) {
			StringBuilder sb = new StringBuilder();
			String[] tbs = name.split("_");
			for (int i = 0; i < tbs.length; i++) {
				String tb = tbs[i];
				if (i == 0) {
					sb.append(tb.substring(0, 1).toLowerCase() + tb.substring(1));
				} else {
					sb.append(tb.substring(0, 1).toUpperCase() + tb.substring(1));
				}
			}
			return sb.toString();
		} else {
			return name.substring(0, 1).toLowerCase() + name.substring(1);
		}
	}
	
	/**
	 * 根据数据库字段类型获取Java属性类型
	 * @param columnType
	 * @param sbpackage
	 * @return
	 */
	public static String getFieldType(String columnType, StringBuffer sbpackage) {
		columnType = columnType.toLowerCase();
		if (columnType.equals("varchar") || columnType.equals("nvarchar") || columnType.equals("char")
				|| columnType.equals("tinytext") || columnType.equals("text") || columnType.equals("mediumtext")
				|| columnType.equals("longtext")) {
			return "String";
		} else if (columnType.equals("tinyblob") || columnType.equals("blob") || columnType.equals("mediumblob")
				|| columnType.equals("longblob")) {
			return "byte[]";
		} else if (columnType.equals("datetime") || columnType.equals("date") || columnType.equals("timestamp")
				|| columnType.equals("time") || columnType.equals("year")) {
			if (!sbpackage.toString().contains("java.util.Date")) {
				sbpackage.append("import java.util.Date;\r\n");
			}
			return "Date";
		} else if (columnType.equals("bit") || columnType.equals("int") || columnType.equals("tinyint")
				|| columnType.equals("smallint") || columnType.equals("bool") || columnType.equals("mediumint")) {
			return "int";
		} else if (columnType.equals("float")) {
			return "float";
		} else if (columnType.equals("bigint")) {
			return "Long";
		} else if (columnType.equals("double")) {
			return "double";
		} else if (columnType.equals("decimal")) {
			if (!sbpackage.toString().contains("java.math.BigDecimal")) {
				sbpackage.append("import java.math.BigDecimal;\r\n");
			}
		    return "BigDecimal";
		}
		return "ErrorType";
	}

}
