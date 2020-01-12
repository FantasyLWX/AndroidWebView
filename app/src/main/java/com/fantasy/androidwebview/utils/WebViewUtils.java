package com.fantasy.androidwebview.utils;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;

import com.fantasy.androidwebview.Constant;

import java.io.File;
import java.util.Map;

/**
 * WebView工具类
 * <pre>
 *     author  : Fantasy
 *     version : 1.0, 2020-01-07
 *     since   : 1.0, 2020-01-07
 * </pre>
 */
public class WebViewUtils {
    private static final String TAG = "WebViewUtils";

    /**
     * 移除有风险的WebView系统隐藏接口
     *
     * @param webView WebView实例
     */
    public static void removeJavascriptInterfaces(WebView webView) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB
                    && Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                webView.removeJavascriptInterface("searchBoxJavaBridge_");
                webView.removeJavascriptInterface("accessibility");
                webView.removeJavascriptInterface("accessibilityTraversal");
            }
        } catch (Exception e) {
            Log.d(Constant.TAG, TAG + " removeJavascriptInterfaces exception", e);
        }
    }

    /**
     * 同步Cookie
     *
     * @param context 上下文
     * @param url     链接
     * @param map     Cookie键值对
     */
    public static void syncCookie(Context context, String url, Map<String, String> map) {
        try {
            // domain可以设置也可以不设置，非必需
            //String domain = url.replace("http://", "").replace("https://", "");
            //if (domain.contains("/")) {
            //    domain = domain.substring(0, domain.indexOf('/'));
            //}

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                CookieSyncManager.createInstance(context);
            }
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);
            // cookieManager.removeSessionCookie(); // 移除所有会话Cookie
            for (Map.Entry<String, String> entry : map.entrySet()) {
                cookieManager.setCookie(url, entry.getKey() + "=" + entry.getValue());
                //cookieManager.setCookie(url, "domain=" + domain);
                //cookieManager.setCookie(url, "path=" + "/");
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                CookieSyncManager.getInstance().sync();
            } else {
                cookieManager.flush();
            }
        } catch (Exception e) {
            Log.d(Constant.TAG, TAG + " syncCookie exception", e);
        }
    }

    /**
     * 清除缓存
     *
     * @param context 上下文
     */
    public static void clearCache(Context context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { // 清除cookie
                CookieManager.getInstance().removeAllCookies(null);
            } else {
                CookieSyncManager.createInstance(context);
                CookieManager.getInstance().removeAllCookie();
                CookieSyncManager.getInstance().sync();
            }

            new WebView(context).clearCache(true);

            File cacheFile = new File(context.getCacheDir().getParent() + "/app_webview");
            clearCacheFolder(cacheFile, System.currentTimeMillis());
        } catch (Exception e) {
            Log.d(Constant.TAG, TAG + " clearCache exception", e);
        }
    }

    private static int clearCacheFolder(File dir, long time) {
        int deletedFiles = 0;
        if (dir != null && dir.isDirectory()) {
            try {
                for (File child : dir.listFiles()) {
                    if (child.isDirectory()) {
                        deletedFiles += clearCacheFolder(child, time);
                    }
                    if (child.lastModified() < time) {
                        if (child.delete()) {
                            deletedFiles++;
                        }
                    }
                }
            } catch (Exception e) {
                Log.d(Constant.TAG, TAG + " clearCacheFolder Exception", e);
            }
        }
        return deletedFiles;
    }

}
