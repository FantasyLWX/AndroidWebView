package com.fantasy.androidwebview.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.fantasy.androidwebview.R;
import com.fantasy.androidwebview.base.BaseActivity;

/**
 * 主界面
 * <pre>
 *     author  : Fantasy
 *     version : 1.0, 2020-01-07
 *     since   : 1.0, 2020-01-07
 * </pre>
 */
public class MainActivity extends BaseActivity implements View.OnClickListener {
    /**
     * 打开主界面
     *
     * @param context 上下文
     */
    public static void actionStart(Context context) {
        context.startActivity(new Intent(context, MainActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindEvent();
    }

    private void bindEvent() {
        findViewById(R.id.iv_title_bar_back).setVisibility(View.GONE);
        ((TextView) findViewById(R.id.tv_title_bar_title)).setText(R.string.app_name);
        findViewById(R.id.cd_main_web_view).setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cd_main_web_view:
                WebActivity.actionStart(mContext, "file:///android_asset/index.html");
                break;
            default:
                break;
        }
    }

}
