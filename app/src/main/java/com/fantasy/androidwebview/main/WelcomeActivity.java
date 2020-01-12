package com.fantasy.androidwebview.main;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import com.fantasy.androidwebview.R;
import com.fantasy.androidwebview.base.BaseActivity;

/**
 * 欢迎页
 * <pre>
 *     author  : Fantasy
 *     version : 1.0, 2020-01-07
 *     since   : 1.0, 2020-01-07
 * </pre>
 */
public class WelcomeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 解决Android应用Launcher重复启动的问题
        if (!isTaskRoot() && getIntent() != null) {
            String action = getIntent().getAction();
            if (getIntent().hasCategory(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN.equals(action)) {
                finish();
                return;
            }
        }

        setContentView(R.layout.activity_welcome);

        // 全屏，透明状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                MainActivity.actionStart(WelcomeActivity.this);
                finish();
            }
        }, 1500);
    }

}
