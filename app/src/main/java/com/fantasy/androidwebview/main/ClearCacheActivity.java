package com.fantasy.androidwebview.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.fantasy.androidwebview.R;
import com.fantasy.androidwebview.base.BaseActivity;
import com.fantasy.androidwebview.utils.WebViewUtils;

/**
 * 清除WebView缓存
 * <pre>
 *     author  : Fantasy
 *     version : 1.0, 2020-01-22
 *     since   : 1.0, 2020-01-22
 * </pre>
 */
public class ClearCacheActivity extends BaseActivity implements View.OnClickListener {
    /**
     * 打开“清除WebView缓存”界面
     *
     * @param context 上下文
     */
    public static void actionStart(Context context) {
        context.startActivity(new Intent(context, ClearCacheActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clear_cache);
        bindEvent();
    }

    private void bindEvent() {
        ((TextView) findViewById(R.id.tv_title_bar_title)).setText(R.string.clear_cache_title);
        findViewById(R.id.iv_title_bar_back).setOnClickListener(this);
        findViewById(R.id.btn_clear_cache).setOnClickListener(this);
        findViewById(R.id.btn_clear_cache_web).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_title_bar_back:
                finish();
                break;
            case R.id.btn_clear_cache:
                WebViewUtils.clearCache(mContext);
                break;
            case R.id.btn_clear_cache_web:
                WebActivity.actionStart(mContext, "https://www.baidu.com");
                break;
            default:
                break;
        }
    }

}
