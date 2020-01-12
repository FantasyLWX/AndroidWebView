package com.fantasy.androidwebview;

import android.app.Application;
import android.content.Context;

import com.fantasy.androidwebview.utils.DirUtils;
import com.fantasy.androidwebview.utils.PreferencesUtils;

/**
 * MyApplication
 * <pre>
 *     author  : Fantasy
 *     version : 1.0, 2020-01-07
 *     since   : 1.0, 2020-01-07
 * </pre>
 */
public class MyApplication extends Application {
    private static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();
        DirUtils.init(this);
        PreferencesUtils.setPreferenceName(getPackageName() + ".sp");
    }

    /**
     * 获取上下文
     *
     * @return 上下文
     */
    public static Context getContext() {
        return sContext;
    }
}
