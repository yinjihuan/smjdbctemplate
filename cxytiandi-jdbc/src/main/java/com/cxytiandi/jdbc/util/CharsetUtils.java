package com.cxytiandi.jdbc.util;


public class CharsetUtils {

	public static byte[] getUTF8Bytes(String s) {
		char[] c = s.toCharArray();
		int len = c.length;
		// Count the number of encoded bytes...计算编码的字节数
		int count = 0;
		for (int i = 0; i < len; i++) {
			int ch = c[i];
			if (ch <= 0x7f) {
				count++;
			} else if (ch <= 0x7ff) {
				count += 2;
			} else {
				count += 3;
			}
		}
		// Now return the encoded bytes...开始进行utf8编码
		byte[] b = new byte[count];
		int off = 0;
		for (int i = 0; i < len; i++) {
			int ch = c[i];
			if (ch <= 0x7f) {
				b[off++] = (byte) ch;
			} else if (ch <= 0x7ff) {
				b[off++] = (byte) ((ch >> 6) | 0xc0);
				b[off++] = (byte) ((ch & 0x3f) | 0x80);
			} else {
				b[off++] = (byte) ((ch >> 12) | 0xe0);
				b[off++] = (byte) (((ch >> 6) & 0x3f) | 0x80);
				b[off++] = (byte) ((ch & 0x3f) | 0x80);
			}
		}
		return b;
	}
	
	public static String gbk2Utf8(String s) throws Exception {
		return new String(getUTF8Bytes(s), "utf-8");
	}

}
