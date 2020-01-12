package com.fantasy.androidwebview.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SharedPreferences工具类
 * <pre>
 *     author  : Fantasy
 *     version : 1.0, 2020-01-07
 *     since   : 1.0, 2020-01-07
 * </pre>
 */
public class PreferencesUtils {
    private static String sPreferenceName = "preference_name";

    public static void setPreferenceName(String name) {
        sPreferenceName = name;
    }

    public static boolean remove(Context context, String key) {
        SharedPreferences settings = context.getSharedPreferences(sPreferenceName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.remove(key);
        return editor.commit();
    }

    public static void putString(Context context, String key, String value) {
        SharedPreferences settings = context.getSharedPreferences(sPreferenceName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static boolean commitString(Context context, String key, String value) {
        SharedPreferences settings = context.getSharedPreferences(sPreferenceName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        return editor.commit();
    }

    public static String getString(Context context, String key) {
        return getString(context, key, null);
    }

    public static String getString(Context context, String key, String defaultValue) {
        SharedPreferences settings = context.getSharedPreferences(sPreferenceName, Context.MODE_PRIVATE);
        return settings.getString(key, defaultValue);
    }

    public static void putInt(Context context, String key, int value) {
        SharedPreferences settings = context.getSharedPreferences(sPreferenceName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public static boolean commitInt(Context context, String key, int value) {
        SharedPreferences settings = context.getSharedPreferences(sPreferenceName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(key, value);
        return editor.commit();
    }

    public static int getInt(Context context, String key) {
        return getInt(context, key, -1);
    }

    public static int getInt(Context context, String key, int defaultValue) {
        SharedPreferences settings = context.getSharedPreferences(sPreferenceName, Context.MODE_PRIVATE);
        return settings.getInt(key, defaultValue);
    }

    public static void putLong(Context context, String key, long value) {
        SharedPreferences settings = context.getSharedPreferences(sPreferenceName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong(key, value);
        editor.apply();
    }

    public static boolean commitLong(Context context, String key, long value) {
        SharedPreferences settings = context.getSharedPreferences(sPreferenceName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong(key, value);
        return editor.commit();
    }

    public static long getLong(Context context, String key) {
        return getLong(context, key, -1);
    }

    public static long getLong(Context context, String key, long defaultValue) {
        SharedPreferences settings = context.getSharedPreferences(sPreferenceName, Context.MODE_PRIVATE);
        return settings.getLong(key, defaultValue);
    }

    public static void putFloat(Context context, String key, float value) {
        SharedPreferences settings = context.getSharedPreferences(sPreferenceName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putFloat(key, value);
        editor.apply();
    }

    public static boolean commitFloat(Context context, String key, float value) {
        SharedPreferences settings = context.getSharedPreferences(sPreferenceName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putFloat(key, value);
        return editor.commit();
    }

    public static float getFloat(Context context, String key) {
        return getFloat(context, key, -1);
    }

    public static float getFloat(Context context, String key, float defaultValue) {
        SharedPreferences settings = context.getSharedPreferences(sPreferenceName, Context.MODE_PRIVATE);
        return settings.getFloat(key, defaultValue);
    }

    public static void putBoolean(Context context, String key, boolean value) {
        SharedPreferences settings = context.getSharedPreferences(sPreferenceName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public static boolean commitBoolean(Context context, String key, boolean value) {
        SharedPreferences settings = context.getSharedPreferences(sPreferenceName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(key, value);
        return editor.commit();
    }

    public static boolean getBoolean(Context context, String key) {
        return getBoolean(context, key, false);
    }

    public static boolean getBoolean(Context context, String key, boolean defaultValue) {
        SharedPreferences settings = context.getSharedPreferences(sPreferenceName, Context.MODE_PRIVATE);
        return settings.getBoolean(key, defaultValue);
    }

}
