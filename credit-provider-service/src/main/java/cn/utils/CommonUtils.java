package cn.utils;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonUtils {

	/**
	 * 判断集合是否为空
	 * 
	 * @param collection
	 * @return
	 */
	public static Boolean isNotEmpty(Collection<?> collection) {
		return (null == collection || collection.size() <= 0);
	}

	/**
	 * 判断字符是否为空
	 * 
	 * @param str
	 * @return
	 */
	public static Boolean isNotString(String str) {
		return (null == str || "".equals(str) || "null".equals(str));
	}

	/**
	 * 验证是否为13位有效数字
	 * @param str
	 * @return
	 */
	public static boolean isNumeric(String str) {
		if (str.length() != 11) {
			return false;
		}
		for (int i = 0; i < str.length(); i++) {
			if (!Character.isDigit(str.charAt(i))) {
				return false;
			}
		}
		return true;
	}
	
	public static boolean isPhoneNum(String mobile){
		String regexp = "^((13[0-9])|(15[^4])|(18[0-9])|(17[0,1,3,5-8])|(14[5,7]))\\d{8}$";
		Pattern p = Pattern.compile(regexp);  
        Matcher m = p.matcher(mobile); 
		return m.matches();		
	}
	
	public static void main(String[] args) {
		System.out.println(CommonUtils.isNotEmpty(null));
		System.out.println(CommonUtils.isNotString(""));
		long starttime = System.nanoTime();
//		System.out.println(CommonUtils.isNumeric("~！@#￥%……&*（）——"));
		System.out.println(CommonUtils.isPhoneNum("~！@#￥%……&*（）——"));
		long endtime = System.nanoTime();
		System.out.println(endtime-starttime);
	}

	
}
