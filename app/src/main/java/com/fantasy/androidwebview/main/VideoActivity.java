package com.fantasy.androidwebview.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fantasy.androidwebview.Constant;
import com.fantasy.androidwebview.R;
import com.fantasy.androidwebview.base.BaseActivity;
import com.fantasy.androidwebview.utils.IntentHelper;
import com.fantasy.androidwebview.utils.WebViewUtils;

/**
 * WebView播放视频并支持全屏
 * <pre>
 *     author  : Fantasy
 *     version : 1.0, 2020-02-13
 *     since   : 1.0, 2020-02-13
 * </pre>
 */
public class VideoActivity extends BaseActivity implements View.OnClickListener {
    private LinearLayout mLlWeb;
    private ImageView mIvClose;
    private TextView mTvTitle;
    private FrameLayout mFlWeb;
    private WebView mWebView;
    private FrameLayout mFlVideoContainer;

    private boolean mIsShowVideo;

    /**
     * 启动WebActivity
     *
     * @param context 上下文
     * @param url     网址
     */
    public static void actionStart(Context context, String url) {
        Intent intent = new Intent(context, VideoActivity.class);
        intent.putExtra("url", url);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        bindEvent();
        initWebView();
        initData();
    }

    @Override
    protected void onResume() {
        if (mWebView != null) {
            // 激活WebView为活跃状态，能正常执行网页的响应
            mWebView.onResume();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mWebView != null) {
            // 当页面被失去焦点被切换到后台不可见状态，需要执行onPause
            // 通过onPause动作通知内核暂停所有的动作，比如DOM的解析、plugin的执行、JavaScript执行
            mWebView.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        if (mWebView != null) {
            mWebView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
            mFlWeb.removeView(mWebView); // 在退出时，移除WebView，可以防止其在后台消耗资源
            // clearCache()和destroy()这两个方法，要在移除WebView后调用，不然APP会崩溃
            mWebView.clearHistory();
            //mWebView.clearCache(true); // 屏蔽的原因：加载网页会保存缓存，如果清掉缓存了，则每次打开网页会重新下载资源
            mWebView.destroy();
            mWebView = null;
        }
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void bindEvent() {
        mLlWeb = findViewById(R.id.ll_web);
        mIvClose = findViewById(R.id.iv_title_bar_close);
        mTvTitle = findViewById(R.id.tv_title_bar_title);
        mFlWeb = findViewById(R.id.fl_web);
        mWebView = findViewById(R.id.wv_web);
        mFlVideoContainer = findViewById(R.id.fl_web_video_container);

        mIvClose.setOnClickListener(this);
        findViewById(R.id.iv_title_bar_back).setOnClickListener(this);
    }

    private void initData() {
        mIsShowVideo = false;
        mWebView.loadUrl(getIntent().getStringExtra("url"));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_title_bar_back:
                goBack();
                break;
            case R.id.iv_title_bar_close:
                finish();
                break;
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView() {
        WebSettings settings = mWebView.getSettings();
        settings.setAppCacheEnabled(true); // 开启 Application Caches 功能
        settings.setDomStorageEnabled(true); // 开启 DOM storage API 功能
        settings.setAllowFileAccess(true); // 设置可以访问文件，默认启用
        settings.setJavaScriptEnabled(true); // 如果访问的页面中要与Javascript交互，则webview必须设置支持Javascript
        settings.setJavaScriptCanOpenWindowsAutomatically(true); // 支持通过JS打开新窗口

        settings.setCacheMode(WebSettings.LOAD_DEFAULT); // 设置缓存模式
        // 缓存模式如下：
        // LOAD_CACHE_ONLY: 不使用网络，只读取本地缓存数据
        // LOAD_DEFAULT: （默认）根据cache-control决定是否从网络上取数据
        // LOAD_NO_CACHE: 不使用缓存，只从网络获取数据
        // LOAD_CACHE_ELSE_NETWORK，只要本地有，无论是否过期，或者no-cache，都使用缓存中的数据

        settings.setSavePassword(false); // 禁止自动保存密码
        settings.setSupportZoom(true); // 支持缩放
        settings.setBuiltInZoomControls(true); // 启动缩放机制
        settings.setDisplayZoomControls(false); // 隐藏原生缩放控件，即缩小放大两个按钮
        settings.setUseWideViewPort(true); // 将图片调整到适合WebView的大小
        settings.setLoadWithOverviewMode(true); // 缩放内容以适应屏幕大小

        // 设置请求头的User-Agent，前端可以通过这来判断页面是不是运行在自家的APP中（这里用 Fantasy 来作为自家的APP标识）
        settings.setUserAgentString(settings.getUserAgentString() + " Fantasy");

        settings.setGeolocationEnabled(true); // 启用地理定位，默认为true
        String dir = getApplicationContext().getDir("database", Context.MODE_PRIVATE).getPath();
        settings.setGeolocationDatabasePath(dir); // 设置定位的数据库路径

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW); // 支持HTTP和HTTPS混合
        }

        WebViewUtils.removeJavascriptInterfaces(mWebView);

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // 作用：打开网页时不调用系统浏览器，而是在本WebView中显示；在网页上的所有加载都经过这个方法
                Log.d(Constant.TAG, mClassName + " loading url : " + url);

                if (!url.startsWith("http") && !url.startsWith("file")) {
                    try {
                        IntentHelper.openBrowser(mContext, url);
                    } catch (Exception e) {
                        Log.d(Constant.TAG, mClassName + " WebViewClient Exception", e);
                        if (url.startsWith("tel:")) {
                            showToast(R.string.main_no_tel);
                        } else if (url.startsWith("sms:")) {
                            showToast(R.string.main_no_sms);
                        } else if (url.startsWith("mailto:")) {
                            showToast(R.string.main_no_mail);
                        } else {
                            showToast(R.string.load_failed);
                        }
                    }
                    return true;
                }
                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // 页面加载结束
                mTvTitle.setText(view.getTitle()); // 获取网页的标题并设置在自定义的标题栏上面
                if (view.canGoBack()) {
                    mIvClose.setVisibility(View.VISIBLE);
                } else {
                    mIvClose.setVisibility(View.GONE);
                }
            }
        });

        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                // 进入视频全屏
                Log.d(Constant.TAG, mClassName + " onShowCustomView");
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); // 横屏
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                mLlWeb.setVisibility(View.GONE);
                mFlVideoContainer.setVisibility(View.VISIBLE);
                mFlVideoContainer.addView(view);
                mIsShowVideo = true;
                super.onShowCustomView(view, callback);
            }

            @Override
            public void onHideCustomView() {
                // 退出视频全屏
                Log.d(Constant.TAG, mClassName + " onHideCustomView");
                //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // 竖屏
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED); // 默认值，系统根据方向感应自动选择屏幕方向
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                mLlWeb.setVisibility(View.VISIBLE);
                mFlVideoContainer.setVisibility(View.GONE);
                mFlVideoContainer.removeAllViews();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // 点击手机返回键的话，会同时调用onKeyDown和onHideCustomView
                        // 为了避免同时执行退出全屏和返回上一个网页这两个操作的情况出现，要延迟修改mIsShowVideo的值
                        mIsShowVideo = false;
                    }
                }, 800);
                super.onHideCustomView();
            }
        });
    }

    /**
     * 返回
     */
    private void goBack() {
        if (mWebView.canGoBack()) {
            if (!mIsShowVideo) {
                mWebView.goBack();
            }
            mIsShowVideo = false;
        } else {
            finish();
        }
    }

}
