package com.cxytiandi.jdbc.util;

public class NumberUtils {

	public static float round(float f, int point){
		float p = (float) Math.pow(10, point);
		return Math.round(f * p) / p;
	}
	
	public static float floor(float f, int point){
		float p = (float) Math.pow(10, point);
		return ((int) Math.floor(f * p)) / p;
	}

}
