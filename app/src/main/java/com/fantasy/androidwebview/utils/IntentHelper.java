package com.fantasy.androidwebview.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

/**
 * Intent帮助类
 * <pre>
 *     author  : Fantasy
 *     version : 1.0, 2020-01-07
 *     since   : 1.0, 2020-01-07
 * </pre>
 */
public class IntentHelper {

    /**
     * 打开系统设置
     *
     * @param context 上下文
     */
    public static void openSettings(Context context) {
        context.startActivity(new Intent(Settings.ACTION_SETTINGS));
    }

    /**
     * 调用系统默认浏览器加载网页
     *
     * @param context 上下文
     * @param url     网址
     */
    public static void openBrowser(Context context, String url) {
        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    /**
     * 调用拨号界面，并自动填写电话号码
     *
     * @param context 上下文
     * @param phone   电话号码，格式为123456
     */
    public static void openDial(Context context, String phone) {
        context.startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone)));
    }

    /**
     * 调用系统发短信
     *
     * @param context 上下文
     * @param phone   电话号码
     * @param message 内容
     */
    public static void sendSMS(Context context, String phone, String message) {
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + phone));
        intent.putExtra("sms_body", message);
        context.startActivity(intent);
    }

}