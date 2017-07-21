package com.cxytiandi.jdbc.util;


public class OSUtils {

	public static boolean isWindows() {
		String os = System.getProperty("os.name");
		return os.toLowerCase().contains("windows");
	}
	
	public static boolean isLinux() {
		String os = System.getProperty("os.name");
		return os.toLowerCase().contains("linux");
	}
}
