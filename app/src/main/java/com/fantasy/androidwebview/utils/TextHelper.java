package com.fantasy.androidwebview.utils;

import java.util.regex.Pattern;

/**
 * 文本帮助类<br>
 * <pre>
 *     author  : Fantasy
 *     version : 1.0, 2020-04-30
 *     since   : 1.0, 2020-04-30
 * </pre>
 */
public class TextHelper {

    /**
     * 判断身份证号码是否正确
     *
     * @param number 身份证号码
     * @return 如果正确的话，则返回true，否则返回false
     */
    public static boolean isIdentificationNumber(String number) {
        String regex = "(^\\d{15}$)|(^\\d{18}$)|(^\\d{17}(\\d|X|x)$)";
        return Pattern.matches(regex, number);
    }

    /**
     * 判断手机号码的格式是否正确
     *
     * @param phone 手机号码
     * @return 如果正确的话，则返回true，否则返回false
     */
    public static boolean isPhone(String phone) {
        String regex = "^(13[0-9]|14[5|7]|15[0|1|2|3|5|6|7|8|9]|18[0|1|2|3|5|6|7|8|9])\\d{8}$";
        return Pattern.matches(regex, phone);
    }

    /**
     * 判断电话号码的格式是否正确
     *
     * @param tel 电话号码
     * @return 如果正确的话，则返回true，否则返回false
     */
    public static boolean isTel(String tel) {
        String regex = "(?:(\\(\\+?86\\))(0[0-9]{2,3}\\-?)?([2-9][0-9]{6,7})+(\\-[0-9]{1,4})?)|"
                + "(?:(86-?)?(0[0-9]{2,3}\\-?)?([2-9][0-9]{6,7})+(\\-[0-9]{1,4})?)";
        return Pattern.matches(regex, tel);
    }

    /**
     * 处理传给JS函数的参数值。因为如果参数值带'、\n等特殊字符，JS会报错
     *
     * @param params 参数值
     * @return 已经处理过参数值
     */
    public static String handleJSFunctionParams(String params) {
        return params.replace("'", "\\'")
                .replace("\n", "\\n");
    }

}